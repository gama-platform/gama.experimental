/*******************************************************************************************************
 *
 * simtools.gaml.extensions.traffic.RoadSkill.java, in plugin simtools.gaml.extensions.traffic, is part of the source
 * code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package escape.gaml.skills;

import java.util.stream.Stream;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
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
import msi.gaml.operators.Spatial;
import msi.gaml.skills.Skill;
import msi.gaml.species.ISpecies;
import msi.gaml.types.GamaIntegerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@skill (
		name = PedestrianRoadSkill.PEDESTRIAN_ROAD_SKILL,
		concept = { IConcept.TRANSPORT, IConcept.SKILL },
		doc = @doc ("A skill for agents representing pedestrian roads"))
@vars ({ 
	@variable (
		name = PedestrianRoadSkill.AGENTS_ON,
		type = IType.LIST,
		of = IType.AGENT,
		init = "[]",
		doc = @doc ("for each people on the road")),
	@variable (
			name = PedestrianRoadSkill.FREE_SPACE,
			type = IType.GEOMETRY,
			init = "nil",
			doc = @doc ("for each people on the road")),
	@variable (
			name = PedestrianRoadSkill.PEDESTRIAN_ROAD_STATUS,
			type = IType.INT,
			init = "1",
			doc = @doc("When road status equals 1 it has 2D continuous space property for pedestrian; when equal to 2 is simply a 1D road")),
	@variable (
			name = PedestrianRoadSkill.EXIT_NODES_HUB,
			type = IType.MAP,
			init = "[]",
			doc = @doc ("The exit hub (several exit connected to each road extremities) that makes it possible to reduce angular distance when travelling to connected pedestrian roads")
			)
	})
public class PedestrianRoadSkill extends Skill {
	
	public final static String PEDESTRIAN_ROAD_SKILL = "pedestrian_road";
	
	public final static String AGENTS_ON = "agents_on";
	public final static String FREE_SPACE = "free_space";
	public final static String PEDESTRIAN_ROAD_STATUS = "road_status";
	public final static String EXIT_NODES_HUB = "exit_nodes";
	public final static String DISTANCE = "distance";

	public final static int SIMPLE_STATUS = 0; // use simple goto operator on those road
	public final static int COMPLEX_STATUS = 1; // use walk operator

	@SuppressWarnings("unchecked")
	@getter (AGENTS_ON)
	public static IList<IAgent> getAgentsOn(final IAgent agent) {
		return (IList<IAgent> ) agent.getAttribute(AGENTS_ON);
	}
	
	@SuppressWarnings("unchecked")
	@getter (EXIT_NODES_HUB)
	public static IMap<GamaPoint,IList<GamaPoint>> getExitNodesHub(final IAgent agent) {
		return (IMap<GamaPoint,IList<GamaPoint>>) agent.getAttribute(EXIT_NODES_HUB);
	}
	
	@setter (EXIT_NODES_HUB)
	public void setExitNodesHub(final IAgent agent, IMap<GamaPoint,IList<GamaPoint>> exitNodesHub) {
		agent.setAttribute(EXIT_NODES_HUB, exitNodesHub);
	}

	@getter (PEDESTRIAN_ROAD_STATUS)
	public int getPedestrianRoadStatus(final IAgent agent) {
		return (int) agent.getAttribute(PEDESTRIAN_ROAD_STATUS);
	}
	
	@setter (PEDESTRIAN_ROAD_STATUS)
	public void setPedestrianRoadStatus(final IAgent agent, int status) {
		agent.setAttribute(PEDESTRIAN_ROAD_STATUS, status);
	}
	
	@getter (FREE_SPACE)
	public IShape getFreeSpace(final IAgent agent) {
		return (IShape) agent.getAttribute(FREE_SPACE);
	}

	@setter (FREE_SPACE)
	public void setFreeSpace(final IAgent agent, final IShape val) {
		agent.setAttribute(FREE_SPACE, val);
	}
	
	@setter (DISTANCE)
	public void setDistance(final IAgent agent, final Double val) {
		agent.setAttribute(DISTANCE, val);
	}
	@getter (DISTANCE)
	public Double getDistance(final IAgent agent) {
		return (Double) agent.getAttribute(DISTANCE);
	}

	
	@action (
			name = "initialize",args = { @arg (
							name = "distance",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the maximal distance to the road")),
					@arg (
							name = "obstacles",
							type = IType.LIST,
							optional = true,
							doc = @doc ("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry")),
					@arg (
							name = "status",
							type = IType.INT,
							optional = true,
							doc = @doc ("the status (int) of the road: 1 (default) for roads where agent move on a continuous 2D space and 0 for 1D roads with queu-in queu-out like movement")),
				},
			doc = @doc (
					value = "action to initialize the free space of roads",
					examples = { @example ("do initialize distance: 10.0 obstacles: [building];") }))
	@SuppressWarnings("unchecked")
	public void primWalkEscape(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		int status = scope.hasArg("status") ? scope.getIntArg("status") : (agent.getGeometry().hasAttribute(PEDESTRIAN_ROAD_STATUS) ? GamaIntegerType
				.staticCast(scope, agent.getGeometry().getAttribute(PEDESTRIAN_ROAD_STATUS), null, false) : 1);
		setPedestrianRoadStatus(agent, status);
		
		IShape freeSpace = agent.getGeometry().copy(scope);
		
		if(status == COMPLEX_STATUS) {
			double dist = scope.hasArg("distance") ? scope.getFloatArg("distance") : 0.0;
			setDistance(agent, dist);
			
			if (dist > 0) {
				freeSpace = Spatial.Transformations.enlarged_by(scope, freeSpace, dist);
			}
			
			IList<ISpecies> speciesList = scope.hasArg("obstacles") ?  scope.getListArg("obstacles") : null;
			if (speciesList != null) {
				for (ISpecies species : speciesList) {
					IContainer<?, IShape> obstacles = (IContainer<?, IShape>) Spatial.Queries.overlapping(scope, species, freeSpace);
					IShape obstGeom = Spatial.Operators.union(scope, obstacles);
					obstGeom =  Spatial.Transformations.enlarged_by(scope,obstGeom,dist/1000.0);
					freeSpace = Spatial.Operators.minus(scope, freeSpace, obstGeom);
					
				}
			}
		}
		
		setFreeSpace(agent, freeSpace);
	}
	
	@action (
			name = "build_exit_hub",
			args = {
				@arg (
					name = PedestrianEscapeSkill.PEDESTRIAN_GRAPH,
					type = IType.GRAPH,
					optional = false,
					doc = @doc("The pedestrian network from which to find connected corridors")
				),
				@arg (
						name = "distance_between_targets",
						type = IType.FLOAT,
						optional = false,
						doc = @doc("min distances between 2 targets")
					)
			},
			doc = @doc (
					value = "Add exit hub to pedestrian corridor to reduce angular distance between node of the network",
					examples = {@example ("do build_exit_hub pedestrian_graph: pedestrian_network distance_between_targets: 10.0;")})
			)
	public void primExitHubEscape(final IScope scope) {
		
		// TODO : Exit hub should probably be symmetric ... 
		final IAgent agent = getCurrentAgent(scope);
		if(!agent.isInstanceOf(PEDESTRIAN_ROAD_SKILL, true)) 
			throw GamaRuntimeException.error("Trying to manipulate agent with "+PEDESTRIAN_ROAD_SKILL+" while being "+agent, scope);
		
		final Double dist = scope.getFloatArg("distance_between_targets");
		@SuppressWarnings("unchecked")
		IMap<GamaPoint,IList<GamaPoint>> exitHub = GamaMapFactory.create();
		IShape bounds = Spatial.Transformations.reduced_by(scope, getFreeSpace(agent), dist);
		if(getRoadStatus(scope, agent) == SIMPLE_STATUS) {
			for (ILocation p : agent.getPoints()) {
				GamaPoint pt = p.toGamaPoint();
				IList<GamaPoint> ptL = GamaListFactory.create();
				ptL.add(pt);
				exitHub.put(pt,ptL);
			}
		} else {
			for (int i = 0; i < agent.getPoints().size(); i++) {
				GamaPoint pt = agent.getPoints().get(i).toGamaPoint().copy(scope);
				GamaPoint pp = (i == 0) ? agent.getPoints().get(i +1).toGamaPoint(): agent.getPoints().get(i -1).toGamaPoint();
				exitHub.put(agent.getPoints().get(i).toGamaPoint(), connectedRoads(scope, agent, dist, pt, pp, bounds));
			}
		} 
		this.setExitNodesHub(agent, exitHub);
		
	}
	
	/**
	 * To quickly access free space within the plugin
	 * 
	 * @param scope
	 * @param road
	 * @return
	 */
	public static IShape getFreeSpace(IScope scope, IShape road) {
		return (IShape) road.getAttribute(FREE_SPACE);
	}
	
	/**
	 * To quickly access to road status within the plugin
	 * 
	 * @param scope
	 * @param road
	 * @return
	 */
	public static int getRoadStatus(IScope scope, IShape road) {
		return (int) road.getAttribute(PEDESTRIAN_ROAD_STATUS);
	}
	
	/**
	 * To quickly access to exit nodes from the hub.
	 * If no exit hub, will only return the exit point of the road
	 * 
	 * @param currentRoad
	 * @param target
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static IList<GamaPoint> getConnectedOutput(IScope scope, IShape currentRoad, GamaPoint target) {
		if(currentRoad.hasAttribute(EXIT_NODES_HUB)) {
			IMap<GamaPoint,IList<GamaPoint>> exitHub = (IMap<GamaPoint,IList<GamaPoint>>) currentRoad.getAttribute(EXIT_NODES_HUB);
			if(exitHub.containsKey(target)) {
				return exitHub.get(target);
			} else {
				return GamaListFactory.create(Types.POINT, Stream.of(target));
			}
		} else {
			throw GamaRuntimeException.error("Looking for exit hub related to "+currentRoad+" but there is none", scope);
		}
	}
	
	/**
	 * To register any agent (not necessary pedestrian agent) to be on the pedestrian road segment
	 * 
	 * @param scope
	 * @param road
	 * @param pedestrian
	 */
	@SuppressWarnings("unchecked")
	public static void register(IScope scope, IAgent road, IAgent pedestrian ) {
		((IList<IAgent> ) road.getAttribute(AGENTS_ON)).add(pedestrian);
	}
	
	/**
	 * To unregister an agent from the set of agent on the pedestrian road segment
	 * 
	 * @param scope
	 * @param road
	 * @param pedestrian
	 */
	@SuppressWarnings("unchecked")
	public static void unregister(IScope scope, IAgent road, IAgent pedestrian ) {
		((IList<IAgent> ) road.getAttribute(AGENTS_ON)).remove(pedestrian);
	}
	
	/*
	 * Create exit hub for a set of connected out edges
	 */
	@SuppressWarnings("unchecked")
	private IList<GamaPoint> connectedRoads(IScope scope, IAgent currentRoad, Double dist, GamaPoint lp, GamaPoint pp, IShape bounds){
		IList<GamaPoint> exitConnections = GamaListFactory.create();
		exitConnections.add(lp.copy(scope));
		double distR = getDistance(currentRoad);
		if (distR <= 0 || bounds == null || bounds.getArea() <= 0.001) 
			return exitConnections;
		
		GamaPoint v = lp.minus(pp);
		GamaPoint n = null;
		if (v.x == 0) {
			n = new GamaPoint(1,0);
		} else if (v.y == 0) {
			n = new GamaPoint(0,1);
		} else {
			double nx = -v.y/v.x;
			double norm = Math.sqrt(nx*nx + 1);
			n = new GamaPoint(nx/norm,1/norm);	
		}
		n = n.multiplyBy(distR);
		IList<IShape> points = GamaListFactory.create();
		points.add(lp.minus(n)); points.add(lp.add(n));
		
		IShape hole = Spatial.Creation.line(scope, points);
		if (hole == null || hole.getPerimeter() <= dist) 
			return exitConnections;
		
		
		IList<GamaPoint> pts = Spatial.Punctal.points_on(hole, dist);
		pts.removeIf(p -> p == null || ! bounds.intersects((GamaPoint)p));
		exitConnections.addAll(pts);
		
		return exitConnections;
	}

}
