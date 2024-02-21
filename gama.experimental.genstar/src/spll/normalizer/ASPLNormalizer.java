package spll.normalizer;

import java.util.Map;
import java.util.Random;

import core.util.random.GenstarRandom;
import gama.core.metamodel.shape.IShape;

/**
 * TODO: make top value control, not just floor value 
 * 
 * @author kevinchapuis
 *
 */
public abstract class ASPLNormalizer {
	
	protected static final int ITER_LIMIT = 1000000;
	protected static final double EPSILON = 0.001;
	public static boolean LOGSYSO = true;
	
	protected double doubleValue;
	
	protected Number noData;
	
	protected final Random random = GenstarRandom.getInstance();
	
	/**
	 * TODO: javadoc
	 * 
	 * @param floorValue
	 * @param noData
	 */
	public ASPLNormalizer(double floorValue, Number noData){
		this.doubleValue = floorValue;
		this.noData = noData;
	}
	
	/**
	 * Compound method to fit matrix requirement, i.e. floor value, integer value and sum output
	 * 
	 * @param matrix
	 * @param output
	 * @param integer
	 * @return
	 */
	public double[][] process(double[][] matrix, float output, boolean integer){
		this.normalize(matrix, output);
		if(integer)
			this.round(matrix, output);
		return matrix;
	}
	
	/**
	 * Compound method to fit map requirement, i.e. floor value, integer value and sum output
	 * 
	 * @param featureOutput
	 * @param output
	 * @param integer
	 * @return
	 */
	public Map<IShape, ? extends Number> process(Map<IShape, Double> featureOutput, double output, boolean integer){
		Map<IShape, Double> outputMap = this.normalize(featureOutput, output);
		if(integer)
			return this.round(featureOutput, output);
		return outputMap;
	}
	
	/**
	 * Normalize the content of a pixel format spll output <br>
	 * HINT: {@code float} type is forced by Geotools implementation of raster file
	 * 
	 * 
	 * @param matrix
	 * @param output
	 * @return
	 */
	public abstract double[][] normalize(double[][] matrix, double output);
	
	/**
	 * Round the value of pixels to fit integer value (stay in float format)
	 * 
	 * @param matrix
	 * @param output
	 * @return
	 */
	public abstract double[][] round(double[][] matrix, double output);
	
	/**
	 * TODO
	 * 
	 * @param featureOutput
	 * @param output
	 * @return
	 */
	public abstract Map<IShape, Double> normalize(Map<IShape, Double> featureOutput, double output);
	
	/**
	 * Round double values to integer and control sum to fit required output
	 * 
	 * @param featureOutput
	 * @param output
	 * @return
	 */
	public abstract Map<IShape, Integer> round(Map<IShape, Double> featureOutput, double output);
	
	// ------------------ shared utility ------------------ //
	
	protected boolean equalEpsilon(double value, double target) {
		return Math.abs(value - target) < EPSILON ? true : false;
	}

	
	
}
