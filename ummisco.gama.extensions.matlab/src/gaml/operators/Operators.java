package gaml.operators;

import java.util.ArrayList;

import com.mathworks.engine.MatlabEngine;

import gaml.types.GamaMatlabEngine;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import msi.gama.precompiler.ITypeProvider;
import msi.gama.precompiler.Reason;
import msi.gama.precompiler.GamlAnnotations.no_test;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.GamaMatrixType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;


public class Operators {
//	@operator(value = "test_matlab")
//	@doc(value = "crea", examples = {
//			@example(value = "as_driving_graph(road,node)  --:  build a graph while using the road agents as edges and the node agents as nodes", isExecutable = false) }, 
//	see = {"as_intersection_graph", "as_distance_graph", "as_edge_graph" })
//	@no_test

	@operator(value = "test_matlab")
	@no_test(Reason.IMPOSSIBLE_TO_TEST)
	public static boolean testConnection(final IScope scope, final boolean isAsync) {
        //Start MATLAB asynchronously
		try {
	        MatlabEngine eng = getGamaEngine(scope, isAsync).getEngine();
			
		    double[][] input = new double[4][4];
	        for (int i = 0; i < 4; i++) {
	            for (int j = 0; j < 4; j++) {
	                double num = Math.random() * 10;
	                input[i][j] = num;
	            }
	        }
	
	        // Put the matrix in the MATLAB workspace
	        eng.putVariableAsync("A", input);
	
	        // Evaluate the command to search in MATLAB
	        eng.eval("B=A(A>5);");
	
	        // Get result from the workspace
	        Future<double[]> futureEval = eng.getVariableAsync("B");
	        double[] output = futureEval.get();		
	        
	        eng.disconnect();			
		} catch ( IllegalArgumentException | IllegalStateException | InterruptedException | ExecutionException e) {
		    throw GamaRuntimeException.error("Connection to Matlab Failed", scope);
		}     
		return true;
	}

	@operator(value = "eval")
	@no_test(Reason.IMPOSSIBLE_TO_TEST)	
	public static Object evalOp(final IScope scope, final GamaMatlabEngine gamaEng, final String exp, final String variableName) {
		return eval(scope, gamaEng, exp, variableName);
	}
	
	@operator(value = "eval", type = ITypeProvider.TYPE_AT_INDEX + 3)
	@no_test(Reason.IMPOSSIBLE_TO_TEST)	
	public static Object evalOp(final IScope scope, final GamaMatlabEngine gamaEng, final String exp, final IExpression variable) {
		return eval(scope, gamaEng, exp, variable.getName());
	}

	@operator(value = "eval", type = ITypeProvider.TYPE_AT_INDEX + 4)
	@no_test(Reason.IMPOSSIBLE_TO_TEST)	
	public static Object evalOp(final IScope scope, final GamaMatlabEngine gamaEng, final String exp, final String variableName, final IType type) {
		return eval(scope, gamaEng, exp, variableName);
	}

	public static Object eval(final IScope scope, final GamaMatlabEngine gamaEng, final String exp, final String variableName) {
		 try {
			gamaEng.getEngine().eval(exp);			
		} catch (CancellationException | InterruptedException | ExecutionException e) {
		    throw GamaRuntimeException.error("MATLAB evaluation failed", scope);
		}

		return getVariable(scope, gamaEng, variableName);			
	}	
	
	@operator(value = "get_variable")
	@no_test(Reason.IMPOSSIBLE_TO_TEST)
	public static Object getVariableOp(final IScope scope, final GamaMatlabEngine gamaEng, final String variableName) {
		return getVariable(scope,gamaEng,variableName);
	}	
	
	@operator(value = "get_variable", type = ITypeProvider.TYPE_AT_INDEX + 3)
	@no_test(Reason.IMPOSSIBLE_TO_TEST)
	public static Object getVariableOp(final IScope scope, final GamaMatlabEngine gamaEng, final IExpression variable) {
		return getVariable(scope,gamaEng,variable.getName());
	}	
	
	@operator(value = "get_variable", type = ITypeProvider.TYPE_AT_INDEX + 4)
	@no_test(Reason.IMPOSSIBLE_TO_TEST)
	public static Object getVariableOp(final IScope scope, final GamaMatlabEngine gamaEng, final String exp, final String variableName, final IType type) {
		return getVariable(scope,gamaEng,variableName);
	}	
	
	public static Object getVariable(final IScope scope, final GamaMatlabEngine gamaEng,  final String variableName) {
		 Object output;

		 try {
			output = gamaEng.getEngine().getVariable(variableName);			
		 } catch (CancellationException | InterruptedException | ExecutionException e) {
		    throw GamaRuntimeException.error("MATLAB evaluation failed", scope);
		 }
		 
		 output = Matlab2GamaType(scope, output);

		return output;		
	}

	
	private static Object Matlab2GamaType(IScope scope, Object output) {

		if(output instanceof double[]) {
			return GamaListFactory.create(scope, Types.FLOAT, (double[]) output ) ;
		} else if(output instanceof double[][]) {
			// Complicated because we need to transpose the matrix provided by matlab 
			double[][] mat = (double[][]) output;
			if(mat.length == 0) {return 0.0;}
			
			IList<IList<?>> lmat = GamaListFactory.create(Types.LIST);
	
			for(int i = 0;i < mat[0].length ; i++) {
				ArrayList<Double> l = new ArrayList<>();
				for(int j = 0 ; j < mat.length ; j ++ ) {
					l.add(mat[j][i]);
				}
				lmat.add(GamaListFactory.create(scope, Types.FLOAT, l));
			}
			return GamaMatrixType.staticCast(scope, lmat, null, Types.FLOAT, true);
		}
		 
		// Other cases checked : float and bool
		return output;
	}

	@operator(value = "get_matlab_engine")
	@no_test(Reason.IMPOSSIBLE_TO_TEST)	
	public static GamaMatlabEngine getGamaEngine(final IScope scope, final boolean isAsync) {
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
		    throw GamaRuntimeException.error("Connection Faile", scope);
		} 
    
    	return new GamaMatlabEngine(ml);
	}

	@operator(value = "close_matlab_engine")
	@no_test(Reason.IMPOSSIBLE_TO_TEST)	
	public static Boolean closeGamaEngine(final IScope scope, final GamaMatlabEngine gamaEng, final boolean aSync) {
		gamaEng.disconnect(scope,aSync);
		return true;
	}

}
