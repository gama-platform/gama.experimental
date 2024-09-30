package hardSyncModeComm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import MPISkill.IMPISkill;
import MPISkill.MPIFunctions;
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
	private final BlockingQueue<Object> answerQueueForLocal = new LinkedBlockingQueue<>(1);
	private final AtomicBoolean running = new AtomicBoolean(true);

	private final Thread consumerThread; // consumer
	private final Thread receiverThread; // receive
	
	private IScope scope;
    
    public HardSyncModeProducerConsumer(IScope scope)
	{
    	DEBUG.OUT("HardSyncModeProducerConsumer init ");

    	DEBUG.OUT("receiverThread init ");
		this.receiverThread = new Thread(this::receiveRequest); // receive thread
    	DEBUG.OUT("consumerThread init ");
        this.consumerThread = new Thread(this::consumeRequest); // consume thread
        
        this.scope = scope;
        
        receiverThread.start();
        consumerThread.start();
	}
	
	 public void stop() 
	 {
        running.set(false);
		requestQueue.offer(new HardSyncRequestRunnable(RequestType.poison)); // poison request that will stop the consumeRequest thread without exception
        
    	try
        {
        	receiverThread.interrupt();
        	
        	if(!requestQueue.isEmpty())
        	{
        		for(var auto : requestQueue)
        		{
        			DEBUG.OUT("requestQueue requestType " + auto.requestType);
        			DEBUG.OUT("requestQueue needasnwer " + auto.needAnswer);
        		}
        	}
        	
        }catch(Exception e)
        {
        	DEBUG.OUT("exception in dispose of consumer/producer hardsync " + e);
        	e.printStackTrace();
        }finally
        {
        	DEBUG.OUT("AFTER STOPPING");
        }
    }
	
	 private void receiveRequest() 
	 {
		DEBUG.OUT("receiveRequests start ");
		try 
		{
	        while (running.get()) 
	        {
				DEBUG.OUT("receiveRequest while " + requestQueue.size());

				DEBUG.OUT("request left");
				for(var auto : requestQueue)
				{
					DEBUG.OUT("rq : " + auto);
				}
	        	HardSyncRequestRunnable request = (HardSyncRequestRunnable) MPIFunctions.MPI_RECV(scope, MPI.ANY_SOURCE, IMPISkill.REQUEST_TYPE); // receive request
				DEBUG.OUT("received request " + request);
				requestQueue.offer(request);
			}
		}
		catch (Exception e) 
		{
			DEBUG.OUT("receiveRequest exception " + e);
        	e.printStackTrace();
		}
		DEBUG.OUT("receiveRequests end "); 
	}
	

	private void consumeRequest() 
	{
		DEBUG.OUT("consumeRequests start ");
		try 
		{
			while(running.get())
			{
				DEBUG.OUT("consumeRequest while " + requestQueue.size());
				HardSyncRequestRunnable request = requestQueue.take();
				request.scope = this.scope; // not sure about this
				
				DEBUG.OUT("starting to process request " + request.requestType);
				
				if(request.requestType == RequestType.poison)
				{
					//stop receiving
					DEBUG.OUT("requestQueue size at poison " + requestQueue.size());
					break;
				}
				
				request.run();
			}
		} catch (InterruptedException e) 
		{
			DEBUG.OUT("processRequests exception " + e);
			DEBUG.OUT("SIEZ OF QUEUE " + this.requestQueue.size());
        	e.printStackTrace();
		}
		DEBUG.OUT("consumeRequests end ");
		DEBUG.OUT("SIEZ OF QUEUE normal" + this.requestQueue.size());
	}
	
	/**
	 * way for local agent to add their request to their processor without having to make a MPI comm
	 * 
	 */
	public void addRequest(HardSyncRequestRunnable request)
	{
		DEBUG.OUT("addRequest from local" + request);
		requestQueue.offer(request);
	}
	
	public boolean isQueueEmpty()
	{
		return this.requestQueue.peek() == null;
	}

	public Object getAnswer() 
	{
		DEBUG.OUT("waiting for answer");
		try {
			Object value = answerQueueForLocal.take();
			DEBUG.OUT("got answer local " + value);
			return value;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			DEBUG.OUT("getAnswer execption " + e);
			e.printStackTrace();
		}
		return null;
	}

	public void setAnswer(Object value) {
		DEBUG.OUT("setting answer " + value);
		answerQueueForLocal.offer(value);
	}
}
