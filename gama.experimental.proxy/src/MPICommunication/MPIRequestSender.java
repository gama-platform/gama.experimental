package MPICommunication;

import java.util.ArrayList;
import java.util.Map;

import gama.core.metamodel.agent.IAgent;
import gama.dev.DEBUG;

/**
 * 
 * Send MPI Request to a process
 * 
 * 
 */
public class MPIRequestSender 
{
	static
	{
		DEBUG.OFF();
	}
	
	
	public static void updateProxy(Map<Integer, ArrayList<IAgent>> updatedProxy) 
	{
		//MPI_ALLTOALLV
		
		// serialize data
		// barrier
		
		// alltoallv
		
		// barrier
	}
	
	public static byte[] serialize(ArrayList<IAgent> agents)
	{
		return new byte[1];
	}
}

