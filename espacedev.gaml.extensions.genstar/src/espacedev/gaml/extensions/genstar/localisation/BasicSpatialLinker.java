package espacedev.gaml.extensions.genstar.localisation;

import java.util.Map;

import espacedev.gaml.extensions.genstar.statement.SpatialLinkerStatement;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import msi.gaml.types.Types;
import spll.localizer.distribution.ISpatialDistribution;
import spll.localizer.distribution.SpatialDistributionFactory;
import spll.localizer.linker.SPLinker;

public class BasicSpatialLinker implements IGenstarLinker {

	/** The Constant INSTANCE. */
	// SINGLETONG
	private static final BasicSpatialLinker INSTANCE = new BasicSpatialLinker();

	/**
	 * Gets the single instance of WithinGeometryLocaliser.
	 *
	 * @return single instance of WithinGeometryLocaliser
	 */
	public static BasicSpatialLinker getInstance() { return INSTANCE; }
	 
	@Override
	public void link(IScope scope, IContainer<?, IAgent> pop, IContainer<?, IShape> candidates, SpatialLinkerStatement linkStatement) {
		String nestAtt = linkStatement.getNestAttribute() != null ? Cast.asString(scope, linkStatement.getNestAttribute().value(scope)) : null;
		IList<IShape> nestList = Cast.asList(scope, candidates).listValue(scope, Types.GEOMETRY, false); 
		Map parameters = linkStatement.getParameters() != null ? Cast.asMap(scope, linkStatement.getParameters().value(scope), false) : null;
		ISpatialDistribution<IShape> distribution = null;
			
		String di = linkStatement.getDistribution() != null ? 
					Cast.asString(scope, linkStatement.getDistribution().value(scope)) : 
					"uniform";
		switch(di) {
			case "gravity":
				Double buffer = (Double) parameters.get("buffer");
				Double frictionCoeff =  (Double) parameters.get("friction_coeff");
				if (frictionCoeff == null) frictionCoeff = 1.0;
				if (buffer != null)
					distribution =  SpatialDistributionFactory.getInstance().
							getGravityModelDistribution(scope, nestList, frictionCoeff, buffer, (IList<IAgent>) pop);
				else
					distribution =  SpatialDistributionFactory.getInstance()
							.getGravityModelDistribution(nestList, frictionCoeff,(IList<IAgent>) pop);
				break;
			case "area":
				distribution = SpatialDistributionFactory.getInstance().getAreaBasedDistribution(scope, nestList);
			    break;
			case "uniform":
				distribution = SpatialDistributionFactory.getInstance().getUniformDistribution();
			    break;
		}
		
	
		SPLinker linker = new SPLinker(distribution); 
		linker.assignLink(scope, pop.listValue(scope, Types.AGENT, false), nestList, nestAtt);
		
		/*Map matcherMap =  (locStatement.getMatcher() != null) ? Cast.asMap(scope, locStatement.getMatcher().value(scope), false) : null;
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
			if (entities != null && !entities.isEmpty() && !dataId.equals("") && !fields.isEmpty())
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
		
		
		// COPY PASTA*/
			

	}

	

}
