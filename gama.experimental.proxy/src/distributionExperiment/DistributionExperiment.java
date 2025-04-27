package distributionExperiment;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import MPISkill.MPIFunctions;
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
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.headless.common.Globals;
import gama.headless.job.IExperimentJob;
import hardSyncModeComm.HardSyncModeProducerConsumer;
import mpi.MPI;
import mpi.MPIException;
import proxy.ProxyAgent;
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
		//DEBUG.ON();
		//DEBUG.FORCE_ON();
	}
	
	public IMap<Integer, List<?>> proxyToMigrate;
	public IMap<Integer, List<?>> proxyToUpdate;
	public IMap<Integer, List<?>> proxyToCopy;
	

	public IMap<Integer, IList<?>> copiedProxyFromOther; // agent copied on this proc
	
	protected HardSyncModeProducerConsumer hardSyncServer; // hardSyncMode server for each proc
	
	int current_step = 0;
	
	public DistributionExperiment(IPopulation<? extends IAgent> s, int index) throws GamaRuntimeException 
	{
		super(s, index);
		MPIFunctions.MPI_INIT_MULTIPLE_THREAD();
		setOuputForDistributedExperiment();    	
		setPopulationFactory(initializePopulationFactory());
		initHardSyncServer();
	}
	
	@Override
	public void dispose() 
	{
    	//DEBUG.OUT("************* disposing DistributionExperiment");
		disposeHardSyncServer();
		super.dispose();
		MPIFunctions.MPI_FINALIZE();
		DEBUG.UNREGISTER_LOG_WRITER();
	}
	
	@Override
	protected void postStep(final IScope scope) 
	{
		this.current_step++;
		//DEBUG_();
		
		postCreateCopyAction();
		//postMigrateAgentAction();
		//postProxyUpdate();
		
		synchronizeDistributedExperiments();
		
		// from this point we are sure that no more request will be made to other hardSyncServer as the current process as reached the postStep method
		// now we have to individually wait on the Queue in hardSyncServer to be empty until we proceed
		waitOnRequestsToBeProcessed();
		
		// from this point we are sure that there are no more request to process for this step
		//DEBUG.OUT("before postStep");
		super.postStep(scope);
		//DEBUG.OUT("after postStep");
		
		//DEBUG.OUT("2n synchronizeDistributedExperiments");
		synchronizeDistributedExperiments(); // might not be needed
	}
	
	@Override
	protected IPopulationFactory initializePopulationFactory() 
	{
		DEBUG.OUT("initializePopulationFactory");
		return new ProxyPopulationFactory();
	}
	
	private void DEBUG_()
	{
		DEBUG.OUT("postStep current step: " + current_step + "-------------------------------------------------");
		DEBUG.OUT("postStep proxyToMigrate : " + proxyToMigrate);
		DEBUG.OUT("postStep proxyToUpdate : " + proxyToUpdate);
		DEBUG.OUT("postStep proxyToCopy : " + proxyToCopy);
		DEBUG.OUT("postStep copiedProxyFromOther : " + copiedProxyFromOther);
	}
	
	public HardSyncModeProducerConsumer getHardSyncServer()
	{
		return this.hardSyncServer;
	}
	
	private void setOuputForDistributedExperiment()
	{
		Globals.OUTPUT_PATH = "output.log"; // directory in which all logs will be written 
		try {
			//DEBUG.OUT("MPI.COMM_WORLD.getRank() " + MPI.COMM_WORLD.getRank());
			//DEBUG.OUT("pre register");
			DEBUG.REGISTER_LOG_WRITER(new IExperimentJob.DebugStream(MPI.COMM_WORLD.getRank()));
			//DEBUG.OUT("post register");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("DEBUG.REGISTER_LOG_WRITER failed");
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("getRankgetRank failed");
		}
	}
	
	private void initHardSyncServer()
	{
    	hardSyncServer = new HardSyncModeProducerConsumer(getScope());
	}
	
	private void disposeHardSyncServer()
	{
		//DEBUG.OUT("hardSyncServer dispose ");
		while(!hardSyncServer.isQueueEmpty())
		{	
			//DEBUG.OUT("waiting for server to process all request before disposing");
		}		
		//DEBUG.OUT("hardSyncServer.stop()");
		hardSyncServer.stop();
	}
	
	private void postCreateCopyAction()
	{
		EndActionOneShotCreateCopy copy;
		if(proxyToCopy != null)
		{
			copy = new EndActionOneShotCreateCopy(proxyToCopy, this.current_step);
		}else
		{
			copy = new EndActionOneShotCreateCopy(GamaMapFactory.create(), current_step); // empty proxyToCopy
		}
		this.postOneShotAction(copy);
	}
	
	private void postMigrateAgentAction()
	{
		EndActionOneShotMigration migration;
		if(proxyToMigrate != null)
		{
			//DEBUG.OUT("posting proxyToMigrate " + proxyToMigrate);
			migration = new EndActionOneShotMigration(proxyToMigrate, this.current_step);
			//DEBUG.OUT("posting EndActionOneShotMigration1 " + migration);
			

			/*if(this.proxyToUpdate != null)
			{
				DEBUG.OUT("starting duplication ");
				var map = this.proxyToUpdate;
				DEBUG.OUT("map before removing : " + map);
				
				if(map != null)
				{
					for(var entry : map.entrySet())
					{
						if(proxyToMigrate.get(entry.getKey()) != null)
						{
							map.get(entry.getKey()).remove(this.proxyToMigrate.get(entry.getKey()));
						}
					}
					this.proxyToUpdate = map;
					DEBUG.OUT("map init after removing : " + map);
				}else
				{
					DEBUG.OUT("var map = this.proxyToUpdate is null");
				}
			}*/
		}else
		{
			//DEBUG.OUT("no agent to migrate proxyToMigrate " + proxyToMigrate);
			migration = new EndActionOneShotMigration(GamaMapFactory.create(), current_step); // empty proxyToMigrate
			//DEBUG.OUT("posting EndActionOneShotMigration2 " + migration);
		}
		this.postOneShotAction(migration);
	}
	
	private void postProxyUpdate()
	{
		EndActionOneShotProxyUpdate update;
		if(proxyToUpdate != null)
		{
			//DEBUG.OUT("new EndActionOneShotProxyUpdate : " + proxyToUpdate);
			update = new EndActionOneShotProxyUpdate(proxyToUpdate);
		}else
		{
			//DEBUG.OUT("new EndActionOneShotProxyUpdate empty");
			update = new EndActionOneShotProxyUpdate(GamaMapFactory.create()); // empty proxyToUpdate
		}
		this.postOneShotAction(update);
	}
	
	private void synchronizeDistributedExperiments()
	{
		//DEBUG.OUT("synchronizeDistributedExperiments");
		try {
			MPI.COMM_WORLD.barrier();
		} catch (MPIException e) {
			DEBUG.OUT("MPIException barrier 1");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void waitOnRequestsToBeProcessed()
	{
		while(true)
		{
			if(this.hardSyncServer.isQueueEmpty())
			{
				//DEBUG.OUT("QueueEmpty SO WE PROCEED");
				break;
			}
			//DEBUG.OUT("QueueEmpty IS TILL PREOCESSORING A RESUETS");
		}
	}
}
