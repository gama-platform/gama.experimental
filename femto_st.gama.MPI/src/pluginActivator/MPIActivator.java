
package pluginActivator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.dev.DEBUG;
import mpi.MPI;
import mpi.MPIException;


/**
 * The MPIActivator.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class MPIActivator implements BundleActivator {

	static
	{
		DEBUG.ON();
	}
	@Override
	public void start(final BundleContext context) throws Exception 
	{
		String[] args = {};
		MPI.InitThread(args, MPI.THREAD_MULTIPLE);
		
		
		//final IExperimentAgent a = scope.getExperiment();
		
		/*if(GAMA.isInHeadLessMode())
		{
			try {
				Globals.OUTPUT_PATH = "output.log";
				DEBUG.REGISTER_LOG_WRITER(new IExperimentJob.DebugStream(15));
				
				DEBUG.OUT("MPI STARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTvv");
				
		    	DEBUG.OUT("************* MPI Init : " + MPI.COMM_WORLD.getRank());
				
			} catch (MPIException e) {
		    	DEBUG.OUT("MPI Init Error" + e);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}else
		{
			DEBUG.OUT("MPI NOT isInHeadLessMode");
			DEBUG.OUT("MPI NOT  isInHeadLessMode");
			DEBUG.OUT("MPI  NOT STisInHeadLessModeisInHeadLessModeART");
			DEBUG.OUT("MPI  NOT START");
			DEBUG.OUT("MPI NOT  START");
			DEBUG.OUT("MPI STisInHeadLessModeART");
			DEBUG.OUT("MPI NOT  NOT   NOT START");	
			DEBUG.OUT("MPI  NOT TisInHeadLessModeART");
			DEBUG.OUT("MPI S NOT  NOT TisInHeadLessModeisInHeadLessModeART");
			DEBUG.OUT("MPI NOT  NOT  STisInHeadLessModeART");
			DEBUG.OUT("MPI STisInHeadLessModeART");
			
		}*/
	}

	@Override
	public void stop(final BundleContext context) throws Exception
	{
		try {
	    	DEBUG.OUT("************* MPI Finalize");
			MPI.Finalize();
	    } catch (final MPIException e) {
	    	DEBUG.OUT("MPI Finalize Error" + e);
	    }
	}

}
