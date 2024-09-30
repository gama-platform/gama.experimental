package mpiExperiment;

import java.io.FileNotFoundException;

import MPISkill.MPIFunctions;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.experiment;
import gama.annotations.precompiler.IConcept;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.headless.common.Globals;
import gama.headless.job.IExperimentJob;
import mpi.MPI;
import mpi.MPIException;

@experiment (IConcept.MPI_EXP)
@doc("MPI experiment")
public class MPIExperiment extends ExperimentAgent
{
	static
	{
		DEBUG.ON();
		DEBUG.FORCE_ON();
	}
	
	public MPIExperiment(IPopulation<? extends IAgent> s, int index) throws GamaRuntimeException 
	{
		super(s, index);
		MPIFunctions.MPI_INIT_MULTIPLE_THREAD();
		setOuputForDistributedExperiment();   
	}
	
	@Override
	public void dispose() 
	{
    	DEBUG.OUT("************* disposing ProxyExperiment");
		super.dispose();
		MPIFunctions.MPI_FINALIZE();
		DEBUG.UNREGISTER_LOG_WRITER();
	}
	
	@Override
	protected void postStep(final IScope scope) 
	{
		// from this point we are sure that there are no more request to process for this step
		DEBUG.OUT("before postStep");
		super.postStep(scope);
		DEBUG.OUT("after postStep");
	}
	
	private void setOuputForDistributedExperiment()
	{
		Globals.OUTPUT_PATH = "output.log"; // directory in which all logs will be written 
		try {
			DEBUG.OUT("MPI.COMM_WORLD.getRank() " + MPI.COMM_WORLD.getRank());
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
	}
}
