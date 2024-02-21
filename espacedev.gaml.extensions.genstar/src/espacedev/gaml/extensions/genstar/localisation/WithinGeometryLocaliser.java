package espacedev.gaml.extensions.genstar.localisation;

import java.util.Map;

import espacedev.gaml.extensions.genstar.statement.LocaliseStatement;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.matrix.GamaField;
import gama.gaml.operators.Cast;
import gama.gaml.types.Types;
import spll.localizer.SPLocalizer;
import spll.localizer.constraint.ASpatialConstraint;
import spll.localizer.constraint.SpatialConstraintLocalization;
import spll.localizer.constraint.SpatialConstraintMaxDensity;
import spll.localizer.constraint.SpatialConstraintMaxDistance;
import spll.localizer.constraint.SpatialConstraintMaxNumber;
import spll.localizer.distribution.SpatialDistributionFactory;

public class WithinGeometryLocaliser implements IGenstarLocaliser {

	/** The Constant INSTANCE. */
	// SINGLETONG
	private static final WithinGeometryLocaliser INSTANCE = new WithinGeometryLocaliser();

	/**
	 * Gets the single instance of WithinGeometryLocaliser.
	 *
	 * @return single instance of WithinGeometryLocaliser
	 */
	public static WithinGeometryLocaliser getInstance() { return INSTANCE; }
	
	@Override
	public void localise(IScope scope, final IContainer<?, IAgent> pop, Object nests, LocaliseStatement locStatement) {
		String nestAtt = locStatement.getNestAttribute() != null ? Cast.asString(scope, locStatement.getNestAttribute().value(scope)) : null;
		IList<IShape> nestList = Cast.asList(scope, nests).listValue(scope, Types.GEOMETRY, false);
		Double maxValLocCst = locStatement.getMaxDistLocCst() != null? Cast.asFloat(scope, locStatement.getMaxDistLocCst().value(scope)) : null; 
		Double stepValLocCst = locStatement.getStepDistLocCst() != null? Cast.asFloat(scope, locStatement.getStepDistLocCst().value(scope)) : null; 
		
		SPLocalizer loc = new SPLocalizer(scope, nestList, nestAtt,maxValLocCst,stepValLocCst);
		Map matcherMap =  (locStatement.getMatcher() != null) ? Cast.asMap(scope, locStatement.getMatcher().value(scope), false) : null;
		if (matcherMap != null) 
			loc.setMatcher(Cast.asList(scope, matcherMap.get("entities")),Cast.asString(scope, matcherMap.get("pop_id")),  Cast.asString(scope, matcherMap.get("data_id")));
	
		Map mapperMap =  (locStatement.getMapper() != null) ? Cast.asMap(scope, locStatement.getMapper().value(scope), false) : null;
		if (mapperMap != null) {
			IList<IShape> entities = mapperMap.containsKey("entities") ? Cast.asList(scope, mapperMap.get("entities")) : null;
			String dataId = mapperMap.containsKey("data_id") ? Cast.asString(scope, mapperMap.get("data_id")) : null; 
					
			loc.setMapper(entities,dataId,
					(GamaField) mapperMap.get("field"));
			IList<GamaField> fields = (IList<GamaField>) mapperMap.get("fields");
			String regAlgo = mapperMap.containsKey("regression_algo") ? Cast.asString(scope, mapperMap.get("regression_algo")) : ""; 
			String normalizerType = mapperMap.containsKey("normalizer_type") ? Cast.asString(scope, mapperMap.get("normalizer_type")) : ""; 
			Double floorValue = mapperMap.containsKey("floor_value") ? Cast.asFloat(scope, mapperMap.get("floor_value")) : 0.0; 
			Integer popTargetSize = pop.length(scope);
			if (entities != null && !entities.isEmpty() && !dataId.equals("") && fields != null && !fields.isEmpty())
				loc.buildMapField(scope, entities, dataId, 
						fields, regAlgo, normalizerType, 
						floorValue, popTargetSize );
				
		}
		if (locStatement.getDistribution() != null) {
			String di = Cast.asString(scope, locStatement.getDistribution().value(scope));
			switch(di) {
			  case "area":
				  loc.setDistribution(SpatialDistributionFactory.getInstance().getAreaBasedDistribution(scope, nestList));
			    break;
			  case "uniform":
				  loc.setDistribution(SpatialDistributionFactory.getInstance().getUniformDistribution());
			    break;

			}
		}
		
		if (locStatement.getConstraints() != null) {
			IList constraints = Cast.asList(scope, loc.getConstraints());
			if (constraints != null && !constraints.isEmpty()) {
				for (Object el : constraints) {
					Map m = Cast.asMap(scope, el, false);
					if (m == null) continue;
					String type = (String) m.get("type");
					if (type == null) continue;
					ASpatialConstraint c = null;
					switch(type) {
					  case "density":
						  Double maxDensity = (Double) m.get("max_density");
						  if (maxDensity != null) 
							  c = new SpatialConstraintMaxDensity(nestList, maxDensity);
						  else {
							  String densityFeature = (String) m.get("density_attribute");
							  if (densityFeature != null)
								  c = new SpatialConstraintMaxDensity(nestList, densityFeature);
						  }
						  break;
					  case "number":
						  Double maxNumber = (Double) m.get("max_number");
						  if (maxNumber != null) 
							  c = new SpatialConstraintMaxNumber(nestList, maxNumber);
						  else {
							  String numberFeature = (String) m.get("density_attribute");
							  if (numberFeature != null)
								  c = new SpatialConstraintMaxNumber(nestList, numberFeature);
						  }
						  
						  break;
					  case "distance":
						  Map<IShape,Double> distanceEntity = (Map) m.get("distance_entity");
						  if (distanceEntity != null && !distanceEntity.isEmpty())
							  c = new SpatialConstraintMaxDistance(distanceEntity);
						  break;
					  case "localization":
						  IShape bounds = (IShape) m.get("bounds");
						  if (bounds != null)
							  c = new SpatialConstraintLocalization(bounds);
						  break;

					}
					if (c != null) {
						Integer priority = (Integer) m.get("priority");
						if (priority != null)
							c.setPriority(priority);
						Integer maxIncrease = (Integer) m.get("max_increase");
						if (maxIncrease != null)
							c.setPriority(maxIncrease);
						Integer increaseStep = (Integer) m.get("step_increase");
						if (increaseStep != null)
							c.setPriority(increaseStep);

						loc.addConstraint(c);
					}
						
				}
			}
			
		}
		
		if (locStatement.getMinDist() != null) {
			loc.setMinDistance(Cast.asFloat(scope, locStatement.getMinDist().value(scope)));
		}
		if (locStatement.getMaxDist() != null) {
			loc.setMaxDistance(Cast.asFloat(scope, locStatement.getMaxDist().value(scope)));
		}
		
		if (locStatement.getMinDist() != null || locStatement.getMaxDist() != null ) {
			loc.computeMinMaxDistance(scope, nestList);
		}
		
		loc.localisePopulation(scope, pop);
		// TODO : transpose the Gama population of agent into Genstar IPopulation / with potential explicit link between them
		
		System.out.println("pop localized");
		
		
		// TODO : go through localisation process of SpllEntity
		
		
		// TODO : transfer back new localisation from SpllEntity to Gama IAgent
		
		
		// COPY PASTA
			

	}

}
