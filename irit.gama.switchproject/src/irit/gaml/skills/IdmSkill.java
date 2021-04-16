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

package irit.gaml.skills;

import irit.gama.common.interfaces.IKeywordIrit;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.topology.ITopology;
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
import msi.gama.util.path.IPath;
import msi.gaml.operators.Cast;
import msi.gaml.skills.MovingSkill;
import msi.gaml.types.IType;

/**
 * IDM skill, IDM implementation
 * 
 * @author Jean-François Erdelyi
 */
@vars({ @variable(name = IKeywordIrit.IDM_VEHICLE_LENGHT, type = IType.FLOAT, init = "5.0", doc = {
		@doc("The length of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_DESIRED_SPEED, type = IType.FLOAT, init = "30.0", doc = {
				@doc("The desired speed of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_SPACING, type = IType.FLOAT, init = "1.0", doc = {
				@doc("The jam distance") }),
		@variable(name = IKeywordIrit.IDM_REACTION_TIME, type = IType.FLOAT, init = "1.5", doc = {
				@doc("The reaction time of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_MAX_ACCELERATION, type = IType.FLOAT, init = "4.0", doc = {
				@doc("The maximum acceleration of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_DESIRED_DECELERATION, type = IType.FLOAT, init = "3.0", doc = {
				@doc("The desired deceleration of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_ACCELERATION, type = IType.FLOAT, init = "0.0", doc = {
				@doc("The acceleration of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_DELTA_SPEED, type = IType.FLOAT, init = "0.0", doc = {
				@doc("The acceleration of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_ACTUAL_GAP, type = IType.FLOAT, init = "0.0", doc = {
				@doc("The acceleration of the vehicle") }),
		@variable(name = IKeywordIrit.IDM_DESIRED_MINIMUM_GAP, type = IType.FLOAT, init = "0.0", doc = {
				@doc("The acceleration of the vehicle") }) })
@skill(name = IKeywordIrit.IDM, concept = { IKeywordIrit.IDM, IConcept.SKILL }, internal = true)
public class IdmSkill extends MovingSkill {

	// ############################################
	// Getter

	@getter(IKeywordIrit.IDM_VEHICLE_LENGHT)
	public double getLength(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_VEHICLE_LENGHT);
	}

	@getter(IKeywordIrit.IDM_DESIRED_SPEED)
	public double getDesiredSpeed(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_DESIRED_SPEED);
	}

	@getter(IKeywordIrit.IDM_SPACING)
	public double getSpacing(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_SPACING);
	}

	@getter(IKeywordIrit.IDM_REACTION_TIME)
	public double getReactionTime(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_REACTION_TIME);
	}

	@getter(IKeywordIrit.IDM_MAX_ACCELERATION)
	public double getMaxAcceleration(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_MAX_ACCELERATION);
	}

	@getter(IKeywordIrit.IDM_DESIRED_DECELERATION)
	public double getDesiredDeceleration(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_DESIRED_DECELERATION);
	}

	@getter(IKeywordIrit.IDM_ACCELERATION)
	public double getAcceleration(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_ACCELERATION);
	}

	@getter(IKeywordIrit.IDM_DELTA_SPEED)
	public double getDeltaSpeed(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_DELTA_SPEED);
	}

	@getter(IKeywordIrit.IDM_ACTUAL_GAP)
	public double getActualGap(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_ACTUAL_GAP);
	}

	@getter(IKeywordIrit.IDM_DESIRED_MINIMUM_GAP)
	public double getDesiredMinimumGap(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (double) agent.getAttribute(IKeywordIrit.IDM_DESIRED_MINIMUM_GAP);
	}

	// ############################################
	// Setter

	@setter(IKeywordIrit.IDM_VEHICLE_LENGHT)
	public void setLength(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.IDM_VEHICLE_LENGHT, value);
	}

	@setter(IKeywordIrit.IDM_DESIRED_SPEED)
	public void setDesiredSpeed(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.IDM_DESIRED_SPEED, value);
	}

	@setter(IKeywordIrit.IDM_SPACING)
	public void setSpacing(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.IDM_SPACING, value);
	}

	@setter(IKeywordIrit.IDM_REACTION_TIME)
	public void setReactionTime(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.IDM_REACTION_TIME, value);
	}

	@setter(IKeywordIrit.IDM_MAX_ACCELERATION)
	public void setMaxAcceleration(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.IDM_MAX_ACCELERATION, value);
	}

	@setter(IKeywordIrit.IDM_DESIRED_DECELERATION)
	public void setDesiredDeceleration(final IAgent agent, final double value) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IKeywordIrit.IDM_DESIRED_DECELERATION, value);
	}

	// ############################################
	// Actions

	@action(name = "goto", args = {
			@arg(name = "target", type = IType.GEOMETRY, optional = false, doc = @doc("the location or entity towards which to move.")),
			@arg(name = "on", type = IType.NONE, optional = true, doc = @doc("graph, topology, list of geometries or map of geometries that restrain this move")),
			@arg(name = "follow", type = IType.AGENT, optional = true, doc = @doc("the agent to folow")),
			@arg(name = "desired_speed", type = IType.FLOAT, optional = true, doc = @doc("the desired speed.")), }, doc = @doc(value = "moves the agent towards the target passed in the arguments.", returns = "optional: the path followed by the agent.", examples = {
					@example("do goto target: (one_of road).location follow: next_car on: road_network;") }))
	public IPath<?, ?, ?> oneStep(final IScope scope) throws GamaRuntimeException {
		// Get arguments
		IAgent agent = scope.getAgent();
		IAgent follow = (IAgent) scope.getArg("follow", IType.AGENT);
		if (scope.hasArg("desired_speed")) {
			double desiredSpeed = (double) scope.getArg("desired_speed", IType.FLOAT);
			agent.setAttribute(IKeywordIrit.IDM_DESIRED_SPEED, desiredSpeed);
		}

		// Compute and set speed
		agent.setAttribute(IKeyword.SPEED, idmOneStep(scope, agent, follow, getTopology(scope)));

		// Execute action goto
		return primGoto(scope);
	}

	// ############################################
	// Internal behavior

	/**
	 * Get topology from the scope
	 * 
	 * @param scope the scope
	 * @return the topology
	 */
	protected ITopology getTopology(final IScope scope) {
		final Object on = scope.getArg("on", IType.NONE);
		final ITopology topo = Cast.asTopology(scope, on);
		if (topo == null) {
			return scope.getTopology();
		}
		return topo;
	}

	/**
	 * IDM one step compute acceleration and speed
	 * 
	 * @param scope  the scope
	 * @param follow the follow
	 * @param on     the graph
	 * @return computed speed
	 */
	protected double idmOneStep(final IScope scope, final IAgent agent, final IAgent follow, final ITopology on) {
		
		// Get current agent data
		double maxAcceleration = (double) agent.getAttribute(IKeywordIrit.IDM_MAX_ACCELERATION);
		double desiredDeceleration = (double) agent.getAttribute(IKeywordIrit.IDM_DESIRED_DECELERATION);
		double speed = (double) agent.getAttribute(IKeyword.SPEED);
		double desiredSpeed = (double) agent.getAttribute(IKeywordIrit.IDM_DESIRED_SPEED);
		if (follow == null || follow.dead()) {
			// If the followed agent is dead or nil then this agent is supposed as the platoon leader
			return computeAsLeader(scope, agent, desiredDeceleration, maxAcceleration, speed, desiredSpeed);
		}
		
		// Check distance
		double distanceBetween = on.distanceBetween(scope, agent, follow);
		if (on.distanceBetween(scope, agent, follow) == Double.MAX_VALUE) {
			// TODO MAX_VALUE = unreachable, maybe change computeAsLeader by euclidean distance ?
			return computeAsLeader(scope, agent, desiredDeceleration, maxAcceleration, speed, desiredSpeed);
		}
		
		// Get current agent data
		double spacing = (double) agent.getAttribute(IKeywordIrit.IDM_SPACING);
		double reactionTime = (double) agent.getAttribute(IKeywordIrit.IDM_REACTION_TIME);
		double length = (double) agent.getAttribute(IKeywordIrit.IDM_VEHICLE_LENGHT);

		// Get followed data
		double followedSpeed = (double) follow.getAttribute(IKeyword.SPEED);
		double followedLength = (double) follow.getAttribute(IKeywordIrit.IDM_VEHICLE_LENGHT);

		// Computation		
		double deltaSpeed = followedSpeed - speed;
		double actualGap = distanceBetween - ((length / 2.0) + (followedLength / 2.0));
		
		double accFactor = maxAcceleration * desiredDeceleration;
		double desiredMinimumGap = spacing + (reactionTime * speed) - ((speed * deltaSpeed) / (2.0 * accFactor * accFactor));
		
		double speedFactor = speed / desiredSpeed;
		double gapFactor = desiredMinimumGap / actualGap;
		double acceleration = maxAcceleration * (1.0 - (speedFactor * speedFactor * speedFactor * speedFactor) - (gapFactor * gapFactor));

		// Set values and return speed
		return setValuesAndComputeSpeed(scope, agent, acceleration, desiredDeceleration, maxAcceleration, speed, deltaSpeed, actualGap, desiredMinimumGap);
	}

	/**
	 * Set values and compute speed
	 * 
	 * @param scope                the scope
	 * @param agent                the agent
	 * @param acceleration         computed acceleration
	 * @param desiredDeceleration  max deceleration
	 * @param maxAcceleration      max acceleration
	 * @param speed                actual speed
	 * @param deltaSpeed           delta speed between the current agent and the followed one
	 * @param actualGap            delta location between the current agent and the followed one
	 * @param desiredMinimumGap    desired minimum gap between the current agent and the followed one
	 * @return computed speed
	 */
	protected double setValuesAndComputeSpeed(final IScope scope, final IAgent agent, double acceleration, final double desiredDeceleration, final double maxAcceleration, final double speed, final double deltaSpeed, final double actualGap, final double desiredMinimumGap) {
		if (acceleration < -desiredDeceleration) {
			acceleration = -desiredDeceleration;
		} else if (acceleration > maxAcceleration) {
			acceleration = maxAcceleration;
		}
		agent.setAttribute(IKeywordIrit.IDM_DELTA_SPEED, deltaSpeed);
		agent.setAttribute(IKeywordIrit.IDM_ACTUAL_GAP, actualGap);
		agent.setAttribute(IKeywordIrit.IDM_DESIRED_MINIMUM_GAP, desiredMinimumGap);
		agent.setAttribute(IKeywordIrit.IDM_ACCELERATION, acceleration);
		return speed + (acceleration * scope.getClock().getStepInSeconds());
	}

	/**
	 * Compute acceleration as leader
	 * 
	 * @param scope                the scope
	 * @param agent                the agent
	 * @param desiredDeceleration  max deceleration
	 * @param maxAcceleration      max acceleration
	 * @param speed                actual speed
	 * @param desiredSpeed         desired speed
	 * @return computed speed
	 */
	protected double computeAsLeader(final IScope scope, final IAgent agent, final double desiredDeceleration, final double maxAcceleration, final double speed, final double desiredSpeed) {
		double speedFactor = speed / desiredSpeed;
		double acceleration = maxAcceleration * (1.0 - (speedFactor * speedFactor * speedFactor * speedFactor));
		return setValuesAndComputeSpeed(scope, agent, acceleration, desiredDeceleration, maxAcceleration, speed, 0.0, 0.0, 0.0);
	}

}
