/*******************************************************************************************************
*
* MovingVehicleSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic.generic;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gaml.skills.traffic.interfaces.IRoadEntitySkill;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.MovingSkill;
import msi.gaml.types.IType;

/**
 * MovingVehicleSkill skill, moving vehicles
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.VEHICLE_LENGTH, type = IType.FLOAT, init = "5.0", doc = {
		@doc("The length of the vehicle") }) })
public abstract class RoadMovingEntitySkill extends MovingSkill implements IRoadEntitySkill {
	// ############################################
	// Getter

	@getter(IKeywordIrit.VEHICLE_LENGTH)
	public double getLength(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.VEHICLE_LENGTH);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.VEHICLE_LENGTH)
	public void setLength(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.VEHICLE_LENGTH, value);
	}

}
