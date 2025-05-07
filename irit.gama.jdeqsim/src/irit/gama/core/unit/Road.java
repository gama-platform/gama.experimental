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

package irit.gama.core.unit;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

import irit.gama.common.Param;
import irit.gama.common.logger.Logger;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.def.DeadlockPreventionMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaDate;

/**
 * The road is simulated as an active agent, moving arround vehicles.
 *
 * @author rashid_waraich
 */
public class Road extends SchedulingUnit {

	// From link data
	private double length = Double.NaN;
	private double freespeed;
	private double flowCapacity;
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

	// maximum capacity in m
	private double maxCapacityOnRoad = 0.0;
	private double currentCapacity = 0.0;
	private double currentCapacityPromisedToEnterRoad = 0.0;

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

	public Road(IScope scope, IAgent agent, Scheduler scheduler, double freespeed, double capacity, int nbLanes,
			double length) {
		super(scope, agent, scheduler);

		this.freespeed = freespeed;
		this.flowCapacity = capacity;
		this.nofLanes = nbLanes;
		this.length = length;
		this.timeOfLastEnteringVehicle = scope.getClock().getCurrentDate();
		this.timeOfLastLeavingVehicle = scope.getClock().getCurrentDate();

		/*
		 * calculate the maximum number of cars, which can be on the road at the same
		 * time
		 */
		this.maxCapacityOnRoad = length * nofLanes * Param.STORAGE_CAPACITY_FACTOR;

		/**
		 * it is assured here, that a road must have the space of at least one car
		 */
		if (this.maxCapacityOnRoad < Param.MIN_CAPACITY) {
			this.maxCapacityOnRoad = Param.MIN_CAPACITY;
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

	public int getNofLanes() {
		return nofLanes;
	}

	public double getFreespeed() {
		return freespeed;
	}

	private double getFlowCapacityPerSec() {
		return flowCapacity / Param.CAPACITY_PERIOD;
	}

	public double getFlowCapacity() {
		return flowCapacity;
	}

	public void leaveRoad(Vehicle vehicle, GamaDate simTime) {
		Logger.addMessage(this, "leaveRoad");

		assert (this.carsOnTheRoad.getFirst() == vehicle);
		assert (this.interestedInEnteringRoad.size() == this.deadlockPreventionMessages.size());

		Vehicle currentVehicle = this.carsOnTheRoad.removeFirst();
		this.currentCapacity -= currentVehicle.getSize();
		this.earliestDepartureTimeOfCar.removeFirst();
		this.timeOfLastLeavingVehicle = simTime;

		/*
		 * the next car waiting for entering the road should now be alloted a time for
		 * entering the road
		 */
		if (this.interestedInEnteringRoad.size() > 0 && (this.currentCapacity
				+ this.interestedInEnteringRoad.getFirst().getSize() < this.maxCapacityOnRoad)) {
			Vehicle nextVehicle = this.interestedInEnteringRoad.removeFirst();
			DeadlockPreventionMessage m = this.deadlockPreventionMessages.removeFirst();
			assert (m.getVehicle() == nextVehicle);
			getScheduler().unschedule(m);

			GamaDate nextAvailableTimeForLeavingStreetWithInflow = this.timeOfLastEnteringVehicle
					.plus(inverseInFlowCapacity * 1000.0, ChronoUnit.MILLIS);
			GamaDate gapTravelTime = simTime.plus(this.gapTravelTime * 1000.0, ChronoUnit.MILLIS);

			GamaDate nextAvailableTimeForEnteringStreet = nextAvailableTimeForLeavingStreetWithInflow;
			if (nextAvailableTimeForEnteringStreet.isBefore(gapTravelTime)) {
				nextAvailableTimeForEnteringStreet = gapTravelTime;
			}

			// double nextAvailableTimeForEnteringStreet =
			// Math.max(this.timeOfLastEnteringVehicle + this.inverseInFlowCapacity, simTime
			// + this.gapTravelTime);

			this.currentCapacityPromisedToEnterRoad += currentVehicle.getSize();

			nextVehicle.scheduleEnterRoadMessage(this, nextAvailableTimeForEnteringStreet, this);
			Logger.addMessage(this, "leaveRoad (if interested to mutch)");

		} else {
			if (this.gap != null) {
				/*
				 * if no one is interested in entering this road (precondition) and there are no
				 * cars on the road, then reset gap (this is required, for enterRequest to
				 * function properly)
				 */
				if (this.carsOnTheRoad.size() == 0) {
					this.gap = null;
					Logger.addMessage(this, "leaveRoad (gap no cars)");
				} else {
					/*
					 * as long as the road is not full once, there is no need to keep track of the
					 * gaps
					 */
					this.gap.add(simTime.plus(this.gapTravelTime * 1000.0, ChronoUnit.MILLIS));
					Logger.addMessage(this, "leaveRoad (gap with cars)");
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
					.plus(inverseOutFlowCapacity * 1000.0, ChronoUnit.MILLIS);
			GamaDate earliestDeparture = this.earliestDepartureTimeOfCar.getFirst();

			GamaDate nextAvailableTimeForLeavingStreet = nextAvailableTimeForLeavingStreetWithOutflow;
			if (nextAvailableTimeForLeavingStreet.isBefore(earliestDeparture)) {
				nextAvailableTimeForLeavingStreet = earliestDeparture;
			}

			// double nextAvailableTimeForLeavingStreet =
			// Math.max(this.earliestDepartureTimeOfCar.getFirst(),
			// this.timeOfLastLeavingVehicle + this.inverseOutFlowCapacity);
			nextVehicle.scheduleEndRoadMessage(this, nextAvailableTimeForLeavingStreet, this);
			Logger.addMessage(this, "leaveRoad (car on roads)");
		}

	}

	public void enterRoad(Vehicle vehicle, GamaDate simTime) {
		Logger.addMessage(this, "enterRoad");

		// Set agents locations
		IAgent vehicleAgent = vehicle.getRelativeAgent();
		IAgent roadAgent = getRelativeAgent();
		if (vehicleAgent != null && roadAgent != null) {
			vehicleAgent.setLocation(roadAgent.getLocation());
			IAgent ownerAgent = vehicle.getOwnerPerson().getRelativeAgent();
			if (ownerAgent != null) {
				ownerAgent.setLocation(roadAgent.getLocation());
			}
		}

		// calculate time, when the car reaches the end of the road
		double minFreespeed = Math.min(freespeed, vehicle.getMaxSpeed());
		GamaDate nextAvailableTimeForLeavingStreet = simTime.plus(length / minFreespeed * 1000.0, ChronoUnit.MILLIS);

		this.currentCapacityPromisedToEnterRoad -= vehicle.getSize();
		this.carsOnTheRoad.add(vehicle);
		this.currentCapacity += vehicle.getSize();

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
					.plus(inverseOutFlowCapacity * 1000.0, ChronoUnit.MILLIS);
			if (nextAvailableTimeForLeavingStreet.isBefore(nextAvailableTimeForLeavingStreetWithOutflow)) {
				nextAvailableTimeForLeavingStreet = nextAvailableTimeForLeavingStreetWithOutflow;
			}

			vehicle.scheduleEndRoadMessage(this, nextAvailableTimeForLeavingStreet, this);
//		} else { // empty else clause
			/*
			 * this car is not the front car in the street queue when the cars in front of
			 * the current car leave the street and this car becomes the front car, it will
			 * be waken up.
			 */
			Logger.addMessage(this, "enterRoad (one car)");
		}
		Logger.addMessage(this, "enterRoad (end)");
	}

	public void enterRequest(Vehicle vehicle, GamaDate simTime) {
		Logger.addMessage(this, "enterRequest");

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
		// if (this.carsOnTheRoad.size() + this.noOfCarsPromisedToEnterRoad <
		// this.maxNumberOfCarsOnRoad) {
		if (this.currentCapacity + this.currentCapacityPromisedToEnterRoad < this.maxCapacityOnRoad) {
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
				Logger.addMessage(this, "enterRequest (full recently)");
			}

			this.currentCapacityPromisedToEnterRoad += vehicle.getSize();

			// Get the max date
			GamaDate timeOfLastEnteringVehicleWithInflow = this.timeOfLastEnteringVehicle
					.plus(inverseInFlowCapacity * 1000.0, ChronoUnit.MILLIS);
			GamaDate nextAvailableTimeForEnteringStreet = timeOfLastEnteringVehicleWithInflow;
			if (nextAvailableTimeForEnteringStreet.isBefore(simTime)) {
				nextAvailableTimeForEnteringStreet = simTime;
			}
			if (arrivalTimeOfGap != null && nextAvailableTimeForEnteringStreet.isBefore(arrivalTimeOfGap)) {
				nextAvailableTimeForEnteringStreet = arrivalTimeOfGap;
			}
			this.timeOfLastEnteringVehicle = nextAvailableTimeForEnteringStreet;
			// End
			vehicle.scheduleEnterRoadMessage(this, nextAvailableTimeForEnteringStreet, this);

			Logger.addMessage(this, "enterRequest (spacefull)");
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
				Logger.addMessage(this, "enterRequest (new gap)");

			} else {
				this.gap.clear();
				Logger.addMessage(this, "enterRequest (else new gap)");

			}

			this.interestedInEnteringRoad.add(vehicle);

			/*
			 * the first car interested in entering a road has to wait 'stuckTime' the car
			 * behind has to wait an additional stuckTime (this logic was adapted to adhere
			 * to the C++ implementation)
			 */
			if (this.deadlockPreventionMessages.size() > 0) {
				this.deadlockPreventionMessages
						.add(vehicle.scheduleDeadlockPreventionMessage(this, this.deadlockPreventionMessages.getLast()
								.getMessageArrivalTime().plus(Param.SQUEEZE_TIME * 1000.0, ChronoUnit.MILLIS), this));
				Logger.addMessage(this, "enterRequest (if deadlock)");

			} else {
				this.deadlockPreventionMessages.add(vehicle.scheduleDeadlockPreventionMessage(this,
						simTime.plus(Param.SQUEEZE_TIME * 1000.0, ChronoUnit.MILLIS), this));
				Logger.addMessage(this, "enterRequest (else deadlock)");

			}

			assert (this.interestedInEnteringRoad.size() == this.deadlockPreventionMessages.size())
					: this.interestedInEnteringRoad.size() + " - " + this.deadlockPreventionMessages.size();

		}
	}

	public void giveBackPromisedSpaceToRoad(Vehicle vehicle) {
		this.currentCapacityPromisedToEnterRoad -= vehicle.getSize();
	}

	public void incrementPromisedToEnterRoad(Vehicle vehicle) {
		this.currentCapacityPromisedToEnterRoad += vehicle.getSize();
	}

	public void setTimeOfLastEnteringVehicle(GamaDate timeOfLastEnteringVehicle) {
		this.timeOfLastEnteringVehicle = timeOfLastEnteringVehicle;
	}

	public double getMaxCapacityOnRoad() {
		return maxCapacityOnRoad;
	}

	// !! Use for debug only !!
	public void setMaxCapacityOnRoad(double maxCapacityOnRoad) {
		this.maxCapacityOnRoad = maxCapacityOnRoad;
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

	public double getCurrentCapacity() {
		return currentCapacity;
	}

	public double getCurrentCapacityPromisedToEnterRoad() {
		return currentCapacityPromisedToEnterRoad;
	}

	public double getLength() {
		return length;
	}

	public void setFreespeed(double value) {
		this.freespeed = value;
	}

	public void setNofLanes(int value) {
		this.nofLanes = value;
	}
}
