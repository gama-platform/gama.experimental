/*******************************************************************************************************
 *
 * SchedulingSkill.java, in plugin irit.gama.switch,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package irit.gaml.skills;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gaml.architecure.event_manager.EventManagerArchitecture;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;
import msi.gama.util.GamaMap;
import msi.gaml.descriptions.ActionDescription;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

/**
 * Scheduling skill
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.EVENT_MANAGER, type = IType.AGENT, doc = {
		@doc("The event manager, must be defined in another species with \"control: event_manager\"") }),
		@variable(name = IKeywordIrit.EVENT_DATE, type = IType.DATE, doc = { @doc("The date of the previous event") }),
		@variable(name = IKeywordIrit.REFER_TO, type = IType.AGENT, doc = {
				@doc("The agent to refer when the event is triggered") }) })
@skill(name = IKeywordIrit.SCHEDULING, concept = { IKeywordIrit.SCHEDULING, IConcept.SKILL }, internal = true)
public class SchedulingSkill extends Skill {

	// ############################################
	// Getter and setter of skill

	@setter(IKeywordIrit.EVENT_MANAGER)
	public void setEventManager(final IAgent agent, final IAgent manager) {
		if (agent == null || manager == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.EVENT_MANAGER, manager);
	}

	@getter(IKeywordIrit.EVENT_MANAGER)
	public IAgent getEventManager(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (IAgent) agent.getAttribute(IKeywordIrit.EVENT_MANAGER);
	}

	@getter(IKeywordIrit.EVENT_DATE)
	public GamaDate getAt(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (GamaDate) agent.getAttribute(IKeywordIrit.EVENT_DATE);
	}

	@getter(IKeywordIrit.REFER_TO)
	public IAgent getReferTo(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (IAgent) agent.getAttribute(IKeywordIrit.REFER_TO);
	}

	// ############################################
	// Action of architecture

	@action(name = "later", args = {
			@arg(name = IKeywordIrit.THE_ACTION, type = IType.ID, optional = false, doc = @doc("The name of an action or a primitive")),
			@arg(name = IKeywordIrit.WITH_ARGUMENTS, type = IType.MAP, optional = true, doc = @doc("A map expression containing the parameters of the action")),
			@arg(name = IKeywordIrit.AT, type = IType.DATE, optional = true, doc = @doc("Call date")),
			@arg(name = IKeywordIrit.REFER_TO, type = IType.AGENT, optional = true, doc = @doc("The agent to refer")) }, doc = @doc(examples = {
					@example("do later execute: my_action arguments: map((\"test\"::2)) date: starting_date") }, value = "Do action when the date is reached."))
	@SuppressWarnings("unchecked")
	public Object register(final IScope scope) throws GamaRuntimeException {
		// Get date
		GamaDate date = (GamaDate) scope.getArg(IKeywordIrit.AT, IType.DATE);
		// Get caller
		IAgent caller = scope.getAgent();
		// Get action
		ActionDescription action = (ActionDescription) scope.getArg(IKeywordIrit.THE_ACTION, IType.ID);
		// Get arguments
		GamaMap<String, Object> args = (GamaMap<String, Object>) scope.getArg(IKeywordIrit.WITH_ARGUMENTS, IType.MAP);
		// Get refer to agent
		IAgent referredAgent = (IAgent) scope.getArg(IKeywordIrit.REFER_TO, IType.AGENT);

		// Get manager
		IAgent manager = (IAgent) scope.getAgent().getAttribute(IKeywordIrit.EVENT_MANAGER);

		if (manager == null) {
			throw GamaRuntimeException.error("The manager must be defined if you have to use the action \"later\"",
					scope);
		}

		// Get skill and do register on it
		EventManagerArchitecture eventSkill = (EventManagerArchitecture) manager.getSpecies().getArchitecture();
		if (eventSkill == null) {
			throw GamaRuntimeException
					.error("The manager must use the control \"event_manager\" to execute the action \"later\"", scope);
		}
		return eventSkill.register(scope, caller, action, args, date, referredAgent);
	}

	@action(name = "clear_events", doc = @doc(examples = { @example("do clear_events") }, value = "Clear all events."))
	public Object clear(final IScope scope) throws GamaRuntimeException {
		// Get caller
		IAgent caller = scope.getAgent();

		// Get manager
		IAgent manager = (IAgent) scope.getAgent().getAttribute(IKeywordIrit.EVENT_MANAGER);

		if (manager == null) {
			throw GamaRuntimeException.error("The manager must be defined if you have to use the action \"later\"",
					scope);
		}

		// Get skill and do register on it
		EventManagerArchitecture eventSkill = (EventManagerArchitecture) manager.getSpecies().getArchitecture();
		if (eventSkill == null) {
			throw GamaRuntimeException
					.error("The manager must use the control \"event_manager\" to execute the action \"later\"", scope);
		}
		return eventSkill.clear(scope, caller);
	}
}
