package spll.normalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import core.util.stats.GSBasicStats;
import core.util.stats.GSEnumStats;
import msi.gama.metamodel.shape.IShape;

public class SPLUniformNormalizer extends ASPLNormalizer {
	
	public SPLUniformNormalizer(double floorValue, Number noData) {
		super(floorValue, noData);
	}

	/** 
	 * {@inheritDoc}
	 * <p>
	 * Two step upscale:
	 * <ul>
	 * <li>up values below floor
	 * <li>normalize to fit targeted total output
	 * </ul>
	 * <p>
	 * WARNING: {@code pixelOutput} must be a complete matrix <br>
	 * WARNING: parallel implementation
	 * 
	 */
	@Override
	public double[][] normalize(double[][] matrix, double output) {

		//if((double) new GSBasicStats<>(GSBasicStats.transpose(matrix), 
		//		Arrays.asList(noData.doubleValue())).getStat(GSEnumStats.min)[0] < doubleValue){

			IntStream.range(0, matrix.length).parallel()
				.forEach(col -> IntStream.range(0, matrix[col].length)
					.forEach(row -> matrix[col][row] = normalizedFloor(matrix[col][row]))
			);
			double floorSum = GSBasicStats.transpose(matrix)
					.parallelStream().filter(val -> val == doubleValue)
					.reduce(0d, Double::sum).doubleValue();

			double nonFloorSum = GSBasicStats.transpose(matrix)
					.parallelStream().filter(val -> val > doubleValue && val != noData.floatValue())
					.reduce(0d, Double::sum).doubleValue();
			double normalizer = (output - floorSum) / nonFloorSum;

			IntStream.range(0, matrix.length).parallel()
				.forEach(col -> IntStream.range(0, matrix[col].length)
					.forEach(row -> matrix[col][row] = normalizedFactor(matrix[col][row], normalizer))
			);
		//}
		
		return matrix;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: {@code pixelOutput} must be a complete matrix <br>
	 * WARNING: parallel implementation
	 * 
	 */
	@Override
	public double[][] round(double[][] matrix, double output){
		// normalize to int: values are rounded to feet most proximal integer & overload are summed
		
		IntStream.range(0, matrix.length).parallel()
			.forEach(col -> IntStream.range(0, matrix[col].length)
				.forEach(row -> matrix[col][row] = normalizedToInt(matrix[col][row]))
		);
		
		double errorload = output - new GSBasicStats<>(GSBasicStats.transpose(matrix), 
				Arrays.asList(noData.doubleValue())).getStat(GSEnumStats.sum)[0];
		
		
		// uniformally spread errorload (can visit pixel multiple time)
		int iter = 0;
		while(Math.round(errorload) != 0 || iter++ > ITER_LIMIT + Math.abs(errorload)){
			int intX, intY;
			double currentVal;
			int update = errorload < 0 ? -1 : 1;
			do {
				intX = super.random.nextInt(matrix.length);
				intY = super.random.nextInt(matrix[intX].length);
				currentVal = matrix[intX][intY];
			} while(currentVal == noData.doubleValue()
					|| currentVal == doubleValue && errorload < 0);
			matrix[intX][intY] = currentVal + update;
			errorload -= update; 
		}
		
		return matrix;
	}

	/**
	 * 
	 */
	@Override
	public Map<IShape, Double> normalize(Map<IShape, Double> featureOutput, double output) {

		// Two step upscale: (1) up values below floor (2) normalize to fit targeted total output
		if(featureOutput.values()
				.parallelStream().min((v1, v2) -> v1.compareTo(v2)).get() < doubleValue){
			featureOutput.keySet().parallelStream()
			.forEach(feature -> featureOutput.put(feature, 
					normalizedFloor(featureOutput.get(feature))));
			
			double floorSum = featureOutput.values()
					.parallelStream().filter(val -> val == doubleValue)
					.reduce(0d, Double::sum).doubleValue();
			double nonFloorSum = featureOutput.values()
					.parallelStream().filter(val -> val > doubleValue && val != noData.doubleValue())
					.reduce(0d, Double::sum).doubleValue();
			double normalizer = (output - floorSum) / nonFloorSum;

			featureOutput.keySet().parallelStream()
				.forEach(feature -> featureOutput.put(feature, normalizedFactor(featureOutput.get(feature), normalizer)));

		}

		return featureOutput;
	}
	
	/**
	 * 
	 */
	@Override
	public Map<IShape, Integer> round(Map<IShape, Double> featureOutput, double output) {
		Map<IShape, Integer> featOut = new HashMap<>();
		// summed residue is spread to all non floor value
		featureOutput.keySet().parallelStream()
			.forEach(feature -> featOut.put(feature, (int) normalizedToInt(featureOutput.get(feature))));

		double errorload = output - new GSBasicStats<>(new ArrayList<>(featOut.values()), 
				Arrays.asList(noData.doubleValue())).getStat(GSEnumStats.sum)[0];
		
		// uniformally spread overload (can visit pixel multiple time)
		int iter = 0;
		List<IShape> feats = featureOutput.entrySet()
				.parallelStream().filter(e -> e.getValue() != noData.doubleValue())
				.map(e -> e.getKey())
				.toList();
		while(Math.round(errorload) != 0 || iter++ > ITER_LIMIT + Math.abs(errorload)){
			IShape feat = feats.get(super.random.nextInt(feats.size()));
			int update = errorload < 0 ? -1 : 1;
			featureOutput.put(feat, featureOutput.get(feat) + update);
			errorload -= update;
		}
		return featOut;
	} 

	// ---------------------- inner utility ---------------------- //

	private double normalizedFloor(double value) {
		if(value < doubleValue && value != noData.doubleValue())
			return (double) doubleValue;
		return value;
	}


	private double normalizedFactor(double value, double factor){
		if(value > doubleValue && value != noData.doubleValue())
			return value * factor;
		return value;
	}

	
	private double normalizedToInt(double value){
		if(value == noData.doubleValue())
			return value;
		double newValue = Math.round(value); 
		if(newValue < doubleValue)
			newValue = (int) value + 1;
		return newValue;
	}

}
