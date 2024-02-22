package HardSyncModeComm;

import gama.dev.DEBUG;

public class HardSyncRequestRunnableServer extends HardSyncRequestRunnable
{
	static
	{
		DEBUG.ON();
	}
	public HardSyncRequestRunnableServer(RequestType requestType, int uniqueID, int myRank, int rankWithLocalAgent, String attributeToRead)
	{
		super(requestType, uniqueID, myRank, rankWithLocalAgent, attributeToRead);
	}
	public HardSyncRequestRunnableServer(RequestType requestType, int uniqueID, int myRank, int rankWithLocalAgent, String attributeToWrite, Object valueToWrite)
	{
		super(requestType, uniqueID, myRank, rankWithLocalAgent, attributeToWrite, valueToWrite);
	}
	
	public HardSyncRequestRunnableServer(byte[] data) // create from byte
	{
		super(data);
	}
	
	@Override
	public void readRequest() {
		DEBUG.OUT("readRequest server side");
	}
	@Override
	public void writeRequest() {
		DEBUG.OUT("writeRequest server side");
	}
}
