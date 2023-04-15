package spll.algo;

import java.util.Map;
import java.util.Set;

import msi.gama.metamodel.shape.IShape;
import spll.datamapper.matcher.ISPLMatcher;
import spll.datamapper.variable.ISPLVariable;

/**
 * Encapsulate spatial regression algorithm
 * @author kevinchapuis
 *
 * @param <V>
 * @param <T>
 */
public interface ISPLRegressionAlgo<V extends ISPLVariable, T> {
	
	/**
	 * Retrieve regression parameter for each variable
	 * @return
	 */
	public Map<V, Double> getRegressionParameter();
	
	public Map<IShape, Double> getResidual();
	
	public double getIntercept();
	
	public void setupData(Map<IShape, Double> observations,
			Set<ISPLMatcher<V, T>> regressors);
	
}
