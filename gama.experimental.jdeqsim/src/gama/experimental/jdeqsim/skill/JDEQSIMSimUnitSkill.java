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

package gama.experimental.jdeqsim.skill;

import gama.core.metamodel.agent.IAgent;
import gama.experimental.jdeqsim.common.IKeyword;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;

/**
 * JDQSIM Vehicle skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeyword.SCHEDULER, type = IType.AGENT, doc = {
		@doc("The Scheduler, must be defined in another species with \"control: event_manager\"") }),
		@variable(name = IKeyword.CORE_DEFINITION, type = IType.NONE, doc = {
				@doc("Core definition in JDEQSIM") }) })
@skill(name = IKeyword.JDQSIM_SIMUNIT, concept = { IKeyword.JDQSIM_SIMUNIT, IConcept.SKILL }, internal = true)
public abstract class JDEQSIMSimUnitSkill extends Skill {
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
