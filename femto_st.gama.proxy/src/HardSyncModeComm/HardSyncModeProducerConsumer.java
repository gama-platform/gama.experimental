package HardSyncModeComm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import MPISkill.IMPISkill;
import gama.core.runtime.IScope;
import gama.dev.DEBUG;
import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

/**
 * This class manage 2 threads :
 * 
 * - receiverThread : this thread will receive 'HardSyncRequestRunnable' from other processors and will store the request in 'requestQueue'
 * - consumerThread : this thread will process the request in 'requestQueue' one request at a time 
 * 
 * 
 * This class act as a server for the whole processor to process the communication for agent with HardSyncMode
 * 
 */
public class HardSyncModeProducerConsumer
{
	static
	{
		DEBUG.ON();
	}
	
	private final BlockingQueue<HardSyncRequestRunnable> requestQueue = new LinkedBlockingQueue<>();
	private final AtomicBoolean running = new AtomicBoolean(true);

	private final Thread consumerThread; // consumer
	private final Thread receiverThread; // receive
    
    IScope scope;
    
    public HardSyncModeProducerConsumer(IScope scope)
	{
		this.scope = scope;
		
		this.receiverThread = new Thread(this::receiveRequest); // receive thread
        this.consumerThread = new Thread(this::consumeRequest); // consume thread
        
        receiverThread.start();
        consumerThread.start();
	}
	
	 public void stop() 
	 {
        running.set(false);
        receiverThread.interrupt();
        consumerThread.interrupt();
    }
	
	 private void receiveRequest() 
	 {
		DEBUG.OUT("receiveRequests start ");
        while (running.get()) 
        {
			try 
			{
				Status st = MPI.COMM_WORLD.probe(MPI.ANY_SOURCE, 0);	
				
				DEBUG.OUT("st source = " + st.getSource());
				DEBUG.OUT("st tag = " + st.getTag());
				DEBUG.OUT("st count  = " + st.getCount(MPI.CHAR));
				
				int sizeOfMessage = st.getCount(MPI.BYTE);
				byte[] arr = new byte[sizeOfMessage];
				
				MPI.COMM_WORLD.recv(arr, sizeOfMessage, MPI.BYTE, MPI.ANY_SOURCE, IMPISkill.REQUEST_TYPE); // receive request

				HardSyncRequestRunnableServer requestProcess = new HardSyncRequestRunnableServer(arr); // creating the runnable associated to the received request to answer it
				DEBUG.OUT("Request create " + requestProcess.toString());
				
				requestQueue.offer(requestProcess);
				
			} catch (MPIException e) 
			{
				DEBUG.OUT("receiveRequests exception " + e);
			}
		}
		DEBUG.OUT("receiveRequests end ");
	}
	

	private void consumeRequest() 
	{
		DEBUG.OUT("consumeRequests start ");
		while(running.get())
		{
			try 
			{
				Thread processRequestThread = new Thread(requestQueue.take());
				processRequestThread.start(); // start thread
				processRequestThread.join(); // wait for the end of the thread
			} catch (InterruptedException e) 
			{
				DEBUG.OUT("processRequests exception " + e);
			}
		}
		DEBUG.OUT("consumeRequests end ");
	}
}
