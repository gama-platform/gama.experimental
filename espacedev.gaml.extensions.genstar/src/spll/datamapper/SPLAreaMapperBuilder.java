/*******************************************************************************************************
 *
 * SPLAreaMapperBuilder.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package spll.datamapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.opengis.referencing.operation.TransformException;

import core.util.GSPerformanceUtil;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gama.util.matrix.IField;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.Queries;
import msi.gaml.types.Types;
import spll.algo.ISPLRegressionAlgo;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.matcher.SPLAreaMatcherFactory;
import spll.datamapper.normalizer.ASPLNormalizer;
import spll.datamapper.normalizer.SPLUniformNormalizer;

/**
 * TODO: javadoc
 *
 * @author kevinchapuis
 *
 */
public class SPLAreaMapperBuilder extends ASPLMapperBuilder<String, Double> {

	/** The mapper. */
	private SPLMapper<String, Double> mapper;

	/**
	 * Simplest constructor that define default regression and normalizer algorithm
	 *
	 * @param mainFile
	 * @param ancillaryFiles
	 * @param variables
	 */
	
	
	public SPLAreaMapperBuilder(final IScope scope, final IList<IShape> mainEntities,
			final String mainAttribute,
			final IList<GamaField> ancillaryFields) {
		this(scope, mainEntities, mainAttribute, ancillaryFields, new LMRegressionOLS());
	} 

	/**
	 * Constructor that enable custom regression algorithm
	 *
	 * @param mainFile
	 * @param ancillaryFiles
	 * @param variables
	 * @param regAlgo
	 */
	public SPLAreaMapperBuilder(final IScope scope, final IList<IShape> mainEntities,
			final String mainAttribute,
			final IList<GamaField> ancillaryFields,final ISPLRegressionAlgo<String, Double> regAlgo) {
		this(scope, mainEntities, mainAttribute, ancillaryFields,  regAlgo,
				new SPLUniformNormalizer(0, ancillaryFields.isEmpty() ? null : ancillaryFields.get(0).getNoData(scope)));
	}

	/**
	 * Constructor that enable full custom density estimation process
	 *
	 * @param mainFile
	 * @param ancillaryFiles
	 * @param variables
	 * @param regAlgo
	 * @param normalizer
	 */
	public SPLAreaMapperBuilder(final IScope scope, final IList<IShape> mainEntities,
			final String mainAttribute,
			final IList<GamaField> ancillaryFields, final ISPLRegressionAlgo<String, Double> regAlgo,
			final ASPLNormalizer normalizer) {
		super( mainEntities, mainAttribute, ancillaryFields);
		super.setRegressionAlgorithm(regAlgo);
		super.setMatcherFactory(new SPLAreaMatcherFactory());
		super.setNormalizer(normalizer);
	}

	/////////////////////////////////////////////////////
	// :::::::::::::::: MAIN CONTRACT :::::::::::::::: //
	/////////////////////////////////////////////////////

	@Override
	public SPLMapper<String, Double> buildMapper(IScope scope)
			throws IOException, TransformException, InterruptedException, ExecutionException {
		if (mapper == null) {
			mapper = new SPLMapper<>();
			mapper.setMainSPLData(mainEntities);
			mapper.setMainProperty(super.getMainAttribute());
			mapper.setRegAlgo(regressionAlgorithm);
		 	mapper.setMatcherFactory(matcherFactory);
			for (GamaField field : ancillaryFields) {
				mapper.insertMatchedVariable(scope, field);
			}
		}
		return mapper;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * WARNING: for performance purpose parallel stream are used !
	 * @throws IllegalRegressionException 
	 *
	 * @throws GSMapperException
	 * @throws IOException
	 *
	 */
	@Override
	protected float[][] buildOutput(final IScope scope, final GamaField outputFormat, final boolean intersect, final boolean integer,
			final Number targetPop) throws IllegalRegressionException, IOException { 
		if (mapper == null)
			throw new IllegalAccessError("Cannot create output before a SPLMapper has been built and regression done");
		if (!ancillaryFields.contains(outputFormat)) throw new IllegalArgumentException(
				"output format field must be one of ancillary field use to proceed regression");

		GSPerformanceUtil gspu = new GSPerformanceUtil("Start processing regression data to output raster");

		// Define output format
		int rows = outputFormat.getRows(null);
		int columns = outputFormat.getCols(scope);
		float[][] pixels = new float[columns][rows];

		// Store regression result
		Map<String, Double> regCoef = mapper.getRegression(scope);
		double intercept = mapper.getIntercept(scope);

		// Correction for each pixel (does not exclude noData pixels)
		Map<IShape, Double> pixCorrection = mapper.getResidual(scope).entrySet().stream()
				.collect(Collectors.toMap(k-> k.getKey(), e -> (outputFormat.get(scope, ((IShape) e).getLocation()) + intercept)
						/ outputFormat.getValuesIntersecting(scope, (IShape) e).size()));

		if (pixCorrection.values().stream().anyMatch(value -> value.isInfinite() || value.isNaN()))
			GAMA.reportError(scope, GamaRuntimeException.error("The ouput format field does not cover all geographical entity !\n"
					, scope), true);
		// Define utilities
		Collection<GamaField> ancillaries =
				new ArrayList<>(super.ancillaryFields);
		ancillaries.remove(outputFormat);

		// Iterate over pixels to apply regression coefficient
		IntStream.range(0, columns).parallel().forEach(
				x -> IntStream.range(0, rows).forEach(y -> pixels[x][y] = (float) this.computePixelWithinOutput(scope, x, y,
						outputFormat, ancillaries, super.mainEntities, regCoef, pixCorrection, gspu, intersect)));

		// debug purpose
		pixelRendered = 0;

	//	super.normalizer.process(pixels, targetPop.floatValue(), integer);

		return pixels;
	}

	
	/////////////////////////////////////////////////////////////////////////////
	// --------------------------- INNER UTILITIES --------------------------- //
	/////////////////////////////////////////////////////////////////////////////

	/** The pixel rendered. */
	// INNER UTILITY PIXEL PROCESS COUNT
	private static int pixelRendered = 0;

	/**
	 * Compute pixel within output.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param geotiff
	 *            the geotiff
	 * @param ancillaries
	 *            the ancillaries
	 * @param mainFeatures
	 *            the main features
	 * @param regCoef
	 *            the reg coef
	 * @param pixResidual
	 *            the pix residual
	 * @param gspu
	 *            the gspu
	 * @param intersect
	 *            the intersect
	 * @return the double
	 */
	@SuppressWarnings ("null")
	private double computePixelWithinOutput(final IScope scope, final int x, final int y, final GamaField geofield,
			final Collection<GamaField> ancillaries,
			final IList<IShape> mainFeatures,
			final Map<String, Double> regCoef, final Map<IShape, Double> pixResidual,
			final GSPerformanceUtil gspu, final boolean intersect) {
		// Output progression
		int prop10for100 = Math.round(Math.round(geofield.numRows * geofield.numCols * 0.1d));
		if ((++pixelRendered + 1) % prop10for100 == 0) {
			gspu.sysoStempPerformance((pixelRendered + 1) / (prop10for100 * 10.0), this);
		}

		// Get the current pixel value
		IShape refPixel = null;
		refPixel = geofield.getCellShapeAt(scope, x,y);
		
		// Get the related feature in main space features
		GamaPoint pixelLocation = refPixel.getLocation();
		Optional<IShape> opFeature =
				mainFeatures.stream().filter(ft -> pixelLocation.intersects(ft)).findFirst();

		if (!opFeature.isPresent()) return geofield.getNoData(scope);
		if (intersect)
			return computePixelIntersectOutput(scope, refPixel, geofield, ancillaries, mainFeatures, regCoef, pixResidual);
		
		
		return computePixelWithin(scope, refPixel, geofield, ancillaries, opFeature.get(), regCoef,
				pixResidual.get(opFeature.get()));
	}

	// --------------------------- INNER ALGORITHM --------------------------- //

	/**
	 * Compute pixel within.
	 *
	 * @param refPixel
	 *            the ref pixel
	 * @param geotiff
	 *            the geotiff
	 * @param ancillaries
	 *            the ancillaries
	 * @param entity
	 *            the entity
	 * @param regCoef
	 *            the reg coef
	 * @param corCoef
	 *            the cor coef
	 * @return the double
	 */
	/*
	 * WARNING: the within function used define inclusion as: centroide of {@code refPixel} geometry is within the
	 * referent geometry
	 */
	private double computePixelWithin(final IScope scope, final IShape refPixel, final GamaField geoField,
			final Collection<GamaField> ancillaries,
			final IShape entity, final Map<String, Double> regCoef, final double corCoef) {

		// Retain info about pixel and his context
		Collection<Double> pixData = (Collection<Double>) geoField.getBands(scope).stream().map(f -> f.get(scope, refPixel.getLocation()));
		if (pixData.stream().allMatch(val ->val == geoField.getNoData(scope))
				&& ancillaries.isEmpty())
			return geoField.getNoData(scope);
		double pixArea = refPixel.getArea();

		// Setup output value for the pixel based on pixels' band values
		double output = pixData.stream().mapToDouble(Double::doubleValue).sum() * pixArea;

		// Iterate over other explanatory variables to update pixels value
		for (GamaField otherExplanVarField : ancillaries) {
			Iterator<IShape> otherItt =
					otherExplanVarField.getCellsIntersecting(scope, refPixel).iterator();
			while (otherItt.hasNext()) {
				IShape other = otherItt.next();
				Set<String> otherValues = regCoef.keySet().stream()
						.filter(var -> other.hasAttribute(var)).collect(Collectors.toSet());
				output += otherValues.stream().mapToDouble(val -> regCoef.get(val) * pixArea).sum();
			}
		}

		return output + corCoef;
	}

	/**
	 * Compute pixel intersect output.
	 *
	 * @param refPixel
	 *            the ref pixel
	 * @param geotiff
	 *            the geotiff
	 * @param ancillaries
	 *            the ancillaries
	 * @param mainFeatures
	 *            the main features
	 * @param regCoef
	 *            the reg coef
	 * @param pixResidual
	 *            the pix residual
	 * @return the double
	 */
	/*
	 * WARNING: intersection area calculation is very computation demanding, so this method is pretty slow
	 */
	private double computePixelIntersectOutput(IScope scope, IShape refPixel, GamaField geoField, 
			Collection<GamaField> ancillaries,
			IList<IShape> mainFeatures, 
			Map<String, Double> regCoef, Map<IShape, Double> pixResidual) {

		// Retain main feature the pixel is within
		IList<IShape> feats = (IList<IShape>) Queries.overlapping(scope, mainFeatures, refPixel).listValue(scope, Types.GEOMETRY, false);
		if (feats.isEmpty()) return geoField.getNoData(scope);

		// Get the values contain in the pixel bands
		Collection<Double> pixData = (Collection<Double>) geoField.getBands(scope).stream().map(f -> f.get(scope, refPixel.getLocation()));
		double pixArea = refPixel.getArea();
		// Setup output value for the pixel based on pixels' band values
		double output = pixData.stream().mapToDouble(Double::doubleValue).sum() * pixArea;

		// Iterate over other explanatory variables to update pixels value
		for (IField otherExplanVarFile : ancillaries) {
			Iterator<IShape> otherItt =
					otherExplanVarFile.getCellsIntersecting(scope, refPixel).iterator();
			while (otherItt.hasNext()) {
				IShape other = otherItt.next();
				Set<String> otherValues = regCoef.keySet().stream()
						.filter(var -> other.hasAttribute(var)).collect(Collectors.toSet());
				output += otherValues.stream()
						.mapToDouble(val -> regCoef.get(val) * Operators.inter(scope, other, refPixel).getArea())
						.sum();
			}
		}

		// Compute corrected value based on output data (to balanced for unknown determinant information)
		// Intersection correction try /catch clause come from GAMA
		double correctedOutput = 0d;
		for (IShape entity : feats) {
			IShape intersectGeom = Operators.inter(scope, entity, refPixel);
			
			correctedOutput +=
					Math.round(output * intersectGeom.getArea() / refPixel.getArea() + pixResidual.get(entity));
		}
		return correctedOutput;
	}

}
