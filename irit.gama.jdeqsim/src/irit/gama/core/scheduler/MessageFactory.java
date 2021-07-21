/*******************************************************************************************************
 *
 * MessageFactory.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.scheduler;

import java.util.LinkedList;

import irit.gama.common.Param;
import irit.gama.core.scheduler.message.DeadlockPreventionMessage;
import irit.gama.core.scheduler.message.EndLegMessage;
import irit.gama.core.scheduler.message.EndRoadMessage;
import irit.gama.core.scheduler.message.EnterRoadMessage;
import irit.gama.core.scheduler.message.LeaveRoadMessage;
import irit.gama.core.scheduler.message.StartingLegMessage;
import irit.gama.core.sim_unit.Vehicle;

/**
 * The message factory is used for creating and disposing messages - mainly for
 * performance gain to have lesser garbage collection.
 * 
 * @author rashid_waraich
 */
public class MessageFactory {

	private static LinkedList<EndLegMessage> endLegMessageQueue = new LinkedList<EndLegMessage>();
	private static LinkedList<EnterRoadMessage> enterRoadMessageQueue = new LinkedList<EnterRoadMessage>();
	private static LinkedList<StartingLegMessage> startingLegMessageQueue = new LinkedList<StartingLegMessage>();
	private static LinkedList<LeaveRoadMessage> leaveRoadMessageQueue = new LinkedList<LeaveRoadMessage>();
	private static LinkedList<EndRoadMessage> endRoadMessageQueue = new LinkedList<EndRoadMessage>();
	private static LinkedList<DeadlockPreventionMessage> deadlockPreventionMessageQueue = new LinkedList<DeadlockPreventionMessage>();

	public static void disposeEndLegMessage(EndLegMessage message) {
		if (!Param.GC_MESSAGES) {
			endLegMessageQueue.add(message);
		}
	}

	public static void disposeEnterRoadMessage(EnterRoadMessage message) {
		if (!Param.GC_MESSAGES) {
			enterRoadMessageQueue.add(message);
		}
	}

	public static void disposeStartingLegMessage(StartingLegMessage message) {
		if (!Param.GC_MESSAGES) {
			startingLegMessageQueue.add(message);
		}
	}

	public static void disposeLeaveRoadMessage(LeaveRoadMessage message) {
		if (!Param.GC_MESSAGES) {
			leaveRoadMessageQueue.add(message);
		}
	}

	public static void disposeEndRoadMessage(EndRoadMessage message) {
		if (!Param.GC_MESSAGES) {
			endRoadMessageQueue.add(message);
		}
	}

	public static void disposeDeadlockPreventionMessage(DeadlockPreventionMessage message) {
		if (!Param.GC_MESSAGES) {
			deadlockPreventionMessageQueue.add(message);
		}
	}

	public static EndLegMessage getEndLegMessage(Scheduler scheduler, Vehicle vehicle) {
		if (endLegMessageQueue.size() == 0) {
			return new EndLegMessage(scheduler, vehicle);
		} else {
			EndLegMessage message = endLegMessageQueue.poll();
			message.resetMessage(scheduler, vehicle);
			return message;
		}
	}

	public static EnterRoadMessage getEnterRoadMessage(Scheduler scheduler, Vehicle vehicle) {
		if (enterRoadMessageQueue.size() == 0) {
			return new EnterRoadMessage(scheduler, vehicle);
		} else {
			EnterRoadMessage message = enterRoadMessageQueue.poll();
			message.resetMessage(scheduler, vehicle);
			return message;
		}
	}

	public static StartingLegMessage getStartingLegMessage(Scheduler scheduler, Vehicle vehicle) {
		if (startingLegMessageQueue.size() == 0) {
			return new StartingLegMessage(scheduler, vehicle);
		} else {
			StartingLegMessage message = startingLegMessageQueue.poll();
			message.resetMessage(scheduler, vehicle);
			return message;
		}
	}

	public static LeaveRoadMessage getLeaveRoadMessage(Scheduler scheduler, Vehicle vehicle) {
		if (leaveRoadMessageQueue.size() == 0) {
			return new LeaveRoadMessage(scheduler, vehicle);
		} else {
			LeaveRoadMessage message = leaveRoadMessageQueue.poll();
			message.resetMessage(scheduler, vehicle);
			return message;
		}
	}

	public static EndRoadMessage getEndRoadMessage(Scheduler scheduler, Vehicle vehicle) {
		if (endRoadMessageQueue.size() == 0) {
			return new EndRoadMessage(scheduler, vehicle);
		} else {
			EndRoadMessage message = endRoadMessageQueue.poll();
			message.resetMessage(scheduler, vehicle);
			return message;
		}
	}

	public static DeadlockPreventionMessage getDeadlockPreventionMessage(Scheduler scheduler, Vehicle vehicle) {
		if (deadlockPreventionMessageQueue.size() == 0) {
			return new DeadlockPreventionMessage(scheduler, vehicle);
		} else {
			DeadlockPreventionMessage message = deadlockPreventionMessageQueue.poll();
			message.resetMessage(scheduler, vehicle);
			return message;
		}
	}

	public static void GC_ALL_MESSAGES() {
		endLegMessageQueue = new LinkedList<EndLegMessage>();
		enterRoadMessageQueue = new LinkedList<EnterRoadMessage>();
		startingLegMessageQueue = new LinkedList<StartingLegMessage>();
		leaveRoadMessageQueue = new LinkedList<LeaveRoadMessage>();
		endRoadMessageQueue = new LinkedList<EndRoadMessage>();

		deadlockPreventionMessageQueue = new LinkedList<DeadlockPreventionMessage>();
	}

	public static LinkedList<EndLegMessage> getEndLegMessageQueue() {
		return endLegMessageQueue;
	}

	public static LinkedList<EnterRoadMessage> getEnterRoadMessageQueue() {
		return enterRoadMessageQueue;
	}

	public static LinkedList<StartingLegMessage> getStartingLegMessageQueue() {
		return startingLegMessageQueue;
	}

	public static LinkedList<LeaveRoadMessage> getLeaveRoadMessageQueue() {
		return leaveRoadMessageQueue;
	}

	public static LinkedList<EndRoadMessage> getEndRoadMessageQueue() {
		return endRoadMessageQueue;
	}

	public static LinkedList<DeadlockPreventionMessage> getDeadlockPreventionMessageQueue() {
		return deadlockPreventionMessageQueue;
	}

}
