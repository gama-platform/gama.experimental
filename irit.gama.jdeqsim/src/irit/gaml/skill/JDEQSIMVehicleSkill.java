/*******************************************************************************************************
*
* JDEQSIMVehicleSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import irit.gama.common.IKeyword;
import irit.gama.core.unit.Person;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

/**
 * JDQSIM Vehicle skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeyword.SCHEDULER, type = IType.AGENT, doc = { @doc("Max speed (m/s)The scheduler") }),
		@variable(name = IKeyword.MAXSPEED, type = IType.FLOAT, doc = { @doc("Max speed (m/s)") }),
		@variable(name = IKeyword.SIZE, type = IType.FLOAT, doc = { @doc("Size (m)") }),
		@variable(name = IKeyword.OWNER, type = IType.AGENT, doc = { @doc("The owner") }),
		@variable(name = IKeyword.CURRENT_ROAD, type = IType.AGENT, doc = { @doc("Current road") }),
		@variable(name = IKeyword.LEG_INDEX, type = IType.INT, doc = { @doc("Leg index") }),
		@variable(name = IKeyword.LINK_INDEX, type = IType.INT, doc = { @doc("Link index") }) })
@skill(name = IKeyword.JDQSIM_VEHICLE, concept = { IKeyword.JDQSIM_VEHICLE, IConcept.SKILL }, internal = true)
public class JDEQSIMVehicleSkill extends JDEQSIMSimUnitSkill {

	// ############################################
	// Getter

	@getter(IKeyword.SCHEDULER)
	public IAgent getScheduler(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getScheduler().getRelativeAgent();
	}

	@getter(IKeyword.MAXSPEED)
	public double getMaxSpeed(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getMaxSpeed();
	}

	@getter(IKeyword.SIZE)
	public double getSize(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getSize();
	}

	@getter(IKeyword.OWNER)
	public IAgent getOwner(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getOwnerPerson().getRelativeAgent();
	}

	@getter(IKeyword.CURRENT_ROAD)
	public IAgent getCurrentRoad(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getCurrentRoad().getRelativeAgent();
	}

	@getter(IKeyword.LEG_INDEX)
	public int getLegIndex(final IAgent agent) {
		if (agent == null) {
			return 0;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getLegIndex();
	}

	@getter(IKeyword.LINK_INDEX)
	public int getLinkIndex(final IAgent agent) {
		if (agent == null) {
			return 0;
		}
		return ((Vehicle) agent.getAttribute(IKeyword.CORE_DEFINITION)).getLinkIndex();
	}

	// ############################################
	// Actions

	@action(name = "init", args = {
			@arg(name = IKeyword.SCHEDULER, type = IType.AGENT, optional = false, doc = @doc("The scheduler.")),
			@arg(name = IKeyword.MAXSPEED, type = IType.FLOAT, optional = false, doc = @doc("Max speed (m/s).")),
			@arg(name = IKeyword.SIZE, type = IType.FLOAT, optional = false, doc = @doc("Size (m).")),
			@arg(name = IKeyword.OWNER, type = IType.AGENT, optional = false, doc = @doc("The owner.")) })
	public Object init(final IScope scope) throws GamaRuntimeException {
		IAgent schedulerAgent = (IAgent) scope.getArg(IKeyword.SCHEDULER, IType.AGENT);
		IAgent ownerAgent = (IAgent) scope.getArg(IKeyword.OWNER, IType.AGENT);
		double maxspeed = (double) scope.getArg(IKeyword.MAXSPEED, IType.FLOAT);
		double size = (double) scope.getArg(IKeyword.SIZE, IType.FLOAT);
		IAgent agent = scope.getAgent();

		Scheduler scheduler = (Scheduler) schedulerAgent.getAttribute(IKeyword.CORE_DEFINITION);
		Person owner = (Person) ownerAgent.getAttribute(IKeyword.CORE_DEFINITION);

		agent.setAttribute(IKeyword.SCHEDULER, schedulerAgent);
		agent.setAttribute(IKeyword.CORE_DEFINITION, new Vehicle(scope, agent, scheduler, owner, maxspeed, size));
		return true;
	}
}
