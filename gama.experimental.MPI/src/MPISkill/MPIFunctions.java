package MPISkill;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gama.api.GAMA;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.dev.DEBUG;
import gama.extension.serialize.binary.BinarySerialisation;
import gama.extension.serialize.gaml.SerialisationOperators;
import mpi.MPI;
import mpi.MPIException;
import mpi.Op;
import mpi.Request;
import mpi.Status;

/**
 * Class containing MPI functions for communication and data exchange
 */
public class MPIFunctions 
{	
	static
	{
		DEBUG.OFF();
	}

    /**
     * Synchronously sends a message to a specified destination
     * Serializes the message object and sends it as bytes
     * @param scope GAMA simulation scope
     * @param msg Object to send
     * @param dest Destination rank
     * @param tag Message identifier
     */
	public static void MPI_SEND(IScope scope, Object msg, int dest, int tag)
	{
		String conversion = SerialisationOperators.serialize(scope, msg);
		//DEBUG.OUT("conversion: " +conversion);
		
		//Object recv = (Object) SerialisationOperators.unserialize(scope, new String(conversion));
		//DEBUG.OUT("unconversion: " +recv);
		
		final byte[] message = conversion.getBytes();
		byte type = message[0];

		DEBUG.OUT("sending object type : " + type);
		
		try {
			MPI.COMM_WORLD.send(message, message.length, MPI.BYTE, dest, tag);
		} catch (Exception e) {
			DEBUG.OUT("MPI_SEND exception " + e);
			e.printStackTrace();
		}
	}

    /**
     * Asynchronously sends a message using non-blocking MPI operations
     * @param scope GAMA simulation scope
     * @param msg Object to send
     * @param dest Destination rank
     * @param tag Message identifier
     */
	public static void MPI_ISEND(IScope scope, Object msg, int dest, int tag)
	{
		DEBUG.OUT("MPI_ISEND to ["+dest+"] : " + msg + " tag " + tag);
		String conversion = SerialisationOperators.serialize(scope, msg);
		final byte[] message = conversion.getBytes();
		byte type = message[0];

		DEBUG.OUT("sending object type : " + type);

		ByteBuffer buffer = MPI.newByteBuffer(message.length);
        buffer.put(message);

		DEBUG.OUT("buffer capacity: " + buffer.capacity());
		DEBUG.OUT("buffer remaining: " + buffer.remaining());
		DEBUG.OUT("buffer limit: " + buffer.limit());
		
		try {
			Request req = MPI.COMM_WORLD.iSend(buffer, buffer.capacity(), MPI.BYTE, dest, tag);
		} catch (Exception e) {
			DEBUG.OUT("MPI_SEND exception " + e);
			e.printStackTrace();
		}
		DEBUG.OUT("End send ");
	}

    /**
     * Synchronously receives a message from a source
     * Probes for message size then receives and deserializes the data
     * @param scope GAMA simulation scope
     * @param source Source rank to receive from
     * @param tag Message identifier to match
     * @return Received and deserialized object, null if error
     */
	public static Object MPI_RECV(IScope scope, int source, int tag)
	{
		try {
			DEBUG.OUT("proibing ["+source+"] " + tag);
			Status st = MPI.COMM_WORLD.probe(source, tag);
	        int sizeOfMessage = st.getCount(MPI.BYTE);
			DEBUG.OUT("sizeOfMessage : " + sizeOfMessage);
	        byte[] message = new byte[sizeOfMessage];
			MPI.COMM_WORLD.recv(message, sizeOfMessage, MPI.BYTE, source, tag);
			DEBUG.OUT("received size : " + message.length);
			byte type = message[0];
			DEBUG.OUT("object type in receive : " + type);
			Object recv = (Object) SerialisationOperators.unserialize(scope, new String(message));
			return recv;
		} catch (Exception e) 
		{
			DEBUG.OUT("MPI_RECV exception " + e);
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Non-blocking receive operation that checks for available messages
     * @param scope GAMA simulation scope
     * @param source Source rank to receive from
     * @param tag Message identifier to match
     * @return Received object if available, null otherwise
     */
	public static Object MPI_IRECV(IScope scope, int source, int tag) 
	{
	    try {
	        DEBUG.OUT("Starting IRECV from ["+source+"] with tag " + tag);
	        Status st = MPI.COMM_WORLD.iProbe(source, tag);
	        if (st != null) {
	            int sizeOfMessage = st.getCount(MPI.BYTE);
	            byte[] message = new byte[sizeOfMessage];
	            MPI.COMM_WORLD.recv(message, sizeOfMessage, MPI.BYTE, st.getSource(), st.getTag());
	            Object recv = (Object) SerialisationOperators.unserialize(scope, new String(message));
	            return recv;
	        }
	        return null;
	    } catch (Exception e) 
	    {
	        DEBUG.OUT("MPI_IRECV exception " + e);
	        e.printStackTrace();
	    }
	    return null;
	}

    /**
     * Gathers variable-sized data from all processes to a recipient
     * Uses two-phase gathering: first sizes, then data
     * @param scope GAMA simulation scope
     * @param msg Local data to contribute
     * @param recipient Rank of gathering process
     * @return Combined list on recipient, null on others
     */
	public static IList<?> MPI_GATHERV(IScope scope, IList<?> msg, int recipient)
	{	
		int my_rank;
		try {
			my_rank = MPI.COMM_WORLD.getRank();
			int world_size = MPI.COMM_WORLD.getSize();
			//DEBUG.OUT("recipient : " + recipient);
			//DEBUG.OUT("my_rank : " + my_rank);
			//DEBUG.OUT("world_size : " + world_size);
			//DEBUG.OUT("msg number of elem  : " + msg.length(scope));
			String conversion = SerialisationOperators.serialize(scope, msg);
			final byte[] message = conversion.getBytes();
			//DEBUG.OUT("message : " + new String(message));
			int totalSize = 0;
	        int sizeGatherIn[] = new int[1]; // buffer to send the size to root
	        sizeGatherIn[0] = message.length;
	        //DEBUG.OUT("sizeOfMessage byte : " + sizeGatherIn[0]);
	        int sizeGatherOut[] = new int[world_size]; // Buffer to receive all the size from others process
	        byte[] dataBufferIn = message; // Buffer to send to root
	        byte[] dataBufferOut; // Buffer to receive all data in root
	        int[] displ = new int[world_size]; // displacements buffer
	        if(my_rank == recipient) // processor gathering data
	        {
	        	// DEBUG.OUT("I'm the recipient : " + my_rank);
	            // DEBUG.OUT("1st Gather recipient");
	            MPI.COMM_WORLD.gather(sizeGatherIn, 1, MPI.INT, sizeGatherOut, 1, MPI.INT, recipient);
	            for (int i = 0; i < sizeGatherOut.length; i++) 
	            {
	                totalSize += sizeGatherOut[i];
	                //DEBUG.OUT("sizeGatherOut["+i+"] : " + sizeGatherOut[i]);
	            }
	            //DEBUG.OUT("total number of data to receive = " + totalSize);
	            dataBufferOut = new byte[totalSize];
	            displ = computeDispl(world_size, sizeGatherOut);
	            //DEBUG.OUT("2nd Gather recipient");
	            MPI.COMM_WORLD.gatherv(dataBufferIn, sizeGatherIn[0], MPI.BYTE, dataBufferOut, sizeGatherOut, displ, MPI.BYTE, recipient);
	            // DEBUG.OUT("after 2n gather recipient");
	            // DEBUG.OUT("displ.length " + displ.length);
	            for(int index = 0; index < displ.length; index++)
	            {
	            	//DEBUG.OUT("displ["+index+"] " + displ[index]);
	            }
	            byte b1[];
	            IList<?> li = GamaListFactory.create();
	            int indexInBuffer = 0;
	            for(int index = 0; index < displ.length; index++)
	            {        
	            	//DEBUG.OUT("index " + index);
	                if(index != displ.length-1)
	                {
	                	//DEBUG.OUT("start displ["+index+"] from displ " + (displ[index]));
	                	//DEBUG.OUT("end displ[" + (index+1) +"] " + (displ[index+1]));
	                	b1 = Arrays.copyOfRange(dataBufferOut, indexInBuffer, displ[index+1]);
	                    indexInBuffer = displ[index+1];
	                }else
	                {             
	                	//DEBUG.OUT("start displ["+index+"]" + (displ[index]));
	                	//DEBUG.OUT("end displ" + dataBufferOut.length);
	                	b1 = Arrays.copyOfRange(dataBufferOut, indexInBuffer, dataBufferOut.length);  	
	                }
	                li.addAll((List)SerialisationOperators.unserialize(scope, new String(b1)));
	                //DEBUG.OUT("created li : " + li);
	            }
	            //DEBUG.OUT("returning li : " + (IList<?>) li);
	            return (IList<?>) li;
	        }else // others processors sending data
	        {
	        	//DEBUG.OUT("I'm other " + my_rank);
	        	//DEBUG.OUT("1st Gather : " + sizeGatherIn[0] + " elem ");
	        	MPI.COMM_WORLD.gather(sizeGatherIn, 1, MPI.INT, recipient); // send size to root
	        	//DEBUG.OUT("2nd Gather " + dataBufferIn);
				MPI.COMM_WORLD.gatherv(dataBufferIn, sizeGatherIn[0], MPI.BYTE, recipient); // send data to root
				//DEBUG.OUT("after 2n gather");
	        }
		} catch (Exception e) {
			DEBUG.OUT("MPI_GATHERV exception " + e);
			e.printStackTrace();
		}
        return null;
	}

    /**
     * Scatters equal-sized chunks of data from root to all processes
     * @param scope GAMA simulation scope
     * @param msg List of data to scatter (only used on root)
     * @param root Rank of root process
     * @return Received portion of data
     */
    public static IList<?> MPI_SCATTER(IScope scope, IList<?> msg, int root) {
        try {
            int my_rank = MPI.COMM_WORLD.getRank();
            int world_size = MPI.COMM_WORLD.getSize();

            if (my_rank == root) {
                // Serialize the full message on root
                String conversion = SerialisationOperators.serialize(scope, msg);
                byte[] fullData = conversion.getBytes();
                
                // Calculate chunk size
                int chunkSize = fullData.length / world_size;
                byte[] recvBuffer = new byte[chunkSize];
                
                MPI.COMM_WORLD.scatter(fullData, chunkSize, MPI.BYTE, 
                                     recvBuffer, chunkSize, MPI.BYTE, root);
                
                // Deserialize received chunk
                return (IList<?>) SerialisationOperators.unserialize(scope, new String(recvBuffer));
            } else {
                // Non-root processes only receive their portion
                Status st = MPI.COMM_WORLD.probe(root, MPI.ANY_TAG);
                int chunkSize = st.getCount(MPI.BYTE);
                byte[] recvBuffer = new byte[chunkSize];
                
                MPI.COMM_WORLD.scatter(null, 0, MPI.BYTE,
                                     recvBuffer, chunkSize, MPI.BYTE, root);
                
                return (IList<?>) SerialisationOperators.unserialize(scope, new String(recvBuffer));
            }
        } catch (Exception e) {
            DEBUG.OUT("MPI_SCATTER exception " + e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Scatters variable-sized chunks of data from root to all processes
     * @param scope GAMA simulation scope
     * @param msg Map of data chunks indexed by destination rank (only used on root)
     * @param root Rank of root process
     * @return Received portion of data
     */
    public static IList<?> MPI_SCATTERV(IScope scope, IMap<Integer, List<?>> msg, int root) {
        try {
            int my_rank = MPI.COMM_WORLD.getRank();
            int world_size = MPI.COMM_WORLD.getSize();

            DEBUG.OUT("ROOT IS " + root);
            DEBUG.OUT("I'm " + my_rank);
            DEBUG.OUT("msg " + msg);
            if (my_rank == root) {
                // Prepare send counts and data
                int[] sendcounts = new int[world_size];
                List<byte[]> chunks = new ArrayList<>();
                int totalSize = 0;

                // Serialize each chunk and calculate sizes
                for (int i = 0; i < world_size; i++) {
					String conversion = SerialisationOperators.serialize(scope, msg.get(i));
					byte[] chunk = conversion.getBytes();
					chunks.add(chunk);
					sendcounts[i] = chunk.length;
					totalSize += chunk.length;
                }

                // Prepare send buffer and displacements
                byte[] sendbuf = new byte[totalSize];
                int[] displs = computeDispl(world_size, sendcounts);
                
                // Copy chunks to send buffer
                int offset = 0;
                for (byte[] chunk : chunks) {
                    System.arraycopy(chunk, 0, sendbuf, offset, chunk.length);
                    offset += chunk.length;
                }

                // Root needs to participate in both operations:
                // 1. Scatter sizes
                MPI.COMM_WORLD.scatter(sendcounts, 1, MPI.INT,
                                     sendcounts, 1, MPI.INT, root); // Root receives its own size

                DEBUG.OUT("scatter size ");
                // 2. Scatter data
                byte[] recvbuf = new byte[sendcounts[my_rank]];
                MPI.COMM_WORLD.scatterv(sendbuf, sendcounts, displs, MPI.BYTE,
                                      recvbuf, sendcounts[my_rank], MPI.BYTE, root);

                DEBUG.OUT("scatterv done ");
                return (IList<?>) SerialisationOperators.unserialize(scope, new String(recvbuf));
            } else {
                // Non-root processes first receive their chunk size
                int[] recvcount = new int[1];
                
                // First scatter operation: receive the size of our chunk
                MPI.COMM_WORLD.scatter(null, 0, MPI.INT,
                                     recvcount, 1, MPI.INT, root);
                DEBUG.OUT("received " + recvcount[0]);
                
                // Now we know how much data we'll receive
                byte[] recvbuf = new byte[recvcount[0]];

                DEBUG.OUT("recvbuf ");
                // Second scatter operation: receive our actual data chunk
                MPI.COMM_WORLD.scatterv(null, null, null, MPI.BYTE,
                                      recvbuf, recvcount[0], MPI.BYTE, root);

                DEBUG.OUT("scatterv ");
                
                IList<?> received = (IList<?>) SerialisationOperators.unserialize(scope, new String(recvbuf));

                DEBUG.OUT("received " + received);
                // Deserialize our received chunk back into a GAMA list
                return received;
            }
        } catch (Exception e) {
            DEBUG.OUT("MPI_SCATTERV exception " + e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gathers variable-sized data from all processes to all processes
     * @param scope GAMA simulation scope
     * @param msg Local data to contribute
     * @return Combined list of all data
     */
	public static IList<?> MPI_ALLGATHERV(IScope scope, IList<?> msg)
    {
        try {	
			int my_rank = MPI.COMM_WORLD.getRank();
            int world_size = MPI.COMM_WORLD.getSize();
            
            // Serialize local data
            String conversion = SerialisationOperators.serialize(scope, msg);
            final byte[] message = conversion.getBytes();
            int totalSize = 0;
            
            // Prepare size exchange buffers
            int[] sizeGatherIn = new int[1];
            sizeGatherIn[0] = message.length;
            int[] sizeGatherOut = new int[world_size];
            
            // Exchange message sizes using allgather
            MPI.COMM_WORLD.allGather(sizeGatherIn, 1, MPI.INT, sizeGatherOut, 1, MPI.INT);
            
            // Calculate total size and displacements
            for(int i = 0; i < sizeGatherOut.length; i++) {
                totalSize += sizeGatherOut[i];
            }
            
            // Prepare data exchange buffers
            byte[] dataBufferOut = new byte[totalSize];
            int[] displ = computeDispl(world_size, sizeGatherOut);
            
            // Exchange actual data using allgatherv
            MPI.COMM_WORLD.allGatherv(message, message.length, MPI.BYTE, 
                                    dataBufferOut, sizeGatherOut, displ, MPI.BYTE);
            
            // Create result list
            IList<?> result = GamaListFactory.create();
            
            // Process received data chunks
            int indexInBuffer = 0;
            for(int index = 0; index < world_size; index++) {
                byte[] b1;
                if(index != world_size-1) {
                    b1 = Arrays.copyOfRange(dataBufferOut, indexInBuffer, displ[index+1]);
                    indexInBuffer = displ[index+1];
                } else {
                    b1 = Arrays.copyOfRange(dataBufferOut, indexInBuffer, dataBufferOut.length);
                }
                
                // Deserialize and add to result
                result.addAll((List)BinarySerialisation.createFromString(scope, new String(b1)));
            }
            
            return result;
            
        } catch (Exception e) {
            DEBUG.OUT("MPI_ALLGATHERV exception " + e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Performs fixed-size all-to-all communication
     * Each process sends equal-sized chunks to all others
     * @param scope GAMA simulation scope
     * @param size Size of each message chunk
     * @return List containing received data
     */
	public static IList<?> MPI_ALLTOALL(IScope scope, int size)
	{
		try 
		{
			int my_rank = MPI.COMM_WORLD.getRank();
	        int world_size = MPI.COMM_WORLD.getSize(); 
	        DEBUG.OUT("my_rank " + my_rank);
	        DEBUG.OUT("world_size " + world_size);
	        byte buffSend[] = new byte[size * world_size];			// buffer to send data
	        byte bufferReceive[] = new byte[size * world_size]; 	// buffer to receive data
	        DEBUG.OUT("size of buffer " + size * world_size);
	        Arrays.fill(buffSend, (byte) my_rank);
	        DEBUG.OUT("buffSend filled ");
	        DEBUG.OUT("buffSend[0] " + buffSend[0]);
	        DEBUG.OUT("buffSend[0] " + buffSend[(size * world_size) - 1]);
			MPI.COMM_WORLD.allToAll(buffSend, size, MPI.BYTE, bufferReceive, size, MPI.BYTE);
	        DEBUG.OUT("bufferReceive received " + bufferReceive[0]);
	        DEBUG.OUT("bufferReceive received -1 " + bufferReceive[(size * world_size) - 1]);
	        DEBUG.OUT("bufferReceive.toString() " + bufferReceive.toString());
	        DEBUG.OUT(Arrays.toString(bufferReceive));
		} catch (Exception e) 
		{
			DEBUG.OUT("MPI_ALLTOALLV exception " + e);
			e.printStackTrace();
		}
		return null;
	}

    /**
     * Tests variable-sized all-to-all communication
     * Creates test data of specified size from each process
     * @param scope GAMA simulation scope
     * @param size Size of test messages
     * @return Map of received data by source rank
     */
	public static IMap<Integer, IList<?>> MPI_ALLTOALLV_test(IScope scope, int size)
	{
		DEBUG.OUT("MPI_ALLTOALLV_test " + size);
		int my_rank;
		try {
			my_rank = MPI.COMM_WORLD.getRank();
	        int world_size = MPI.COMM_WORLD.getSize(); // number of process in comm
	        DEBUG.OUT("my_rank : " + my_rank);
	        DEBUG.OUT("world_size : " + world_size);
	        DEBUG.OUT("msg number of elem  : " + size);
	        int bufferReceiveSize[] = new int[world_size]; // buffer to receive size of incoming buffer in allToAllv
	        int buffSendSize[] = new int[world_size]; // buffer to send size of incoming buffer to all
	        List<byte[]> serializedMessage = new ArrayList<byte[]>();
	        for(int index = 0; index < world_size; index++)
	        {
	        	byte[] message = new byte[size];
		        Arrays.fill(message, (byte) my_rank);
        		buffSendSize[index] = message.length;
        		serializedMessage.add(message);
	        }
	        byte[] finalMessage = new byte[Arrays.stream(buffSendSize).sum()];
	        int offset = 0;
	        for (byte[] byteArray : serializedMessage) {
	            System.arraycopy(byteArray, 0, finalMessage, offset, byteArray.length);
	            offset += byteArray.length;
	        }
			DEBUG.OUT("finalMessage lenght : "  +finalMessage.length);
	        int displsSend[] = computeDispl(world_size, buffSendSize); // displs of send buffer
			DEBUG.OUT("computeDispl displsSend ");
			for(var auto : buffSendSize)
	        {
	        	DEBUG.OUT("buffSendSize " + auto);
	        }
			for(var auto : bufferReceiveSize)
	        {
	        	DEBUG.OUT("bufferReceiveSize " + auto);
	        }
			DEBUG.OUT("1st all to all ");
	        MPI.COMM_WORLD.allToAll(buffSendSize, 1, MPI.INT, bufferReceiveSize, 1, MPI.INT); // send to all + receive from all size of incoming buffer
			DEBUG.OUT("bufferReceiveSize received : " + bufferReceiveSize.length);
	        int displsReceive[] = computeDispl(world_size, bufferReceiveSize); // displs of receive buffer*/
			DEBUG.OUT("computeDispl displsReceive ");
	        byte bufferReceiveData[] = new byte[Arrays.stream(bufferReceiveSize).sum()]; // buffer to receive data
	        for(var auto : displsSend)
	        {
	        	DEBUG.OUT("displsSend " + auto);
	        }
	        for(var auto : displsReceive)
	        {
	        	DEBUG.OUT("displsReceive " + auto);
	        }
			DEBUG.OUT("bufferReceiveData");
	        MPI.COMM_WORLD.allToAllv(finalMessage, buffSendSize, displsSend, MPI.BYTE, bufferReceiveData, bufferReceiveSize, displsReceive, MPI.BYTE); // send to all + receive from all with different size
			DEBUG.OUT("post allToAllv");
	        DEBUG.OUT("received : " + bufferReceiveData.length+ " elements");
		} catch (Exception e) {
			DEBUG.OUT("MPI_ALLTOALLV exception " + e);
			e.printStackTrace();
		} // rank of process
		return null;
	}

    /**
     * Performs all-to-all communication where each process can send different amounts of data to each other process
     * Steps:
     * 1. Serialize messages for each destination
     * 2. Exchange message sizes between all processes
     * 3. Calculate displacements for variable message sizes
     * 4. Perform all-to-all communication
     * 5. Deserialize received messages into result map
     * 
     * @param scope GAMA simulation scope
     * @param msg Map of messages indexed by destination
     * @return Map of received messages indexed by source
     */
	public static IMap<Integer, IList<?>> MPI_ALLTOALLV(IScope scope, IMap<Integer, List<?>> msg)
	{
		DEBUG.OUT("MPI_ALLTOALLV " + msg);
		int my_rank;
		try {
			my_rank = MPI.COMM_WORLD.getRank();
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
			for(var auto : buffSendSize)
	        {
	        	DEBUG.OUT("buffSendSize " + auto);
	        }
			for(var auto : bufferReceiveSize)
	        {
	        	DEBUG.OUT("bufferReceiveSize " + auto);
	        }
			DEBUG.OUT("1st all to all ");
	        MPI.COMM_WORLD.allToAll(buffSendSize, 1, MPI.INT, bufferReceiveSize, 1, MPI.INT); // send to all + receive from all size of incoming buffer
			DEBUG.OUT("bufferReceiveSize received : " + bufferReceiveSize.length);
	        int displsReceive[] = computeDispl(world_size, bufferReceiveSize); // displs of receive buffer*/
			DEBUG.OUT("computeDispl displsReceive ");
	        byte bufferReceiveData[] = new byte[Arrays.stream(bufferReceiveSize).sum()]; // buffer to receive data
	        for(var auto : displsSend)
	        {
	        	DEBUG.OUT("displsSend " + auto);
	        }
	        for(var auto : displsReceive)
	        {
	        	DEBUG.OUT("displsReceive " + auto);
	        }
			DEBUG.OUT("finalMessage size : " + finalMessage + " bytes");
	        MPI.COMM_WORLD.allToAllv(finalMessage, buffSendSize, displsSend, MPI.BYTE, bufferReceiveData, bufferReceiveSize, displsReceive, MPI.BYTE); // send to all + receive from all with different size
			DEBUG.OUT("post allToAllv");
	        IMap<Integer, IList<?>> ma = GamaMapFactory.create();
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
	                DEBUG.OUT("subBufferStart [" + displsReceive[index]);
	                DEBUG.OUT("subBufferEnd [" + displsReceive[index+1]);            
	           
	            }else
	            {           
	                subBufferStart = displsReceive[index];  
	            	subBufferEnd = bufferReceiveData.length;
	            }
	    		if(subBufferStart != subBufferEnd)
	    		{
	                DEBUG.OUT("subBufferStart != subBufferEnd");
	    	        IList<Object> li = GamaListFactory.create();
	                DEBUG.OUT("li created");
	                DEBUG.OUT("subBufferStart " + subBufferStart);
	                DEBUG.OUT("subBufferEnd " + subBufferEnd);
	    			b1 = Arrays.copyOfRange(bufferReceiveData, subBufferStart, subBufferEnd);
	    			DEBUG.OUT("b1 copied " + b1.length);
	    			String byteToString = new String(b1);
	    			DEBUG.OUT("byteToString created " + b1);
	    			DEBUG.OUT("scope created" + scope);
	    			Object deserialized = (Object)SerialisationOperators.unserialize(scope, byteToString);
	    			DEBUG.OUT("deserialized" + deserialized);
	    			if(deserialized instanceof List)
	    			{	DEBUG.OUT("IList<Object>");
// check si c'est correct de faire ça
		    			ma.put(index, (IList<Object>)deserialized);
	    			}else
	    			{
		    			li.add(deserialized);
		    			ma.put(index, li);
	    			}
	    			DEBUG.OUT("created li : " + li);
	    			DEBUG.OUT("ma.put(index, li); " + ma);
	    		}
			}
			DEBUG.OUT("returning alltoall li : " + ma);
			return ma;
		} catch (Exception e) {
			DEBUG.OUT("MPI_ALLTOALLV exception " + e);
			e.printStackTrace();
		} // rank of process
		return null;
	}

    /**
     * Computes displacement array for variable-sized message operations
     * Used in GATHERV and ALLTOALLV operations
     * @param tasks Number of MPI tasks
     * @param buffSendSize Array of message sizes
     * @return Array of displacements for MPI collective operations
     */
	static int[] computeDispl(int tasks, int[] buffSendSize)
    {
        int[] displs = new int[tasks];
        displs[0] = 0;
        for(int index = 1; index < buffSendSize.length; index++)
        {
            displs[index] = displs[index-1] + buffSendSize[index-1];
        }
        return displs;
    }

    /**
     * Finalizes the MPI environment
     * Should be called at end of MPI usage
     */
	public static void MPI_FINALIZE()
	{
		try {
	    	DEBUG.OUT("************* MPI Finalize");
			MPI.Finalize();
	    } catch (final MPIException e) {
	    	DEBUG.OUT("MPI Finalize Error" + e);
	    }
	}

    /**
     * Initializes MPI in single-threaded mode
     * Must be called before any MPI operations
     */
	public static void MPI_INIT()
	{
		String[] args = {};
		try {
			if(!MPI.isInitialized())
			{
				MPI.InitThread(args, MPI.THREAD_SINGLE);
				DEBUG.LOG("MPI start thread single");
			}else
			{
				DEBUG.OUT("MPI ALREADY INITIALIZED");
			}
		} catch (MPIException e) {
			e.printStackTrace();
			DEBUG.OUT("MPI init failed");
		}
		DEBUG.OUT("isInServerMode : " +  GAMA.isInServerMode());
		DEBUG.OUT("isInHeadLessMode : " + GAMA.isInHeadLessMode());
	}

    /**
     * Initializes MPI in multi-threaded mode
     * Allows multiple threads to make MPI calls
     */
	public static void MPI_INIT_MULTIPLE_THREAD()
	{
		String[] args = {};
		try {
			if(!MPI.isInitialized())
			{
				MPI.InitThread(args, MPI.THREAD_MULTIPLE);
				DEBUG.LOG("MPI start thread multiple");
			}else
			{
				DEBUG.OUT("MPI ALREADY INITIALIZED");
			}
		} catch (MPIException e) {
			e.printStackTrace();
			DEBUG.OUT("MPI init failed");
		}
		DEBUG.OUT("isInServerMode : " +  GAMA.isInServerMode());
		DEBUG.OUT("isInHeadLessMode : " + GAMA.isInHeadLessMode());
	}

    /**
     * Reduces values from all processes to root using a specified operation
     * Examples of reduction operations:
     * - MAX: [1,5,3,2] -> 5 (maximum value)
     * - MIN: [1,5,3,2] -> 1 (minimum value) 
     * - SUM: [1,5,3,2] -> 11 (sum all values)
     * - PROD: [1,5,3,2] -> 30 (multiply all values)
     * - LAND: [true,false,true] -> false (logical AND)
     * - LOR: [true,false,true] -> true (logical OR)
     * - BAND: [5(101),3(011)] -> 1(001) (bitwise AND)
     * - BOR: [5(101),3(011)] -> 7(111) (bitwise OR)
     * 
     * @param scope GAMA simulation scope
     * @param value Local value to reduce
     * @param op Reduction operation (sum, max, min, etc)
     * @param root Rank of root process
     * @return Result on root process, null on others
     */
    public static Object MPI_REDUCE(IScope scope, Object value, String op, int root) {
        try {
            int my_rank = MPI.COMM_WORLD.getRank();
            
            // Serialize local value
            String conversion = SerialisationOperators.serialize(scope, value);
            byte[] localData = conversion.getBytes();
            
            // Get size of largest local data across all processes
            int[] sizes = new int[1];
            sizes[0] = localData.length;
            int[] allSizes = new int[MPI.COMM_WORLD.getSize()];
            MPI.COMM_WORLD.allGather(sizes, 1, MPI.INT, allSizes, 1, MPI.INT);
            int maxSize = Arrays.stream(allSizes).max().getAsInt();
            
            // Prepare buffers
            byte[] sendBuf = Arrays.copyOf(localData, maxSize);
            byte[] recvBuf = new byte[maxSize];
            
            // Convert operation string to MPI Op
            Op mpiOp;
            switch(op.toUpperCase()) {
                case "MAX":
                    mpiOp = MPI.MAX;
                    break;
                case "MIN":
                    mpiOp = MPI.MIN;
                    break;
                case "SUM":
                    mpiOp = MPI.SUM;
                    break;
                case "PROD":
                    mpiOp = MPI.PROD;
                    break;
                case "LAND":
                    mpiOp = MPI.LAND;
                    break;
                case "LOR":
                    mpiOp = MPI.LOR;
                    break;
                case "BAND":
                    mpiOp = MPI.BAND;
                    break;
                case "BOR":
                    mpiOp = MPI.BOR;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported reduction operation: " + op);
            }
            
            // Perform reduction with specified operation
            if (my_rank == root) {
                MPI.COMM_WORLD.reduce(sendBuf, recvBuf, maxSize, MPI.BYTE, mpiOp, root);
                return SerialisationOperators.unserialize(scope, new String(recvBuf).trim());
            } else {
                MPI.COMM_WORLD.reduce(sendBuf, null, maxSize, MPI.BYTE, mpiOp, root);
                return null;
            }
        } catch (Exception e) {
            DEBUG.OUT("MPI_REDUCE exception " + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Broadcasts a message from root to all processes
     * @param scope GAMA simulation scope
     * @param msg Message to broadcast (only used on root)
     * @param root Rank of root process
     * @return Received message on all processes
     */
    public static Object MPI_BROADCAST(IScope scope, Object msg, int root) {
        try {
            int my_rank = MPI.COMM_WORLD.getRank();
            
            if (my_rank == root) {
                // Root process serializes and sends
                String conversion = SerialisationOperators.serialize(scope, msg);
                byte[] data = conversion.getBytes();
                
                // First broadcast the size
                int[] size = new int[]{data.length};
                MPI.COMM_WORLD.bcast(size, 1, MPI.INT, root);
                
                // Then broadcast the data
                MPI.COMM_WORLD.bcast(data, data.length, MPI.BYTE, root);
                return msg;
            } else {
                // Other processes receive
                int[] size = new int[1];
                MPI.COMM_WORLD.bcast(size, 1, MPI.INT, root);
                
                byte[] data = new byte[size[0]];
                MPI.COMM_WORLD.bcast(data, size[0], MPI.BYTE, root);
                
                return SerialisationOperators.unserialize(scope, new String(data));
            }
        } catch (Exception e) {
            DEBUG.OUT("MPI_BCAST exception " + e);
            e.printStackTrace();
            return null;
        }
    }
}
