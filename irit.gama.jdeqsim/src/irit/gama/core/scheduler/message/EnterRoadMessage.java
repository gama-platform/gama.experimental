/*******************************************************************************************************
 *
 * EnterRoadMessage.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.scheduler.message;

import irit.gama.common.IConst;
import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.sim_unit.Road;
import irit.gama.core.sim_unit.Vehicle;

/**
 * The micro-simulation internal handler for entering a road.
 *
 * @author rashid_waraich
 */
public class EnterRoadMessage extends Message {

	@Override
	public void handleMessage() {
		// enter the next road
		Road road = vehicle.getCurrentRoad();
		road.enterRoad(vehicle, getMessageArrivalTime());
	}

	public EnterRoadMessage(Scheduler scheduler, Vehicle vehicle) {
		super(scheduler, vehicle);
		priority = IConst.PRIORITY_ENTER_ROAD_MESSAGE;
	}
}
