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

package irit.gaml.architecture;

import irit.gama.common.IKeyword;
import irit.gama.core.unit.Scheduler;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.architecture.reflex.ReflexArchitecture;

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
		agent.setAttribute(IKeyword.CORE_DEFINITION, new Scheduler(scope, agent, scope.getClock().getCurrentDate()));
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
		return (Scheduler) agent.getAttribute(IKeyword.SCHEDULER);
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
