package spll.localizer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.geotools.feature.SchemaException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import core.metamodel.attribute.Attribute;
import msi.gama.common.interfaces.IValue;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.normalizer.SPLUniformNormalizer;
import spll.localizer.constraint.ISpatialConstraint;
import spll.localizer.distribution.ISpatialDistribution;
import spll.localizer.linker.ISPLinker;

/**
 * This is the main object to localize population. It is the main ressource in Spll process.
 * It contains a <i>must have</i> module and two optional ones:
 * <p>
 * <ul>
 * <li>1) <b>MANDATORY</b>: A geographically referenced population, i.e. {@link SpllPopulation}
 * <li>2) <b>OPTIONAL</b>: A geographical match between a population's entity attribute and geographical entities (denote as <i>match</i>)
 * <li>3) <b>OPTIONAL</b>: A density map (without any match with population) OR a spatial regression setup to estimate one
 * </ul>
 * </p>
 * These three options outline what Spll localization process cover: <br> 
 * (1) localize entity into nest {@link ADemoEntity#getNest()} <br>
 * (2) match entity with the geography {@link ADemoEntity#getLocation()} (if no match, it is equal to the nest) <br> 
 * (3) ancillary information on density (even estimated one using regression techniques) <br>
 * <p>
 * Localizer also provide spatial linking process, which consists in binding entity of the population to places.
 * Usually this places will be school, work place, etc.
 * 
 * @author kevinchapuis
 * @author taillandier patrick
 *
 */
public interface ISPLocalizer {

	// -------------- MAIN CONTRACT -------------- //
	
	/**
	 * The main method to localize a population of entity. Returns a population
	 * of located entity, i.e. SpllEntity 
	 * <p>
	 * Make extensive use of {@link ISpatialConstraint} and {@link ISpatialDistribution}
	 * to localize entity. Hence, most of parametric properties will be made adding constraints
	 * and defining the type of spatial distribution algorithm to be used
	 * 
	 * @param population
	 * @return
	 */
	public void localisePopulation(IScope scope, IList<IAgent> population);
	
	/**
	 * Link entity of a population to a spatial entity using provided linker
	 * 
	 * @param linkedPlaces
	 * @param attribute
	 * @param linker
	 * @return
	 */
	public void linkPopulation(IList<IAgent> population, ISPLinker<IShape> linker, 
			IList<IShape> linkedPlaces, 
			Attribute<? extends IValue> attribute);
	
	////////////////////////////////////////////////
	// -------------- MATCHER PART -------------- //
	//  Matcher part corresponds to the matching  // 
	// 	   phase between population and space     //
	////////////////////////////////////////////////
	
	/**
	 * Setup a "matched geography" between population's entities attribute and a geographical entitites
	 * 
	 * @param match : the file of referenced geographical entities
	 * @param keyAttPop : the population attribute to link to geographical entities
	 * @param keyAttMatch : the geographical feature to link to population
	 */
	public void setMatcher(IList<IShape> match, 
			String keyAttPop, String keyAttMatch);
	
	/**
	 * Add a matcher with options to release the matching constraint
	 * 
	 * @param match : the file of referenced geographical entities
	 * @param keyAttPop : the population attribute to link to geographical entities
	 * @param keyAttMatch : the geographical feature to link to population
	 * @param releaseLimit : the maximum extend to locate synthetic entities abroad from the match
	 * @param releaseStep : the increase step to which released constraint
	 * @param priority : the priority of the matching over other constraint
	 */
	public void setMatcher(IList<IShape> match, 
			String keyAttPop, String keyAttMatch, double releaseLimit, double releaseStep, int priority);
	
	/**
	 * This method must setup matcher variable (i.e. the number of entity) in
	 * the proper output format geofile
	 * 
	 * @return
	 * @throws TransformException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws MismatchedDimensionException 
	 * @throws SchemaException 
	 */
	//public IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> estimateMatcher(File destination) 
	//		throws MismatchedDimensionException, IllegalArgumentException, IOException, TransformException, SchemaException;
	
	
	///////////////////////////////////////////////
	// -------------- MAPPER PART -------------- //
	// 	  Mapper part corresponds to the Areal   // 
	// 	  Interpolation phase of localization    //
	///////////////////////////////////////////////
	
	
	/**
	 * Setup a density map - through external files that define spatial contingency without any 
	 * match with population entities
	 * 
	 * @param entityNbAreas
	 * @param numberProperty
	 */
	public void setMapper(IList<IShape> map, 
			String numberProperty);
	
	/**
	 * Setup a density map - from the result of spatial interpolation: this interpolation
	 * is based on the previous match setup !
	 * <p>
	 * WARNING: will throw a Exception if no match have been set before
	 * 
	 * @param endogeneousVarFile
	 * @param varList
	 * @param lmRegressionOLS
	 * @param splUniformNormalizer
	 * @throws IOException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IllegalRegressionException
	 * @throws IndexOutOfBoundsException
	 * @throws GSMapperException
	 * @throws SchemaException 
	 * @throws InvalidGeoFormatException 
	 * @throws IllegalArgumentException 
	 * @throws MismatchedDimensionException 
	 */
	public void setMapper(IList<IList<IShape>> endogeneousEntities, 
			List<? extends IValue> varList, LMRegressionOLS lmRegressionOLS, SPLUniformNormalizer splUniformNormalizer) 
					throws IOException, TransformException, InterruptedException, ExecutionException, IllegalRegressionException, 
					IndexOutOfBoundsException, GSMapperException, SchemaException, MismatchedDimensionException, IllegalArgumentException;
	
	/**
	 * Setup a density map - from the result of spatial interpolation: this interpolation
	 * is based on a given match file 
	 * 
	 * @param mainMapper
	 * @param mainAttribute
	 * @param endogeneousVarFile
	 * @param varList
	 * @param lmRegressionOLS
	 * @param splUniformNormalizer
	 * @throws IOException
	 * @throws TransformException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IllegalRegressionException
	 * @throws IndexOutOfBoundsException
	 * @throws GSMapperException
	 * @throws SchemaException 
	 * @throws InvalidGeoFormatException 
	 * @throws IllegalArgumentException 
	 * @throws MismatchedDimensionException 
	 */
	public void setMapper(IList<IShape> mainMapper, 
			String mainAttribute, IList<IList<IShape>> endogeneousdata, 
			List<? extends IValue> varList, LMRegressionOLS lmRegressionOLS, SPLUniformNormalizer splUniformNormalizer) 
					throws IOException, TransformException, InterruptedException, ExecutionException, IllegalRegressionException, 
					IndexOutOfBoundsException, GSMapperException, SchemaException, MismatchedDimensionException, IllegalArgumentException;
	
	///////////////////////////////////////////////////
	// -------------- CONSTRAINT PART -------------- //
	// 	  constraint corresponds to variable that    // 
	// 	  shapes final localization step: choose     //
	//	  a nest whitin defined constraints and a    //
	//	  x, y within it							    //
	///////////////////////////////////////////////////
	
	/**
	 * Add a new spatial constraint to this localizer
	 * 
	 * @see ISpatialConstraint
	 * 
	 * @param constraint
	 */
	public void addConstraint(ISpatialConstraint constraint);
	
	/**
	 * Set the constraint all in a row
	 * 
	 * @param constraints
	 */
	public void setConstraints(List<ISpatialConstraint> constraints);
	
	/**
	 * Returns all setted constraints
	 * 
	 * @return
	 */
	public List<ISpatialConstraint> getConstraints();
	
	/////////////////////////////////////////////////////
	// -------------- DISTRIBUTION PART -------------- //
	// 	   distribution encapsulate the type of        //
	//	   algorithm used to localize each entity	  //
	//	   within a given spatial entity (nest)		  //
	/////////////////////////////////////////////////////
	
	/**
	 * Set the spatial distribution to be used in order to draw a nest from a list of candidate
	 * 
	 * @param distribution
	 * @return
	 */
	public void setDistribution(ISpatialDistribution<IShape> distribution);
	
	/**
	 * Get the spatial distribution
	 * 
	 * @return
	 */
	public ISpatialDistribution<IShape> getDistribution();
	
}
