package espacedev.gaml.extensions.genstar.localisation;

import espacedev.gaml.extensions.genstar.statement.LocaliseStatement;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import spll.localizer.SPLocalizer;

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
	public void localise(IScope scope, IList<? extends IAgent> pop, Object nests, LocaliseStatement locStatement) {
		SPLocalizer loc = new SPLocalizer(scope, Cast.asList(scope, nests));
		loc.localisePopulation(scope, (IList<IAgent>) pop);
		// TODO : transpose the Gama population of agent into Genstar IPopulation / with potential explicit link between them
		
		
		
		// TODO : go through localisation process of SpllEntity
		
		
		// TODO : transfer back new localisation from SpllEntity to Gama IAgent
		
		
		// COPY PASTA
			

	}

}
