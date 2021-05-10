/*******************************************************************************************************
 *
 * EventQueue.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.util.event_manager;

import java.util.PriorityQueue;

import irit.gama.util.event_manager.Event.EventComparator;

/**
 * Event queue used by the event manager
 * 
 * @author Jean-François Erdelyi
 */
public class EventQueue extends PriorityQueue<Event> {

	// ############################################
	// Attributes

	/**
	 * The serializable class EventQueue does not declare a static final
	 * serialVersionUID field of type long
	 */
	private static final long serialVersionUID = 1L;

	// ############################################
	// Methods

	/**
	 * Constructor with comparator
	 */
	public EventQueue(EventComparator eventComparator) {
		super(eventComparator);
	}

	/**
	 * Return true if the time of the next element is reached
	 */
	public boolean isTimeReached() {
		return peek().isTimeReached();
	}
}
