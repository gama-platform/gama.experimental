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

package irit.gaml.skills.traffic.vehicle;

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
 * JDQSIM Vehicle implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.C, type = IType.STRING, doc = { @doc("C") }), })
@skill(name = IKeywordIrit.JDQSIMVEHICLE, concept = { IKeywordIrit.JDQSIMVEHICLE, IConcept.SKILL }, internal = true)
public class JDEQSIMVehicleSkill extends Skill {

	// ############################################
	// Getter and setter

	@getter(IKeywordIrit.C)
	public String getC(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (String) agent.getAttribute(IKeywordIrit.C);
	}

	@setter(IKeywordIrit.C)
	public void setC(final IAgent agent, final String value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.C, value);
	}

	// ############################################
	// Actions

	@action(name = "test", args = { @arg(name = IKeywordIrit.A, type = IType.STRING, optional = false, doc = @doc("X")),
			@arg(name = IKeywordIrit.B, type = IType.STRING, optional = false, doc = @doc("Y")) }, doc = @doc(examples = {
					@example("do test a: \"A\" b: \"B\"") }, value = "Return A : B : C"))
	public Object logPlot2d(final IScope scope) throws GamaRuntimeException {
		String a = (String) scope.getArg("a", IType.STRING);
		String b = (String) scope.getArg("b", IType.STRING);
		String c = (String) scope.getAgent().getAttribute(IKeywordIrit.C);

		return a + " : " + b + " : " + c;
	}

}
