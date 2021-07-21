/*******************************************************************************************************
*
* JDEQSIMSimUnitSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import irit.gama.common.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

/**
 * JDQSIM Vehicle skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeyword.SCHEDULER, type = IType.AGENT, doc = {
		@doc("The Scheduler, must be defined in another species with \"control: event_manager\"") }),
		@variable(name = IKeyword.CORE_DEFINITION, type = IType.NONE, doc = {
				@doc("The Scheduler, must be defined in another species with \"control: event_manager\"") }) })
@skill(name = IKeyword.JDQSIM_SIMUNIT, concept = { IKeyword.JDQSIM_SIMUNIT, IConcept.SKILL }, internal = true)
public class JDEQSIMSimUnitSkill extends Skill {
	// ############################################
	// Getter and setter of skill

	@setter(IKeyword.SCHEDULER)
	public void setEventScheduler(final IAgent agent, final IAgent scheduler) {
		if (agent == null || scheduler == null) {
			return;
		}
		agent.setAttribute(IKeyword.SCHEDULER, scheduler);
	}

	@getter(IKeyword.SCHEDULER)
	public IAgent getEventScheduler(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (IAgent) agent.getAttribute(IKeyword.SCHEDULER);
	}
}
