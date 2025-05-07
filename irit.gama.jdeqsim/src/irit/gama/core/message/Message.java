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

package irit.gama.core.message;

import irit.gama.core.INamable;
import irit.gama.core.SchedulingUnit;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.util.GamaDate;

/**
 * The basic message type used in the micro-simulation.
 *
 * @author rashid_waraich
 */
public abstract class Message implements Comparable<Message>, INamable {
	// SchedulingUnit to call
	protected SchedulingUnit receivingUnit;
	// Scheduler used
	protected Scheduler scheduler = null;
	// Vehicle impacted
	protected Vehicle vehicle = null;
	// Message time
	protected GamaDate messageArrivalTime = null;

	// If there is two messages at the same time, them use the priority
	protected int priority = 0;
	// If false this message is ignored
	protected boolean isAlive = true;

	public Message() {
	}

	public Message(SchedulingUnit receivingUnit, Scheduler scheduler, Vehicle vehicle, GamaDate messageArrivalTime) {
		resetMessage(receivingUnit, scheduler, vehicle, messageArrivalTime);
	}

	public void resetMessage(SchedulingUnit receivingUnit, Scheduler scheduler, Vehicle vehicle,
			GamaDate scheduleTime) {
		this.receivingUnit = receivingUnit;
		this.scheduler = scheduler;
		this.vehicle = vehicle;
		this.messageArrivalTime = scheduleTime;
	}

	public GamaDate getMessageArrivalTime() {
		return messageArrivalTime;
	}

	public void setMessageArrivalTime(GamaDate messageArrivalTime) {
		this.messageArrivalTime = messageArrivalTime;
	}

	public SchedulingUnit getReceivingUnit() {
		return receivingUnit;
	}

	public void setReceivingUnit(SchedulingUnit receivingUnit) {
		// the receiving unit seems to be the object that one needs when handling the
		// message. I don't find this totally clear since maybe one would need two
		// objects (such
		// as when they collide) or even more? Then one needs to somehow find the other
		// objects, indicating that one could find the first object through those
		// methods as
		// well. kai, feb'18
		this.receivingUnit = receivingUnit;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public Scheduler getScheduler() {
		return scheduler;
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

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public abstract void handleMessage();
}
