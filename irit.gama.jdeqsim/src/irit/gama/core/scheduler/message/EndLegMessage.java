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

package irit.gama.core.scheduler.message;

import java.util.List;

import irit.gama.common.IConst;
import irit.gama.common.ITool;
import irit.gama.core.Activity;
import irit.gama.core.Leg;
import irit.gama.core.Plan;
import irit.gama.core.IPlanElement;
import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.sim_unit.Road;
import irit.gama.core.sim_unit.Vehicle;
import msi.gama.util.GamaDate;

/**
 * The micro-simulation internal handler for ending a leg.
 *
 * @author rashid_waraich
 */
public class EndLegMessage extends Message {

	public EndLegMessage(final Scheduler scheduler, final Vehicle vehicle) {
		// need the time interpretation info here. Attaching it to the message feels
		// weird. The scheduler seems a pure simulation object.
		// Consequence: attach it to Vehicle
		super(scheduler, vehicle);
		this.priority = IConst.PRIORITY_ARRIVAL_MESSAGE;
	}

	@Override
	public void handleMessage() {
		/*
		 * start next leg. assumption: actions and legs are alternating in plans file
		 */
		this.vehicle.setLegIndex(this.vehicle.getLegIndex() + 2);
		// reset link index
		this.vehicle.setLinkIndex(-1);

		Plan plan = this.vehicle.getOwnerPerson().getSelectedPlan();
		List<? extends IPlanElement> actsLegs = plan.getPlanElements();
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
			this.vehicle.scheduleStartingLegMessage(departureTime, road);
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
			if (endTime.isBefore(now)) {
				return endTime;
			}
			return now;
		}
	}
}
