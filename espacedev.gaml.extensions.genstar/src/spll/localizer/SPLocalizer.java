/*******************************************************************************************************
 *
 * SPLocalizer.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package spll.localizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opengis.referencing.operation.TransformException;

import core.metamodel.attribute.Attribute;
import core.util.GSPerformanceUtil;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gaml.operators.Cast;
import msi.gaml.operators.Containers;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.Queries;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.types.Types;
import spll.localizer.constraint.ISpatialConstraint;
import spll.localizer.constraint.SpatialConstraintLocalization;
import spll.localizer.distribution.ISpatialDistribution;
import spll.localizer.distribution.SpatialDistributionFactory;
import spll.localizer.linker.ISPLinker;
import spll.localizer.linker.SPLinker;
import spll.localizer.pointInalgo.PointInLocalizer;
import spll.localizer.pointInalgo.RandomPointInLocalizer;

/**
 * The Class SPLocalizer.
 */
public class SPLocalizer implements ISPLocalizer {

	/** The gspu. */
	/*
	 * Performance purpose logger
	 */
	protected GSPerformanceUtil gspu;

	/** The match. */
	// main referenced area for placing the agents (e.g. Iris)
	protected IList<IShape> match;

	/** The map. */
	// gives the number of entities per area (e.g. regression cells)
	protected IList<IShape> map;
	
	protected GamaField mapField;

		/** The linker. */
	protected ISPLinker<IShape> linker; // Encapsulate spatial distribution and constraints to link entity and

	/** The localization constraint. */
	// spatial object
	protected SpatialConstraintLocalization localizationConstraint; // the localization constraint;

	/** The point in localizer. */
	protected PointInLocalizer pointInLocalizer; // allows to return one or several points in a geometry

	/** The key att map. */
	protected String keyAttMap; // name of the attribute that contains the number of entities in the map file

	/** The key att pop. */
	protected String keyAttPop; // name of the attribute that is used to store the id of the referenced area in the

	/** The key att match. */
	// population
	protected String keyAttMatch; // name of the attribute that is used to store the id of the referenced area in the
									// match file

	protected String nestAttribute;
	
	protected Double minDist = null;
	
	protected Double maxDist = null;
	/**
	 * Private constructor to setup random engine
	 */
	private SPLocalizer(IScope scope) {
		this.pointInLocalizer = new RandomPointInLocalizer();
		this.linker = new SPLinker(SpatialDistributionFactory.getInstance().getUniformDistribution());
	}

	/**
	 * Build a localizer based on a geographically grounded population
	 *
	 * @param population
	 */
	public SPLocalizer(final IScope scope, IContainer<?, ? extends IShape> geoms, String nestAttribute) {
		this(scope);
		this.localizationConstraint = new SpatialConstraintLocalization(null);
		this.localizationConstraint.setGeoms(geoms);
		this.linker.addConstraints(localizationConstraint);
		this.nestAttribute = nestAttribute;
	}

	///////////////////////////////////////////////////////////
	// ------------------- MAIN CONTRACT ------------------- //
	///////////////////////////////////////////////////////////

	@Override
	public void localisePopulation(IScope scope, final IContainer<?, IAgent> population) {
		try {
			// case where the referenced file is not defined
			if (match == null) {
				
				// case where there is no information about the number of entities in specific spatial areas
				if ((keyAttMap == null || map == null) && mapField == null)  {
					localizationInNest(scope,population, null);
				}
				// case where we have information about the number of entities per specific areas (entityNbAreas)
				else {
					localizationInNestWithNumbers(scope, population, null);
				}
			}
			// case where the referenced file is defined
			else {
				for (IShape globalfeature : match) {
					String valKeyAtt = Cast.asString(scope, globalfeature.getAttribute(keyAttMatch));

					IList<IAgent> entities = GamaListFactory.createWithoutCasting(Types.AGENT,population.listValue(scope, Types.AGENT, false).stream()
							.filter(s -> s.getAttribute(keyAttPop).equals(valKeyAtt)).toList());
					if ((keyAttMap == null || map == null) && mapField == null) {
						localizationInNest(scope, entities, globalfeature);
					} else {
						localizationInNestWithNumbers(scope, entities, globalfeature);
					}

				}
			}
			//outputPopulation.removeIf(a -> a.getLocation() == null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/*@Override
	public void linkPopulation(IList<IAgent> population, ISPLinker<IShape> linker, 
			IList<IShape> linkedPlaces, 
			Attribute<? extends IValue> attribute) {
	}*/
		//population.forEach(entity -> entity.addLinkedPlaces(attribute.getAttributeName(),
		//		linker.getCandidate(entity, linkedPlaces).orElseGet(null)));
		
	

	// ----------------------------------------------------- //
	// ---------------------- MATCHER ---------------------- //
	// ----------------------------------------------------- //

	@Override
	public void setMatcher(IList<IShape> match, 
			String keyAttPop, String keyAttMatch) {
	/*	if (!match.isCoordinateCompliant(localizationConstraint.getReferenceFile())) throw new IllegalArgumentException(
				"The Coordinate Referent System of matcher does not fit population's geography:\n" + "Match = "
						+ match.getWKTCoordinateReferentSystem() + "\n" + "Geography = "
						+ localizationConstraint.getReferenceFile().getWKTCoordinateReferentSystem());

		if (match.getGeoAttributes().stream().noneMatch(att -> att.getAttributeName().equals(keyAttMatch)))
			throw new IllegalArgumentException("The match file does not contain any attribute named " + keyAttMatch
					+ "while this name has been setup to be the key attribute match");
		if (population.getPopulationAttributes().stream().noneMatch(att -> att.getAttributeName().equals(keyAttPop)))
			throw new IllegalArgumentException("The population does not contains any attribute named " + keyAttPop
					+ " while this name has been setup to be the key attribute population");*/
		this.match = match;
		this.keyAttPop = keyAttPop;
		this.keyAttMatch = keyAttMatch;
	}

	@Override
	public void setMatcher(IList<IShape> match, 
			String keyAttPop, String keyAttMatch, double releaseLimit, double releaseStep, int priority) {
			this.setMatcher(match, keyAttPop, keyAttMatch);
		this.localizationConstraint.setMaxIncrease(releaseLimit);
		this.localizationConstraint.setIncreaseStep(releaseStep);
		this.localizationConstraint.setPriority(priority);
	}
	
	

	
	// ----------------------------------------------------- //
	// ----------------------- MAPPER ---------------------- //
	// ----------------------------------------------------- //

	/**
	 * Gets the mapper output.
	 *
	 * @return the mapper output
	 */
	//public IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> getMapperOutput() { return map; }

	/*@Override
	public void setMapper(IList<IShape> map, 
			String mapAttribute) {
		this.map = map;
		this.keyAttMap = mapAttribute;
	}

	@Override
	public void setMapper(
			final List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> ancillaryFileList,
			final List<? extends IValue> varList, final LMRegressionOLS lmRegressionOLS,
			final SPLUniformNormalizer splUniformNormalizer) throws IndexOutOfBoundsException, IOException,
			TransformException, InterruptedException, ExecutionException, IllegalRegressionException, GSMapperException,
			SchemaException, MismatchedDimensionException, IllegalArgumentException, InvalidGeoFormatException {
		String keyAttribute = "count";
		File tmp = File.createTempFile("match", "." + (GeoGSFileType.VECTOR.equals(match.getGeoGSFileType())
				? SPLGisFileExtension.shp.toString() : SPLGisFileExtension.tif.toString()));
		tmp.deleteOnExit();

		this.setMapper(this.estimateMatcher(tmp), keyAttribute, ancillaryFileList, varList, lmRegressionOLS,
				splUniformNormalizer);

	}

	@Override
	public void setMapper(final IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue> mainMapper,
			final String mainAttribute,
			final List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> ancillaryFileList,
			final List<? extends IValue> varList, final LMRegressionOLS lmRegressionOLS,
			final SPLUniformNormalizer splUniformNormalizer) throws IndexOutOfBoundsException, IOException,
			TransformException, InterruptedException, ExecutionException, IllegalRegressionException, GSMapperException,
			SchemaException, MismatchedDimensionException, IllegalArgumentException, InvalidGeoFormatException {
		this.setMapper(new SPLAreaMapperBuilder(mainMapper, mainAttribute, ancillaryFileList, varList, lmRegressionOLS,
				splUniformNormalizer));

	}*/

	/**
	 * Sets the mapper.
	 *
	 * @param splMapperBuilder
	 *            the spl mapper builder
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TransformException
	 *             the transform exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 * @throws IllegalRegressionException
	 *             the illegal regression exception
	 * @throws IndexOutOfBoundsException
	 *             the index out of bounds exception
	 * @throws GSMapperException
	 *             the GS mapper exception
	 * @throws SchemaException
	 *             the schema exception
	 * @throws MismatchedDimensionException
	 *             the mismatched dimension exception
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 * @throws InvalidGeoFormatException
	 *             the invalid geo format exception
	 */
	/*
	 * Inner utility set mapper from regression
	 *
	 */
	/*private void setMapper(final ASPLMapperBuilder<? extends ISPLVariable, ? extends Number> splMapperBuilder)
			throws IOException, TransformException, InterruptedException, ExecutionException,
			IllegalRegressionException, IndexOutOfBoundsException, GSMapperException, MismatchedDimensionException,
			IllegalArgumentException, InvalidGeoFormatException {
		splMapperBuilder.buildMapper();
		switch (splMapperBuilder.getAncillaryFiles().get(0).getGeoGSFileType()) {
			case RASTER:
				File tmpRaster = Files.createTempFile("regression_raster_output", ".tif").toFile();
				tmpRaster.deleteOnExit();
				this.setMapper(splMapperBuilder.buildOutput(tmpRaster,
						(SPLRasterFile) splMapperBuilder.getAncillaryFiles().get(0), false, true,
						(double) population.size()), GeoEntityFactory.ATTRIBUTE_PIXEL_BAND + 0);
				break;
			case VECTOR:
				File tmpVector = Files.createTempFile("regression_vector_output", ".shp").toFile();
				tmpVector.deleteOnExit();
				this.setMapper(splMapperBuilder.buildOutput(tmpVector,
						(SPLRasterFile) splMapperBuilder.getAncillaryFiles().get(0), false, true,
						(double) population.size()), splMapperBuilder.getMainAttribute());
				break;
			default:
				throw new IllegalArgumentException(
						"Ancillary could not be resolve to a proper geo file type (" + GeoGSFileType.values() + ")");
		}
	}*/

	/**
	 * Clear map cache.
	 */
	/*public void clearMapCache() {
		if (map instanceof SPLRasterFile file) { file.clearCache(); }
	}*/

	// ----------------------------------------------------- //
	// -------------------- CONSTRAINTS -------------------- //
	// ----------------------------------------------------- //

	@Override
	public void setConstraints(final List<ISpatialConstraint> constraints) {
		this.linker.setConstraints(constraints);
	}

	@Override
	public void addConstraint(final ISpatialConstraint constraint) {
		this.linker.addConstraints(constraint);
	}

	@Override
	public List<ISpatialConstraint> getConstraints() { return linker.getConstraints(); }

	/**
	 * The first constraint that allows to select only a limited number of nests to locate in according to a enclosing
	 * geometry
	 *
	 * @return
	 */
	public SpatialConstraintLocalization getLocalizationConstraint() { return localizationConstraint; }

	// ----------------------------------------------------- //
	// ------------------- DISTRIBUTION -------------------- //
	// ----------------------------------------------------- //

	@Override
	public ISpatialDistribution<IShape> getDistribution() { return linker.getDistribution(); }

	@Override
	public void setDistribution(final ISpatialDistribution<IShape> candidatesDistribution) {
		this.linker.setDistribution(candidatesDistribution);
	}

	// ----------------------------------------------------- //
	// ------------------ POINT LOCALIZER ------------------ //
	// ----------------------------------------------------- //

	/**
	 * Define the {@link PointInLocalizer} that will bind synthetic entities with precise coordinate (point)
	 *
	 * @param pointInLocalizer
	 */
	public void setPointInLocalizer(final PointInLocalizer pointInLocalizer) {
		this.pointInLocalizer = pointInLocalizer;
	}

	/**
	 * Get the {@link PointInLocalizer} that will place synthetic entities at a precise coordinate (point)
	 *
	 * @return
	 */
	public PointInLocalizer getPointInLocalizer() { return pointInLocalizer; }

	/////////////////////////////////////////////////////
	// --------------- INNER UTILITIES --------------- //
	/////////////////////////////////////////////////////

	// set to all the entities given as argument, a given nest chosen randomly in the possible geoEntities
	// of the localisation shapefile (all if not bounds is defined, only the one in the bounds if the one is not null)
	/**
	 * describe the algorithm PLZ !
	 *
	 * To be revised absolutly, because there is incoherences: e.g. loop over constraints to loop over constraint again,
	 * and then localize updating constraint.
	 *
	 * @param entities
	 * @param spatialBounds
	 * @throws IOException
	 * @throws TransformException
	 */
	private void localizationInNest(final IScope scope, final IContainer<?, IAgent> entities, final IShape spatialBounds)
			throws IOException {
		localizationConstraint.setBounds(spatialBounds);
		
		IList<IShape> possibleNests = localizationConstraint.getCandidates(scope,null);
		System.out.println("Possibles nest computed");
		if (linker.getConstraints().isEmpty()) {
			localizationInNestOp(scope, entities, possibleNests, null);

		} else {

			List<ISpatialConstraint> otherConstraints =
					Stream.concat(linker.getConstraints().stream(), Stream.of(localizationConstraint))
							.sorted(Comparator.comparing(ISpatialConstraint::getPriority)).toList();

			IContainer<?,IAgent> remainingEntities = entities.listValue(scope, Types.AGENT, true);
			for (ISpatialConstraint cr : otherConstraints) {
				while (!cr.isConstraintLimitReach()) {

					IList<IShape> candidates =(IList<IShape>) localizationConstraint.getGeoms().listValue(scope, Types.GEOMETRY, true);
					for (ISpatialConstraint constraint : otherConstraints) {
						candidates = constraint.getCandidates(scope, candidates);
					}

					remainingEntities = localizationInNestOp(scope, remainingEntities, candidates, null);
					if (remainingEntities == null || (remainingEntities.length(scope) ==0)) return;
					cr.relaxConstraint(possibleNests);

				}
			}
		}
	}

	/**
	 * Localization in nest op.
	 *
	 * @param entities
	 *            the entities
	 * @param possibleNests
	 *            the possible nests
	 * @param val
	 *            the val
	 * @return the list
	 */
	
	
	private IContainer<?, IAgent> localizationInNestOp(final IScope scope, final IContainer<?, IAgent> entities,
			final IList<IShape> possibleNests, Long val) {
		IContainer<?, IAgent>  chosenEntities = null;
		IList<IAgent>  entityToLocalize = entities.listValue(scope, Types.AGENT, true);
		if (val != null) {
			val = Math.min(val, entities.length(scope));
			chosenEntities = Containers.among(scope, val.intValue(), entities);
		} else {
			chosenEntities = entities;
		}
		if(possibleNests.isEmpty()) return chosenEntities;
		linker.getDistribution().setCandidate(possibleNests);
		int cpt = 0;
		System.out.println("ici: "+ possibleNests.size());
		for (IAgent entity : chosenEntities.iterable(scope)) {
			cpt ++;
			if (cpt % 100 == 0)
				System.out.println("fait: " + cpt + "/" + chosenEntities.length(scope));
			if (possibleNests.isEmpty()) { break; }
			Optional<IShape> oNest = linker.getCandidate(scope, entity, possibleNests);
			boolean removeObject = false;
			if (oNest.isPresent()) {
				IShape nest = oNest.get();
				for (ISpatialConstraint constraint : linker.getConstraints()) {
					removeObject = removeObject || constraint.updateConstraint(nest);
				}
				if (removeObject) { 
					IShape n = possibleNests.remove(0);
					linker.getDistribution().removeNest(n);
				}
				if (nestAttribute != null) {
					/*if (proxyNest != null)
						entity.setAttribute(nestAttribute, proxyNest.get(nest));
					else*/
						entity.setAttribute(nestAttribute, nest);
				}
				entityToLocalize.remove(entity);
				entity.setLocation(pointInLocalizer.pointIn(scope, nest));
				
			}
		}
		return entityToLocalize;
	}

	@Override
	public void linkPopulation(IList<IAgent> population, ISPLinker<IShape> linker, IList<IShape> linkedPlaces,
			Attribute<? extends msi.gama.common.interfaces.IValue> attribute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMapper(IList<IShape> map, String numberProperty, GamaField field) {
		this.map = map;
		this.keyAttMap = numberProperty;
		this.mapField = field;
	}
 
	
	// For each area concerned of the entityNbAreas shapefile (all if not bounds is defined, only the one in the bounds
	// if the one is not null),
	// define the number of entities from the entities list to locate inside, then try to set a nest to this randomly
	// chosen number of entities.
	/**
	 * Localization in nest with numbers.
	 *
	 * @param entities
	 *            the entities
	 * @param spatialBounds
	 *            the spatial bounds
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TransformException
	 *             the transform exception
	 */
	// NOTE: if no nest is located inside the area, not entities will be located inside.
	@SuppressWarnings ("unchecked")
	private void localizationInNestWithNumbers(IScope scope, final IContainer<?, IAgent> entities, final IShape spatialBounds)
			throws IOException {
		if (mapField != null)
			localizationInNestWithNumbersField(scope, entities, spatialBounds);
		else
			localizationInNestWithNumbersMap(scope, entities, spatialBounds);
			
	}
	
	@SuppressWarnings ("unchecked")
	private void localizationInNestWithNumbersField(IScope scope, final IContainer<?, IAgent> entities, final IShape spatialBounds)
			throws IOException {
		List<ISpatialConstraint> otherConstraints = new ArrayList<>(linker.getConstraints());
		IList<IShape> areas = mapField.getCellsIntersecting(scope, spatialBounds == null ? scope.getSimulation().getGeometry() : spatialBounds) ;
		IList<Double> valsMap =  spatialBounds == null ? mapField.listValue(scope, Types.FLOAT, false) : mapField.getValuesIntersecting(scope, spatialBounds);
		double unknowVal = mapField.getNoData(scope);
		Double	tot = mapField.listValue(scope, Types.FLOAT, false).stream().mapToDouble(s -> ( s == unknowVal  ? 0 : s)).sum();
		if (tot == 0) return;
		
		IContainer<?, IAgent>  remainingEntities = (IContainer<?, IAgent>) entities.copy(scope);
		for (int i = 0; i < areas.size(); i++) { 
			Double val = valsMap.get(i);
			if (val == unknowVal) continue;
			IShape feature = areas.get(i);
			localizationConstraint.setBounds(feature);
			long valR = Math.round(entities.length(scope) * val / tot );

			if (entities.isEmpty(scope))  { break; }
			for (ISpatialConstraint cr : linker.getConstraints()) {
				while (!remainingEntities.isEmpty(scope) && !cr.isConstraintLimitReach()) {
					IList<IShape> possibleNestsInit = localizationConstraint.getCandidates(scope, null);
					IList<IShape> possibleNests = possibleNestsInit.copy(scope);
					for (ISpatialConstraint constraint : otherConstraints) {
						possibleNests = constraint.getCandidates(scope, possibleNests);
					}
					remainingEntities = localizationInNestOp(scope, remainingEntities, possibleNests, valR);
					if (!remainingEntities.isEmpty(scope)) {
						cr.relaxConstraint((IList<IShape>) localizationConstraint.getGeoms());
					}
				}
				if (remainingEntities.isEmpty(scope)) { break; }
			}
		}
	}

	
	
	@SuppressWarnings ("unchecked")
	private void localizationInNestWithNumbersMap(IScope scope, final IContainer<?, IAgent> entities, final IShape spatialBounds)
			throws IOException {
		List<ISpatialConstraint> otherConstraints = new ArrayList<>(linker.getConstraints());
		IList<IShape> areas = spatialBounds == null ? map.copy(scope) : (IList<IShape>) Queries.overlapping(scope, map, spatialBounds);
			areas = msi.gaml.operators.Random.opShuffle(scope, areas);
			Map<IShape, Double> vals = map.stream().collect(Collectors.toMap(a ->a,
					e -> Cast.asFloat(scope, e.getAttribute(keyAttMap))));
			//Map<IShape, Double> vals2 = areas.stream().collect(Collectors.toMap(a->a,
			//		e ->  Cast.asFloat(scope, e.getAttribute(keyAttMap))));

			
		Double tot = vals.values().stream().mapToDouble(s -> s).sum();
		//Double tot2 = vals2.values().stream().mapToDouble(s -> s).sum();
		
		if (tot == 0) return;
		
		IContainer<?, IAgent>  remainingEntities = (IContainer<?, IAgent>) entities.copy(scope);
		if (areas != null)
		for (IShape feature : areas) {
			
			localizationConstraint.setBounds(feature);
			long val = Math.round(entities.length(scope) * vals.get(feature) / tot );
			
			if (entities.isEmpty(scope))  { break; }
			for (ISpatialConstraint cr : linker.getConstraints()) {
				while (!remainingEntities.isEmpty(scope) && !cr.isConstraintLimitReach()) {
					IList<IShape> possibleNestsInit = localizationConstraint.getCandidates(scope, null);
					IList<IShape> possibleNests = possibleNestsInit.copy(scope);
					for (ISpatialConstraint constraint : otherConstraints) {
						possibleNests = constraint.getCandidates(scope, possibleNests);
					}
					remainingEntities = localizationInNestOp(scope, remainingEntities, possibleNests, val);
					if (!remainingEntities.isEmpty(scope)) {
						cr.relaxConstraint((IList<IShape>) localizationConstraint.getGeoms());
					}
				}
				if (remainingEntities.isEmpty(scope)) { break; }
			}
		}
	}

	public void setMinDistance(Double mind) {
		minDist = mind;
	}
	
	public void setMaxDistance(Double maxd) {
		maxDist = maxd;
	}
	
	
	/**
	 * Associate a proxy geometry to each spatial entity (#SpllFeature) that correspond to the area
	 * at minDist and maxDist from the original geometry
	 * 
	 * @param minDist
	 * @param maxDist
	 * @param avoidOverlapping
	 */
	public void computeMinMaxDistance(IScope scope, IList<IShape> nests)  {
		IShape u = Operators.union(scope, nests);
		IShape s = null;
		if (maxDist != null && maxDist > 0.0) {
			if (minDist != null && minDist >= maxDist) {
				s = Transformations.enlarged_by(scope, u, maxDist).getExteriorRing(scope);
			} else {
				s = Transformations.enlarged_by(scope, u, maxDist);
				if (minDist != null && minDist > 0.0) {
					s = Operators.minus(scope, s, Transformations.enlarged_by(scope, u, minDist));
				}
			}
			s = Operators.inter(scope, s, scope.getSimulation().getGeometry());
			
		} else if (minDist != null && minDist > 0.0) {
			s = Operators.minus(scope,  scope.getSimulation().getGeometry(), Transformations.enlarged_by(scope, u, minDist));
		}
		this.localizationConstraint.setGeoms((IList<IShape>) s.getGeometries());
	}
	




}