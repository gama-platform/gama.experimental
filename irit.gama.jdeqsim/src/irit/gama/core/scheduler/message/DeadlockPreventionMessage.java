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

package irit.gama.core.scheduler.message;

import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.sim_unit.Road;
import irit.gama.core.sim_unit.Vehicle;

/**
 * The micro-simulation internal handler for preventig deadlocks.
 *
 * @author rashid_waraich
 */
public class DeadlockPreventionMessage extends Message {

	@Override
	// let enter the car into the road immediatly
	public void handleMessage() {

		Road road = (Road) this.getReceivingUnit();

		road.incrementPromisedToEnterRoad(); // this will be decremented in enter road
		road.setTimeOfLastEnteringVehicle(getMessageArrivalTime());
		road.removeFirstDeadlockPreventionMessage(this);
		road.removeFromInterestedInEnteringRoad();

		vehicle.scheduleEnterRoadMessage(getMessageArrivalTime(), road);
	}

	public DeadlockPreventionMessage(Scheduler scheduler, Vehicle vehicle) {
		super(scheduler, vehicle);
	}
}
