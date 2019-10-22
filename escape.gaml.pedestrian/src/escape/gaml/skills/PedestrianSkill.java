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
import msi.gaml.operators.Points;
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
	@variable(
			name = "body_depth", 
			type = IType.FLOAT, init = "0.280",
			doc = @doc ("The body depth of the pedestrian (in meters) - classic values: [0.235,0.325]")),
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
	@variable (
			name = "pedestrian_model",
			type = IType.STRING,
			init = "'simple'",
			doc = @doc ("Model use for the movement of agents. Can be either \"SFM\" for the Social Force Model of Helbing "
					+ "or \"simple\" (default) for a custom model, which is a simpler version of SFM Helbing model")),
	@variable (
			name = "A_SFM",
			type = IType.FLOAT,
			init = "4.5",
			doc = @doc ("Value of A in the SFM model (classic values : mean = 4.5, std = 0.3)")),
	@variable (
			name = "relaxion_SFM",
			type = IType.FLOAT,
			init = "0.54",
			doc = @doc ("Value of relaxion in the SFM model (classic values : mean = 0.54, std = 0.05)")),
	@variable (
			name = "gama_SFM",
			type = IType.FLOAT,
			init = "0.35",
			doc = @doc ("Value of gama in the SFM model (classic values : mean = 0.35, std = 0.01)")),
	@variable (
			name = "n_SFM",
			type = IType.FLOAT,
			init = "2.0",
			doc = @doc ("Value of n in the SFM model (classic values : mean = 2.0, std = 0.1)")),
	@variable (
			name = "n_prime_SFM",
			type = IType.FLOAT,
			init = "3.0",
			doc = @doc ("Value of n\' in the SFM model (classic values : mean = 3.0, std = 0.7)")),
	@variable (
			name = "lambda_SFM",
			type = IType.FLOAT,
			init = "2.0",
			doc = @doc ("Value of lambda in the SFM model (classic values : mean = 2.0, std = 0.2)")),
	@variable(
			name = "velocity", 
			type = IType.POINT, init = "{0,0,0}",
			doc = @doc ("The velocity of the pedestrian (in meters)")),
	})
public class PedestrianSkill extends MovingSkill {
	
	// ---------- CONSTANTS -------------- //
	
	public static boolean BENCHMARK = false;
	public static Long t;
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
	
	// VAR
	
	// General mode of walking
	public final static String PEDESTRIAN_MODEL = "pedestrian_model";
	
	public final static String SHOULDER_LENGTH = "shoulder_length";
	public final static String CURRENT_TARGET = "current_target";
	public final static String OBSTACLE_DISTANCE_REPULSION_COEFF = "obstacle_distance_repulsion_coeff";
	public final static String OBSTACLE_CONSIDERATION_DISTANCE = "obstacle_consideration_distance";
	public final static String OVERLAPPING_COEFFICIENT = "overlapping_coefficient";
	public final static String PERCEPTION_SENSIBILITY = "perception_sensibility";
	public final static String PROBA_DETOUR = "proba_detour";
	public final static String AVOID_OTHER = "avoid_other";
	public final static String OBSTACLE_SPECIES = "obstacle_species";
	public final static String VELOCITY = "velocity";

	public final static String A_SFM = "A_SFM";
	public final static String RELAXION_SFM = "relaxion_SFM";
	public final static String GAMA_SFM = "gama_SFM";
	public final static String N_SFM = "n_SFM";
	public final static String N_PRIME_SFM = "n_prime_SFM";
	public final static String lAMBDA_SFM = "lambda_SFM";
	public final static String BODY_DEPTH = "body_depth";
	
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
	
	@setter(BODY_DEPTH)
	public void setBodyDepth(final IAgent agent, final double s) {
	    agent.setAttribute(BODY_DEPTH, s);
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
	
	@getter (OBSTACLE_CONSIDERATION_DISTANCE)
	public Double getObstacleConsiderationDistance(final IAgent agent) {
		return (Double) agent.getAttribute(OBSTACLE_CONSIDERATION_DISTANCE);
	}

	@setter (OBSTACLE_CONSIDERATION_DISTANCE)
	public void setObstacleConsiderationDistance(final IAgent agent, final Double val) {
		agent.setAttribute(OBSTACLE_CONSIDERATION_DISTANCE, val);
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
	
	@setter (lAMBDA_SFM)
	public void setlAMBDA_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(lAMBDA_SFM, val);
	}
	
	@getter (lAMBDA_SFM)
	public Double getlAMBDA_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(lAMBDA_SFM);
	}

	@setter (N_PRIME_SFM)
	public void setN_PRIME_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(N_PRIME_SFM, val);
	}
	@getter (N_PRIME_SFM)
	public Double getN_PRIME_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(N_PRIME_SFM);
	}

	@setter (N_SFM)
	public void setN_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(N_SFM, val);
	}
	@getter (N_SFM)
	public Double getN_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(N_SFM);
	}

	@setter (GAMA_SFM)
	public void setGAMA_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(GAMA_SFM, val);
	}
	@getter (GAMA_SFM)
	public Double getGAMA_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(GAMA_SFM);
	}

	@getter (RELAXION_SFM)
	public Double getRELAXION_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(RELAXION_SFM);
	}

	@setter (RELAXION_SFM)
	public void setRELAXION_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(RELAXION_SFM, val);
	}
	@getter (A_SFM)
	public Double getA_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(A_SFM);
	}

	@setter (A_SFM)
	public void setA_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(A_SFM, val);
	}
	
	@getter (PEDESTRIAN_MODEL)
	public String getPedestrianModel(final IAgent agent) {
		return (String) agent.getAttribute(PEDESTRIAN_MODEL);
	}

	@setter (PEDESTRIAN_MODEL)
	public void setPedestrianModel(final IAgent agent, final String val) {
		if (val.equals("SFM") || val.equals("simple")) 
			agent.setAttribute(PEDESTRIAN_MODEL, val);
		else GamaRuntimeException.error("" + val + " is not a possible value for pedestrian model; possible values: ['simple', 'SFM']", agent.getScope());
	}
	
	@getter (VELOCITY)
	public GamaPoint getVelocity(final IAgent agent) {
		return (GamaPoint) agent.getAttribute(VELOCITY);
	}

	@setter (VELOCITY)
	public void setVelocity(final IAgent agent, final GamaPoint val) {
		agent.setAttribute(VELOCITY, val);
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
		double maxDist = computeDistance(scope, agent);
		double realSpeed = walkWithForceModel(scope, agent, currentTarget, getAvoidOther(agent), bounds, obstacles, maxDist); 
		
		setRealSpeed(agent, realSpeed);
		
	}
	
	/**
	 * General walking dynamic with force based avoidance (either Simple or SFM - 09/2019)
	 * 
	 * @param scope
	 * @param agent
	 * @param currentTarget
	 * @param avoidOther
	 * @param bounds
	 * @param obstaclesList
	 * @param maxDist
	 * @return
	 */
	public double walkWithForceModel(IScope scope, IAgent agent, GamaPoint currentTarget, boolean avoidOther,
			IShape bounds, IContainer<Integer, ?> obstaclesList, double maxDist) {
		if (BENCHMARK) {t = System.currentTimeMillis();}
		
		GamaPoint location = (GamaPoint) getLocation(agent).copy(scope);
		double dist = location.distance(currentTarget);
		String modelType = getPedestrianModel(agent);
		if (dist == 0.0) return 0.0;
		GamaPoint velocity = currentTarget.copy(scope).minus(location);
		
		if (BENCHMARK) {t1 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		if (avoidOther) {
			double distPercep = Math.max(maxDist, getObstacleConsiderationDistance(agent));
			switch(modelType) {
				case "SFM":
					velocity = avoidSFM(scope, agent, location, currentTarget, distPercep,obstaclesList);
					break;
				case "simple":
				default:
					velocity = avoidSimple(scope, agent, location, currentTarget, distPercep,obstaclesList);
					break;
			}
		}
		
		if(BENCHMARK) { t2 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		GamaPoint target = velocity.copy(scope).add(location);
		double distToTarget = location.euclidianDistanceTo(target);
		if (distToTarget > 0.0) {
			double coeff = Math.min(maxDist/distToTarget, 1.0);
			if (coeff == 1.0) 
				location = target;
			else {
				velocity = velocity.multiplyBy(coeff);
				location = location.add(velocity);
			}
		}
		if (BENCHMARK) {t3 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		if (bounds != null && !Spatial.Properties.overlaps(scope, location,bounds )) {
			location = Spatial.Punctal.closest_points_with(location,bounds).get(1);
		}

		if (BENCHMARK) {t4 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		double realSpeed = 0.0;
		double proba_detour = getProbaDetour(agent);
		if (!(Random.opFlip(scope, (1.0 - proba_detour)) && 
			((location.euclidianDistanceTo(currentTarget)) > (agent.getLocation().euclidianDistanceTo(currentTarget))))) {
			realSpeed = agent.euclidianDistanceTo(location)/ scope.getSimulation().getTimeStep(scope);
			setVelocity(agent, location.copy(scope).minus(getLocation(agent).toGamaPoint()).toGamaPoint());
			setLocation(agent,location);
		} else {
			setVelocity(agent, new GamaPoint(0,0,0));
		}

		if(BENCHMARK) { 
			t5 += System.currentTimeMillis() - t;
			cpt++;
			if (cpt % 1000 == 0) {
				System.out.println("t1: " + t1 + " t2: " + t2+ " t3: " + t3+ " t4: " 
						+ t4+ " t5: " + t5+ " t6: " + t6+ " t7: " + t7+ " t8: " 
						+ t8+ " t9: " + t9+ " t10: " + t10);
			}
		}
		
		return realSpeed;
	}
	
	/**
	 * Simple version of force/repulsion inspired by SFM
	 * 
	 * @param scope
	 * @param agent
	 * @param location
	 * @param currentTarget
	 * @param distPercep
	 * @param obstaclesList
	 * @return
	 */
	public GamaPoint avoidSimple(IScope scope, IAgent agent, GamaPoint location, GamaPoint currentTarget,  double distPercep, IContainer obstaclesList) {
		if (BENCHMARK) { t = System.currentTimeMillis();}
		
		GamaPoint target_dir = currentTarget.copy(scope).minus(location);
		GamaPoint acc = new GamaPoint(0,0);
		double Ra = getShoulderLength(agent);
		IList<ISpecies> speciesList = getObstacleSpecies(agent);
		IList<IAgent> obstacles = GamaListFactory.create(Types.AGENT);
		
		if (BENCHMARK) {t6 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		obstacles.addAll( (IList<IAgent>) Spatial.Queries.at_distance(scope, obstaclesList, distPercep));
		obstacles.remove(agent);
		
		if (BENCHMARK) {t7 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		//obstacles.removeIf(a -> a.euclidianDistanceTo(currentTarget) > location.euclidianDistanceTo(currentTarget));
		double lambda = getPerceptionSensibility(agent);
		double A = getObstacleDistRepulsionCoeff(agent);
		double k = getOverlappingCoeff(agent);
		double distTarget = location.distance(currentTarget);
		
		if (BENCHMARK) {t8 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
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
			vector = vector.multiplyBy(w/distance * A * Maths.exp((shoulder-distance + k*g) / distPercep ));
			acc = acc.add(vector);
			if (g > 0) acc = acc.add(delta);
		}  
		
		if (BENCHMARK) {t9+= System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		if (Double.isNaN(acc.x)) return new GamaPoint(0,0);
		acc.multiplyBy(distTarget);
		
		if (BENCHMARK) {t10 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		return target_dir.add(acc);
	}
		
	/**
	 * Classical implementation of the Social Force Model (Helbing and Molnar, 1998)
	 * 
	 * @param scope
	 * @param agent
	 * @param location
	 * @param currentTarget
	 * @param distPercep
	 * @param obstaclesList
	 * @return
	 */
	public GamaPoint avoidSFM(IScope scope, IAgent agent, GamaPoint location, GamaPoint currentTarget,  double distPercep, IContainer obstaclesList) {
		GamaPoint current_velocity = getVelocity(agent).copy(scope);
		GamaPoint fsoc = new GamaPoint(0,0,0);
		double dist = location.euclidianDistanceTo(currentTarget);
		double step = scope.getSimulation().getClock().getStepInSeconds();
		double speed = getSpeed(agent);
		IList<ISpecies> speciesList = getObstacleSpecies(agent);
		IList<IAgent> obstacles = GamaListFactory.create(Types.AGENT);
		
		if (BENCHMARK) {t6 += System.currentTimeMillis() - t; t = System.currentTimeMillis();}
		
		obstacles.addAll( (IList<IAgent>) Spatial.Queries.at_distance(scope, obstaclesList, distPercep));
	
		obstacles.remove(agent);
		double lambda = getlAMBDA_SFM(agent);
		double gama_ = getGAMA_SFM(agent);
		double A = getA_SFM(agent);
		double n = getN_SFM(agent);
		double n_prime = getN_PRIME_SFM(agent);
		for (IAgent ag : obstacles) {
			GamaPoint force = new GamaPoint(0,0,0);
			
			double distance = agent.euclidianDistanceTo(ag);
			GamaPoint itoj = Points.subtract(ag.getLocation().toGamaPoint(), agent.getLocation().toGamaPoint()).toGamaPoint();
			itoj = itoj.divideBy(Maths.sqrt(scope, (itoj.x * itoj.x + itoj.y * itoj.y + itoj.z * itoj.z)));
			
			GamaPoint D = current_velocity.copy(scope).subtract(getVelocity(ag)).multiplyBy(lambda).add(itoj);
			double D_norm = Maths.sqrt(scope, D.x * D.x + D.y * D.y + D.z * D.z);
			double B = gama_ * D_norm; 
			GamaPoint t_ = D.divideBy(D_norm);
			GamaPoint n_;
			if (t_.x == 0) {
				n_ = new GamaPoint(t_.y > 0 ? -1 : 1,0,0);
			} else if (t_.y == 0) {
				n_ = new GamaPoint(0,t_.x > 0 ? 1 : -1,0);
			} else {
				double nx = -t_.y/t_.x;
				double norm = Math.sqrt(nx*nx + 1);
				n_ = t_.x > 0 ?new GamaPoint(-nx/norm,-1/norm,0) : new GamaPoint(nx/norm,1/norm,0)  ;	
			}
			double t_xDotitoj = t_.x * itoj.x + t_.y * itoj.y + t_.z * itoj.z;
			t_xDotitoj = Math.max(Math.min(t_xDotitoj, 1.0), -1.0);
			double teta = Math.abs((Maths.acos(t_xDotitoj)) * Math.PI / 180);
			if (teta <= Math.PI) {
				GamaPoint f_1 = t_.multiplyBy(Math.exp(-(Math.pow(n_prime * B * teta,2))));
				GamaPoint f_2 = n_.multiplyBy(Math.exp(-(Math.pow(n * B * teta,2))));
				force =  f_1.add(f_2).multiplyBy((- A) * Math.exp(-distance/B)) ;
				fsoc = fsoc.add(force);
			}
		}
		GamaPoint desiredVelo = (currentTarget.copy(scope).minus(location).divideBy(dist * Math.min(getSpeed(agent), dist/scope.getSimulation().getClock().getStepInSeconds()))) ;
		GamaPoint fdest = desiredVelo.minus(current_velocity).multiplyBy(getRELAXION_SFM(agent));
	
		GamaPoint forces = fdest.add(fsoc);
		GamaPoint pref_velocity = current_velocity.add(forces.multiplyBy(step));
		double norm_vel = Maths.sqrt(scope, pref_velocity.x * pref_velocity.x +pref_velocity.y * pref_velocity.y+pref_velocity.z * pref_velocity.z);
		if (norm_vel > speed) {
			current_velocity = pref_velocity.divideBy(norm_vel * speed); 
		} else {
			current_velocity = pref_velocity;
		}
		return current_velocity;//.multiplyBy(step);
	}
	
		
}
