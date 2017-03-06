package femto_st.gama.mpi.runtime;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import mpi.MPI;

public class Startup implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		String[] args= {};
		MPI.Init(args);
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		MPI.Finalize();
		
	}

}
