package spll.datamapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gaml.operators.Cast;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.matcher.ISPLMatcher;
import spll.datamapper.matcher.ISPLMatcherFactory;

/**
 * TODO: force <T> generic to fit a regression style contract: either boolean (variable is present or not) 
 * or numeric (variable has a certain amount)
 * <p>
 * This object purpose is to setup a regression between a variable contains in a main geographic file
 * with variables contains in one or more ancillary geographic files. The mapping is based on a shared
 * geographical referent space: all file must use the same {@link CoordinateReferenceSystem} and overlap
 * -- at least only overlapped places will be processed.
 * <p> 
 * This object should be created using any {@link ASPLMapperBuilder}
 * 
 * @author kevinchapuis
 *
 * @param <F>
 * @param <Variable>
 * @param <T>
 */
public class SPLMapper<V, T> {

	private ISPLRegressionAlgo<V, T> regFunction;
	private boolean setupReg;
	

	private IList<IShape> mainSPLData;
	private String targetProp;

	private Set<ISPLMatcher<V, T>> mapper = new HashSet<>();
	
	private ISPLMatcherFactory<V, T> matcherFactory;

	// --------------------- Constructor --------------------- //

	protected SPLMapper() { }

	// --------------------- Modifier --------------------- //

	protected void setRegAlgo(ISPLRegressionAlgo<V, T> regressionAlgorithm) {
		this.regFunction = regressionAlgorithm;
	}

	protected void setMatcherFactory(ISPLMatcherFactory<V, T> matcherFactory){
		this.matcherFactory = matcherFactory;
	}

	protected void setMainSPLData(IList<IShape> mainSPLData){
		this.mainSPLData = mainSPLData;
	}

	protected void setMainProperty(String propertyName){
		this.targetProp = propertyName;
	}

	protected boolean insertMatchedVariable(IScope scope, GamaField regressorsFiles) 
			throws IOException, TransformException, InterruptedException, ExecutionException{
		boolean result = true;
		for(ISPLMatcher<V, T> matchedVariable : matcherFactory
				.getMatchers(scope, mainSPLData, regressorsFiles))
			if(!insertMatchedVariable(matchedVariable) && result)
				result = false;
		return result;
	}

	protected boolean insertMatchedVariable(ISPLMatcher<V, T> matchedVariable) {
		return mapper.add(matchedVariable);
	}



	// ------------------- Main Contract ------------------- //

	/**
	 * Gives the intercept of the regression
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 * @throws IOException 
	 */
	public double getIntercept(IScope scope) throws IllegalRegressionException, IOException {
		this.setupRegression(scope);
		return regFunction.getIntercept();
	}
	
	/**
	 * Operate regression given the data that have been setup for this mapper
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 * @throws IOException 
	 */
	public Map<V, Double> getRegression(IScope scope) throws IllegalRegressionException, IOException {
		this.setupRegression(scope);
		return regFunction.getRegressionParameter();
	}
	
	/**
	 * 
	 * TODO javadoc
	 * 
	 * @return
	 * @throws IllegalRegressionException
	 * @throws IOException 
	 */
	public Map<IShape, Double> getResidual(IScope scope) throws IllegalRegressionException, IOException {
		this.setupRegression(scope);
		return regFunction.getResidual(); 
	}

	// ------------------- Inner utilities ------------------- //
	
	private void setupRegression(IScope scope) throws IllegalRegressionException, IOException{
		if(mapper.stream().anyMatch(var -> !var.getEntity().getOrCreateAttributes().containsKey(this.targetProp)))
			throw new IllegalRegressionException("Property "+this.targetProp+" is not present in each Feature of the main SPLMapper");
		if(!setupReg){
			regFunction.setupData(mainSPLData.stream().collect(Collectors.toMap(feat -> feat, 
					feat -> Cast.asFloat(scope, feat.getAttribute(this.targetProp)))), mapper);
			setupReg = true; 
		}
	}
	
}
