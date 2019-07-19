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

import java.util.List;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;

import msi.gama.common.geometry.GeometryUtils;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
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
import msi.gama.precompiler.ITypeProvider;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.operators.Spatial; 
import msi.gaml.skills.Skill;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars ({ @variable (
		name = "agents_on",
		type = IType.LIST,
		of = IType.AGENT,
		init = "[]",
		doc = @doc ("for each people on the road")),
	@variable (
			name = "free_space",
			type = IType.GEOMETRY,
			init = "nil",
			doc = @doc ("for each people on the road"))})
@skill (
		name = "pedestrian_road",
		concept = { IConcept.TRANSPORT, IConcept.SKILL },
		doc = @doc ("A skill for agents representing pedestrian roads"))
public class PedestrianRoadSkill extends Skill {
	public final static String AGENTS_ON = "agents_on";
	public final static String FREE_SPACE = "free_space";
	

	@getter (AGENTS_ON)
	public static IList<IAgent> getAgentsOn(final IAgent agent) {
		return (IList<IAgent> ) agent.getAttribute(AGENTS_ON);
	}

	
	@getter (FREE_SPACE)
	public IShape getFreeSpace(final IAgent agent) {
		return (IShape) agent.getAttribute(FREE_SPACE);
	}

	@setter (FREE_SPACE)
	public void setFreeSpace(final IAgent agent, final IShape val) {
		agent.setAttribute(FREE_SPACE, val);
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
				},
			doc = @doc (
					value = "action to initialize the free space of roads",
					examples = { @example ("do initialize distance: 10.0 obstacles: [building];") }))
	public void primWalkEscape(final IScope scope) throws GamaRuntimeException {
		IAgent agent = getCurrentAgent(scope);
		double dist = scope.hasArg("distance") ? scope.getFloatArg("distance") : 0.0;
		IShape freeSpace = agent.getGeometry().copy(scope);
		if (dist > 0) {
			freeSpace = Spatial.Transformations.enlarged_by(scope, freeSpace, dist);
		}
		
		IList<ISpecies> speciesList = scope.hasArg("obstacles") ?  scope.getListArg("obstacles") : null;
		if (speciesList != null) {
			for (ISpecies species : speciesList) {
				IContainer obstacles = (IContainer) Spatial.Queries.overlapping(scope, species, freeSpace);
				IShape obstGeom = Spatial.Operators.union(scope, obstacles);
				obstGeom =  Spatial.Transformations.enlarged_by(scope,obstGeom,dist/1000.0);
				freeSpace = Spatial.Operators.minus(scope, freeSpace, obstGeom);
				
			}
		}
		
		
		setFreeSpace(agent, freeSpace);
	}
	
	public static IShape getFreeSpace(IScope scope, IShape road) {
		return (IShape) road.getAttribute(FREE_SPACE);
	}
		
	public static void register(IScope scope, IAgent road, IAgent pedestrian ) {
		((IList<IAgent> ) road.getAttribute(AGENTS_ON)).add(pedestrian);
	}
	
	public static void unregister(IScope scope, IAgent road, IAgent pedestrian ) {
		((IList<IAgent> ) road.getAttribute(AGENTS_ON)).remove(pedestrian);
	}


}
