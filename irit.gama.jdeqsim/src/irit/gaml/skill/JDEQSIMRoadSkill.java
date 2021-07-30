/*******************************************************************************************************
*
* JDEQSIMRoadSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import irit.gama.common.IKeyword;
import irit.gama.core.unit.Road;
import irit.gama.core.unit.Scheduler;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

/**
 * JDQSIM Road skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@skill(name = IKeyword.JDQSIM_ROAD, concept = { IKeyword.JDQSIM_ROAD, IConcept.SKILL }, internal = true)
public class JDEQSIMRoadSkill extends JDEQSIMSimUnitSkill {
	// ############################################
	// Actions

	@action(name = "init", args = {
			@arg(name = IKeyword.SCHEDULER, type = IType.AGENT, optional = false, doc = @doc("The scheduler.")),
			@arg(name = IKeyword.FREESPEED, type = IType.FLOAT, optional = false, doc = @doc("Freespeed (m/s).")),
			@arg(name = IKeyword.CAPACITY, type = IType.FLOAT, optional = true, doc = @doc("Capacity (s).")),
			@arg(name = IKeyword.LANES, type = IType.INT, optional = false, doc = @doc("Number of lane.")),
			@arg(name = IKeyword.LENGTH, type = IType.FLOAT, optional = false, doc = @doc("length (m).")) })
	public Object init(final IScope scope) throws GamaRuntimeException {
		IAgent schedulerAgent = (IAgent) scope.getArg(IKeyword.SCHEDULER, IType.AGENT);
		double freespeed = (double) scope.getArg(IKeyword.FREESPEED, IType.FLOAT);
		double capacity = (double) scope.getArg(IKeyword.CAPACITY, IType.FLOAT);
		int lanes = (int) scope.getArg(IKeyword.LANES, IType.INT);
		double length = (double) scope.getArg(IKeyword.LENGTH, IType.FLOAT);
		Scheduler scheduler = (Scheduler) schedulerAgent.getAttribute(IKeyword.CORE_DEFINITION);

		if (capacity <= 0) {
			capacity = 3600.0;
		}

		IAgent agent = scope.getAgent();
		agent.setAttribute(IKeyword.SCHEDULER, schedulerAgent);
		agent.setAttribute(IKeyword.CORE_DEFINITION,
				new Road(scope, agent, scheduler, freespeed, capacity, lanes, length));
		return true;
	}
}
