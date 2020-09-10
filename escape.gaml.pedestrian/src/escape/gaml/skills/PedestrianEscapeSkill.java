package escape.gaml.skills;

import java.util.Comparator;

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
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gama.util.path.IPath;
import msi.gaml.descriptions.ConstantExpressionDescription;
import msi.gaml.operators.Containers;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.IStatement;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;

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
			doc = @doc ("has the pedestrian to avoid other pedestrians?")),
	@variable (
			name = "tolerance_target",
			type = IType.FLOAT,
			init = "1.0",
			doc = @doc ("distance to a target (in meters) to consider that an agent is arrived at the target"))
	
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
	public final static String TOLERANCE_TARGET = "tolerance_target";
	
	public final static String REDUCE_ANGULAR_DISTANCE = "reduce_angular_distance";
	
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
	
	@getter(TOLERANCE_TARGET)
	public double getToleranceTarget(final IAgent agent) {
	    return (Double) agent.getAttribute(TOLERANCE_TARGET);
	}

	@setter(TOLERANCE_TARGET)
	public void setToleranceTarget(final IAgent agent, final double s) {
	    agent.setAttribute(TOLERANCE_TARGET, s);
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
	public IMap getRoadsTargets(final IAgent agent) {
		return (IMap) agent.getAttribute(ROADS_TARGET);
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
					@arg (
						name = REDUCE_ANGULAR_DISTANCE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("True means that intermediary targets will be choosen so to reduce LOCAL "
								+ "angular distance (from any node n to n+2 on the current_path) "
								+ "while False means the path follow the exact sequence of nodes of the pedestrian network")
							)
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
		IShape source = agent.getLocation();
		
		thePath = ((GraphTopology) graph.getTopology(scope)).pathBetween(scope, source, target);
		
		// If there is no path between source and target ...
		if(thePath == null) { return thePath; }
		IMap<GamaPoint,IShape> roadTarget = GamaMapFactory.create();
		IList<GamaPoint> targets = GamaListFactory.create();
		IList<IShape> segments = thePath.getEdgeGeometry();
		if (scope.hasArg(REDUCE_ANGULAR_DISTANCE)&& scope.getBoolArg(REDUCE_ANGULAR_DISTANCE) ) {
			GamaPoint pp = source.getCentroid();
			
			for(int i = 0; i < segments.size(); i++) {
				IShape cSeg = segments.get(i);
				IShape cRoad = thePath.getRealObject(cSeg);
				IShape nSeg = (i == segments.size() -1 ) ? null : segments.get(i+1);
				for (int j = 1; j< cSeg.getPoints().size(); j++) {
					GamaPoint pt = cSeg.getPoints().get(j).toGamaPoint();
					GamaPoint cTarget = null;
					if (PedestrianRoadSkill.getRoadStatus(scope, cRoad) == PedestrianRoadSkill.SIMPLE_STATUS) {
						cTarget = pt;
					} else {
						GamaPoint nextPt = null;
						if ((j == (cSeg.getPoints().size() - 1))) 
							nextPt = nSeg == null ? null : nSeg.getPoints().get(1).toGamaPoint();
						else 
							nextPt =  cSeg.getPoints().get(j+1).toGamaPoint();
						
						cTarget = getReducedAngularDistanceTarget(scope,cRoad.getAgent(), pp, pt, nextPt);
						if (cTarget == null) cTarget = pt;
					}
					targets.add(cTarget);
					roadTarget.put(cTarget,cRoad);
					pp = cTarget;
				}
			}
		} else {
			targets = thePath.getGeometry().getPoints()
					.stream(scope).map(ILocation::toGamaPoint)
					.collect(GamaListFactory.toGamaList());
			targets = Containers.remove_duplicates(scope, targets);
			if (targets == null || targets.isEmpty()) return thePath;
			//edges = (IList<IShape>) edges.reverse(scope);
			IList<IShape> edges = thePath.getEdgeList();
			
			for(GamaPoint tar : targets) {
				roadTarget.put(tar, edges.stream(scope)
							.filter(e -> e.getPoints().contains(tar))
							.findFirst().orElse(null)
						);
			}
		}
		agent.setAttribute(ROADS_TARGET, roadTarget);
		setCurrentIndex(agent, 0);
		setTargets(agent, targets);
			
		setFinalTarget(agent, target.getLocation());
		agent.setLocation(targets.get(0));
		GamaPoint targ =  targets.get(0);
		setCurrentTarget(agent, targ);
		
		IAgent road = (IAgent) roadTarget.get(targ);
		if (road != null) PedestrianRoadSkill.register(scope, road , agent);
				
		agent.setAttribute(CURRENT_PATH, thePath);
		return thePath;
	}
	
	@action (
			name = "release_path",args = { @arg (
							name = "current_road",
							type = IType.AGENT,
							optional = true,
							doc = @doc ("current road on which the agent is located (can be nil)")),
					},
			doc = @doc (
					value = "clean all the interne state of the agent"))
	public void primArrivedAtDestination(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		IAgent road = (IAgent) scope.getArg("current_road", IType.AGENT);
		setCurrentIndex(agent, 0 );
		setCurrentTarget(agent, null);
		setTargets(agent, GamaListFactory.create());
		setFinalTarget(agent, null);
		setCurrentPath(agent, null);
		setCurrentEdge(agent, (IShape) null);
		setRealSpeed(agent, 0.0);
		if (road != null) PedestrianRoadSkill.unregister(scope, road, agent);
	}
	
	
	@action (
			name = WALK,
			args = { 
					@arg (
						name = IKeyword.SPEED,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the speed to use for this move (replaces the current value of speed)"))
					},		
			doc = @doc (
					value = "action to walk toward the final target",
					examples = { @example ("do walk;") })
			)
	public void primWalkEscape(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (agent == null || agent.dead()) { return; }
		
		final GamaPoint finalTarget = getFinalTarget(agent);
		if (finalTarget == null) { return; }
		
		final IList<GamaPoint> targets = getTargets(agent);
		if (targets == null || targets.isEmpty()) { return; }
		
		DEBUG.ON();
		String debug_id = "["+agent+"]\t";
		
		GamaPoint location = (GamaPoint) getLocation(agent).copy(scope);
		double maxDist = computeDistance(scope, agent);
		
//		DEBUG.OUT("Agent walk", 15, agent);

		boolean movement = true;
		int maxIndex = targets.size() - 1;

		while(movement) {
			
			movement = false;
			int index = getCurrentIndex(agent);
			GamaPoint currentTarget = getCurrentTarget(agent);
			IAgent road = (IAgent) getRoadsTargets(agent).get(currentTarget);
			
//			DEBUG.OUT(debug_id+"On road "+road);
			
			IShape bounds = null;
			boolean avoidOther = getAvoidOther(agent);
			
			if(road != null) {
				
				avoidOther = PedestrianRoadSkill.getRoadStatus(scope, road) == PedestrianRoadSkill.SIMPLE_STATUS ? false : avoidOther;
				bounds = PedestrianRoadSkill.getFreeSpace(scope, road);
				
			}
			
			IContainer<Integer, ?> obstacles = road == null ? GamaListFactory.create() :PedestrianRoadSkill.getAgentsOn(road);
			
			GamaPoint prevLoc = location.copy(scope);
			walkWithForceModel(scope, agent, currentTarget, avoidOther, bounds,obstacles,maxDist);
			location = agent.getLocation().toGamaPoint();
			
//			DEBUG.OUT(debug_id+"moves from "+prevLoc+" to "+location);
			
			if (arrivedAtTarget(scope,location,currentTarget,getToleranceTarget(agent), index, maxIndex, targets)) {
				if (road != null) PedestrianRoadSkill.unregister(scope, road, agent);
				if (index < maxIndex) {
					index++;
					
					setCurrentIndex(agent, index );
					setCurrentTarget(agent, targets.get(index));
					road = (IAgent) getRoadsTargets(agent).get(getCurrentTarget(agent));
					
					if (road != null) { PedestrianRoadSkill.register(scope, road, agent); }
					
//					DEBUG.OUT(debug_id+"switches to a new road context: "+road);
					
					maxDist -= location.distance(prevLoc);
					if (maxDist > 0) movement = true;
				} else {
					final ISpecies context = agent.getSpecies();
					final IStatement.WithArgs actionTNR = context.getAction("release_path");
					final Arguments argsTNR = new Arguments();
					argsTNR.put("current_road", ConstantExpressionDescription.create(road));
					actionTNR.setRuntimeArgs(scope, argsTNR);
					
					actionTNR.executeOn(scope) ;
					
//					DEBUG.OUT(debug_id+"End of movement and launch 'release_path' action");
				}
			}
		}
	
		
	}
	
	boolean arrivedAtTarget(IScope scope, GamaPoint location, GamaPoint currentTarget, double size, int index, int maxIndex, IList<GamaPoint> targets) {
		double dist = location.euclidianDistanceTo(currentTarget);
		if (dist <= size){ return true;}
		return false;		
	}	
	
	/*
	 * Identify the exit point that locally minimize euclidian distance from exit hub (globally reduce angular distance)
	 * 
	 * TODO : identify actual angular distance to filter exit from exit hub
	 * 
	 */
	private GamaPoint getReducedAngularDistanceTarget(IScope scope,IAgent road, GamaPoint loc, GamaPoint cTarget, GamaPoint nextTarget) {
		if (nextTarget == null) return null;
		IList<GamaPoint> exitNodes = PedestrianRoadSkill.getExitNodesHub(road).get(cTarget);
		if (exitNodes.isEmpty()) return null;
		
		GamaPoint pt = exitNodes.stream().min(Comparator
					.comparing(t -> (loc.euclidianDistanceTo(t) + t.euclidianDistanceTo(nextTarget))))
					.orElseGet(() -> null);
		
		return pt;
		
	}
		
}
