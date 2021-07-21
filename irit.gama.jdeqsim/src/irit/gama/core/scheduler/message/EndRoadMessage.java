/*******************************************************************************************************
 *
 * EndRoadMessage.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.scheduler.message;

import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.sim_unit.Road;
import irit.gama.core.sim_unit.Vehicle;

/**
 * The micro-simulation internal handler, when the end of a road is reached.
 *
 * @author rashid_waraich
 */
public class EndRoadMessage extends Message {

	@Override
	public void handleMessage() {
		if (vehicle.isCurrentLegFinished()) {
			/*
			 * the leg is completed, try to enter the last link but do not enter it (just
			 * wait, until you have clearance for enter and then leave the road)
			 */

			vehicle.initiateEndingLegMode();
			vehicle.moveToFirstLinkInNextLeg();
			Road road = vehicle.getCurrentRoad();
			road.enterRequest(vehicle, getMessageArrivalTime());
		} else if (!vehicle.isCurrentLegFinished()) {
			// if leg is not finished yet
			vehicle.moveToNextLinkInLeg();

			Road nextRoad = vehicle.getCurrentRoad();
			nextRoad.enterRequest(vehicle, getMessageArrivalTime());
		}
	}

	public EndRoadMessage(Scheduler scheduler, Vehicle vehicle) {
		super(scheduler, vehicle);
	}

}
