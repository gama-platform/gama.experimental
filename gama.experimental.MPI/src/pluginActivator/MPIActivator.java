
package pluginActivator;

import java.io.FileNotFoundException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.headless.common.Globals;
import gama.headless.job.IExperimentJob;
import mpi.*;


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
		try {
			String[] args = {};
			MPI.InitThread(args, MPI.THREAD_MULTIPLE);
			
			DEBUG.OUT("isInServerMode : " +  GAMA.isInServerMode());
			DEBUG.OUT("isInHeadLessMode : " + GAMA.isInHeadLessMode());
			
			Globals.OUTPUT_PATH = "output.log";
			DEBUG.OUT("OK  ? MPI.COMM_WORLD.getRank() " + MPI.COMM_WORLD.getRank());
			
			DEBUG.OUT("pre register");
			DEBUG.REGISTER_LOG_WRITER(new IExperimentJob.DebugStream(MPI.COMM_WORLD.getRank()));
			DEBUG.OUT("post register");
			
			DEBUG.LOG("MPI STARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTSTARTvv");
			DEBUG.LOG("************* MPI Init : " + MPI.COMM_WORLD.getRank());
		} catch (FileNotFoundException | MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DEBUG.OUT("getRankgetRank failed");
		}
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
