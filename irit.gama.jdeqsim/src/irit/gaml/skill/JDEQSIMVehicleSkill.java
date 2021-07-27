/*******************************************************************************************************
*
* JDEQSIMVehicleSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import irit.gama.common.IKeyword;
import irit.gama.core.unit.Person;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
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
 * JDQSIM Vehicle skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@skill(name = IKeyword.JDQSIM_VEHICLE, concept = { IKeyword.JDQSIM_VEHICLE, IConcept.SKILL }, internal = true)
public class JDEQSIMVehicleSkill extends JDEQSIMSimUnitSkill {
	// ############################################
	// Actions

	@action(name = "init", args = {
			@arg(name = IKeyword.SCHEDULER, type = IType.AGENT, optional = false, doc = @doc("The scheduler.")),
			@arg(name = IKeyword.OWNER, type = IType.AGENT, optional = false, doc = @doc("The owner.")) })
	public Object init(final IScope scope) throws GamaRuntimeException {
		IAgent schedulerAgent = (IAgent) scope.getArg(IKeyword.SCHEDULER, IType.AGENT);
		IAgent ownerAgent = (IAgent) scope.getArg(IKeyword.OWNER, IType.AGENT);
		IAgent agent = scope.getAgent();

		Scheduler scheduler = (Scheduler) schedulerAgent.getAttribute(IKeyword.CORE_DEFINITION);
		Person owner = (Person) ownerAgent.getAttribute(IKeyword.CORE_DEFINITION);

		agent.setAttribute(IKeyword.SCHEDULER, schedulerAgent);
		agent.setAttribute(IKeyword.CORE_DEFINITION, new Vehicle(scope, agent, scheduler, owner));
		return true;
	}
}
