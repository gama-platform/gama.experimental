/*******************************************************************************************************
*
* RoadSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic.generic;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gaml.skills.traffic.interfaces.IRoadSkill;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

@vars({ 
		@variable(name = IKeywordIrit.NODE_IN, type = IType.AGENT, doc = @doc("the input node of the road")),
		@variable(name = IKeywordIrit.NODE_OUT, type = IType.AGENT, doc = @doc("the output node of the road")),
		@variable(name = IKeywordIrit.MAXSPEED, type = IType.FLOAT, doc = @doc("the maximal speed on the road")) })
public abstract class RoadSkill extends Skill implements IRoadSkill {

	// ############################################
	// Getter

	@getter(IKeywordIrit.NODE_IN)
	public IAgent getNodeIn(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (IAgent) agent.getAttribute(IKeywordIrit.NODE_IN);
	}

	@getter(IKeywordIrit.NODE_OUT)
	public IAgent getNodeOut(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (IAgent) agent.getAttribute(IKeywordIrit.NODE_OUT);
	}

	@getter(IKeywordIrit.MAXSPEED)
	public double getMaxSpeed(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.MAXSPEED);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.NODE_IN)
	public void setNodeIn(final IAgent agent, IAgent value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.NODE_IN, value);
	}

	@setter(IKeywordIrit.NODE_OUT)
	public void setNodeOut(final IAgent agent, IAgent value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.NODE_OUT, value);
	}

	@setter(IKeywordIrit.MAXSPEED)
	public void setMaxSpeed(final IAgent agent, double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.MAXSPEED, value);
	}
	
	// ############################################
	// Action
	
	@action (
			name = "join",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("the agent to register on the road.")) },
			doc = @doc (
					value = "unregister the agent on the road",
					examples = { @example ("do join agent: entity") }))
	public abstract void join(final IScope scope) throws GamaRuntimeException;
	
	@action (
			name = "leave",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("the agent to unregister on the road.")) },
			doc = @doc (
					value = "unregister the agent on the road",
					examples = { @example ("do leave agent: entity") }))
	public abstract void leave(final IScope scope) throws GamaRuntimeException;

}