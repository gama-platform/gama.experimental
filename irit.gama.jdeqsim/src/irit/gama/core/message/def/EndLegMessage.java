/*******************************************************************************************************
 *
 * EndLegMessage.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.message.def;

import java.time.temporal.ChronoUnit;
import java.util.List;

import irit.gama.common.IConst;
import irit.gama.common.Param;
import irit.gama.common.logger.Logger;
import irit.gama.core.IPlanElement;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.Message;
import irit.gama.core.plan.Activity;
import irit.gama.core.plan.Leg;
import irit.gama.core.plan.Plan;
import irit.gama.core.unit.Person;
import irit.gama.core.unit.Road;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.util.GamaDate;

/**
 * The micro-simulation internal handler for ending a leg.
 *
 * @author rashid_waraich
 */
public class EndLegMessage extends Message {

	public EndLegMessage(SchedulingUnit receivingUnit, Scheduler scheduler, Vehicle vehicle, GamaDate scheduleTime) {
		// need the time interpretation info here. Attaching it to the message feels
		// weird. The scheduler seems a pure simulation object.
		// Consequence: attach it to Vehicle
		super(receivingUnit, scheduler, vehicle, scheduleTime);
		this.priority = IConst.PRIORITY_ARRIVAL_MESSAGE;
	}

	@Override
	public void handleMessage() {
		Logger.addMessage(this);

		/*
		 * start next leg. assumption: actions and legs are alternating in plans file
		 */
		this.vehicle.setLegIndex(this.vehicle.getLegIndex() + 2);
		// reset link index
		this.vehicle.setLinkIndex(-1);

		Plan plan = this.vehicle.getOwnerPerson().getSelectedPlan();
		List<? extends IPlanElement> actsLegs = plan.getPlanElements();

		// If there is another leg
		if ((actsLegs.size() > this.vehicle.getLegIndex())) {
			this.vehicle.setCurrentLeg((Leg) actsLegs.get(this.vehicle.getLegIndex()));
			// current act
			Activity currentAct = (Activity) actsLegs.get(this.vehicle.getLegIndex() - 1);
			// the leg the agent performs

			GamaDate departureTime = calculateDepartureTime(currentAct, getMessageArrivalTime());

			/*
			 * if the departureTime from the act is in the past (this means we arrived
			 * late), then set the departure time to the current simulation time this avoids
			 * that messages in the past are put into the scheduler (which makes no sense
			 * anyway)
			 */
			if (departureTime.isBefore(getMessageArrivalTime())) {
				departureTime = getMessageArrivalTime();
			}

			// Get current road from activity
			Road road = currentAct.getRoad();

			// update current link (we arrived at a new activity)
			this.vehicle.setCurrentRoad(road);

			// schedule a departure from the current link in future
			this.vehicle.scheduleStartingLegMessage(this, departureTime, road);
			setAgentsLocation(actsLegs);

			Logger.addMessage(this, "another leg");
		} else {
			// Move owner to building then kill vehicle
			setAgentsLocation(actsLegs);
			this.vehicle = (Vehicle) this.vehicle.die();
		}

	}

	private void setAgentsLocation(List<? extends IPlanElement> actsLegs) {
		// Move owner to building then kill vehicle
		Activity currentAct = (Activity) actsLegs.get(this.vehicle.getLegIndex() - 1);
		Person owner = this.vehicle.getOwnerPerson();
		IAgent building = currentAct.getBuildingAgent();
		IAgent vehicleAgent = vehicle.getRelativeAgent();
		IAgent roadAgent = vehicle.getCurrentRoad().getRelativeAgent();

		// Person
		if (owner != null && building != null) {
			owner.setToLocation(building.getLocation());
		}
		
		// Vehicle
		if (vehicleAgent != null && roadAgent != null) {
			vehicleAgent.setLocation(roadAgent.getLocation());
		}

	}

	private GamaDate calculateDepartureTime(Activity act, GamaDate now) {
		GamaDate endTime = decideOnActivityEndTime(act, now);
		if (endTime == null) {
			return null;
		} else {
			// we cannot depart before we arrived, thus change the time so the time stamp in
			// events will be right
			// [[how can events not use the simulation time? kai, aug'10]]
			// actually, we will depart in (now+1) because we already missed the departing
			// in this time step
			if (endTime.isAfter(now)) {
				return endTime;
			}
			return now;
		}
	}

	// TODO : It is not clear, see Activity
	public GamaDate decideOnActivityEndTime(Activity act, GamaDate now) {

		switch (Param.ACTIVITY_DURATION_INTERPRETATION) {
		case tryEndTimeThenDuration:
			if (act.getEndTime() != null) {
				return act.getEndTime();
			} else if (act.getMaximumDuration() > 0.0) {
				return now.plus(act.getMaximumDuration() * 1000.0, ChronoUnit.MILLIS);
			} else {
				return null;
			}

		case minOfDurationAndEndTime:
			if (act.getEndTime() == null && act.getMaximumDuration() < 0.0) {
				return null;
			} else if (act.getMaximumDuration() <= 0.0) {
				return act.getEndTime();
			} else if (act.getEndTime() == null) {
				return now.plus(act.getMaximumDuration() * 1000.0, ChronoUnit.MILLIS);
			} else {
				GamaDate durationBasedEndTime = now.plus(act.getMaximumDuration() * 1000.0, ChronoUnit.MILLIS);
				return act.getEndTime().isSmallerThan(durationBasedEndTime, false) ? act.getEndTime()
						: durationBasedEndTime;
			}

		default:
			throw new IllegalArgumentException("Unsupported 'activityDurationInterpretation' enum type: "
					+ Param.ACTIVITY_DURATION_INTERPRETATION);
		}
	}
}
