package gama.experimental.matlab.gaml.species;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mathworks.engine.MatlabEngine;

import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.GamaFile;
import gama.experimental.matlab.gama.matlabengine.MatlabEngineInteraction;
import gama.experimental.matlab.gama.matlabengine.MatlabEngineManager;
import gama.experimental.matlab.gama.utils.IMatlabKeyword;
import gama.gaml.types.IType;

@species (
		name = "agent_MATLAB",
		doc = @doc ("The species of agents that can connect to MATLAB software to launch some computations on it."))
@vars ({ 
	@variable (name = IMatlabKeyword.MATLAB_PATH, type = IType.STRING, doc = @doc ("The path to Matlab."))
})
public class AgentMATLAB extends GamlAgent {
	String pathToMatlab ;
	// For Mac "/Applications/MATLAB_R2019a.app/bin/maci64/"
	MatlabEngine eng;
	boolean aSyncEngine = true;
	ArrayList<String> addedPaths;

	
	public AgentMATLAB(IPopulation<? extends IAgent> s, int index) {
		super(s, index);
	}

	@getter (value = IMatlabKeyword.MATLAB_PATH, initializer = true)
	public String getPathToMatlab() {
		return pathToMatlab;
	}

	@setter (IMatlabKeyword.MATLAB_PATH)
	public void setPathToMatlab(final String p) {
		pathToMatlab = p;
	}	

	@Override
	public boolean init(final IScope scope) {
		boolean i = super.init(scope);
		
		eng = MatlabEngineManager.startMatlabEngine(getScope(), aSyncEngine);	
		addedPaths = new ArrayList<>();
		
		return i;
	}
	
	/*
	 * Restart the Matlab Engine
	 * 
	 * @return void
	 */
	@action (name = "restart_engine", 
		args = { 
			@arg ( name = IMatlabKeyword.MATLAB_ASYNC, type = IType.BOOL, optional = true,
					doc = @doc ("whether the engin is asynchronous"))
		})
	public void restartEngine(final IScope scope) throws GamaRuntimeException {
		Boolean aSyncEngine = (Boolean) scope.getArg(IMatlabKeyword.MATLAB_ASYNC, IType.BOOL);
		if(aSyncEngine == null ) {
			aSyncEngine = false;
		}
		
		MatlabEngineManager.disconnectMatlabEngine(scope, eng, aSyncEngine);
		eng = MatlabEngineManager.startMatlabEngine(getScope(), aSyncEngine);
		addedPaths = new ArrayList<>();		
	}
	
	
	/*
	 * Disconnect the Matlab Engine
	 * 
	 * @return void
	 */
	@action (name = "disconnect_engine", 
		args = { 
			@arg ( name = IMatlabKeyword.MATLAB_ASYNC, type = IType.BOOL, optional = true,
					doc = @doc ("whether the engin is asynchronous"))
		})
	public void disconnectEngine(final IScope scope) throws GamaRuntimeException {
		Boolean aSyncEngine = (Boolean) scope.getArg(IMatlabKeyword.MATLAB_ASYNC, IType.BOOL);
		if(aSyncEngine == null ) {
			aSyncEngine = false;
		}
		
		MatlabEngineManager.disconnectMatlabEngine(scope, eng, aSyncEngine);
	}	

	
	/*
	 * Test the connection with the Matlab Engine
	 * 
	 * @return true whether the computation on the Matlab engine succeed
	 */
	@action (name = "test_engine")
	public boolean testConnection(final IScope scope) {
        //Start MATLAB asynchronously
		try {
		    double[][] input = new double[4][4];
	        for (int i = 0; i < 4; i++) {
	            for (int j = 0; j < 4; j++) {
	                double num = Math.random() * 10;
	                input[i][j] = num;
	            }
	        }
	
	        // Put the matrix in the MATLAB workspace
	        eng.putVariableAsync("testGAMA", input);
	
	        // Evaluate the command to search in MATLAB
	        eng.eval("testGAMAResult=testGAMA(testGAMA>5);");
	
	        // Get result from the workspace
	        Future<double[]> futureEval = eng.getVariableAsync("testGAMAResult");
	        
	        // Get a result that could be manipulate as a tab of double
	        futureEval.get();
	        //double[] output = futureEval.get();
	        
		} catch ( IllegalArgumentException | IllegalStateException | InterruptedException | ExecutionException e) {
		    throw GamaRuntimeException.error("Connection to Matlab Failed", scope);
		}     
		return true;
	}	

	
	/*
	 * Evaluate an expression or a file script in the MATLAB Engine
	 * 
	 * @return void
	 * 
	 */
	@action (
		name = "eval", 
		args = { 
			@arg ( name = IMatlabKeyword.MATLAB_EXPRESSION, type = IType.STRING, optional = true,
				doc = @doc ("the expression to evaluate with the Matlab engine")),
			@arg ( name = IMatlabKeyword.MATLAB_FILE, type = IType.FILE, optional = true,
				doc = @doc ("the file to load in the Matlab Engine"))				
		})
	public void eval(final IScope scope) {
		if(scope.hasArg(IMatlabKeyword.MATLAB_EXPRESSION)) {
			final String expr = (String) scope.getArg(IMatlabKeyword.MATLAB_EXPRESSION, IType.STRING);				
			MatlabEngineInteraction.eval(scope, eng, expr) ;
		} else if(scope.hasArg(IMatlabKeyword.MATLAB_FILE)) {
			final GamaFile<?,?> file = (GamaFile<?,?>) scope.getArg(IMatlabKeyword.MATLAB_FILE, IType.FILE);				
			System.out.println(file.getPath(scope));
			System.out.println(file.getName(scope));
			
			String pathAndFile = file.getPath(scope);
			String fileName = file.getName(scope);
			
			String fullDir = pathAndFile.substring(0, pathAndFile.length() - fileName.length());
			if( ! addedPaths.contains(fullDir) ) {
				MatlabEngineInteraction.eval(scope, eng, "path('" + fullDir + "', path)");
				addedPaths.add(fullDir);
			}
			
			String fileNameWithoutExtension = fileName.substring(0, fileName.length() - file.getExtension(scope).length() - 1);
			// file.getExtension(scope)
			MatlabEngineInteraction.eval(scope, eng, fileNameWithoutExtension) ;		
		}

		
	}	
	

	/*
	 * Get the value of a variable
	 * 
	 * @return the value of the variable
	 */
	@action (
		name = "value_of",
		args = { 		
			@arg ( name = IMatlabKeyword.MATLAB_VARIABLE_NAME, type = IType.STRING, optional = false,
				doc = @doc ("the name of the MATLAB variable that we want to get the value"))	})
	public Object get(final IScope scope) {
		final String var = (String) scope.getArg(IMatlabKeyword.MATLAB_VARIABLE_NAME, IType.STRING);	
		
		return MatlabEngineInteraction.getVariable(scope, eng, var) ;
	}	
	
	
}
