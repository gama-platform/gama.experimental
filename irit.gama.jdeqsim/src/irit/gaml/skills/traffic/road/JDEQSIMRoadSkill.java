/*******************************************************************************************************
*
* LoggerSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic.road;

import irit.gama.common.interfaces.IKeywordIrit;
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
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

/**
 * JDQSIM Road implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.Z, type = IType.STRING, doc = { @doc("Z") }), })
@skill(name = IKeywordIrit.JDQSIMROAD, concept = { IKeywordIrit.JDQSIMROAD, IConcept.SKILL }, internal = true)
public class JDEQSIMRoadSkill extends Skill {

	// ############################################
	// Getter and setter

	@getter(IKeywordIrit.Z)
	public String getZ(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (String) agent.getAttribute(IKeywordIrit.Z);
	}

	@setter(IKeywordIrit.Z)
	public void setZ(final IAgent agent, final String value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.Z, value);
	}

	// ############################################
	// Actions

	@action(name = "test", args = { @arg(name = IKeywordIrit.X, type = IType.STRING, optional = false, doc = @doc("X")),
			@arg(name = IKeywordIrit.Y, type = IType.STRING, optional = false, doc = @doc("Y")) }, doc = @doc(examples = {
					@example("do test x: \"X\" y: \"Y\"") }, value = "Return X : Y : Z"))
	public Object logPlot2d(final IScope scope) throws GamaRuntimeException {
		String x = (String) scope.getArg("x", IType.STRING);
		String y = (String) scope.getArg("y", IType.STRING);
		String z = (String) scope.getAgent().getAttribute(IKeywordIrit.Z);

		return x + " : " + y + " : " + z;
	}

}
