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
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;

/**
 * IRoadEntitySkill skill, entity on the road skill interface
 * 
 * @author Jean-François Erdelyi
 */
public interface IRoadEntitySkill extends ISkill {
	// ############################################
	// Getter

	@getter(IKeywordIrit.VEHICLE_LENGTH)
	public abstract double getLength(final IAgent agent);

	// ############################################
	// Setter

	@setter(IKeywordIrit.VEHICLE_LENGTH)
	public abstract void setLength(final IAgent agent, final double value);
}
