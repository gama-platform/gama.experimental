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
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

/**
 * JDQSIM Road skill implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeyword.CAPACITY, type = IType.FLOAT, doc = { @doc("Capacity") }),
		@variable(name = IKeyword.MAX_CAPACITY, type = IType.FLOAT, doc = { @doc("Max capacity") }),
		@variable(name = IKeyword.SCHEDULER, type = IType.AGENT, doc = { @doc("The scheduler") }),
		@variable(name = IKeyword.FREESPEED, type = IType.FLOAT, doc = { @doc("Freespeed (m/s)") }),
		@variable(name = IKeyword.FLOW_CAPACITY, type = IType.FLOAT, doc = { @doc("Flow capacity (s)") }),
		@variable(name = IKeyword.NO_LANES, type = IType.INT, doc = { @doc("Number of lane") }),
		@variable(name = IKeyword.LENGTH, type = IType.FLOAT, doc = { @doc("Length (m)") }) })
@skill(name = IKeyword.JDQSIM_ROAD, concept = { IKeyword.JDQSIM_ROAD, IConcept.SKILL }, internal = true)
public class JDEQSIMRoadSkill extends JDEQSIMSimUnitSkill {
	// ############################################
	// Setter

	@setter(IKeyword.NO_LANES)
	public void setNofLanes(final IAgent agent, final int value) {
		if (agent == null) {
			return;
		} else if (((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)) != null) {
			((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).setNofLanes(value);
		}
	}

	@setter(IKeyword.FREESPEED)
	public void setFreespeed(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		} else if (((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)) != null) {
			((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).setFreespeed(value);
		}
	}

	// ############################################
	// Getter

	@getter(IKeyword.CAPACITY)
	public double getCurrentCapacity(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getCurrentCapacity();
	}

	@getter(IKeyword.MAX_CAPACITY)
	public double getMaxCapacityOnRoad(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getMaxCapacityOnRoad();
	}

	@getter(IKeyword.PROMISE_CAPACITY)
	public double getCurrentCapacityPromisedToEnterRoad(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getCurrentCapacityPromisedToEnterRoad();
	}

	@getter(IKeyword.SCHEDULER)
	public IAgent getScheduler(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getScheduler().getRelativeAgent();
	}

	@getter(IKeyword.FREESPEED)
	public double getFreespeed(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getFreespeed();
	}

	@getter(IKeyword.FLOW_CAPACITY)
	public double getFlowCapacity(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getFlowCapacity();
	}

	@getter(IKeyword.NO_LANES)
	public int getNofLanes(final IAgent agent) {
		if (agent == null) {
			return 0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getNofLanes();
	}

	@getter(IKeyword.LENGTH)
	public double getLenght(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return ((Road) agent.getAttribute(IKeyword.CORE_DEFINITION)).getLength();
	}

	// ############################################
	// Actions

	@action(name = "init", args = {
			@arg(name = IKeyword.SCHEDULER, type = IType.AGENT, optional = false, doc = @doc("The scheduler.")),
			@arg(name = IKeyword.MAXSPEED, type = IType.FLOAT, optional = false, doc = @doc("Freespeed (m/s).")),
			@arg(name = IKeyword.FLOW_CAPACITY, type = IType.FLOAT, optional = true, doc = @doc("Flow capacity (s).")),
			@arg(name = IKeyword.NO_LANES, type = IType.INT, optional = false, doc = @doc("Number of lane.")),
			@arg(name = IKeyword.LENGTH, type = IType.FLOAT, optional = false, doc = @doc("Length (m).")) })
	public Object init(final IScope scope) throws GamaRuntimeException {
		IAgent schedulerAgent = (IAgent) scope.getArg(IKeyword.SCHEDULER, IType.AGENT);
		double freespeed = (double) scope.getArg(IKeyword.MAXSPEED, IType.FLOAT);
		double capacity = (double) scope.getArg(IKeyword.FLOW_CAPACITY, IType.FLOAT);
		int lanes = (int) scope.getArg(IKeyword.NO_LANES, IType.INT);
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
