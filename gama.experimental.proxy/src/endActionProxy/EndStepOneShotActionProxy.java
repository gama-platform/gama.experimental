package endActionProxy;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.statements.IExecutable;


/**
 * EndActionProxy
 * 
 * Action aimed to be executed sometimes between step
 * 
 * Example : Update distant proxy, swap data from T to T-1, migrate agents, ....
 * 
 * @author lucas
 *
 */

public class EndStepOneShotActionProxy implements IExecutable 
{

	static
	{
		DEBUG.ON();
	}
	
	public EndStepOneShotActionProxy()
	{
		DEBUG.OUT("EndStepOneShotActionProxy created");
	}
	
	@Override
	public Object executeOn(IScope scope) throws GamaRuntimeException 
	{
		updateDistantProxy();
		return null;
	}

	private void updateDistantProxy()
	{
		// gather all agents with distant proxy
	}
}
