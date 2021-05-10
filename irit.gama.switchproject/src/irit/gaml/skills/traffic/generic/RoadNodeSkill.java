/*******************************************************************************************************
*
* RoadNodeSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic.generic;

import java.util.List;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gaml.skills.traffic.interfaces.IRoadNodeSkill;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

@vars({ @variable(name = IKeywordIrit.ROADS_IN, type = IType.LIST, of = IType.AGENT, doc = @doc("the list of input roads")),
		@variable(name = IKeywordIrit.ROADS_OUT, type = IType.LIST, of = IType.AGENT, doc = @doc("the list of output roads")) })
@SuppressWarnings("unchecked")
public abstract class RoadNodeSkill extends Skill implements IRoadNodeSkill {

	// ############################################
	// Getter

	@getter(IKeywordIrit.ROADS_IN)
	public List<IAgent> getRoadsIn(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (List<IAgent>) agent.getAttribute(IKeywordIrit.ROADS_IN);
	}

	@getter(IKeywordIrit.ROADS_OUT)
	public List<IAgent> getRoadsOut(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (List<IAgent>) agent.getAttribute(IKeywordIrit.ROADS_OUT);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.ROADS_IN)
	public void setRoadsIn(final IAgent agent, final List<IAgent> roads) {
		agent.setAttribute(IKeywordIrit.ROADS_IN, roads);
	}

	@setter(IKeywordIrit.ROADS_OUT)
	public void setRoadsOut(final IAgent agent, final List<IAgent> roads) {
		agent.setAttribute(IKeywordIrit.ROADS_OUT, roads);
	}
}