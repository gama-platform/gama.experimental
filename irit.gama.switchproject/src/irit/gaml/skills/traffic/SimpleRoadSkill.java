/*******************************************************************************************************
*
* SimpleRoadSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic;

import java.util.List;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gaml.skills.traffic.generic.RoadSkill;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.IConcept;

@vars({ @variable(name = IKeywordIrit.ALL_ENTITIES, type = IType.LIST, of = IType.AGENT, doc = @doc("the list of agents on the road")) })
@skill(name = IKeywordIrit.ROAD, concept = { IKeywordIrit.ROAD, IConcept.SKILL }, internal = true)
@SuppressWarnings("unchecked")
public class SimpleRoadSkill extends RoadSkill {

	// ############################################
	// Getter

	@getter(IKeywordIrit.ALL_ENTITIES)
	public List<IAgent> getAllEntities(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (List<IAgent>) agent.getAttribute(IKeywordIrit.ALL_ENTITIES);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.ALL_ENTITIES)
	public void setAllEntities(final IAgent agent, List<IAgent> values) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.ALL_ENTITIES, values);
	}

	// ############################################
	// Action

	@action (
			name = "join",
			args = { @arg (
					name = "entity",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("the entity to register on the road.")) },
			doc = @doc (
					value = "register the agent on the road",
					examples = { @example ("do join agent: entity") }))
	@Override
	public void join(final IScope scope) throws GamaRuntimeException {
		final IAgent entity = (IAgent) scope.getArg("entity", IType.AGENT);
		if (entity != null && !entity.dead()) {
			((List<IAgent>) scope.getAgent().getAttribute(IKeywordIrit.ALL_ENTITIES)).add(entity);			
		}
	}

	@action (
			name = "leave",
			args = { @arg (
					name = "entity",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("the entity to unregister on the road.")) },
			doc = @doc (
					value = "unregister the agent on the road",
					examples = { @example ("do leave entity: entity") }))
	@Override
	public void leave(final IScope scope) throws GamaRuntimeException {
		final IAgent entity = (IAgent) scope.getArg("entity", IType.AGENT);
		if (entity != null) {
			((List<IAgent>) scope.getAgent().getAttribute(IKeywordIrit.ALL_ENTITIES)).remove(entity);			
		}
	}

}