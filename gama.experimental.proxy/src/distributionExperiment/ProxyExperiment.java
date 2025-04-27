package distributionExperiment;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.experiment;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.IPopulationFactory;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import proxyPopulation.ProxyPopulationFactory;

/**
 * Experiment that create a ProxyAgent with every Agent of the simulation
 * Those ProxyAgent will control the of these Agent's attribute from other Agent in the simulation
 *
 *
 *	After each step the experiment will :
 *		 send proxyToMigrate to the corresponding processor
 *		 update proxyToUpdate to the corresponding processor
 *
 */
@experiment (IKeyword.PROXY)
@doc("proxy experiment")
public class ProxyExperiment extends ExperimentAgent
{
	static
	{
		//DEBUG.ON();
	}
	
	public ProxyExperiment(IPopulation<? extends IAgent> s, int index) throws GamaRuntimeException 
	{
		super(s, index);
		setPopulationFactory(initializePopulationFactory());
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	@Override
	protected IPopulationFactory initializePopulationFactory() 
	{
		DEBUG.OUT("initializePopulationFactory");
		return new ProxyPopulationFactory();
	}
}
