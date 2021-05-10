/*******************************************************************************************************
 *
 * IEventManager.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

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
