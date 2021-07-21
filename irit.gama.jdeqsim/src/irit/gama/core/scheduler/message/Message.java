/*******************************************************************************************************
 *
 * Message.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.scheduler.message;

import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.sim_unit.SimUnit;
import irit.gama.core.sim_unit.Vehicle;
import msi.gama.util.GamaDate;

/**
 * The basic message type used in the micro-simulation.
 *
 * @author rashid_waraich
 */
public abstract class Message implements Comparable<Message> {
	// Message time
	protected GamaDate messageArrivalTime = null;
	// If there is two messages at the same time, them use the priority
	protected int priority = 0;
	// If false this message is ignored
	private boolean isAlive = true;
	// SimUnit to call
	private SimUnit receivingUnit;

	public Vehicle vehicle = null;
	public Scheduler scheduler = null;

	public Message() {
	}

	public Message(Scheduler scheduler, Vehicle vehicle) {
		this.vehicle = vehicle;
		this.scheduler = scheduler;
	}

	public void resetMessage(Scheduler scheduler, Vehicle vehicle) {
		this.scheduler = scheduler;
		this.vehicle = vehicle;
	}

	public GamaDate getMessageArrivalTime() {
		return messageArrivalTime;
	}

	public void setMessageArrivalTime(GamaDate messageArrivalTime) {
		this.messageArrivalTime = messageArrivalTime;
	}

	public SimUnit getReceivingUnit() {
		return receivingUnit;
	}

	public void setReceivingUnit(SimUnit receivingUnit) {
		// the receiving unit seems to be the object that one needs when handling the
		// message. I don't find this totally clear since maybe one would need two
		// objects (such
		// as when they collide) or even more? Then one needs to somehow find the other
		// objects, indicating that one could find the first object through those
		// methods as
		// well. kai, feb'18
		this.receivingUnit = receivingUnit;
	}

	/**
	 * The comparison is done according to the message arrival Time. If the time is
	 * equal of two messages, then the priority of the messages is compared
	 */
	@Override
	public int compareTo(Message otherMessage) {
		if (messageArrivalTime.isAfter(otherMessage.messageArrivalTime)) {
			return 1;
		} else if (messageArrivalTime.isBefore(otherMessage.messageArrivalTime)) {
			return -1;
		} else {
			// higher priority means for a queue, that it comes first
			return otherMessage.priority - priority;
		}
	}

	public abstract void handleMessage();
	// yyyy we always seem to have "processEvent()" immediately followed by
	// "handleMessage()", and it is not clear to me why we have both. kai, feb'19
	// I think that the idea is that in "processEvent()" the normal MATSim event is
	// generated and given to the eventsManager, while in handleMessage, everything
	// else is done.

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void killMessage() {
		isAlive = false;
	}

	public void reviveMessage() {
		isAlive = true;
	}

	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * Is greater than m
	 */
	public boolean isGreaterThan(Message m) {
		return messageArrivalTime.isGreaterThan(m.messageArrivalTime, true);
	}

}
