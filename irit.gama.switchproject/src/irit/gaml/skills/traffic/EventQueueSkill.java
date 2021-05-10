/*******************************************************************************************************
*
* IdmSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.traffic;

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
import msi.gaml.types.IType;

/**
 * EventQueue skill, Meso road event and queue based implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.BPR_ALPHA, type = IType.FLOAT, init = "0.15", doc = {
				@doc("alpha parameter of the BPR function") }),
		@variable(name = IKeywordIrit.BPR_BETA, type = IType.FLOAT, init = "4.0", doc = {
				@doc("beta parameter of the BPR function") }),
		@variable(name = IKeywordIrit.EVENT_QUEUE_VOLUME, type = IType.FLOAT, init = "0.0", doc = {
				@doc("volume of entities") }),
		@variable(name = IKeywordIrit.EVENT_QUEUE_LENGTH, type = IType.FLOAT, doc = { @doc("capacity of the road") }) })
@skill(name = IKeywordIrit.EVENT_QUEUE, concept = { IKeywordIrit.EVENT_QUEUE, IConcept.SKILL }, internal = true)
public class EventQueueSkill extends SimpleRoadSkill {
	// ############################################
	// Getter

	@getter(IKeywordIrit.BPR_ALPHA)
	public double getAlpha(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.BPR_ALPHA);
	}

	@getter(IKeywordIrit.BPR_BETA)
	public double getBeta(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.BPR_BETA);
	}

	@getter(IKeywordIrit.EVENT_QUEUE_VOLUME)
	public double getVolume(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.EVENT_QUEUE_VOLUME);
	}

	@getter(IKeywordIrit.EVENT_QUEUE_LENGTH)
	public double getLength(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.EVENT_QUEUE_LENGTH);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.BPR_ALPHA)
	public void setAlpha(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.BPR_ALPHA, value);
	}

	@setter(IKeywordIrit.BPR_BETA)
	public void setBeta(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.BPR_BETA, value);
	}

	@setter(IKeywordIrit.EVENT_QUEUE_LENGTH)
	public void setLength(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.EVENT_QUEUE_LENGTH, value);
	}

	// ############################################
	// Actions

	@action(name = "compute_travel_time", args = {
			@arg(name = "desired_speed", type = IType.FLOAT, optional = false, doc = @doc("the desired speed.")),
			@arg(name = "delta_pos", type = IType.FLOAT, optional = true, doc = @doc("if the vehicle does not start at the begining of the road, then set the delta pos.")), }, doc = @doc(value = "compute the free flow travel time using BPR function.", returns = "travel time depending of the croad capacity.", examples = {
					@example("do compute_travel_time desired_speed: 50.0#km/#h delta_pos: 3.0#m;") }))
	public double computeTravelTimeBPR(final IScope scope) throws GamaRuntimeException {
		// Get arguments
		IAgent agent = scope.getAgent();
		double desiredSpeed = (double) scope.getArg("desired_speed", IType.FLOAT);
		double length = (double) scope.getArg("length", IType.FLOAT);
		double deltaPos = 0.0;
		if (scope.hasArg("delta_pos")) {
			deltaPos = (double) scope.getArg("delta_pos", IType.FLOAT);
		}

		// BPR parameters
		double alpha = (double) agent.getAttribute("alpha");
		double beta = (double) agent.getAttribute("beta");

		// Road variables
		double c = (double) agent.getAttribute("length");
		double v = c - (double) agent.getAttribute(IKeywordIrit.EVENT_QUEUE_VOLUME);

		// Compute time to travel
		double tf = ((length - deltaPos) / desiredSpeed);
		return tf * (1.0 + alpha * Math.pow((v / c), beta));
	}

	@action (
			name = "join",
			args = { @arg (
					name = "entity",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("the entity to register on the road.")) },
			doc = @doc (
					value = "register the agent on the road",
					examples = { @example ("do join agent: entity") }))
	@Override
	public void join(final IScope scope) throws GamaRuntimeException {
		super.join(scope);
		
		// Get arguments
		IAgent agent = scope.getAgent();
		IAgent entity = (IAgent) scope.getArg("entity", IType.AGENT);

		// Set volume
		agent.setAttribute("volume", (double) agent.getAttribute("volume") + (double) entity.getAttribute("length"));
	}

	@action (
			name = "leave",
			args = { @arg (
					name = "entity",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("the entity to unregister on the road.")) },
			doc = @doc (
					value = "unregister the agent on the road",
					examples = { @example ("do leave entity: entity") }))
	@Override
	public void leave(final IScope scope) throws GamaRuntimeException {
		super.leave(scope);
		
		// Get arguments
		IAgent agent = scope.getAgent();
		IAgent entity = (IAgent) scope.getArg("entity", IType.AGENT);

		// Set volume
		agent.setAttribute("volume", (double) agent.getAttribute("volume") - (double) entity.getAttribute("length"));
	}
	
	/**
	 * 
	 */

	protected double computeBPR(IAgent agent, double desiredSpeed, double length, double deltaPos) {
		// BPR parameters
		double alpha = (double) agent.getAttribute("alpha");
		double beta = (double) agent.getAttribute("beta");

		// Road variables
		double c = (double) agent.getAttribute("length");
		double v = c - (double) agent.getAttribute(IKeywordIrit.EVENT_QUEUE_VOLUME);

		// Compute time to travel
		double tf = ((length - deltaPos) / desiredSpeed);
		return tf * (1.0 + alpha * Math.pow((v / c), beta));
	}
}
