package distributionExperiment;

import java.io.FileNotFoundException;
import java.util.List;

import MPICommunication.MPIThreadListener;
import endActionProxy.EndActionOneShotCreateCopy;
import endActionProxy.EndActionOneShotMigration;
import endActionProxy.EndActionOneShotProxyUpdate;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.experiment;
import gama.annotations.precompiler.IConcept;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.IPopulationFactory;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.headless.common.Globals;
import gama.headless.job.IExperimentJob;
import mpi.MPI;
import mpi.MPIException;
import proxyPopulation.ProxyPopulationFactory;

/**
 * Experiment that create a ProxyAgent with every Agent of the simulation
 * Those ProxyAgent will control the of these Agent's attribute from other Agent in the simulation
 *
 *
 *	After each step the experiment will :
 *		 copy proxyToCopy to the corresponding processor
 *		 send proxyToMigrate to the corresponding processor
 *		 update proxyToUpdate to the corresponding processor
 *
 */
@experiment (IConcept.DISTRIBUTION)
@doc("distribution experiment")
public class DistributionExperiment extends ExperimentAgent
{
	static
	{
		DEBUG.ON();
	}
	
	MPIThreadListener listeningThread;
	
	public IMap<Integer, List<?>> proxyToMigrate;
	public IMap<Integer, List<?>> proxyToUpdate;
	public IMap<Integer, List<?>> proxyToCopy;
	

	public List<Object> copiedProxyFromOther;
	
	int index = 0;
	
	public DistributionExperiment(IPopulation<? extends IAgent> s, int index) throws GamaRuntimeException 
	{
		super(s, index);
		setPopulationFactory(initializePopulationFactory());
		
		String[] args = {};
		try {
			if(!MPI.isInitialized())
			{
				MPI.InitThread(args, MPI.THREAD_MULTIPLE);
			}
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("MPIi nit failed");
		}
		
		DEBUG.OUT("isInServerMode : " +  GAMA.isInServerMode());
		DEBUG.OUT("isInHeadLessMode : " + GAMA.isInHeadLessMode());
		
		Globals.OUTPUT_PATH = "output.log";
		try {
			DEBUG.OUT("OK  ? MPI.COMM_WORLD.getRank() " + MPI.COMM_WORLD.getRank());
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("getRankgetRankgetRankgetRankgetRankgetRank failed");
		}
		try {
			DEBUG.OUT("pre register");
			DEBUG.REGISTER_LOG_WRITER(new IExperimentJob.DebugStream(MPI.COMM_WORLD.getRank()));
			DEBUG.OUT("post register");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("DEBUG.REGISTER_LOG_WRITER failed");
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("getRankgetRank failed");
		}

		//DEBUG.REGISTER_LOG_WRITER(new IExperimentJob.DebugStream(15));
		DEBUG.LOG("MPI STARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTvv");
		
    	try {
			DEBUG.LOG("************* MPI Init : " + MPI.COMM_WORLD.getRank());
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("getRankgetRank failed");
		}
	}
	
	@Override
	public void dispose() {
    	DEBUG.OUT("************* disposing ProxyExperiment");
    	try {
	    	DEBUG.OUT("************* MPI Finalize");
			MPI.Finalize();
	    } catch (final MPIException e) {
	    	DEBUG.OUT("MPI Finalize Error" + e);
	    }
    	DEBUG.UNREGISTER_LOG_WRITER();
		super.dispose();
	}
	
	@Override
	protected IPopulationFactory initializePopulationFactory() 
	{
		DEBUG.OUT("initializePopulationFactory");
		return new ProxyPopulationFactory();
	}
	
	@Override
	protected void postStep(final IScope scope) 
	{
		this.index++;
		

		DEBUG.OUT("postStep current step: " + index + "-------------------------------------------------");
		DEBUG.OUT("postStep proxyToMigrate : " + proxyToMigrate);
		DEBUG.OUT("postStep proxyToUpdate : " + proxyToUpdate);
		DEBUG.OUT("postStep proxyToCopy : " + proxyToCopy);
		DEBUG.OUT("postStep copiedProxyFromOther : " + copiedProxyFromOther);

		if(proxyToCopy != null)
		{
			EndActionOneShotCreateCopy copy = new EndActionOneShotCreateCopy(proxyToCopy, this.index);
			this.postOneShotAction(copy);
		}else
		{
			EndActionOneShotCreateCopy copy = new EndActionOneShotCreateCopy(GamaMapFactory.create(), index); // empty proxyToCopy
			this.postOneShotAction(copy);
		}
		
		
		if(proxyToMigrate != null)
		{
			EndActionOneShotMigration migration = new EndActionOneShotMigration(proxyToMigrate,this.index);
			this.postOneShotAction(migration);
			

			if(this.proxyToUpdate != null)
			{
				DEBUG.OUT("starting duplication ");
				var map = this.proxyToUpdate;
				DEBUG.OUT("map before removing : " + map);
				
				for(var entry : map.entrySet())
				{
					if(proxyToMigrate.get(entry.getKey()) != null)
					{
						map.get(entry.getKey()).remove(this.proxyToMigrate.get(entry.getKey()));
					}
				}
				this.proxyToUpdate = map;
				DEBUG.OUT("map init after removing : " + map);
			}
		}else
		{
			@SuppressWarnings("unchecked")
			EndActionOneShotMigration migration = new EndActionOneShotMigration(GamaMapFactory.create(), index); // empty proxyToMigrate
			this.postOneShotAction(migration);
		}

		if(proxyToUpdate != null)
		{
			DEBUG.OUT("new EndActionOneShotProxyUpdate : " + proxyToUpdate);
			EndActionOneShotProxyUpdate update = new EndActionOneShotProxyUpdate(proxyToUpdate);
			this.postOneShotAction(update);
		}else
		{
			DEBUG.OUT("new EndActionOneShotProxyUpdate empty");
			EndActionOneShotProxyUpdate update = new EndActionOneShotProxyUpdate(GamaMapFactory.create()); // empty proxyToUpdate
			this.postOneShotAction(update);
		}
		
		DEBUG.OUT("after postEndAction");
		
		try {
			MPI.COMM_WORLD.barrier();
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.postStep(scope);
		DEBUG.OUT("after postStep");
		try {
			MPI.COMM_WORLD.barrier();
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	
	}
}
