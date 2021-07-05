/*******************************************************************************************************
 *
 * EventManager.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.util.event_manager;

import java.util.UUID;

import irit.gama.util.event_manager.Event.EventComparator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;

/**
 * Fast event manager
 */
public class EventManager extends EventQueue {

	// ############################################
	// Attributes

	/**
	 * Serial
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * If true execution is active
	 */
	private boolean executeActive = false;

	/**
	 * The last event executed
	 */
	private Event lastEvent = null;

	/**
	 * The last event executed
	 */
	private int queueSize = 0;

	// ############################################
	// Methods

	public EventManager(IScope scope) {
		super(new EventComparator(scope));
	}

	/**
	 * Get queue size
	 */
	public int getQueueSize() {
		return queueSize;
	}

	/**
	 * Register with action and arguments as map (return event ID)
	 */
	public String register(IScope scope, Event event) throws GamaRuntimeException {
		if (event.getAgent().dead()) {
			return null;
		}

		if (event.getDate() == null) {
			event.execute();
			return null;
		} else {
			if (executeActive) {
				// Causality check
				if (lastEvent.isGreaterThan(event)) {
					throw GamaRuntimeException.warning(
							"Exec: Past is not allowed " + lastEvent.getDate() + " vs " + event.getDate(), scope);
				}
			} else {
				// Causality check
				GamaDate simDate = scope.getSimulation().getClock().getCurrentDate();
				if (simDate.isGreaterThan(event.getDate(), true)) {
					throw GamaRuntimeException.warning("Past is not allowed "
							+ scope.getSimulation().getClock().getCurrentDate() + " vs " + event.getDate(), scope);
				}
			}

			// Add event
			add(event);
			eventMap.put(event.getId(), event);
			queueSize++;

			return event.getId().toString();
		}
	}

	/**
	 * Execute the next events
	 */
	public Object execute(IScope scope) throws GamaRuntimeException {
		executeActive = true;
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy
		// HH:mm:ss.SSS");

		// System.out.println("EXECUTE " + scope.getAgent().getName());
		// LocalDateTime dateTime = LocalDateTime.now();
		// System.out.println("EVENT START " + dateTime.format(formatter));

		// int nbEvent = 0;
		while ((size() > 0) && isTimeReached()) {
			// Remove event
			lastEvent = poll();
			eventMap.remove(lastEvent.getId());
			if (!lastEvent.getAgent().dead()) {
				if (lastEvent.execute() == true) {
					queueSize--;
				}
			} else {
				queueSize--;
			}

			// nbEvent++;
			// LocalDateTime dateTimeEv = LocalDateTime.now();
			// System.out.println("EVENT " + lastEvent + " : " +
			// dateTimeEv.format(formatter));
		}
		// dateTime = LocalDateTime.now();
		/*
		 * if (nbEvent > 0) { long diff = ChronoUnit.MILLIS.between(dateTime,
		 * LocalDateTime.now()); System.out.println("EVENT END " + nbEvent + " : " +
		 * (diff / 1000.0)); System.out.println(); }
		 */
		executeActive = false;
		return true;
	}

	/**
	 * Clear
	 */
	public void kill(IScope scope, String id) {
		// Get event by ID
		Event e = eventMap.get(UUID.fromString(id));
		if(e != null) {
			e.kill();
		}
		queueSize--;
	}
}
