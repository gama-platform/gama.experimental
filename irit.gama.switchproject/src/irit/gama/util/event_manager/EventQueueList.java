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

import java.util.ArrayList;
import java.util.ListIterator;

import irit.gama.util.event_manager.Event.EventComparator;

/**
 * Event queue used by the event manager
 * 
 * @author Jean-François Erdelyi
 */
public class EventQueueList {

	// ############################################
	// Attributes
	
	// Event list
	protected ArrayList<Event> eventList = new ArrayList<>();
	
	// Event comparator
	private EventComparator eventComparator;

	
	// ############################################
	// Methods

	/**
	 * Constructor with comparator
	 */
	public EventQueueList(EventComparator eventComparator) {
		this.eventComparator = eventComparator;
	}

	/**
	 * Return true if the time of the next element is reached
	 */
	public boolean isTimeReached() {
		return eventList.get(0).isTimeReached();
	}
	
	/**
	 * Add data
	 */
	public void add(Event e) {
		Event[] array = (Event[]) eventList.toArray();
		/*for (int i = 1; i < array.length; i++) {
			Event current = array[i];
	        int j = i - 1;
	        while(j >= 0 && eventComparator.compare(current, array[j]) < 0) {
	            array[j+1] = array[j];
	            j--;
	        }
	        // at this point we've exited, so j is either -1
	        // or it's at the first element where current >= a[j]
	        array[j+1] = current;
	    }*/
		
		Event current = e;
        int j = array.length - 1;
        while(j >= 0 && eventComparator.compare(current, array[j]) < 0) {
            array[j+1] = array[j];
            j--;
        }
        // at this point we've exited, so j is either -1
        // or it's at the first element where current >= a[j]
        array[j+1] = current;
	    
	}
	
	/**
	 * Poll event
	 */
	public Event poll() {
		Event e = eventList.get(0);
		eventList.remove(0);
		return e;
	}
	
	/**
	 * Get iterator
	 */
	public ListIterator<Event> iterator() {
		return eventList.listIterator();
	}
	
	/**
	 * Get size
	 */
	public int size() {
		return eventList.size();
	}
}
