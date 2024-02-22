package HardSyncModeComm;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import gama.dev.DEBUG;

/**
 * Request for HardSyncMode communication : 
 * 
 * 1) Read : 
 * 		Client : send to server a request to read a given attribute
 * 		Server : receive a request to send a given attribute to the sender
 * 
 * 2) Write :
 * 		Client : send to server a request to change a given attribute to a given value
 * 		Server : receive a request to change a given attribute to a given value
 * 
 */
public abstract class HardSyncRequestRunnable implements Runnable
{

	RequestType requestType;
	int rankWithLocalAgent;
	int myRank;
	int uniqueID;
	String attribute;
	Object attributeValue;
	
	static
	{
		DEBUG.ON();
	}
	
	public HardSyncRequestRunnable(RequestType requestType, int uniqueID, int myRank, int rankWithLocalAgent, String attributeToRead)
	{
		DEBUG.OUT("HardSyncRequestRunnable write");
		this.requestType = requestType;
		this.myRank = myRank; 
		this.rankWithLocalAgent = rankWithLocalAgent; 
		this.attribute = attributeToRead;
		DEBUG.OUT("HardSyncRequestRunnable " + this);
	}
	
	public HardSyncRequestRunnable(RequestType requestType, int uniqueID, int myRank, int rankWithLocalAgent, String attributeToWrite, Object valueToWrite)
	{
		DEBUG.OUT("HardSyncRequestRunnable write");
		this.requestType = requestType;
		this.myRank = myRank; 
		this.rankWithLocalAgent = rankWithLocalAgent; 
		this.attribute = attributeToWrite;
		this.attributeValue = valueToWrite;
		DEBUG.OUT("HardSyncRequestRunnable " + this);
	}
	
	public HardSyncRequestRunnable(byte[] data)
	{
		deserializeObject(data);
	}

	@Override
	public void run() {
		DEBUG.OUT("HardSyncRequestRunnable run " + this.getClass());
		switch(this.requestType)
		{
			case READ:
				readRequest();
				break;
			case WRITE:
				writeRequest();
				break;
			default:
				break;
		}
	}
	
	public abstract void readRequest();
	public abstract void writeRequest();
	
	public byte[] serializeObject()
	{
		DEBUG.OUT("HardSyncRequestRunnable serializeObject");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
		
			DEBUG.OUT("requestType write");
	        oos.writeInt(requestType.ordinal()); // write requestType
	        
			DEBUG.OUT("uniqueID write"); 
	        oos.writeInt(uniqueID); // write uniqueID
	
			DEBUG.OUT("attribute write"); 
	        oos.writeUTF(attribute); // write uniqueID
	        
	        if(this.requestType == RequestType.WRITE)
	        {
	    		DEBUG.OUT("attributevalue write"); 
	            oos.writeObject(attributeValue); // write uniqueID
	        }
	        oos.flush();
	        oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		return baos.toByteArray();
	}
	
	public void deserializeObject(byte[] data)
	{
		DEBUG.OUT("deserializeObject deserializeObject");
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			
			DEBUG.OUT("readObject begin");
			DEBUG.OUT("requestType read");
			Integer requestType = ois.readInt(); // read requestType
			
			DEBUG.OUT("requestType creating");
			this.requestType = RequestType.values()[requestType];
			
			DEBUG.OUT("uniqueID read");
			this.uniqueID = ois.readInt(); // uniqueID
			
			DEBUG.OUT("attribute read"); 
			this.attribute = ois.readUTF(); // write uniqueID
			
			if(this.requestType == RequestType.WRITE)
			{
				DEBUG.OUT("attributevalue read"); 
				this.attributeValue = ois.readObject(); // write uniqueID
			}
			DEBUG.OUT("Request received = "+this);   
			ois.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	 public String toString()
	 {
		String str = "Request : sending to: " + this.rankWithLocalAgent + ""
				+ "/// looking for agent("+this.uniqueID+") for attribute : " + this.attribute + " ";
	    
		if(this.requestType == RequestType.WRITE)
        {
			str += " to set new value : " + this.attributeValue;
        }
		str += " //// RequestType : "+requestType.name();
		return str;
	}
	
}
