/*******************************************************************************************************
*
* RoadEntitySkill.java, in plugin irit.gama.switchproject,
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
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

/**
 * RoadEntitySkill skill, road entity skill
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.VEHICLE_LENGTH, type = IType.FLOAT, doc = {
		@doc("The length of the vehicle") }) })
public abstract class RoadEntitySkill extends Skill implements IRoadEntitySkill {

}
