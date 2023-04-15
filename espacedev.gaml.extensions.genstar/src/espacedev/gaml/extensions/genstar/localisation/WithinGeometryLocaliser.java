package espacedev.gaml.extensions.genstar.localisation;

import java.util.Map;

import espacedev.gaml.extensions.genstar.statement.LocaliseStatement;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import msi.gaml.types.Types;
import spll.localizer.SPLocalizer;
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
		
		SPLocalizer loc = new SPLocalizer(scope, nestList, nestAtt);
		Map matcherMap =  (locStatement.getMatcher() != null) ? Cast.asMap(scope, locStatement.getMatcher().value(scope), false) : null;
		if (matcherMap != null) 
			loc.setMatcher(Cast.asList(scope, matcherMap.get("entities")),Cast.asString(scope, matcherMap.get("pop_id")),  Cast.asString(scope, matcherMap.get("data_id")));
	
		Map mapperMap =  (locStatement.getMapper() != null) ? Cast.asMap(scope, locStatement.getMapper().value(scope), false) : null;
		if (mapperMap != null) 
			loc.setMapper(Cast.asList(scope, mapperMap.get("entities")),Cast.asString(scope, mapperMap.get("data_id")));
	
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
		
		
		// COPY PASTA
			

	}

}
