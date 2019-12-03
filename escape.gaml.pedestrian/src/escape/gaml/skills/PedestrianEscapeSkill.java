package escape.gaml.skills;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.graph.GraphTopology;
import msi.gama.metamodel.topology.graph.ISpatialGraph;
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
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gama.util.path.IPath;
import msi.gaml.operators.Containers;
import msi.gaml.operators.Spatial;
import msi.gaml.species.ISpecies;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@skill(name = "escape_pedestrian",
	concept = { IConcept.TRANSPORT, IConcept.SKILL },
	doc = @doc ("A skill that provides agent with the ability to walk on continuous space while"
			+ " finding their way on a virtual network"))
@vars({
	@variable(
			name = "shoulder_length", 
			type = IType.FLOAT, init = "0.47",
			doc = @doc ("The length of the pedestrian (in meters)")),
	@variable(
			name = "velocity", 
			type = IType.POINT, init = "{0,0,0}",
			doc = @doc ("The velocity of the pedestrian")),

	@variable (
			name = "final_target",
			type = IType.POINT,
			init = "nil",
			doc = @doc ("the final target of the agent")),
	@variable (
			name = "current_target",
			type = IType.POINT,
			init = "nil",
			doc = @doc ("the current target of the agent")),
	@variable (
			name = "current_index",
			type = IType.INT,
			init = "0",
			doc = @doc ("the current index of the agent target (according to the targets list)")),
	@variable (
			name = "targets",
			type = IType.LIST,
			of = IType.POINT,
			init = "[]",
			doc = @doc ("the current list of points that the agent has to reach (path)")),
	@variable (
			name = "roads_targets",
			type = IType.MAP,
			init = "[]",
			doc = @doc ("for each target, the associated road")),
	@variable (
			name = "other_people_distance_repulsion",
			type = IType.FLOAT,
			init = "1.0",
			doc = @doc ("the maximal distance to take into account the repulsion of the other pedestrians")),
	@variable (
			name = "other_people_repulsion",
			type = IType.FLOAT,
			init = "1.0",
			doc = @doc ("repulsion coefficient from other pedestrian")),
	@variable (
			name = "min_repulsion_dist",
			type = IType.FLOAT,
			init = "0.3",
			doc = @doc ("minimal distance considered for repulsion (below this value, the impact of repulsion is similar) ")),
	@variable (
			name = "avoid_other",
			type = IType.BOOL,
			init = "true",
			doc = @doc ("has the pedestrian to avoid other pedestrians?"))
	
	})
public class PedestrianEscapeSkill extends PedestrianSkill {
	
	// ---------- CONSTANTS -------------- //
	
	// VAR
	
	public final static String TARGETS = "targets";
	public final static String ROADS_TARGET = "roads_targets";
	
	public final static String SHOULDER_LENGTH = "shoulder_length";
	public final static String CURRENT_TARGET = "current_target";
	public final static String CURRENT_INDEX = "current_index";
	public final static String FINAL_TARGET = "final_target";
	public final static String CURRENT_PATH = "current_path";
	public final static String PEDESTRIAN_GRAPH = "pedestrian_graph";
	public final static String SOURCE = "source";
	public final static String VELOCITY = "velocity";
	public final static String OTHER_PEOPLE_DISTANCE_REPULSION = "other_people_distance_repulsion";
	public final static String OTHER_PEOPLE_REPULSION = "other_people_repulsion";
	public final static String MIN_REPULSION_DIST = "min_repulsion_dist";
	public final static String AVOID_OTHER = "avoid_other";
	
	
	
	// ACTION
	
	public final static String COMPUTE_VIRTUAL_PATH = "compute_virtual_path";
	public final static String WALK = "walk";
	
	// ---------- VARIABLES GETTER AND SETTER ------------- //
	
	@getter(SHOULDER_LENGTH)
	public double getShoulderLength(final IAgent agent) {
	    return (Double) agent.getAttribute(SHOULDER_LENGTH);
	}

	@setter(SHOULDER_LENGTH)
	public void setShoulderLength(final IAgent agent, final double s) {
	    agent.setAttribute(SHOULDER_LENGTH, s);
	}
	@getter (VELOCITY)
	public GamaPoint getVelocity(final IAgent agent) {
		return (GamaPoint) agent.getAttribute(VELOCITY);
	}

	@setter (VELOCITY)
	public void setVelocity(final IAgent agent, final ILocation point) {
		agent.setAttribute(VELOCITY, point);
	}
	
	@getter (TARGETS)
	public IList<GamaPoint> getTargets(final IAgent agent) {
		return (IList<GamaPoint>) agent.getAttribute(TARGETS);
	}

	@getter (ROADS_TARGET)
	public GamaMap getRoadsTargets(final IAgent agent) {
		return (GamaMap) agent.getAttribute(ROADS_TARGET);
	}

	@setter (TARGETS)
	public void setTargets(final IAgent agent, final IList<GamaPoint> points) {
		agent.setAttribute(TARGETS, points);
	}

	@getter (CURRENT_TARGET)
	public GamaPoint getCurrentTarget(final IAgent agent) {
		return (GamaPoint) agent.getAttribute(CURRENT_TARGET);
	}

	@setter (CURRENT_TARGET)
	public void setCurrentTarget(final IAgent agent, final ILocation point) {
		agent.setAttribute(CURRENT_TARGET, point);
	}

	@getter (FINAL_TARGET)
	public GamaPoint getFinalTarget(final IAgent agent) {
		return (GamaPoint) agent.getAttribute(FINAL_TARGET);
	}

	@setter (FINAL_TARGET)
	public void setFinalTarget(final IAgent agent, final ILocation point) {
		agent.setAttribute(FINAL_TARGET, point);
	}

	@getter (CURRENT_INDEX)
	public Integer getCurrentIndex(final IAgent agent) {
		return (Integer) agent.getAttribute(CURRENT_INDEX);
	}

	@setter (CURRENT_INDEX)
	public void setCurrentIndex(final IAgent agent, final Integer index) {
		agent.setAttribute(CURRENT_INDEX, index);
	}
	
	
	@getter (OTHER_PEOPLE_DISTANCE_REPULSION)
	public Double getOtherPeopleDistRepulsion(final IAgent agent) {
		return (Double) agent.getAttribute(OTHER_PEOPLE_DISTANCE_REPULSION);
	}

	@setter (OTHER_PEOPLE_DISTANCE_REPULSION)
	public void setOtherPeopleDistRepulsion(final IAgent agent, final Double val) {
		agent.setAttribute(OTHER_PEOPLE_DISTANCE_REPULSION, val);
	}
	
	@getter (OTHER_PEOPLE_REPULSION)
	public Double getOtherPeopleRepulsion(final IAgent agent) {
		return (Double) agent.getAttribute(OTHER_PEOPLE_REPULSION);
	}

	@setter (OTHER_PEOPLE_REPULSION)
	public void setOtherPeopleRepulsion(final IAgent agent, final Double val) {
		agent.setAttribute(OTHER_PEOPLE_REPULSION, val);
	}
	
	@getter (MIN_REPULSION_DIST)
	public Double getMinDistRepulsion(final IAgent agent) {
		return (Double) agent.getAttribute(MIN_REPULSION_DIST);
	}

	@setter (MIN_REPULSION_DIST)
	public void setMinDistRepulsion(final IAgent agent, final Double val) {
		agent.setAttribute(MIN_REPULSION_DIST, val);
	}
	
	@getter (AVOID_OTHER)
	public Boolean getAvoidOther(final IAgent agent) {
		return (Boolean) agent.getAttribute(AVOID_OTHER);
	}

	@setter (AVOID_OTHER)
	public void setAvoidOther(final IAgent agent, final Boolean val) {
		agent.setAttribute(AVOID_OTHER, val);
	}
	
	// ----------------------------------- //
	
	@action (
			name = COMPUTE_VIRTUAL_PATH,
			args = { @arg (
						name = PEDESTRIAN_GRAPH,
						type = IType.GRAPH,
						optional = false,
						doc = @doc ("the graph on wich compute the path")),
					@arg (
						name = FINAL_TARGET,
						type = IType.GEOMETRY,
						optional = false,
						doc = @doc ("the target to reach, can be any agent")),
					 },
			doc = @doc (
					value = "action to compute a path to a target location according to a given graph",
					returns = "the computed path, return nil if no path can be taken",
					examples = { @example ("do compute_virtual_path graph: pedestrian_network target: any_point;") }))
	public IPath primComputeVirtualPath(final IScope scope) throws GamaRuntimeException {
		IPath thePath = null;
		
		final ISpatialGraph graph = (ISpatialGraph) scope.getArg(PEDESTRIAN_GRAPH, IType.GRAPH);
		final IAgent agent = getCurrentAgent(scope);
		
		IShape target = (IShape) scope.getArg(PedestrianEscapeSkill.FINAL_TARGET, IType.GEOMETRY);
		IShape source = (IShape) scope.getArg(PedestrianEscapeSkill.SOURCE, IType.GEOMETRY);
		
		source = agent.getLocation();
		
		thePath = ((GraphTopology) graph.getTopology(scope)).pathBetween(scope, source, target);
		
		IList<GamaPoint> targets = GamaListFactory.create(Types.POINT);
		int index = 0;
		for(ILocation pt : thePath.getGeometry().getPoints()) targets.add(pt.toGamaPoint());
		if (!targets.lastValue(scope).equals(target)) targets.add(target.getLocation().toGamaPoint());
		targets = Containers.remove_duplicates(scope, targets);
		if (targets == null || targets.isEmpty()) return thePath;
		IMap roadTarget = GamaMapFactory.create();
		IList<IShape> edges = thePath.getEdgeList();
		edges = (IList<IShape>) edges.reverse(scope);
		for(GamaPoint tar : targets) {
			boolean notOk = true;
			for( IShape edge : edges) {
				if (edge.getPoints().contains(tar)) {
					roadTarget.put(tar, edge);
					notOk = false;
					break;
				}
			}
			if (notOk) {
				if (tar == targets.firstValue(scope)) roadTarget.put(tar, edges.lastValue(scope));
				else roadTarget.put(tar, edges.firstValue(scope));
			}
			
		}
		agent.setAttribute(ROADS_TARGET, roadTarget);
		
		PedestrianRoadSkill.register(scope, (IAgent) thePath.getEdgeList().get(0), agent);
		setCurrentIndex(agent, index);
		setTargets(agent, targets);
			
		setFinalTarget(agent, target.getLocation());
		agent.setLocation(targets.get(0));
		setCurrentTarget(agent, targets.get(0));
		
		 
		agent.setAttribute("current_path", thePath);
		return thePath;
	}
	
	@action (
			name = WALK,args = { @arg (
							name = IKeyword.SPEED,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the speed to use for this move (replaces the current value of speed)")),
					},
			doc = @doc (
					value = "action to walk toward the final target",
					examples = { @example ("do walk;") }))
	public void primWalkEscape(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (agent == null || agent.dead()) { return; }
		final GamaPoint finalTarget = getFinalTarget(agent);
		if (finalTarget == null) { return; }
		GamaPoint location = (GamaPoint) getLocation(agent).copy(scope);
		IList<GamaPoint> targets = getTargets(agent);
		double maxDist = computeDistance(scope, agent);
		GamaPoint currentTarget = getCurrentTarget(agent);
			
		boolean movement = true;
		int maxIndex = targets.size() - 1;
		while(movement) {
			 movement = false;
			 int index = getCurrentIndex(agent);
				
			GamaPoint prevLoc = location.copy(scope);
			IAgent road = (IAgent) getRoadsTargets(agent).get(targets.get(index));
			IShape bounds = PedestrianRoadSkill.getFreeSpace(scope, road);
			IContainer obstacles = PedestrianRoadSkill.getAgentsOn(road);
			walkWithForceModel(scope, agent, currentTarget, getAvoidOther(agent), bounds, obstacles, maxDist);
			
			if (arrivedAtTarget(scope,location,currentTarget, getShoulderLength(agent)*2, index, maxIndex, targets)) {
				PedestrianRoadSkill.unregister(scope, road, agent);
				if (index < maxIndex) {
					index++;
					currentTarget = targets.get(index);
					setCurrentIndex(agent, index );
					setCurrentTarget(agent, currentTarget);
					road = (IAgent) getRoadsTargets(agent).get(targets.get(index));
					
					PedestrianRoadSkill.register(scope, road, agent);
					
					maxDist -= location.distance(prevLoc);
						
					if (maxDist > 0) movement = true;
				}
			}
		}
		
	}
	
	boolean arrivedAtTarget(IScope scope, GamaPoint location, GamaPoint currentTarget, double size, int index, int maxIndex, IList<GamaPoint> targets) {
		double dist = location.euclidianDistanceTo(currentTarget);
		if (dist < (2 * size)){ return true;}
		/*if (dist < (size * 5) && (index < maxIndex)) {
			GamaPoint newPt = currentTarget = targets.get(index + 1);
			IList<IShape> pts = GamaListFactory.create(Types.GEOMETRY);
			pts.add(currentTarget);
			pts.add(newPt);
			return location.euclidianDistanceTo(Spatial.Creation.line(scope, pts)) <  dist;
		}*/
		return false;		
		
	}
		
		
	
		
}
