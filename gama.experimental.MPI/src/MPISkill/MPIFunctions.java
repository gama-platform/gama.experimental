package MPISkill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import mpi.MPI;
import mpi.MPIException;
import mpi.Status;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.extension.serialize.gaml.SerialisationOperators;
import gama.extension.serialize.implementations.BinarySerialisation;

public class MPIFunctions 
{	
	static
	{
		DEBUG.ON();
	}
	static void MPI_SEND(IScope scope, Object msg, int dest, int tag) throws MPIException
	{
		String conversion = SerialisationOperators.serialize(scope, msg);
		DEBUG.OUT("conversion: " +conversion);
		
		final byte[] message = conversion.getBytes();
		
		byte type = message[0];

		DEBUG.OUT("object type : " + type);
		DEBUG.OUT("send message: " + message);
		DEBUG.OUT("message lenght: " + message.length);
		MPI.COMM_WORLD.send(message, message.length, MPI.BYTE, dest, tag);

		DEBUG.OUT("End send ");
	}
	
	static Object MPI_RECV(IScope scope, int source, int tag) throws MPIException
	{
		DEBUG.OUT("Before MPI.COMM_WORLD.recv");
			
		Status st = MPI.COMM_WORLD.probe(source, tag);
        int sizeOfMessage = st.getCount(MPI.BYTE);
        
        DEBUG.OUT("sizeOfMessage : " + sizeOfMessage);
        byte[] message = new byte[sizeOfMessage];
		
		MPI.COMM_WORLD.recv(message, sizeOfMessage, MPI.BYTE, source, tag);
		DEBUG.OUT("after MPI.COMM_WORLD.recv");
		DEBUG.OUT("received : " + message);

		byte type = message[0];
		DEBUG.OUT("object type in receive : " + type);
		
		Object t2 = (Object) SerialisationOperators.unserialize(scope, new String(message));

		return t2;
	}
	
	static IList<?> MPI_GATHERV(IScope scope, IList<?> msg, int recipient) throws MPIException
	{	
		int my_rank = MPI.COMM_WORLD.getRank();
		int world_size = MPI.COMM_WORLD.getSize();
		

        DEBUG.OUT("recipient : " + recipient);
        DEBUG.OUT("my_rank : " + my_rank);
        DEBUG.OUT("world_size : " + world_size);
        DEBUG.OUT("msg number of elem  : " + msg.length(scope));
		
		String conversion = SerialisationOperators.serialize(scope, msg);
		final byte[] message = conversion.getBytes();
        DEBUG.OUT("message : " + new String(message));
		
		int totalSize = 0;
		
        int sizeGatherIn[] = new int[1]; // buffer to send the size to root
        sizeGatherIn[0] = message.length;
        

        DEBUG.OUT(" b0  size : " + message.length);
        DEBUG.OUT(" li  sizedddd : " + new String(message).length());
        IList<?> li2 = (IList<?>) BinarySerialisation.createFromString(scope, new String(message));
        DEBUG.OUT(" li  size : " + li2.length(scope));
        DEBUG.OUT("self li : " + li2);
        
        DEBUG.OUT("sizeOfMessage byte : " + sizeGatherIn[0]);

        int sizeGatherOut[] = new int[world_size]; // Buffer to receive all the size from others process

        byte[] dataBufferIn = message; // Buffer to send to root
        byte[] dataBufferOut; // Buffer to receive all data in root
        int[] displ = new int[world_size]; // displacements buffer => displ[i] = starting index where to write the data from process i in buffer dataBufferOut

        if(my_rank == recipient) // processor gathering data
        {

            DEBUG.OUT("I'm the recipient : " + my_rank);
            DEBUG.OUT("1st Gather");
			MPI.COMM_WORLD.gather(sizeGatherIn, 1, MPI.INT, sizeGatherOut, 1, MPI.INT, recipient); // receive size from all process
            for (int i = 0; i < sizeGatherOut.length; i++) 
            {
                totalSize += sizeGatherOut[i]; // setup buffer size for gatherV
                DEBUG.OUT("sizeGatherOut["+i+"] : " + sizeGatherOut[i]);
            }
            DEBUG.OUT("total number of data to receive = " + totalSize);
            dataBufferOut = new byte[totalSize]; // buffer to receive all the data
            
            displ = computeDispl(world_size, sizeGatherOut);

            DEBUG.OUT("2nd Gather");
            MPI.COMM_WORLD.gatherv(dataBufferIn, sizeGatherIn[0], MPI.BYTE, dataBufferOut, sizeGatherOut, displ, MPI.BYTE, recipient); // receive data from all process
            
            DEBUG.OUT("after 2n gather");

            DEBUG.OUT("displ.length " + displ.length);
            for(int index = 0; index < displ.length; index++)
            {
                DEBUG.OUT("displ["+index+"] " + displ[index]);
            }
                    
            byte b1[];
            IList<?> li = GamaListFactory.create();

            int indexInBuffer = 0;
            for(int index = 0; index < displ.length; index++)
            {        
                DEBUG.OUT("index " + index);
                if(index != displ.length-1)
                {
                    DEBUG.OUT("start displ["+index+"] from displ " + (displ[index]));
                    DEBUG.OUT("end displ[" + (index+1) +"] " + (displ[index+1]));
                	b1 = Arrays.copyOfRange(dataBufferOut, indexInBuffer, displ[index+1]);
                    indexInBuffer = displ[index+1];
                }else
                {             
                    DEBUG.OUT("start displ["+index+"]" + (displ[index]));
                    DEBUG.OUT("end displ" + dataBufferOut.length);
                	b1 = Arrays.copyOfRange(dataBufferOut, indexInBuffer, dataBufferOut.length);  	
                }
                
                li.addAll((List)BinarySerialisation.createFromString(scope, new String(b1)));
                DEBUG.OUT("created li : " + li);
            }
            
            return (IList<?>) li;

        }else // others processors sending data
        {

            DEBUG.OUT("I'm other " + my_rank);
            DEBUG.OUT("1st Gather : " + sizeGatherIn[0] + " elem ");
        	MPI.COMM_WORLD.gather(sizeGatherIn, 1, MPI.INT, recipient); // send size to root
            DEBUG.OUT("2nd Gather " + dataBufferIn);
			MPI.COMM_WORLD.gatherv(dataBufferIn, sizeGatherIn[0], MPI.BYTE, recipient); // send data to root
            DEBUG.OUT("after 2n gather");
        }
        
        return null;
	}
	
	public static IList<?> MPI_ALLTOALLV(IScope scope, IMap<Integer, List<?>> msg) throws MPIException
	{
		int my_rank = MPI.COMM_WORLD.getRank(); // rank of process
        int world_size = MPI.COMM_WORLD.getSize(); // number of process in comm
        
        DEBUG.OUT("my_rank : " + my_rank);
        DEBUG.OUT("world_size : " + world_size);
        DEBUG.OUT("msg number of elem  : " + msg.length(scope));
        DEBUG.OUT("msg size  : " + msg.size());
        
        DEBUG.OUT("msg.entrySet() : " + msg.entrySet());
        for(var auto : msg.entrySet())
		{
        	DEBUG.OUT("rank : " + auto.getKey());
			for(var copyAgent : auto.getValue())
			{
				DEBUG.OUT("agent to send : " + copyAgent);
			}
		}

        
        int bufferReceiveSize[] = new int[world_size]; // buffer to receive size of incoming buffer in allToAllv
        int buffSendSize[] = new int[world_size]; // buffer to send size of incoming buffer to all
        
        List<byte[]> serializedMessage = new ArrayList<byte[]>();
        
        for(int index = 0; index < world_size; index++)
        {
        	if(msg.get(index) != null && msg.get(index).size() != 0)
        	{
        		
        		String conversion = SerialisationOperators.serialize(scope, msg.get(index));
        		DEBUG.OUT("conversion: " +conversion);
        		
        		final byte[] message = conversion.getBytes();
        		
        		buffSendSize[index] = message.length;
        		serializedMessage.add(message);
        	}else
        	{
        		buffSendSize[index] = 0;
        	}
        }
         
        byte[] finalMessage = new byte[Arrays.stream(buffSendSize).sum()];
        int offset = 0;
        for (byte[] byteArray : serializedMessage) {
            System.arraycopy(byteArray, 0, finalMessage, offset, byteArray.length);
            offset += byteArray.length;
        }
        
		DEBUG.OUT("finalMessage lenght : " +finalMessage.length);
        
        int displsSend[] = computeDispl(world_size, buffSendSize); // displs of send buffer
		DEBUG.OUT("computeDispl displsSend ");

		DEBUG.OUT("1st all to all ");
        MPI.COMM_WORLD.allToAll(buffSendSize, 1, MPI.INT, bufferReceiveSize, 1, MPI.INT); // send to all + receive from all size of incoming buffer

		DEBUG.OUT("bufferReceiveSize received : " + bufferReceiveSize.length);
		
        int displsReceive[] = computeDispl(world_size, bufferReceiveSize); // displs of receive buffer*/
		DEBUG.OUT("computeDispl displsReceive ");
        byte bufferReceiveData[] = new byte[Arrays.stream(bufferReceiveSize).sum()]; // buffer to receive data

		DEBUG.OUT("bufferReceiveData");
        MPI.COMM_WORLD.allToAllv(finalMessage, buffSendSize, displsSend, MPI.BYTE, bufferReceiveData, bufferReceiveSize, displsReceive, MPI.BYTE); // send to all + receive from all with different size
        
        IList<?> li = GamaListFactory.create();
        byte b1[];

		DEBUG.OUT("displsReceive.length : " + displsReceive.length);

        int subBufferStart;
        int subBufferEnd;
        for(int index = 0; index < displsReceive.length; index++)
        {        
        	DEBUG.OUT("index " + index);
        	DEBUG.OUT("displsReceive.length " + displsReceive.length);
    	    if(index != displsReceive.length-1)
            {
                DEBUG.OUT("start displ["+index+"] from displ " + (displsReceive[index]));
                DEBUG.OUT("end displ[" + (index+1) +"] " + (displsReceive[index+1]));         
                subBufferStart = displsReceive[index];
                subBufferEnd = displsReceive[index+1];
           
            }else
            {           
                subBufferStart = displsReceive[index];  
            	subBufferEnd = bufferReceiveData.length;
            }
    		if(subBufferStart != subBufferEnd)
    		{
    			b1 = Arrays.copyOfRange(bufferReceiveData, subBufferStart, subBufferEnd);
    			li.addAll((List)BinarySerialisation.createFromString(scope, new String(b1)));
    			DEBUG.OUT("created li : " + li);
    			
    			for(var auto : li)
    			{
    				
					//DEBUG.OUT("agnts attributes  : " + ((IAgent)auto).getAttributes(false));
    				
    			}
    		}
		}
        
        
		return li;
	}

	
	static int[] computeDispl(int tasks, int[] buffSendSize)
    {
        int[] displs = new int[tasks];
        displs[0] = 0;
        
        StringBuilder str = new StringBuilder("computeDispl : \n");
        str.append("displs" + 0 + " :: " + displs[0]+ "\n");

        for(int index = 1; index < buffSendSize.length; index++)
        {
            str.append(index + " :: " + buffSendSize[index]+ "\n");
            displs[index] = displs[index-1] + buffSendSize[index-1];
            str.append("displs" + index + " :: " + displs[index]+ "\n");
        }
        //System.out.println(str);
        return displs;
    }
}
