package synchronizationMode;

import gama.core.util.IMap;
import gama.dev.DEBUG;

public abstract class LocalHardSyncMode extends LocalSynchronizationMode 
{

	static
	{
		DEBUG.ON();
	}
	
	@Override
	synchronized public IMap<String, Object> getOrCreateAttributes()
	{
		DEBUG.OUT("LocalHardSyncMode getOrCreateAttributes");
		return this.proxiedAgent.getOrCreateAttributes();
	}
	
	@Override
	synchronized public Object getAttribute(String key)
	{
		DEBUG.OUT("LocalHardSyncMode getAttribute " + key);
		return this.proxiedAgent.getAttribute(key);
	}

	@Override
	synchronized public void setAttribute(String key, Object value) 
	{
		DEBUG.OUT("LocalHardSyncMode setAttribute " + key + " :: " + value);
		this.proxiedAgent.setAttribute(key, value);
	}
}
