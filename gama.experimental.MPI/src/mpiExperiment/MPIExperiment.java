package mpiExperiment;

import java.io.FileNotFoundException;

import MPISkill.MPIFunctions;
import gama.annotations.doc;
import gama.annotations.experiment;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.core.experiment.ExperimentAgent;
import gama.dev.DEBUG;
import gama.headless.common.Globals;
import gama.headless.job.IExperimentJob;
import mpi.MPI;
import mpi.MPIException;



@experiment ("MPI_EXP")
@doc("MPI experiment")
public class MPIExperiment extends ExperimentAgent
{
	
	public MPIExperiment(IPopulation<? extends IAgent> s, int index) throws GamaRuntimeException 
	{
		super(s, index);
		MPIFunctions.MPI_INIT_MULTIPLE_THREAD();
		setOuputForDistributedExperiment();   
	}
	
	@Override
	public void dispose() 
	{
    	DEBUG.OUT("************* disposing MPIExperiment");
		super.dispose();
		MPIFunctions.MPI_FINALIZE();
		DEBUG.UNREGISTER_LOG_WRITER();
	}
	
	@Override
	protected void postStep(final IScope scope) 
	{
		// from this point we are sure that there are no more request to process for this step
		super.postStep(scope);
	}
	
	private void setOuputForDistributedExperiment()
	{
		Globals.OUTPUT_PATH = "output.log"; // directory in which all logs will be written 
		try {
			DEBUG.REGISTER_LOG_WRITER(new IExperimentJob.DebugStream(MPI.COMM_WORLD.getRank()));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
