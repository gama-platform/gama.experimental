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

import java.util.List;

import irit.gama.common.IConst;
import irit.gama.common.ITool;
import irit.gama.common.logger.Logger;
import irit.gama.core.IPlanElement;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.Message;
import irit.gama.core.plan.Activity;
import irit.gama.core.plan.Leg;
import irit.gama.core.plan.Plan;
import irit.gama.core.unit.Road;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
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

			Logger.addMessage(this, "another leg");
		}

	}

	private GamaDate calculateDepartureTime(Activity act, GamaDate now) {
		GamaDate endTime = ITool.decideOnActivityEndTime(act, now,
				ITool.ActivityDurationInterpretation.tryEndTimeThenDuration);
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
}
