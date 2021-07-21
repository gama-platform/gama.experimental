/*******************************************************************************************************
*
* JDEQSIMPersonSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import irit.gama.common.IKeyword;
import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.sim_unit.Person;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

/**
 * JDQSIM Road skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@skill(name = IKeyword.JDQSIM_PERSON, concept = { IKeyword.JDQSIM_PERSON, IConcept.SKILL }, internal = true)
public class JDEQSIMPersonSkill extends JDEQSIMSimUnitSkill {
	// ############################################
	// Actions

	@action(name = "init", args = {
			@arg(name = IKeyword.SCHEDULER, type = IType.AGENT, optional = false, doc = @doc("The scheduler.")) })
	public Object init(final IScope scope) throws GamaRuntimeException {
		IAgent schedulerAgent = (IAgent) scope.getArg(IKeyword.SCHEDULER, IType.AGENT);
		IAgent agent = scope.getAgent();

		agent.setAttribute(IKeyword.SCHEDULER, schedulerAgent);

		Scheduler scheduler = (Scheduler) schedulerAgent.getAttribute(IKeyword.SCHEDULER);
		Person innerPerson = new Person(scope, scheduler);

		agent.setAttribute(IKeyword.CORE_DEFINITION, innerPerson);
		return true;
	}
}
