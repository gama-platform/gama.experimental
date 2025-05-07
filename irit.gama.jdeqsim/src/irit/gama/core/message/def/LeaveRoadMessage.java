/*******************************************************************************************************
 *
 * LeaveRoadMessage.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.message.def;

import irit.gama.common.IConst;
import irit.gama.common.logger.Logger;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.Message;
import irit.gama.core.unit.Road;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.util.GamaDate;

/**
 * The micro-simulation internal handler for leaving a road.
 *
 * @author rashid_waraich
 */
public class LeaveRoadMessage extends Message {
	public LeaveRoadMessage(SchedulingUnit receivingUnit, Scheduler scheduler, Vehicle vehicle, GamaDate scheduleTime) {
		super(receivingUnit, scheduler, vehicle, scheduleTime);
		priority = IConst.PRIORITY_LEAVE_ROAD_MESSAGE;
	}

	@Override
	public void handleMessage() {
		Logger.addMessage(this);

		Road road = (Road) this.getReceivingUnit();
		road.leaveRoad(vehicle, getMessageArrivalTime());
	}
}
