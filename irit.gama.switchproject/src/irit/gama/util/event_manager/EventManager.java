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

import java.util.HashMap;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.ExecutionResult;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.GamaPair;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * A queue of event queue
 */
public class EventManager extends HashMap<IAgent, EventQueue> implements IEventManager {

	// ############################################
	// Attributes

	/**
	 * The serializable class EventQueues does not declare a static final
	 * serialVersionUID field of type long
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Next event
	 */
	private Entry<IAgent, EventQueue> bestEntry = null;

	/**
	 * If true, execution is active
	 */
	private boolean executeActive = false;

	/**
	 * The last event executed
	 */
	private Event lastEvent = null;

	// ############################################
	// Methods

	/**
	 * Prepare event
	 */
	private void prepareBestQueue() {
		Event bestEvent = null;

		for (Entry<IAgent, EventQueue> entry : entrySet()) {
			Event currentEvent = entry.getValue().peek();

			if (currentEvent == null) {
				continue;
			}

			if ((bestEvent == null) || bestEvent.isGreaterThan(currentEvent)) {
				bestEvent = currentEvent;
				bestEntry = entry;
			}
		}
	}

	/**
	 * Get and remove next event
	 */
	private Event pop() {
		Event e = bestEntry.getValue().poll();

		if (bestEntry.getValue().size() <= 0) {
			remove(bestEntry.getKey());
		}

		return e;
	}

	/**
	 * If the next event time is reached
	 */
	private boolean isTimeReached() {
		prepareBestQueue();
		return bestEntry.getValue().isTimeReached();
	}

	/**
	 * Get or create a queue (sorted by agent)
	 */
	private EventQueue getOrCreateQueue(IAgent agent) {
		EventQueue ret = get(agent);

		// If not found, create and add a next queue
		if (ret == null) {
			ret = new EventQueue(new Event.EventComparator(agent.getScope()));
			put(agent, ret);
		}

		return ret;
	}

	/**
	 * Inner Register
	 */
	private Object innerRegister(Event event) throws GamaRuntimeException {
		if (event.getDate() == null) {
			return event.execute();
		} else {
			if (executeActive) {
				if (lastEvent.getDate().isGreaterThan(event.getDate(), true)) {
					throw GamaRuntimeException.warning(
							"Past is not allowed " + event.getAgent().getName() + " at " + event.getDate(),
							event.getScope());
				}
			}
			// Add event
			EventQueue events = getOrCreateQueue(event.getAgent());
			// If the caller is dead so do not add the event
			if (!event.getAgent().dead()) {
				// Add event
				events.add(event);
			}
			return ExecutionResult.withValue(true);
		}
	}

	/**
	 * Inner Execute
	 */
	@SuppressWarnings("unchecked")
	private GamaMap<String, Object> innerExecute(IScope scope) throws GamaRuntimeException {
		GamaMap<String, Object> results = (GamaMap<String, Object>) GamaMapFactory.create();

		executeActive = true;
		while ((size() > 0) && isTimeReached()) {
			// Execute action
			lastEvent = pop();
			if (!lastEvent.getAgent().dead()) {
				results.addValue(scope, new GamaPair<String, Object>(lastEvent.toString(), lastEvent.execute(),
						Types.get(IType.STRING), Types.get(IType.NONE)));
			}
		}
		executeActive = false;

		return results;
	}

	/**
	 * Inner Clear
	 */
	private void innerClear(IScope scope, final IAgent caller) throws GamaRuntimeException {
		EventQueue queue = get(caller);
		if (queue != null) {
			queue.clear();
			remove(caller);
		}
	}

	/**
	 * Clear all priority queue
	 */
	@Override
	public void clear() {
		for (Entry<IAgent, EventQueue> entry : entrySet()) {
			entry.getValue().clear();
		}

		super.clear();
	}

	/**
	 * Get size of all queues
	 */
	@Override
	public int size() {
		int ret = 0;

		for (Entry<IAgent, EventQueue> entry : entrySet()) {
			ret += entry.getValue().size();
		}

		return ret;
	}

	// ############################################
	// Register, execute and clear

	/**
	 * Register with action and arguments as map
	 */
	@Override
	public Object register(final IScope scope, final Event event) throws GamaRuntimeException {
		// Create a new event
		return innerRegister(event);
	}

	/**
	 * Execute the next events
	 */
	@Override
	public Object execute(final IScope scope) throws GamaRuntimeException {
		// Return result
		return innerExecute(scope);
	}

	/**
	 * Execute the next events
	 */
	@Override
	public void clear(final IScope scope, final IAgent caller) throws GamaRuntimeException {
		// Clear
		innerClear(scope, caller);
	}
}
