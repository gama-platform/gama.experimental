/*******************************************************************************************************
 *
 * Vehicle.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.sim_unit;

import java.util.List;

import irit.gama.core.Activity;
import irit.gama.core.Leg;
import irit.gama.core.Plan;
import irit.gama.core.IPlanElement;
import irit.gama.core.scheduler.MessageFactory;
import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.scheduler.message.DeadlockPreventionMessage;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaDate;

/**
 * Represents a vehicle.
 *
 * @author rashid_waraich
 */
public class Vehicle extends SimUnit {

	private Person ownerPerson = null;
	private Leg currentLeg = null;
	private int legIndex;
	private Road currentRoad = null;
	private int linkIndex;
	private Road[] currentRoute = null;

	public Vehicle(IScope scope, Scheduler scheduler, Person ownerPerson) {
		super(scope, scheduler);
		this.ownerPerson = ownerPerson;
		initialize();
	}

	// put the first start leg event into the message queue
	public void initialize() {

		/*
		 * we must start with linkIndex=-1, because the first link on which the start
		 * activity resides is not in the Leg. So, for being consistent with the rest of
		 * the simulation, we start with linkIndex=-1
		 */
		linkIndex = -1;

		/*
		 * return at this point, if we are just testing using a dummy person/plan (to
		 * avoid null pointer exception)
		 */
		if (ownerPerson.getSelectedPlan() == null) {
			return;
		}

		Plan plan = ownerPerson.getSelectedPlan();
		List<? extends IPlanElement> actsLegs = plan.getPlanElements();

		/*
		 * return at this point, if a person just performs one activity during the whole
		 * day (e.g. stays at home), because no event needs to be scheduled for this
		 * person.
		 */

		if (actsLegs.size() <= 1) {
			return;
		}

		// actsLegs(0) is the first activity, actsLegs(1) is the first leg
		legIndex = 1;
		setCurrentLeg((Leg) actsLegs.get(legIndex));
		Activity firstAct = (Activity) actsLegs.get(0);
		// an agent starts the first leg at the end_time of the fist act
		GamaDate departureTime = firstAct.getEndTime();

		// this is the link, where the first activity took place
		setCurrentRoad(firstAct.getRoad());

		Road road = getCurrentRoad();
		// schedule start leg message
		scheduleStartingLegMessage(departureTime, road);
	}

	/**
	 * based on the current Leg, the previous activity is computed; this could be
	 * implemented more efficiently in future.
	 *
	 * @return
	 */
	public Activity getPreviousActivity() {
		Plan plan = ownerPerson.getSelectedPlan();
		List<? extends IPlanElement> actsLegs = plan.getPlanElements();

		for (int i = 0; i < actsLegs.size(); i++) {
			if (actsLegs.get(i) == currentLeg) {
				return ((Activity) actsLegs.get(i - 1));
			}
		}
		return null;
	}

	/**
	 * based on the current Leg, the next activity is computed; this could be
	 * implemented more efficiently in future.
	 *
	 * @return
	 */
	public Activity getNextActivity() {
		Plan plan = ownerPerson.getSelectedPlan();
		List<? extends IPlanElement> actsLegs = plan.getPlanElements();

		for (int i = 0; i < actsLegs.size(); i++) {
			if (actsLegs.get(i) == currentLeg) {
				return ((Activity) actsLegs.get(i + 1));
			}
		}
		return null;
	}

	public void setCurrentLeg(Leg currentLeg) {
		this.currentLeg = currentLeg;
		List<Road> roads = currentLeg.getRoads();
		currentRoute = (Road[]) roads.toArray();
	}

	protected Road[] getCurrentLinkRoute() {
		return currentRoute;
	}

	public void setLegIndex(int legIndex) {
		this.legIndex = legIndex;
	}

	public Person getOwnerPerson() {
		return ownerPerson;
	}

	public Leg getCurrentLeg() {
		return currentLeg;
	}

	public int getLegIndex() {
		return legIndex;
	}

	public Road getCurrentRoad() {
		return currentRoad;
	}

	public int getLinkIndex() {
		return linkIndex;
	}

	public void setCurrentRoad(Road newCurrentRoad) {
		this.currentRoad = newCurrentRoad;
	}

	public void setLinkIndex(int linkIndex) {
		this.linkIndex = linkIndex;
	}

	public boolean isCurrentLegFinished() {
		return getCurrentLinkRoute().length == getLinkIndex() + 1;
	}

	/**
	 * updates both the currentLink and link index variables with the next link in
	 * the link route of the current leg attention: only applicable, if
	 * isCurrentLegFinished==false
	 */
	public void moveToNextLinkInLeg() {
		setLinkIndex(getLinkIndex() + 1);
		setCurrentRoad(getCurrentLinkRoute()[getLinkIndex()]);
	}

	// note: does not affect the link index
	public void moveToFirstLinkInNextLeg() {
		Plan plan = getOwnerPerson().getSelectedPlan();
		List<? extends IPlanElement> actsLegs = plan.getPlanElements();
		setCurrentRoad(((Activity) actsLegs.get(getLegIndex() + 1)).getRoad());
	}

	/**
	 * find out, if the vehicle is in endingLegMode this means, that the vehicle is
	 * just waiting until it can enter the last link (without entering it) and then
	 * ends the leg
	 *
	 * @return
	 */
	public boolean isEndingLegMode() {
		return (getCurrentLinkRoute().length == getLinkIndex());
	}

	// invoking this method causes the "isEndingLegMode" method to return true
	public void initiateEndingLegMode() {
		linkIndex = getCurrentLinkRoute().length;
	}

	public void scheduleEnterRoadMessage(GamaDate scheduleTime, Road road) {
		/*
		 * before entering the new road, we must leave the previous road (if there is a
		 * previous road) the first link does not need to be left (which has index -1)
		 */
		if (this.getLinkIndex() >= 0) {
			scheduleLeavePreviousRoadMessage(scheduleTime);
		}

		if (isEndingLegMode()) {
			/*
			 * attention: as we are not actually entering the road, we need to give back the
			 * promised space to the road else a precondition of the enterRequest would not
			 * be correct any more (which involves the noOfCarsPromisedToEnterRoad variable)
			 */
			road.giveBackPromisedSpaceToRoad(); // next road
			scheduleEndLegMessage(scheduleTime, road);
		} else {
			_scheduleEnterRoadMessage(scheduleTime, road);
		}
	}

	public void scheduleLeavePreviousRoadMessage(GamaDate scheduleTime) {
		Road previousRoad = null;

		/*
		 * we need to handle the first road in a leg specially, because the load to be
		 * left is accessed over the last act performed instead of the leg
		 */
		if (this.getLinkIndex() == 0) {
			Plan plan = ownerPerson.getSelectedPlan();
			List<? extends IPlanElement> actsLegs = plan.getPlanElements();
			previousRoad = ((Activity) actsLegs.get(legIndex - 1)).getRoad();
		} else if (this.getLinkIndex() >= 1) {
			previousRoad = this.getCurrentLinkRoute()[this.getLinkIndex() - 1];
		} else {
			System.err.println("Some thing is wrong with the simulation: Why is this.getLinkIndex() negative");
		}

		scheduleLeaveRoadMessage(scheduleTime, previousRoad);
	}

	protected void _scheduleEnterRoadMessage(GamaDate scheduleTime, Road road) {
		sendMessage(MessageFactory.getEnterRoadMessage(road.scheduler, this), road, scheduleTime);
	}

	public void scheduleEndRoadMessage(GamaDate scheduleTime, Road road) {
		sendMessage(MessageFactory.getEndRoadMessage(road.scheduler, this), road, scheduleTime);
	}

	public void scheduleLeaveRoadMessage(GamaDate scheduleTime, Road road) {
		sendMessage(MessageFactory.getLeaveRoadMessage(road.scheduler, this), road, scheduleTime);
	}

	public void scheduleEndLegMessage(GamaDate scheduleTime, Road road) {
		sendMessage(MessageFactory.getEndLegMessage(road.scheduler, this), road, scheduleTime);
	}

	public void scheduleStartingLegMessage(GamaDate scheduleTime, Road road) {
		sendMessage(MessageFactory.getStartingLegMessage(road.scheduler, this), road, scheduleTime);
	}

	public DeadlockPreventionMessage scheduleDeadlockPreventionMessage(GamaDate scheduleTime, Road road) {
		DeadlockPreventionMessage dpMessage = MessageFactory.getDeadlockPreventionMessage(road.scheduler, this);
		sendMessage(dpMessage, road, scheduleTime);
		return dpMessage;
	}

	public int getCurrentLinkRouteLength() {
		return currentRoute.length;
	}

	/*
	 * public PlansConfigGroup.ActivityDurationInterpretation
	 * getActivityEndTimeInterpretation() { return
	 * this.activityEndTimeInterpretation ; }
	 */

}
