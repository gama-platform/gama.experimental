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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.regression.AbstractMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.GLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.geotools.feature.SchemaException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import core.metamodel.attribute.Attribute;
import core.util.GSPerformanceUtil;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gaml.operators.Cast;
import msi.gaml.operators.Containers;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.Queries;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.types.Types;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.datamapper.normalizer.ASPLNormalizer;
import spll.datamapper.normalizer.SPLUniformNormalizer;
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
	public IList<IShape> getMapperOutput() { return map; }



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
	
	
	public void buildMapField(IScope scope, IList<IShape> mainEntities, String mainAttribute, 
			IList<GamaField> fields, String regressionAlgo, String normalizerType, 
			double floorValue, int popTargetSize ) {
		List data =  buildMapFieldData(scope, mainEntities, mainAttribute, fields);
		if (data == null) {
			return;
		}
		GamaField field = fields.get(0).copy(scope, fields.get(0).getDimensions(), true);
		field.setNoData(scope, -1.0);
		field.setAllValues(scope, -1.0);
		
		AbstractMultipleLinearRegression reg = null;
		switch(regressionAlgo) {
			case "GLS" :
				reg = new GLSMultipleLinearRegression();
				break;
			default:
				reg = new OLSMultipleLinearRegression();
				((OLSMultipleLinearRegression)reg).newSampleData((double[])data.get(0),(double[][])data.get(1));
				
		}
		double[] d = (double[])data.get(0);
		double[] coe = reg.estimateRegressionParameters(); 
		double intercept = coe[0];
		double [] res = reg.estimateResiduals();
		List<IList<Double>> possiblesValues = (List<IList<Double>>) data.get(2);
		Map<IShape,Integer> refObjects = (Map<IShape, Integer>) data.get(3);
		
		for (int i = 0; i < coe.length; i++) {
			coe[i] += intercept;
		}
		double[][] pixels = new double[field.numCols][field.numRows];
		for (int i = 0; i < field.numRows; i++) {
			for (int j = 0; j < field.numCols; j++) {
				double output = 0.0;
				
				IShape s = field.getCellShapeAt(scope, j, i);
				Integer index = refObjects.get(s);
				if (index == null) {
				//	System.out.println("i: " + i + " j: " + j);
					continue;
				}
				System.out.println("index: " + index);
				double cor = res[index];
				int cpt = 0;
				for (int k = 0; k < possiblesValues.size(); k++) {
					GamaField f = fields.get(k);
					
					IList<Double> possibleVals = possiblesValues.get(k);
					IList<Double> cellVals = f.getValuesIntersecting(scope, s);
					if (k == 0) {
						cellVals = GamaListFactory.EMPTY_LIST;
						cellVals.add(f.get(scope, j, i));
					} else {
						cellVals = f.getValuesIntersecting(scope, s);
					}
					GamaPoint spt = f.getCellSize(scope);
					double area = spt.x * spt.y;
					
					for (int l = 0; l < possibleVals.length(scope); l++) {
						Double v = possibleVals.get(l);
						System.out.println("freq: " + Collections.frequency(cellVals, v) + " coe[l+cpt]: " + coe[l+cpt]);
						
						output +=Collections.frequency(cellVals, v) *coe[l+cpt] * area; 
					}
					
					cpt += possibleVals.size();
				}
				if (output > 0)
					System.out.println("output: " + output + " cor: " + cor);
				pixels[i][j] = output + cor;
			}
				
		}

		saveDouble(pixels,"pixel");
		ASPLNormalizer normalizer;
		switch(normalizerType) {
			default:
				normalizer = new SPLUniformNormalizer(floorValue, field.getNoData(scope)); 
			
		}
				
		double[][] afterNorm = normalizer.process(pixels, popTargetSize, true);
		
		for(int i = 0; i < field.numCols; i++)
			for(int j = 0; j < field.numRows; j++)
				field.set(scope, i, j, afterNorm[i][j]);
		mapField = field;
		
		saveDouble(afterNorm,"afterNorm");
		//System.out.println("mapField: " + mapField);
	}
	
	private void saveDouble(double[][] tab, String name) {
		FileWriter myWriter = null;
		try {
			myWriter = new FileWriter("C:\\Users\\admin_ptaillandie\\Desktop\\" + name + ".asc");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String v = "ncols        295\nnrows        172\nxllcorner    557070.341329975170\nyllcorner    6925932.962285792455\ncellsize     30.0\nNODATA_value 0\n";
		try {
			myWriter.write(v);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < tab.length; i++) {
			String t = "";
	    	for (int j = 0; j < tab[i].length; j++) {
				t += " " +tab[i][j];
			}
			try {
				myWriter.write(t);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
		try {
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private List buildMapFieldData(IScope scope, IList<IShape> mainEntities, String mainAttribute, IList<GamaField> fields) {
		if (fields.isEmpty() || mainEntities.isEmpty() || !mainEntities.get(0).hasAttribute(mainAttribute))
			return null;
		Map<Double,IList<Double>> associations = GamaMapFactory.create();
		double x[] = new double[mainEntities.size()];
		List<IList<Double>> possiblesValues = new ArrayList<>();
		int nbVar = 0;
		for (GamaField f : fields) {
			IList vars = Containers.remove_duplicates(scope, f.listValue(scope, Types.FLOAT, false));
			possiblesValues.add(vars);
			nbVar += vars.size();
		}
		double y[][] = new double[mainEntities.size()][nbVar];
		Map<IShape,Integer> refObjects = GamaMapFactory.create();
		
		int fs = fields.size(); 
		for(int i = 0; i < x.length;i++) {
			IShape s = mainEntities.get(i);
			x[i] = Cast.asFloat(scope, s.getAttribute(mainAttribute));
			int cpt = 0;
			for (int j = 0; j < fs; j++) {  
				GamaField field = fields.get(j);
				GamaPoint spt = field.getCellSize(scope);
				double area = spt.x * spt.y;
				if (j == 0) {
					IList<IShape> cells = field.getCellsIntersecting(scope, s);
					for (IShape c: cells) {
						refObjects.put(c, i);
					}
				}
				List<Double> cellVals = field.getValuesIntersecting(scope, s);
				IList<Double> possibleVals = possiblesValues.get(j);
				for(Double v : possibleVals) {
					y[i][cpt] = Collections.frequency(cellVals, v) * area; 
					cpt++;
					
				}
			}
		}
		List al = new ArrayList<>();
		al.add(x);
		al.add(y);
		al.add(possiblesValues);
		al.add(refObjects); 
		return al;
	}
	
	
	
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
		IList<IShape> areas = null;
		if (spatialBounds == null) {
			areas = GamaListFactory.create();
			for (int i = 0; i <mapField.numCols; i++) {
				for (int j = 0; j <mapField.numRows; j++) {
					IShape s = mapField.getCellShapeAt(scope, i, j);
					areas.add(s);
				}
			}
		} else areas = mapField.getCellsIntersecting(scope, spatialBounds) ;
		
		
		double unknowVal = mapField.getNoData(scope);
		Double	tot = mapField.listValue(scope, Types.FLOAT, false).stream().mapToDouble(s -> ( s == unknowVal  ? 0 : s)).sum();
		System.out.println("tot: " + tot);
		if (tot == 0) return;
		
		IContainer<?, IAgent>  remainingEntities = (IContainer<?, IAgent>) entities.copy(scope);
		long totV = 0;
		for (int i = 0; i < areas.size(); i++) { 
			IShape s = areas.get(i);
			Double val = mapField.get(scope, s.getLocation());
		//	System.out.println("val:" + val);
			if (val == unknowVal) continue;
			IShape feature = areas.get(i);
			localizationConstraint.setBounds(feature);
			long valR = Math.round(entities.length(scope) * val / tot );
			if (valR == 0) continue;
			totV += valR;
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
		System.out.println("totV: " + totV);
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