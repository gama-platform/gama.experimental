/*******************************************************************************************************
*
* IRoadEntitySkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic.interfaces;

import irit.gama.common.interfaces.IKeywordIrit;
import msi.gama.common.interfaces.ISkill;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.types.IType;

/**
 * IRoadEntitySkill skill, entity on the road skill interface
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.VEHICLE_LENGTH, type = IType.FLOAT, doc = {
		@doc("The length of the vehicle") }) })
public interface IRoadEntitySkill extends ISkill {
	// ############################################
	// Getter

	@getter(IKeywordIrit.VEHICLE_LENGTH)
	public default double getLength(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.VEHICLE_LENGTH);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.VEHICLE_LENGTH)
	public default void setLength(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.VEHICLE_LENGTH, value);
	}
}
