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

package gama.experimental.jdeqsim.core.message.def;

import gama.core.util.GamaDate;
import gama.experimental.jdeqsim.common.logger.Logger;
import gama.experimental.jdeqsim.core.SchedulingUnit;
import gama.experimental.jdeqsim.core.message.Message;
import gama.experimental.jdeqsim.core.unit.Road;
import gama.experimental.jdeqsim.core.unit.Scheduler;
import gama.experimental.jdeqsim.core.unit.Vehicle;

/**
 * The micro-simulation internal handler, when the end of a road is reached.
 *
 * @author rashid_waraich
 */
public class EndRoadMessage extends Message {

	public EndRoadMessage(SchedulingUnit receivingUnit, Scheduler scheduler, Vehicle vehicle, GamaDate scheduleTime) {
		super(receivingUnit, scheduler, vehicle, scheduleTime);
	}

	@Override
	public void handleMessage() {
		Logger.addMessage(this);

		if (vehicle.isCurrentLegFinished()) {
			/*
			 * the leg is completed, try to enter the last link but do not enter it (just
			 * wait, until you have clearance for enter and then leave the road)
			 */

			vehicle.initiateEndingLegMode();
			vehicle.moveToFirstLinkInNextLeg();
			Road road = vehicle.getCurrentRoad();

			road.enterRequest(vehicle, getMessageArrivalTime());
			Logger.addMessage(this, "current leg finished");
		} else if (!vehicle.isCurrentLegFinished()) {
			// if leg is not finished yet
			vehicle.moveToNextLinkInLeg();

			Road nextRoad = vehicle.getCurrentRoad();
			nextRoad.enterRequest(vehicle, getMessageArrivalTime());
			Logger.addMessage(this, "current leg not finished");
		}
	}
}
