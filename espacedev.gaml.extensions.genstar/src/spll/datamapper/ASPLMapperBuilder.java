package spll.datamapper;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gama.util.matrix.IField;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.matcher.ISPLMatcherFactory;
import spll.datamapper.normalizer.ASPLNormalizer;

/**
 * The mapper is the main concept of SPLL algorithm. It matches main geographical features
 * contained in a shape file to various geographical variables (e.g. other features, satellite image).
 * It also setup regression algorithm to compute the relationship between an attribute of main features
 * (dependent variable) and ancillary geographical variable (explanatory variables). Last, it encapsulate
 * the method to fit regression output to a proper contract (e.g. integer values, adjusted output sum).
 * 
 * @author kevinchapuis
 *
 * @param <V>
 * @param <T>
 */
public abstract class ASPLMapperBuilder<V, T> {
	
	protected final IList<IShape> mainEntities;
	private final String mainAttribute;
	
	protected IList<GamaField> ancillaryFields;
	protected ISPLMatcherFactory<V, T> matcherFactory;
	
	protected ISPLRegressionAlgo<V, T> regressionAlgorithm;
	
	protected ASPLNormalizer normalizer;
	
	public ASPLMapperBuilder(IList<IShape> mainEntities, String mainAttribute, 
			IList<GamaField> ancillaryFields) {
		this.mainEntities = mainEntities;
		this.mainAttribute = mainAttribute; 
		this.ancillaryFields = ancillaryFields;
	}
	////////////////////////////////////////////////////////////////
	// ------------------------- SETTERS ------------------------ //
	////////////////////////////////////////////////////////////////
	
	
	/**
	 * Setup the regression algorithm
	 * 
	 * @param regressionAlgorithm
	 */
	public void setRegressionAlgorithm(ISPLRegressionAlgo<V, T> regressionAlgorithm){
		this.regressionAlgorithm = regressionAlgorithm;
	}
	
	public IList<IShape> getMainEntities() {
		return mainEntities;
	}


	public IList<GamaField> getAncillaryFields() {
		return ancillaryFields;
	}


	/**
	 * Setup the matcher factory, i.e. the object whose responsible for variable matching
	 * 
	 * @param matcherFactory
	 */
	public void setMatcherFactory(ISPLMatcherFactory<V, T> matcherFactory){
		this.matcherFactory = matcherFactory;
	}
	
	/**
	 * Setup the object that will ensure output value format to fit built-in normalizer requirements
	 * 
	 * @param normalizer 
	 */
	public void setNormalizer(ASPLNormalizer normalizer){
		this.normalizer = normalizer;
	}
	
	///////////////////////////////////////////////////////////////
	// ------------------------ GETTERS ------------------------ //
	///////////////////////////////////////////////////////////////
	
	
	public String getMainAttribute(){
		return mainAttribute;
	}
	
	/////////////////////////////////////////////////////////////////
	// ---------------------- main contract ---------------------- // 
	/////////////////////////////////////////////////////////////////
	
	/**
	 * This method match all ancillary files with the main shape file. More precisely,
	 * all geographic variables ancillary files contain will be bind to corresponding feature
	 * of the main file. Each {@link ASPLMapperBuilder} has its own definition of how
	 * feature and geographical variable should match to one another (e.g. within, intersect)
	 * 
	 * @return
	 * @throws IOException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public abstract SPLMapper<V, T> buildMapper(IScope scope) throws IOException, TransformException, InterruptedException, ExecutionException;
	
	protected abstract float[][] buildOutput(final IScope scope, final GamaField outputFormat, final boolean intersect, final boolean integer,
			final Number targetPop) throws IllegalRegressionException, IOException ;

}
