package gaml.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import gaml.types.GamaMatlabEngine;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.no_test;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.IList;
import msi.gaml.types.GamaListType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;


public class Operators {
//	@operator(value = "test_matlab")
//	@doc(value = "crea", examples = {
//			@example(value = "as_driving_graph(road,node)  --:  build a graph while using the road agents as edges and the node agents as nodes", isExecutable = false) }, 
//	see = {"as_intersection_graph", "as_distance_graph", "as_edge_graph" })
//	@no_test
	public static List<Double> test(final IScope scope, final String n) {
		try {
	        //Start MATLAB asynchronously
	        Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
	
	     // Get engine instance from the future result
	        MatlabEngine ml = eng.get();
	
	    /*
	     * Find elements greater than 5
	     * 1. Create input matrix
	     * 2. Put variable matrix in the MATLAB base workspace
	     * 3. Solve A(A>5) in MATLAB
	     * 4. Return results and display
	     * 5. Call the power function in MATLAB on the returned matrix
	     * 6. Display results
	     */
	        double[][] input = new double[4][4];
	        for (int i = 0; i < 4; i++) {
	            for (int j = 0; j < 4; j++) {
	                double num = Math.random() * 10;
	                input[i][j] = num;
	            }
	        }
	        System.out.println("\nFind numbers from a matrix that are greater than five and square them:\n");
	        System.out.println("Input matrix: ");
	        for (int i = 0; i < 4; i++) {
	            {
	                for (int j = 0; j < 4; j++) {
	                    System.out.print(String.format("%.2f", input[i][j]) + "\t");
	                }
	                System.out.print("\n");
	            }
	        }
	
	        // Put the matrix in the MATLAB workspace
	        ml.putVariableAsync("A", input);
	
	        // Evaluate the command to search in MATLAB
	        ml.eval("B=A(A>5);");
	
	        // Get result from the workspace
	        Future<double[]> futureEval = ml.getVariableAsync("B");
	        double[] output = futureEval.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        List<Double> l = new ArrayList<Double>();
        return l;
		
	}

	@operator(value = "test_matlab")
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
	public static IList eval(final IScope scope, final GamaMatlabEngine gamaEng, final String exp, final String variableName) {
		 double[] output;		
        // Evaluate the command to search in MATLAB
		try {
			gamaEng.getEngine().eval(exp);
	        // Get result from the workspace
		//	Object eval = gamaEng.getEngine().get 
			
			
	        Future<double[]> futureEval = gamaEng.getEngine().getVariableAsync(variableName);
	        output = futureEval.get();			
		} catch (CancellationException | InterruptedException | ExecutionException e) {
		    throw GamaRuntimeException.error("MATLAB evaluation failed", scope);
		}

		return GamaListType.staticCast(scope, output, Types.FLOAT, true);
	}
	
	
	@operator(value = "get_matlab_engine")
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
	public static Boolean closeGamaEngine(final IScope scope, final GamaMatlabEngine gamaEng, final boolean aSync) {
		gamaEng.disconnect(scope,aSync);
		return true;
	}

	
     // Get engine instance from the future result
     //   MatlabEngine ml = eng.
  
/*        
         * Find elements greater than 5
         * 1. Create input matrix
         * 2. Put variable matrix in the MATLAB base workspace
         * 3. Solve A(A>5) in MATLAB
         * 4. Return results and display
         * 5. Call the power function in MATLAB on the returned matrix
         * 6. Display results
         * /
            double[][] input = new double[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    double num = Math.random() * 10;
                    input[i][j] = num;
                }
            }
            System.out.println("\nFind numbers from a matrix that are greater than five and square them:\n");
            System.out.println("Input matrix: ");
            for (int i = 0; i < 4; i++) {
                {
                    for (int j = 0; j < 4; j++) {
                        System.out.print(String.format("%.2f", input[i][j]) + "\t");
                    }
                    System.out.print("\n");
                }
            }

            // Put the matrix in the MATLAB workspace
            ml.putVariableAsync("A", input);

            // Evaluate the command to search in MATLAB
            ml.eval("B=A(A>5);");

            // Get result from the workspace
            Future<double[]> futureEval = ml.getVariableAsync("B");
            double[] output = futureEval.get();

            // Display result
            System.out.println("\nElements greater than 5: ");
            for (int i = 0; i < output.length; i++) {
                System.out.print(" " + String.format("%.2f", output[i]));
            }

            // Square the returned elements using the power function in MATLAB
            double[] powResult = ml.feval("power", output, Double.valueOf(2));
            System.out.println("\n\nSquare of numbers greater than 5:");
            for (int i = 0; i < powResult.length; i++) {

                //Set precision for the output values
                System.out.print(" " + String.format("%.2f", powResult[i]));
            }
            System.out.println("\n");        
   */     

	


    public static void main(String args[]) {
    	
    	
       	List l = Operators.test(null, "hello");
    }	
	
}
