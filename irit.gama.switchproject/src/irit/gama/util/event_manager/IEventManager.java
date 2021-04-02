package irit.gama.util.event_manager;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

/**
 * Event manager interface
 */
public interface IEventManager {

	/**
	 * Clear events of given agent
	 */
	public abstract void clear(final IScope scope, final IAgent caller);

	/**
	 * Register new event
	 */
	public abstract Object register(final IScope scope, final Event event) throws GamaRuntimeException;

	/**
	 * Execute events
	 */
	public abstract Object execute(final IScope scope) throws GamaRuntimeException;

	/**
	 * Get size of the event queue
	 */
	public abstract int size();

}
