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

import java.util.Iterator;
import irit.gama.util.event_manager.Event.EventComparator;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;

/**
 * Fast event manager
 */
public class EventManager extends EventQueue implements IEventManager {

	// ############################################
	// Attributes

	/**
	 * 
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

	// ############################################
	// Methods

	public EventManager(IScope scope) {
		super(new EventComparator(scope));
	}

	// ############################################
	// Register, execute and clear

	/**
	 * Register with action and arguments as map
	 */
	@Override
	public Object register(IScope scope, Event event) throws GamaRuntimeException {
		if (event.getAgent().dead()) {
			return false;
		}

		if (event.getDate() == null) {
			return event.execute();
		} else {
			if (executeActive) {
				// Causality check
				if (lastEvent.isGreaterThan(event)) {
					throw GamaRuntimeException
							.warning("Past is not allowed " + lastEvent.getMilli() + " vs " + event.getMilli(), scope);
				}
			} else {
				// Causality check
				long simDate = scope.getSimulation().getClock().getStepInMillis();
				if (simDate > event.getMilli()) {
					throw GamaRuntimeException.warning("Past is not allowed " + simDate + " vs " + event.getMilli(), scope);
				}
			}

			// Add event
			add(event);

			return true;
		}
	}

	/**
	 * Execute the next events
	 */
	@Override
	public Object execute(IScope scope) throws GamaRuntimeException {
		executeActive = true;
		while ((size() > 0) && isTimeReached()) {
			lastEvent = poll();

			// If the caller is dead so do not execute the event
			if (!lastEvent.getAgent().dead()) {
				lastEvent.execute();
			}
		}
		executeActive = false;
		return true;
	}

	/**
	 * Clear
	 */
	@Override
	public void clear(IScope scope, IAgent caller) {
		Iterator<Event> value = iterator();

		while (value.hasNext()) {
			if (value.next().getAgent() == caller) {
				value.remove();
			}
		}
	}
}
