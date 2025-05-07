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

package irit.gama.core.message;

import java.util.LinkedList;

import irit.gama.common.Param;
import irit.gama.common.logger.Logger;
import irit.gama.core.INamable;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.def.DeadlockPreventionMessage;
import irit.gama.core.message.def.EndLegMessage;
import irit.gama.core.message.def.EndRoadMessage;
import irit.gama.core.message.def.EnterRoadMessage;
import irit.gama.core.message.def.LeaveRoadMessage;
import irit.gama.core.message.def.StartingLegMessage;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.util.GamaDate;

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

	public static EndLegMessage getEndLegMessage(INamable emitter, SchedulingUnit receiver, Scheduler scheduler,
			Vehicle vehicle, GamaDate scheduleTime) {
		EndLegMessage message;
		if (endLegMessageQueue.size() == 0) {
			message = new EndLegMessage(receiver, scheduler, vehicle, scheduleTime);
		} else {
			message = endLegMessageQueue.poll();
			message.resetMessage(receiver, scheduler, vehicle, scheduleTime);
		}
		Logger.addMessage(emitter, receiver, message);
		return message;
	}

	public static EnterRoadMessage getEnterRoadMessage(INamable emitter, SchedulingUnit receiver, Scheduler scheduler,
			Vehicle vehicle, GamaDate scheduleTime) {
		EnterRoadMessage message;
		if (enterRoadMessageQueue.size() == 0) {
			message = new EnterRoadMessage(receiver, scheduler, vehicle, scheduleTime);
		} else {
			message = enterRoadMessageQueue.poll();
			message.resetMessage(receiver, scheduler, vehicle, scheduleTime);
		}
		Logger.addMessage(emitter, receiver, message);
		return message;
	}

	public static StartingLegMessage getStartingLegMessage(INamable emitter, SchedulingUnit receiver,
			Scheduler scheduler, Vehicle vehicle, GamaDate scheduleTime) {
		StartingLegMessage message;
		if (startingLegMessageQueue.size() == 0) {
			message = new StartingLegMessage(receiver, scheduler, vehicle, scheduleTime);
		} else {
			message = startingLegMessageQueue.poll();
			message.resetMessage(receiver, scheduler, vehicle, scheduleTime);
			return message;
		}
		Logger.addMessage(emitter, receiver, message);
		return message;
	}

	public static LeaveRoadMessage getLeaveRoadMessage(INamable emitter, SchedulingUnit receiver, Scheduler scheduler,
			Vehicle vehicle, GamaDate scheduleTime) {
		LeaveRoadMessage message;
		if (leaveRoadMessageQueue.size() == 0) {
			message = new LeaveRoadMessage(receiver, scheduler, vehicle, scheduleTime);
		} else {
			message = leaveRoadMessageQueue.poll();
			message.resetMessage(receiver, scheduler, vehicle, scheduleTime);
			return message;
		}
		Logger.addMessage(emitter, receiver, message);

		return message;
	}

	public static EndRoadMessage getEndRoadMessage(INamable emitter, SchedulingUnit receiver, Scheduler scheduler,
			Vehicle vehicle, GamaDate scheduleTime) {
		EndRoadMessage message;
		if (endRoadMessageQueue.size() == 0) {
			message = new EndRoadMessage(receiver, scheduler, vehicle, scheduleTime);
		} else {
			message = endRoadMessageQueue.poll();
			message.resetMessage(receiver, scheduler, vehicle, scheduleTime);
			return message;
		}
		Logger.addMessage(emitter, receiver, message);
		return message;
	}

	public static DeadlockPreventionMessage getDeadlockPreventionMessage(INamable emitter, SchedulingUnit receiver,
			Scheduler scheduler, Vehicle vehicle, GamaDate scheduleTime) {
		DeadlockPreventionMessage message;
		if (deadlockPreventionMessageQueue.size() == 0) {
			message = new DeadlockPreventionMessage(receiver, scheduler, vehicle, scheduleTime);
		} else {
			message = deadlockPreventionMessageQueue.poll();
			message.resetMessage(receiver, scheduler, vehicle, scheduleTime);
			return message;
		}
		Logger.addMessage(emitter, receiver, message);
		return message;
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
