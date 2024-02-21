package gama.experimental.matlab.gama.matlabengine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

public class MatlabEngineManager {
	public static MatlabEngine startMatlabEngine(final IScope scope, final boolean isAsync) {
		MatlabEngine ml;
		try {	
			if(isAsync) {
			    //Start MATLAB asynchronously
			    Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
			    // Get engine instance from the future result
			    ml = eng.get();	
			} else {
			    //Start MATLAB synchronously				
				ml = MatlabEngine.startMatlab();
			}
		} catch (IllegalArgumentException | IllegalStateException | InterruptedException | ExecutionException e) {
		    throw GamaRuntimeException.error("Connection Failed", scope);
		} 
    
    	return ml;
	}

	
	public static void disconnectMatlabEngine(final IScope scope, final MatlabEngine eng, final boolean aSync) {
		try {
			if(aSync) {
				eng.disconnectAsync();
			} else {
				eng.disconnect();
			}
		} catch (EngineException e) {
		    throw GamaRuntimeException.error("Disconnection Failed", scope);
		}	
	}
}
