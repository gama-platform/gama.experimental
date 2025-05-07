/*******************************************************************************************************
*
* JDEQSIMPersonSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import java.util.ArrayList;
import java.util.List;

import irit.gama.common.IKeyword;
import irit.gama.core.plan.Activity;
import irit.gama.core.plan.Leg;
import irit.gama.core.unit.Person;
import irit.gama.core.unit.Road;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;
import msi.gaml.types.IType;

/**
 * JDQSIM Road skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@SuppressWarnings("unchecked")
@skill(name = IKeyword.JDQSIM_PERSON, concept = { IKeyword.JDQSIM_PERSON, IConcept.SKILL }, internal = true)
public class JDEQSIMPersonSkill extends JDEQSIMSimUnitSkill {
	// ############################################
	// Actions

	@action(name = "init", args = {
			@arg(name = IKeyword.SCHEDULER, type = IType.AGENT, optional = false, doc = @doc("The scheduler.")) })
	public Object init(final IScope scope) throws GamaRuntimeException {
		IAgent schedulerAgent = (IAgent) scope.getArg(IKeyword.SCHEDULER, IType.AGENT);
		IAgent agent = scope.getAgent();

		agent.setAttribute(IKeyword.SCHEDULER, schedulerAgent);
		agent.setAttribute(IKeyword.CORE_DEFINITION, new Person(scope, agent));
		return true;
	}

	@action(name = IKeyword.ADD_LEG, args = {
			@arg(name = IKeyword.LEG, type = IType.LIST, optional = false, doc = @doc("The leg (route as list of road).")) })
	public Object setLeg(final IScope scope) throws GamaRuntimeException {

		List<IAgent> roadsAgent = (List<IAgent>) scope.getArg(IKeyword.LEG, IType.LIST);
		Person innerPerson = (Person) scope.getAgent().getAttribute(IKeyword.CORE_DEFINITION);

		List<Road> roads = new ArrayList<>();
		if (roadsAgent.size() > 1) {
			for (var i = 1; i < roadsAgent.size() - 1; ++i) {
				roads.add((Road) roadsAgent.get(i).getAttribute(IKeyword.CORE_DEFINITION));
			}
		}

		return innerPerson.addLeg(new Leg(roads));
	}

	@action(name = IKeyword.ADD_ACTIVITY, args = {
			@arg(name = IKeyword.ACTIVITY_DATE, type = IType.DATE, optional = false, doc = @doc("The activity end date.")),
			@arg(name = IKeyword.ACTIVITY_DURATION, type = IType.FLOAT, optional = false, doc = @doc("The activity duration.")),
			@arg(name = IKeyword.ACTIVITY_ROAD, type = IType.AGENT, optional = false, doc = @doc("The activity road.")),
			@arg(name = IKeyword.ACTIVITY_BUILDING, type = IType.AGENT, optional = false, doc = @doc("The activity building.")) })
	public Object setActivity(final IScope scope) throws GamaRuntimeException {

		GamaDate date = (GamaDate) scope.getArg(IKeyword.ACTIVITY_DATE, IType.DATE);
		double duration = (double) scope.getArg(IKeyword.ACTIVITY_DURATION, IType.FLOAT);
		IAgent roadAgent = (IAgent) scope.getArg(IKeyword.ACTIVITY_ROAD, IType.AGENT);
		IAgent buildingAgent = (IAgent) scope.getArg(IKeyword.ACTIVITY_BUILDING, IType.AGENT);

		Person innerPerson = (Person) scope.getAgent().getAttribute(IKeyword.CORE_DEFINITION);
		Road road = (Road) roadAgent.getAttribute(IKeyword.CORE_DEFINITION);

		return innerPerson.addActivity(new Activity(date, duration, road, buildingAgent));
	}
}
