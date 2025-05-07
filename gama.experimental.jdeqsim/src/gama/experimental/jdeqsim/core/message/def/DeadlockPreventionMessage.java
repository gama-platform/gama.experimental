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

package gama.experimental.jdeqsim.core.message.def;

import gama.core.util.GamaDate;
import gama.experimental.jdeqsim.common.logger.Logger;
import gama.experimental.jdeqsim.core.SchedulingUnit;
import gama.experimental.jdeqsim.core.message.Message;
import gama.experimental.jdeqsim.core.unit.Road;
import gama.experimental.jdeqsim.core.unit.Scheduler;
import gama.experimental.jdeqsim.core.unit.Vehicle;

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

		road.incrementPromisedToEnterRoad(vehicle); // this will be decremented in enter road
		road.setTimeOfLastEnteringVehicle(getMessageArrivalTime());
		road.removeFirstDeadlockPreventionMessage(this);
		road.removeFromInterestedInEnteringRoad();

		vehicle.scheduleEnterRoadMessage(this, getMessageArrivalTime(), road);
	}
}
