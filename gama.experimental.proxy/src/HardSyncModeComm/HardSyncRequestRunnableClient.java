package HardSyncModeComm;

import gama.dev.DEBUG;

/**
 * Runnable capable of processing a RequestRunnable as the receiver
 * 
 * 
 */
public class HardSyncRequestRunnableClient extends HardSyncRequestRunnable
{
	static
	{
		DEBUG.ON();
	}
	public HardSyncRequestRunnableClient(RequestType requestType, int uniqueID, int myRank, int rankWithLocalAgent, String attributeToRead)
	{
		super(requestType, uniqueID, myRank, rankWithLocalAgent, attributeToRead);
	}
	
	public HardSyncRequestRunnableClient(RequestType requestType, int uniqueID, int myRank, int rankWithLocalAgent, String attributeToWrite, Object valueToWrite)
	{
		super(requestType, uniqueID, myRank, rankWithLocalAgent, attributeToWrite, valueToWrite);
	}
	
	public HardSyncRequestRunnableClient(byte[] data)
	{
		super(data);
	}

	@Override
	public void readRequest() {
		DEBUG.OUT("readRequest client side");
	}
	@Override
	public void writeRequest() {
		DEBUG.OUT("writeRequest client side");
	}	
}
