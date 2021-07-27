/*******************************************************************************************************
 *
 * DeadlockPreventionMessage.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.message.def;

import irit.gama.common.logger.Logger;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.Message;
import irit.gama.core.unit.Road;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.util.GamaDate;

/**
 * The micro-simulation internal handler for preventig deadlocks.
 *
 * @author rashid_waraich
 */
public class DeadlockPreventionMessage extends Message {

	public DeadlockPreventionMessage(SchedulingUnit receivingUnit, Scheduler scheduler, Vehicle vehicle,
			GamaDate scheduleTime) {
		super(receivingUnit, scheduler, vehicle, scheduleTime);
	}

	@Override
	// let enter the car into the road immediatly
	public void handleMessage() {
		Logger.addMessage(this);

		Road road = (Road) this.getReceivingUnit();

		road.incrementPromisedToEnterRoad(); // this will be decremented in enter road
		road.setTimeOfLastEnteringVehicle(getMessageArrivalTime());
		road.removeFirstDeadlockPreventionMessage(this);
		road.removeFromInterestedInEnteringRoad();

		vehicle.scheduleEnterRoadMessage(this, getMessageArrivalTime(), road);
	}
}
