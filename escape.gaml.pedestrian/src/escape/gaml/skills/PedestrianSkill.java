package escape.gaml.skills;

import java.util.Collection;

import msi.gama.common.interfaces.IKeyword;
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
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.operators.Maths;
import msi.gaml.operators.Random;
import msi.gaml.operators.Spatial;
import msi.gaml.skills.MovingSkill;
import msi.gaml.species.ISpecies;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@skill(name = "pedestrian",
	concept = { IConcept.TRANSPORT, IConcept.SKILL },
	doc = @doc ("A skill that provides agent with the ability to walk on continuous space while"
			+ " finding their way on a virtual network"))
@vars({
	@variable(
			name = "shoulder_length", 
			type = IType.FLOAT, init = "0.5",
			doc = @doc ("The length of the pedestrian (in meters)")),
	
	@variable (
			name = "obstacle_distance_repulsion_coeff",
			type = IType.FLOAT,
			init = "3.0",
			doc = @doc ("the coefficient for the maximal distance to take into account the repulsion of the obstacles")),
	@variable (
			name = "obstacle_repulsion_intensity",
			type = IType.FLOAT,
			init = "1.0",
			doc = @doc ("Intensity of reaction to obstacles")),
	@variable (
			name = "overlapping_coefficient",
			type = IType.FLOAT,
			init = "1.0",
			doc = @doc ("Coefficient for the tendency to avoid averlapping")),
	@variable (
			name = "perception_sensibility",
			type = IType.FLOAT,
			init = "1.0",
			doc = @doc ("Sensibility of perception (between 0 and 1)")),
	@variable (
			name = "avoid_other",
			type = IType.BOOL,
			init = "true",
			doc = @doc ("has the pedestrian to avoid other pedestrians?")),
	@variable (
			name = "obstacle_species",
			type = IType.LIST,
			init = "[]",
			doc = @doc ("the list of species that are considered as obstacles")),

	@variable (
			name = "proba_detour",
			type = IType.FLOAT,
			init = "0.1",
			doc = @doc ("probability to accept to do a detour")),

	})
public class PedestrianSkill extends MovingSkill {
	
	// ---------- CONSTANTS -------------- //
	
	// VAR
	
	public static Long t1 = 0l;
	public static Long t2 = 0l;
	public static Long t3 = 0l;
	public static Long t4 = 0l;
	public static Long t5 = 0l;
	public static Long t6 = 0l;
	public static Long t7 = 0l;
	public static Long t8 = 0l;
	public static Long t9 = 0l;
	public static Long t10 = 0l;
	
	public static int cpt = 0;
	
	public final static String SHOULDER_LENGTH = "shoulder_length";
	public final static String CURRENT_TARGET = "current_target";
	public final static String OBSTACLE_DISTANCE_REPULSION_COEFF = "obstacle_distance_repulsion_coeff";
	public final static String OBSTACLE_REPULSION_INTENSITY = "obstacle_repulsion_intensity";
	public final static String OVERLAPPING_COEFFICIENT = "overlapping_coefficient";
	public final static String PERCEPTION_SENSIBILITY = "perception_sensibility";
	public final static String PROBA_DETOUR = "proba_detour";
	public final static String AVOID_OTHER = "avoid_other";
	public final static String OBSTACLE_SPECIES = "obstacle_species";

	
	
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
	@getter (OBSTACLE_SPECIES)
	public GamaList<ISpecies> getObstacleSpecies(final IAgent agent) {
		return (GamaList<ISpecies>) agent.getAttribute(OBSTACLE_SPECIES);
	}

	@setter (OBSTACLE_SPECIES)
	public void setObstacleSpecies(final IAgent agent, final GamaList<ISpecies> os) {
		agent.setAttribute(OBSTACLE_SPECIES, os);
	}

	@getter (CURRENT_TARGET)
	public GamaPoint getCurrentTarget(final IAgent agent) {
		return (GamaPoint) agent.getAttribute(CURRENT_TARGET);
	}

	@setter (CURRENT_TARGET)
	public void setCurrentTarget(final IAgent agent, final ILocation point) {
		agent.setAttribute(CURRENT_TARGET, point);
	}

		
	@getter (OBSTACLE_DISTANCE_REPULSION_COEFF)
	public Double getObstacleDistRepulsionCoeff(final IAgent agent) {
		return (Double) agent.getAttribute(OBSTACLE_DISTANCE_REPULSION_COEFF);
	}

	@setter (OBSTACLE_DISTANCE_REPULSION_COEFF)
	public void setObstacleDistRepulsionCoeff(final IAgent agent, final Double val) {
		agent.setAttribute(OBSTACLE_DISTANCE_REPULSION_COEFF, val);
	}
	
	@getter (OBSTACLE_REPULSION_INTENSITY)
	public Double getObstacleRepulsionIntensity(final IAgent agent) {
		return (Double) agent.getAttribute(OBSTACLE_REPULSION_INTENSITY);
	}

	@setter (OBSTACLE_REPULSION_INTENSITY)
	public void setObstacleRepulsionIntensity(final IAgent agent, final Double val) {
		agent.setAttribute(OBSTACLE_REPULSION_INTENSITY, val);
	}
	
	@getter (OVERLAPPING_COEFFICIENT)
	public Double getOverlappingCoeff(final IAgent agent) {
		return (Double) agent.getAttribute(OVERLAPPING_COEFFICIENT);
	}

	@setter (OVERLAPPING_COEFFICIENT)
	public void setOverlappingCoeff(final IAgent agent, final Double val) {
		agent.setAttribute(OVERLAPPING_COEFFICIENT, val);
	}
	
	@getter (PERCEPTION_SENSIBILITY)
	public Double getPerceptionSensibility(final IAgent agent) {
		return (Double) agent.getAttribute(PERCEPTION_SENSIBILITY);
	}

	@setter (PERCEPTION_SENSIBILITY)
	public void setPerceptionSensibility(final IAgent agent, final Double val) {
		agent.setAttribute(PERCEPTION_SENSIBILITY, val);
	}
	@getter (PROBA_DETOUR)
	public Double getProbaDetour(final IAgent agent) {
		return (Double) agent.getAttribute(PROBA_DETOUR);
	}

	@setter (PROBA_DETOUR)
	public void setProbaDetour(final IAgent agent, final Double val) {
		agent.setAttribute(PROBA_DETOUR, val);
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
			name = WALK,
			args = { @arg (
							name = "target",
							type = IType.GEOMETRY,
							optional = false,
							doc = @doc ("the location or entity towards which to move.")),
							@arg (
									name = IKeyword.SPEED,
									type = IType.FLOAT,
									optional = true,
									doc = @doc ("the speed to use for this move (replaces the current value of speed)")),
							@arg (
									name = IKeyword.BOUNDS,
									type = IType.GEOMETRY,
									optional = true,
									doc = @doc ("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry")),
							
			},
					
			doc = @doc (
					value = "action to walk toward the final target",
					examples = { @example ("do walk;") }))
	public void primWalk(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (agent == null || agent.dead()) { return; }
		IShape goal = computeTarget(scope, agent);
		if (goal == null) { return; }
		IShape bounds = null;
		if (scope.hasArg(IKeyword.BOUNDS)) {
			final Object obj = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			bounds = GamaGeometryType.staticCast(scope, obj, null, false);
		}
		IList<ISpecies> speciesList = getObstacleSpecies(agent);
		IContainer obstacles = null;
		if (speciesList.size() == 1) 
			obstacles = speciesList.get(0);
		else {
			obstacles = GamaListFactory.create(Types.AGENT);
			for (ISpecies species : speciesList) {
				((IList<IAgent>) obstacles).addAll( (Collection<? extends IAgent>) species.getAgents(scope));
			}
		}
		
		GamaPoint currentTarget = goal.getLocation().toGamaPoint() ;
		setRealSpeed(agent, walkWithForceModel(scope,agent,currentTarget,bounds,obstacles));
		
	}
	
	public double walkWithForceModel(IScope scope, IAgent agent, GamaPoint currentTarget, IShape bounds, IContainer obstaclesList) {
		//long t = System.currentTimeMillis();
		GamaPoint location = (GamaPoint) getLocation(agent).copy(scope);
		double dist = location.distance(currentTarget);
		
		if (dist == 0.0) return 0.0;
		double maxDist = computeDistance(scope, agent);
		GamaPoint velocity = currentTarget.copy(scope).minus(location);
		//t1 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		if (getAvoidOther(agent)) {
			velocity = avoid(scope, agent, location,velocity, currentTarget, maxDist,obstaclesList);
		}
		//t2 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		GamaPoint target = velocity.copy(scope).add(location);
		double distToTarget = location.euclidianDistanceTo(target);
		if (distToTarget > 0.0) {
			double coeff = Math.min(maxDist/distToTarget, 1.0);
			if (coeff == 1) 
				location = target;
			else
				location = location.add(velocity.multiplyBy(coeff));
			
		}
		//t3 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		if (bounds != null && !Spatial.Properties.overlaps(scope, location,bounds )) {
			location = Spatial.Punctal.closest_points_with(location,bounds).get(1);
		}
		//t4 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		double realSpeed = 0.0;
		double proba_detour = getProbaDetour(agent);
		if (!(Random.opFlip(scope, (1.0 - proba_detour)) && 
			((location.euclidianDistanceTo(currentTarget)) > (agent.getLocation().euclidianDistanceTo(currentTarget))))) {
			realSpeed = agent.euclidianDistanceTo(location)/ scope.getSimulation().getTimeStep(scope);
			setLocation(agent,location);
		}
		/*t5 += System.currentTimeMillis() - t;
		
		cpt++;
		if (cpt % 1000 == 0) {
			System.out.println("t1: " + t1 + " t2: " + t2+ " t3: " + t3+ " t4: " + t4+ " t5: " + t5+ " t6: " + t6+ " t7: " + t7+ " t8: " + t8+ " t9: " + t9+ " t10: " + t10);
		}*/
		return realSpeed;
	}
	public GamaPoint avoid(IScope scope, IAgent agent, GamaPoint location, GamaPoint velocity, GamaPoint currentTarget,  double maxDist, IContainer obstaclesList) {
		//long t = System.currentTimeMillis();
		GamaPoint acc = new GamaPoint(0,0);
		double Ra = getShoulderLength(agent);
		maxDist = (maxDist + Ra) * getObstacleRepulsionIntensity(agent);
		IList<ISpecies> speciesList = getObstacleSpecies(agent);
		IList<IAgent> obstacles = GamaListFactory.create(Types.AGENT);
		//t6 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		//for (ISpecies species : speciesList) {
			obstacles.addAll( (IList<IAgent>) Spatial.Queries.at_distance(scope, obstaclesList, maxDist));
		//}
		obstacles.remove(agent);
		//t7 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
			//obstacles.removeIf(a -> a.euclidianDistanceTo(currentTarget) > location.euclidianDistanceTo(currentTarget));
		double lambda = getPerceptionSensibility(agent);
		double A = getObstacleDistRepulsionCoeff(agent);
		double k = getOverlappingCoeff(agent);
		double distTarget = location.distance(currentTarget);
		//t8 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		for (IAgent a : obstacles){
			double distance = location.euclidianDistanceTo(a);
			if (distance == 0) continue;
			double w = lambda == 1.0 ? 0.0 : (lambda + (1 - lambda) * (1 - Maths.cos(Spatial.Punctal.angleInDegreesBetween(scope, location, a.getCentroid(), currentTarget)))/ 2.0);
			GamaPoint locA = a.getGeometry().isPoint()? a.getLocation().copy(scope).toGamaPoint() : (Spatial.Punctal.closest_points_with(location,a.getGeometry()).get(1));
			GamaPoint vector = locA.minus(location.toGamaPoint());
			double shoulder = 2 * Ra;
			boolean contact = (distance < shoulder);
			double g = contact ? (shoulder - distance): 0.0;
			GamaPoint delta = null;
			if (g > 0) {
				boolean right =Random.opFlip(scope, 0.5);
				delta = (right ? new GamaPoint(-vector.y,vector.x) :new GamaPoint(vector.y,-vector.x) ).multiplyBy(k  / distance );
			}
			vector = vector.multiplyBy(w/distance * A * Maths.exp((shoulder-distance + k*g) / maxDist ));
			acc = acc.add(vector);
			if (g > 0) acc = acc.add(delta);
		}  
		//t9+= System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		if (Double.isNaN(acc.x)) return new GamaPoint(0,0);
		acc.multiplyBy(distTarget);
		//t10 += System.currentTimeMillis() - t;
		//t = System.currentTimeMillis();
		
		return velocity.add(acc);
	}
		
		
	
		
}
