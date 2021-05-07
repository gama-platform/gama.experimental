/*******************************************************************************************************
*
* SimpleRoadNodeSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gaml.skills.traffic.generic.RoadNodeSkill;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.skill;

@skill(name = IKeywordIrit.ROAD_NODE, concept = { IKeywordIrit.ROAD_NODE, IConcept.SKILL }, internal = true)
public class SimpleRoadNodeSkill extends RoadNodeSkill {

}