/*******************************************************************************************************
 *
 * Road.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.sim_unit;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

import irit.gama.common.Param;
import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.scheduler.message.DeadlockPreventionMessage;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaDate;

/**
 * The road is simulated as an active agent, moving arround vehicles.
 *
 * @author rashid_waraich
 */
public class Road extends SimUnit {
	/**
	 * this must be initialized before starting the simulation! mapping: key=linkId
	 * used to find a road corresponding to a link
	 */
	// static HashMap<Id<Link>, Road> allRoads = null;

	// public static HashMap<Id<Link>, Road> getAllRoads() {
	// return allRoads;
	// }

	// public static void setAllRoads(HashMap<Id<Link>, Road> allRoads) {
	// Road.allRoads = allRoads;
	// }

	// protected Link link;

	// From link data
	private double length = Double.NaN;
	private double freespeed;
	private double capacity;
	private int nofLanes;

	// see method enterRequest for a detailed description of variable 'gap'
	private LinkedList<GamaDate> gap;

	/**
	 * all roads, which are interested in entering the road, but wasn't allowed to
	 * do so yet
	 */
	private LinkedList<Vehicle> interestedInEnteringRoad = new LinkedList<>();
	private GamaDate timeOfLastEnteringVehicle = null;
	protected GamaDate timeOfLastLeavingVehicle = null;

	/**
	 * the inverseFlowCapacity is simply the inverse of the respective capacities
	 * meaning, and corresponds to the minimal time between two cars
	 * entering/leaving the road
	 */
	private double inverseInFlowCapacity = 0;
	protected double inverseOutFlowCapacity = 0;

	/**
	 * this variable keeps track of the number of cars, which are not on the road,
	 * but which have been promised to enter the road (given a time in future, when
	 * they can enter the road)
	 */
	protected int noOfCarsPromisedToEnterRoad = 0;

	// maximum number of cars on the road at one time
	private long maxNumberOfCarsOnRoad = 0;

	// the time it takes for a gap to get to the back of the road
	private double gapTravelTime = 0;

	// the cars, which are currently on the road
	protected LinkedList<Vehicle> carsOnTheRoad = new LinkedList<>();
	/**
	 * for each of the cars in carsOnTheRoad, the earliest departure time from road
	 * is written here
	 */
	protected LinkedList<GamaDate> earliestDepartureTimeOfCar = new LinkedList<>();
	/**
	 * when trying to enter a road, a deadlock prevention message is put into the
	 * queue this allows a car to enter the road, even if no space on it
	 */
	private LinkedList<DeadlockPreventionMessage> deadlockPreventionMessages = new LinkedList<>();

	public Road(IScope scope, Scheduler scheduler) {
		super(scope, scheduler);

		/*
		 * calculate the maximum number of cars, which can be on the road at the same
		 * time
		 */
		this.maxNumberOfCarsOnRoad = Math.round(length * nofLanes * Param.STORAGE_CAPACITY_FACTOR / Param.CAR_SIZE);

		/**
		 * it is assured here, that a road must have the space of at least one car
		 */
		if (this.maxNumberOfCarsOnRoad == 0) {
			this.maxNumberOfCarsOnRoad = 1;
		}

		double maxInverseInFlowCapacity = 3600
				/ (Param.MINIMUM_IN_FLOW_CAPACITY * Param.FLOW_CAPACITY_FACTOR * nofLanes);

		this.inverseOutFlowCapacity = 1 / (getFlowCapacityPerSec() * Param.FLOW_CAPACITY_FACTOR);

		if (this.inverseOutFlowCapacity > maxInverseInFlowCapacity) {
			this.inverseInFlowCapacity = maxInverseInFlowCapacity;
		} else {
			this.inverseInFlowCapacity = this.inverseOutFlowCapacity;
		}

		this.gapTravelTime = length / Param.GAP_TRAVEL_SPEED;

		// gap must be initialized to null because of the application logic
		this.gap = null;
	}

	private double getFlowCapacityPerSec() {
		return capacity / Param.CAPACITY_PERIOD;
	}

	public void leaveRoad(Vehicle vehicle, GamaDate simTime) {
		assert (this.carsOnTheRoad.getFirst() == vehicle);
		assert (this.interestedInEnteringRoad.size() == this.deadlockPreventionMessages.size());

		this.carsOnTheRoad.removeFirst();
		this.earliestDepartureTimeOfCar.removeFirst();
		this.timeOfLastLeavingVehicle = simTime;

		/*
		 * the next car waiting for entering the road should now be alloted a time for
		 * entering the road
		 */
		if (this.interestedInEnteringRoad.size() > 0) {
			Vehicle nextVehicle = this.interestedInEnteringRoad.removeFirst();
			DeadlockPreventionMessage m = this.deadlockPreventionMessages.removeFirst();
			assert (m.vehicle == nextVehicle);
			this.scheduler.unschedule(m);

			GamaDate nextAvailableTimeForLeavingStreetWithInflow = this.timeOfLastEnteringVehicle
					.plus(inverseInFlowCapacity, ChronoUnit.SECONDS);
			GamaDate gapTravelTime = simTime.plus(this.gapTravelTime, ChronoUnit.SECONDS);

			GamaDate nextAvailableTimeForEnteringStreet = nextAvailableTimeForLeavingStreetWithInflow;
			if (nextAvailableTimeForEnteringStreet.isBefore(gapTravelTime)) {
				nextAvailableTimeForEnteringStreet = gapTravelTime;
			}

			// double nextAvailableTimeForEnteringStreet =
			// Math.max(this.timeOfLastEnteringVehicle + this.inverseInFlowCapacity, simTime
			// + this.gapTravelTime);

			this.noOfCarsPromisedToEnterRoad++;

			nextVehicle.scheduleEnterRoadMessage(nextAvailableTimeForEnteringStreet, this);
		} else {
			if (this.gap != null) {

				/*
				 * as long as the road is not full once, there is no need to keep track of the
				 * gaps
				 */
				this.gap.add(simTime.plus(this.gapTravelTime, ChronoUnit.SECONDS));

				/*
				 * if no one is interested in entering this road (precondition) and there are no
				 * cars on the road, then reset gap (this is required, for enterRequest to
				 * function properly)
				 */
				if (this.carsOnTheRoad.size() == 0) {
					this.gap = null;
				}
			}
		}

		/*
		 * tell the car behind the fist car (which is the first car now), when it
		 * reaches the end of the read
		 */
		if (this.carsOnTheRoad.size() > 0) {
			Vehicle nextVehicle = this.carsOnTheRoad.getFirst();

			GamaDate nextAvailableTimeForLeavingStreetWithOutflow = this.timeOfLastLeavingVehicle
					.plus(inverseOutFlowCapacity, ChronoUnit.SECONDS);
			GamaDate earliestDeparture = this.earliestDepartureTimeOfCar.getFirst();

			GamaDate nextAvailableTimeForLeavingStreet = nextAvailableTimeForLeavingStreetWithOutflow;
			if (nextAvailableTimeForLeavingStreet.isBefore(earliestDeparture)) {
				nextAvailableTimeForLeavingStreet = earliestDeparture;
			}

			// double nextAvailableTimeForLeavingStreet =
			// Math.max(this.earliestDepartureTimeOfCar.getFirst(),
			// this.timeOfLastLeavingVehicle + this.inverseOutFlowCapacity);
			nextVehicle.scheduleEndRoadMessage(nextAvailableTimeForLeavingStreet, this);
		}

	}

	public void enterRoad(Vehicle vehicle, GamaDate simTime) {
		// calculate time, when the car reaches the end of the road
		GamaDate nextAvailableTimeForLeavingStreet = simTime.plus(length / freespeed, ChronoUnit.SECONDS);

		this.noOfCarsPromisedToEnterRoad--;
		this.carsOnTheRoad.add(vehicle);

		/*
		 * needed to remove the following assertion because for deadlock prevention
		 * there might be more cars on the road than its capacity assert
		 * maxNumberOfCarsOnRoad >= carsOnTheRoad.size() : "There are more cars on the
		 * road, than its capacity!";
		 */
		this.earliestDepartureTimeOfCar.add(nextAvailableTimeForLeavingStreet);

		/*
		 * if we are in the front of the queue, then we can just drive with free speed
		 * to the front and have to have at least inverseFlowCapacity time-distance to
		 * the previous car
		 */
		if (this.carsOnTheRoad.size() == 1) {
			GamaDate nextAvailableTimeForLeavingStreetWithOutflow = this.timeOfLastLeavingVehicle
					.plus(inverseOutFlowCapacity, ChronoUnit.SECONDS);
			if (nextAvailableTimeForLeavingStreet.isBefore(nextAvailableTimeForLeavingStreetWithOutflow)) {
				nextAvailableTimeForLeavingStreet = nextAvailableTimeForLeavingStreetWithOutflow;
			}

			// nextAvailableTimeForLeavingStreet =
			// Math.max(nextAvailableTimeForLeavingStreet, this.timeOfLastLeavingVehicle +
			// this.inverseOutFlowCapacity);
			vehicle.scheduleEndRoadMessage(nextAvailableTimeForLeavingStreet, this);
//		} else { // empty else clause
			/*
			 * this car is not the front car in the street queue when the cars in front of
			 * the current car leave the street and this car becomes the front car, it will
			 * be waken up.
			 */
		}

	}

	public void enterRequest(Vehicle vehicle, GamaDate simTime) {
		assert (this.interestedInEnteringRoad.size() == this.deadlockPreventionMessages.size());
		/*
		 * assert maxNumberOfCarsOnRoad >= carsOnTheRoad.size() : "There are more cars
		 * on the road, than its capacity!"; assert maxNumberOfCarsOnRoad >=
		 * carsOnTheRoad.size() + noOfCarsPromisedToEnterRoad : "You promised too many
		 * cars, that they can enter the street!"; These asserts has been commented out
		 * for deadlock prevention: If a car waits too long, it is allowed to enter the
		 * road.
		 */

		// is there any space on the road (including promised entries?)
		if (this.carsOnTheRoad.size() + this.noOfCarsPromisedToEnterRoad < this.maxNumberOfCarsOnRoad) {
			/*
			 * - check, if the gap needs to be considered for entering the road - we can
			 * find out, the time since when we have a free road for entrance for sure:
			 */

			// the gap queue will only be empty in the beginning
			GamaDate arrivalTimeOfGap = null;
			// if the road has been full recently then find out, when the next
			// gap arrives
			if ((this.gap != null) && (this.gap.size() > 0)) {
				arrivalTimeOfGap = this.gap.remove();
			}

			this.noOfCarsPromisedToEnterRoad++;

			// Get the max date
			GamaDate timeOfLastEnteringVehicleWithInflow = this.timeOfLastEnteringVehicle.plus(inverseInFlowCapacity,
					ChronoUnit.SECONDS);
			GamaDate nextAvailableTimeForEnteringStreet = timeOfLastEnteringVehicleWithInflow;
			if (nextAvailableTimeForEnteringStreet.isBefore(simTime)) {
				nextAvailableTimeForEnteringStreet = simTime;
			}
			if (arrivalTimeOfGap != null && nextAvailableTimeForEnteringStreet.isBefore(arrivalTimeOfGap)) {
				nextAvailableTimeForEnteringStreet = arrivalTimeOfGap;
			}
			this.timeOfLastEnteringVehicle = nextAvailableTimeForEnteringStreet;
			// End

			vehicle.scheduleEnterRoadMessage(nextAvailableTimeForEnteringStreet, this);
		} else {
			/*
			 * - if the road was empty then create a new queue else empty the old queue As
			 * long as the gap is null, the road is not full (and there is no reason to keep
			 * track of the gaps => see leaveRoad) But when the road gets full once, we need
			 * to start keeping track of the gaps Once the road is empty again, gap is reset
			 * to null (see leaveRoad).
			 *
			 * The gap variable in only needed for the situation, where the street has been
			 * full recently, but the interestedInEnteringRoad is empty and a new car
			 * arrives (or a few). So, if the street is long, it takes time for the gap to
			 * come back.
			 *
			 * As long as interestedInEnteringRoad is not empty, newly generated gaps get
			 * used by the new cars (see leaveRoad)
			 */
			if (this.gap == null) {
				this.gap = new LinkedList<>();
			} else {
				this.gap.clear();
			}

			this.interestedInEnteringRoad.add(vehicle);

			/*
			 * the first car interested in entering a road has to wait 'stuckTime' the car
			 * behind has to wait an additional stuckTime (this logic was adapted to adhere
			 * to the C++ implementation)
			 */
			if (this.deadlockPreventionMessages.size() > 0) {
				this.deadlockPreventionMessages
						.add(vehicle.scheduleDeadlockPreventionMessage(this.deadlockPreventionMessages.getLast()
								.getMessageArrivalTime().plus(Param.SQUEEZE_TIME, ChronoUnit.SECONDS), this));

			} else {
				this.deadlockPreventionMessages.add(vehicle
						.scheduleDeadlockPreventionMessage(simTime.plus(Param.SQUEEZE_TIME, ChronoUnit.SECONDS), this));
			}

			assert (this.interestedInEnteringRoad.size() == this.deadlockPreventionMessages.size())
					: this.interestedInEnteringRoad.size() + " - " + this.deadlockPreventionMessages.size();
		}
	}

	public void giveBackPromisedSpaceToRoad() {
		this.noOfCarsPromisedToEnterRoad--;
	}

	public void incrementPromisedToEnterRoad() {
		this.noOfCarsPromisedToEnterRoad++;
	}

	public void setTimeOfLastEnteringVehicle(GamaDate timeOfLastEnteringVehicle) {
		this.timeOfLastEnteringVehicle = timeOfLastEnteringVehicle;
	}

	public void removeFirstDeadlockPreventionMessage(DeadlockPreventionMessage dpMessage) {

		assert (this.deadlockPreventionMessages.getFirst() == dpMessage)
				: "Inconsitency in logic!!! => this should only be invoked from the handler of this message";
		this.deadlockPreventionMessages.removeFirst();
	}

	public void removeFromInterestedInEnteringRoad() {
		this.interestedInEnteringRoad.removeFirst();
		assert (this.interestedInEnteringRoad.size() == this.deadlockPreventionMessages.size());
	}

	// public static Road getRoad(Id<Link> linkId) {
	// return getAllRoads().get(linkId);
	// }

}
