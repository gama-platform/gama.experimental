/*******************************************************************************************************
 *
 * SchedulerArchitecture.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.experimental.jdeqsim.architecture;

import gama.core.metamodel.agent.IAgent;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.experimental.jdeqsim.common.IKeyword;
import gama.experimental.jdeqsim.core.unit.Scheduler;
import gama.gaml.architecture.reflex.ReflexArchitecture;

/**
 * Scheduler architecture
 * 
 * @author Jean-François Erdelyi
 */

@skill(name = IKeyword.JDQSIM_SCHEDULER, concept = { IConcept.BEHAVIOR,
		IConcept.ARCHITECTURE }, doc = @doc("Scheduler behavior"))
public class SchedulerArchitecture extends ReflexArchitecture {

	/**
	 * Initialization
	 */
	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		agent.setAttribute(IKeyword.CORE_DEFINITION, new Scheduler(scope, agent));
		return true;
	}

	/**
	 * Execution (each step)
	 */
	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		super.executeOn(scope);
		return executeCurrentManager(scope);
	}

	/**
	 * Execute current manager
	 */
	protected Object executeCurrentManager(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (scope.interrupted() || agent == null) {
			return null;
		}

		return getCurrentSchedulerIfExists(agent).execute(scope);
	}

	/**
	 * Get current scheduler by agent
	 */
	protected Scheduler getCurrentScheduler(final IAgent agent) throws GamaRuntimeException {
		return (Scheduler) agent.getAttribute(IKeyword.CORE_DEFINITION);
	}

	/**
	 * Get current scheduler by agent. throw exception if does not exists
	 */
	protected Scheduler getCurrentSchedulerIfExists(final IAgent agent) throws GamaRuntimeException {
		Scheduler manager = getCurrentScheduler(agent);
		if (manager == null) {
			throw GamaRuntimeException.error("No scheduler agent was detected", agent.getScope());
		}
		return manager;
	}
}
