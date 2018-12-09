package genstar.gamaplugin.operators;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.operation.TransformException;

import core.configuration.GenstarConfigurationFile;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.entity.AGeoEntity;
import core.metamodel.io.IGSGeofile;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import gospl.GosplEntity;
import gospl.GosplPopulation;
import gospl.algo.sr.ISyntheticReconstructionAlgo;
import gospl.algo.sr.ds.DirectSamplingAlgo;
import gospl.distribution.GosplContingencyTable;
import gospl.distribution.GosplInputDataManager;
import gospl.distribution.exception.IllegalControlTotalException;
import gospl.distribution.exception.IllegalDistributionCreation;
import gospl.distribution.matrix.INDimensionalMatrix;
import gospl.distribution.matrix.coordinate.ACoordinate;
import gospl.generator.DistributionBasedGenerator;
import gospl.generator.ISyntheticGosplPopGenerator;
import gospl.io.exception.InvalidSurveyFormatException;
import gospl.sampler.IDistributionSampler;
import gospl.sampler.ISampler;
import gospl.sampler.sr.GosplBasicSampler;
import genstar.gamaplugin.types.GamaPopGenerator;
import genstar.gamaplugin.utils.GenStarGamaUtils;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gaml.operators.Spatial;
import msi.gaml.types.Types;
import spin.SpinPopulation;
import spin.algo.generator.ISpinNetworkGenerator;
import spll.SpllEntity;
import spll.SpllPopulation;
import spll.algo.LMRegressionOLS;
import spll.algo.exception.IllegalRegressionException;
import spll.datamapper.exception.GSMapperException;
import spll.io.SPLGeofileBuilder;
import spll.io.SPLRasterFile;
import spll.io.SPLVectorFile;
import spll.io.exception.InvalidGeoFormatException;
import spll.popmapper.SPLocalizer;
import spll.popmapper.normalizer.SPLUniformNormalizer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenstarGenerationOperators {


	@operator(value = "with_generation_algo", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "define the algorithm used for the population generation among: IS (independant hypothesis Algorothm) and simple_draw (simple draw of entities in a sample)",
			examples = @example(value = "my_pop_generator with_generation_algo \"simple_draw\"", test = false))
	public static GamaPopGenerator withGenerationAlgo(GamaPopGenerator gen, String algo) {
		if (gen == null) {
			gen = new GamaPopGenerator();
		}
		gen.setGenerationAlgorithm(algo);
		return gen;
	}
	
	public static IPopulation<ADemoEntity, Attribute<? extends IValue>> generatePop(final IScope scope, GamaPopGenerator gen, Integer targetPopulation) {
		if (gen == null) {
			return null;
		}

		Path baseDirectory = FileSystems.getDefault().getPath(".");		
		
		GenstarConfigurationFile confFile = new GenstarConfigurationFile();
		confFile.setBaseDirectory(baseDirectory);
		confFile.setSurveyWrappers(gen.getInputFiles());
		confFile.setDictionary(gen.getInputAttributes());

		////////////////////////////////////////////////////////////////////////
		// Gospl generation
		////////////////////////////////////////////////////////////////////////
		
		GosplInputDataManager gdb = new GosplInputDataManager(confFile);
		
		IPopulation<ADemoEntity, Attribute<? extends IValue>> population = new GosplPopulation();
		
		if ("simple_draw".equals(gen.getGenerationAlgorithm())) {
    	
			try {
				gdb.buildSamples();
			} catch (final RuntimeException | IOException | InvalidSurveyFormatException | InvalidFormatException e) {
				e.printStackTrace();
			} 

    	   IPopulation p = gdb.getRawSamples().iterator().next();
	       if (targetPopulation <= 0)
	    	  return p;
	       List<ADemoEntity> popSample = new ArrayList<>(p);
	       for (int i= 0; i < targetPopulation; i++) {
	    	   ADemoEntity ent =  popSample.get(scope.getRandom().between(0, popSample.size()-1));
	    	   Map<Attribute<? extends IValue>, IValue> atts = ent.getAttributes().stream().collect(Collectors.toMap(a -> a, a -> ent.getValueForAttribute(a)));
	    	   ADemoEntity entity = new GosplEntity(atts);
	    	   population.add(entity);
	       }
	        
	   } else if ("IS".equals(gen.getGenerationAlgorithm())) {
		   try {
			   gdb.buildDataTables();
			} catch (final RuntimeException | IOException | InvalidSurveyFormatException | InvalidFormatException e) {
				e.printStackTrace();
			}

			INDimensionalMatrix<Attribute<? extends IValue>, IValue, Double> distribution = null;
			try {
				distribution = gdb.collapseDataTablesIntoDistribution();
			} catch (final IllegalDistributionCreation e1) {
				e1.printStackTrace();
			} catch (final IllegalControlTotalException e1) {
				e1.printStackTrace();
			}
			
			// BUILD THE SAMPLER WITH THE INFERENCE ALGORITHM
			final ISyntheticReconstructionAlgo<IDistributionSampler> distributionInfAlgo = new DirectSamplingAlgo();
			ISampler<ACoordinate<Attribute<? extends IValue>, IValue>> sampler = null;
			try {
				sampler = distributionInfAlgo.inferSRSampler(distribution, new GosplBasicSampler());
			} catch (final IllegalDistributionCreation e1) {
				e1.printStackTrace();
			}
			
			if (targetPopulation < 0) {
				int min = Integer.MAX_VALUE;
				for (INDimensionalMatrix<Attribute<? extends IValue>,IValue,? extends Number> mat: gdb.getRawDataTables()) {
					if (mat instanceof GosplContingencyTable) {
						GosplContingencyTable cmat = (GosplContingencyTable) mat;
						min = Math.min(min, cmat.getMatrix().values().stream().mapToInt(v -> v.getValue()).sum());
					}
				}
				if (min < Integer.MAX_VALUE) {
					targetPopulation =min;
				} else targetPopulation = 1;
			}
			targetPopulation = targetPopulation <= 0 ? 1 : targetPopulation;
			
			// BUILD THE GENERATOR
			final ISyntheticGosplPopGenerator ispGenerator = new DistributionBasedGenerator(sampler);
			// BUILD THE POPULATION
			try {
				population = ispGenerator.generate(targetPopulation);
				
			} catch (final NumberFormatException e) {
				e.printStackTrace();
			}
	   }
       
       if (population == null) return null;
       
		////////////////////////////////////////////////////////////////////////
		// Spll generation
		////////////////////////////////////////////////////////////////////////       
       if (gen.isSpatializePopulation())
			population = spatializePopulation(scope, gen, population);
      
		////////////////////////////////////////////////////////////////////////
		// Spin generation
		////////////////////////////////////////////////////////////////////////      
       if (gen.isSocialPopulation())
			population = socializePopulation(scope, gen, population);
       
		return population;
	}


	@operator(value = "generate_localized_entities", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "generate a spatialized population taking the form of a list of geometries while trying to infer the entities number from the data", examples = @example(value = "generateLocalizedEntities(my_pop_generator)", test = false))
	public static IList<IShape> generateLocalizedEntities(final IScope scope,GamaPopGenerator gen) {
		return generateLocalizedEntities(scope,gen, null);
	}
	
	@operator(value = "generate_localized_entities", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "generate a population composed of the given number of entities taking the form of a list of geometries", examples = @example(value = "generateLocalizedEntities(my_pop_generator, 1000)", test = false))
	public static IList<IShape> generateLocalizedEntities(final IScope scope,GamaPopGenerator gen, Integer number) {
		if (number == null) number = -1;
		IPopulation<? extends ADemoEntity, Attribute<? extends IValue>> population = generatePop(scope, gen, number);
		IList<IShape> entities =  GamaListFactory.create(Types.GEOMETRY);
		if (gen == null) return entities;
		final Collection<Attribute<? extends IValue>> attributes = population.getPopulationAttributes();
	   int nb = 0;
        List<ADemoEntity> es = new ArrayList(population);
        if (number > 0 && number < es.size()) es = scope.getRandom().shuffle(es);
        for (final ADemoEntity e : es) {
        	IShape entity = null;
        	if (population instanceof SpllPopulation) {
        		SpllEntity newE = (SpllEntity) e;
        		if (newE.getLocation() == null) continue;
        		entity = new GamaShape(gen.getCrs() != null ?
						Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(newE.getLocation()), gen.getCrs())
						: Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(newE.getLocation())));
        	} else 
        		entity = new GamaShape(Spatial.Punctal.any_location_in(scope, scope.getRoot().getGeometry()));
            		
        	for (final Attribute<? extends IValue> attribute : attributes) {
        		 entity.setAttribute(attribute.getAttributeName(), GenStarGamaUtils.toGAMAValue(scope,e.getValueForAttribute(attribute), true));
        	  }
        	
            entities.add(entity);
            nb ++;
            if (number > 0 && nb >= number) break;
        }	
		return entities;
	}

	
	public static IList<IShape> genPop(IScope scope, IPopulation<? extends ADemoEntity, Attribute<? extends IValue>> population, String crs, int number) {
		IList<IShape> entities =  GamaListFactory.create(Types.GEOMETRY);
		final Collection<Attribute<? extends IValue>> attributes = population.getPopulationAttributes();
	    int nb = 0;
        List<ADemoEntity> es = new ArrayList(population);
        if (number > 0 && number < es.size()) es = scope.getRandom().shuffle(es);
        for (final ADemoEntity e : es) {
        	IShape entity = null;
        	if (population instanceof SpllPopulation) {
        		SpllEntity newE = (SpllEntity) e;
        		if (newE.getLocation() == null) continue;
        		entity = new GamaShape(crs != null ? 
						Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(newE.getLocation()), crs)
						: Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(newE.getLocation())));
        	} else 
        		entity = new GamaShape(Spatial.Punctal.any_location_in(scope, scope.getRoot().getGeometry()));
            		
        	for (final Attribute<? extends IValue> attribute : attributes)
                entity.setAttribute(attribute.getAttributeName(), GenStarGamaUtils.toGAMAValue(scope, e.getValueForAttribute(attribute), true));
            entities.add(entity);
            nb ++;
            if (number > 0 && nb >= number) break;
        }	
		return entities;
	}
	
	
	@operator(value = "generate_entities", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "generate a population taking the form of of a list of map (each map representing an entity) while trying to infer the entities number from the data", examples = @example(value = "generate_entities(my_pop_generator)", test = false))
	public static IList<Map> generateEntities(final IScope scope,GamaPopGenerator gen) {
		return generateEntities(scope, gen, null);
	}
	
	@operator(value = "generate_entities", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "generate a population composed of the given number of entities taking the form of a list of map: each map representing an entity", examples = @example(value = "generate_entities(my_pop_generator, 1000)", test = false))
	public static IList<Map> generateEntities(final IScope scope,GamaPopGenerator gen, Integer number) {
		if (number == null) number = -1;
		IPopulation<? extends ADemoEntity, Attribute<? extends IValue>> population = generatePop(scope, gen, number);
		IList<Map> entities =  GamaListFactory.create(Types.MAP);
		if (gen == null) return entities;
		final Collection<Attribute<? extends IValue>> attributes = population.getPopulationAttributes();
      
        int nb = 0;
        List<ADemoEntity> es = new ArrayList(population);
        if (number > 0 && number < es.size()) es = scope.getRandom().shuffle(es);
        for (final ADemoEntity e : es) {
        	
        	Map entity =(Map) GamaMapFactory.create();
        	 		
        	for (final Attribute<? extends IValue> attribute : attributes) {
                final String name = attribute.getAttributeName();
                entity.put(name, GenStarGamaUtils.toGAMAValue(scope, e.getValueForAttribute(attribute), true));

            	if (population instanceof SpllPopulation) {
            		SpllEntity newE = (SpllEntity) e;
            		if (newE.getLocation() != null) {
            			entity.put("location", new GamaShape(new GamaShape(gen.getCrs() != null ?
								Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(newE.getLocation()), gen.getCrs())
								: Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(newE.getLocation())))));
            		}
            	}
            }
            entities.add(entity);
            nb ++;
            if (number > 0 && nb >= number) break;
        }	
		return entities;
	}
	
	
	private static IPopulation spatializePopulation(IScope scope, GamaPopGenerator gen, IPopulation population) {
		File sfGeomsF = gen.getPathNestedGeometries() == null ? null : new File(gen.getPathNestedGeometries());
		File sfCensusF = gen.getPathCensusGeometries() == null ? null : new File(gen.getPathCensusGeometries());

		SPLVectorFile sfGeoms = null;
		SPLVectorFile sfCensus = null;		
		
		if (sfGeomsF != null && !sfGeomsF.exists()) return population;			
	 				
		try {
			if(sfGeomsF != null) {
				sfGeoms = SPLGeofileBuilder.getShapeFile(sfGeomsF, null);
				if(gen.getMaxDistanceLocalize() > 0.0) {
					sfGeoms.minMaxDistance(gen.getMinDistanceLocalize(), gen.getMaxDistanceLocalize(), gen.isLocalizeOverlaps());				
				}
				// TODO limité aux geom ? ou aussi au census ?
				gen.setCrs(sfGeoms.getWKTCoordinateReferentSystem());				
			}
			
			if(sfCensusF != null) {			
				sfCensus = SPLGeofileBuilder.getShapeFile(sfCensusF, null); 
			}
		} catch (IOException | InvalidGeoFormatException | GSIllegalRangedData e) {
			throw GamaRuntimeException.error(e.getMessage(), scope);
		}
		
		// SETUP THE LOCALIZER
		SPLocalizer localizer;		
		if(sfGeoms != null) {
			localizer = new SPLocalizer(population, sfGeoms);	
			localizer.setDistribution(gen.getSpatialDistribution(sfGeoms));
		} else {
			localizer = new SPLocalizer(population, sfCensus);
			localizer.setDistribution(gen.getSpatialDistribution(sfCensus));			
		}
		
		// SETUP GEOGRAPHICAL MATCHER
		// use of the IRIS attribute of the population
		if (sfCensus != null) {
			localizer.setMatcher(sfCensus, gen.getStringOfCensusIdInCSVfile(), gen.getStringOfCensusIdInShapefile());				
		}				

		// SETUP REGRESSION
		List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> endogeneousVarFile = new ArrayList<>();
		for(String path : gen.getPathAncilaryGeofiles()){
			try {
				File pathF = new File(path);
				if (pathF.exists())
					endogeneousVarFile.add(new SPLGeofileBuilder().setFile(pathF).buildGeofile());
			} catch ( IOException | IllegalArgumentException | TransformException | InvalidGeoFormatException | GSIllegalRangedData e) {
				e.printStackTrace();
			}
		}		
		
		if (endogeneousVarFile != null && !endogeneousVarFile.isEmpty())
			try {
				// TODO cntingency ID ????? 
//				if (gen.getSpatialContingencyId() != null && !gen.getSpatialContingencyId().isEmpty()) {
//					localizer.setMapper(endogeneousVarFile.get(0), gen.getSpatialContingencyId());				
//				}
//				else 
				if (sfCensus != null)
					localizer.setMapper(endogeneousVarFile, new ArrayList<>(), new LMRegressionOLS(), new SPLUniformNormalizer(0, SPLRasterFile.DEF_NODATA));
				
			} catch (IndexOutOfBoundsException | IllegalRegressionException 
					| IllegalArgumentException | IOException | TransformException 
					| InterruptedException | ExecutionException | GSMapperException 
					| SchemaException | InvalidGeoFormatException e) {
				e.printStackTrace();
			} 	
		
		//localize the population
		SpllPopulation localizedPop = localizer.localisePopulation();
		
		return localizedPop;
	}
	
	
	
//	private static IPopulation spatializePopulation(GamaPopGenerator gen, IPopulation population) {
//	
//		File sfGeomsF = gen.getPathNestedGeometries() == null ? null : new File(gen.getPathNestedGeometries());
//		
//		if (sfGeomsF != null && !sfGeomsF.exists()) return population;
//		
//		SPLVectorFile sfGeoms = null;
//		SPLVectorFile sfCensus = null;
//
//		File sfCensusF = gen.getPathCensusGeometries() == null ? null : new File(gen.getPathCensusGeometries());
//		
//		try {
//			sfGeoms = SPLGeofileBuilder.getShapeFile(sfGeomsF, null);
//			if (sfCensusF != null && sfCensusF.exists())
//				sfCensus = SPLGeofileBuilder.getShapeFile(sfCensusF, null);
//		} catch (IOException | InvalidGeoFormatException | GSIllegalRangedData e) {
//			e.printStackTrace();
//		} 
//		
//		gen.setCrs(sfGeoms.getWKTCoordinateReferentSystem());
//		List<IGSGeofile<? extends AGeoEntity<? extends IValue>, ? extends IValue>> endogeneousVarFile = new ArrayList<>();
//		for(String path : gen.getPathsRegressionData()){
//			try {
//				File pathF = new File(path);
//				if (pathF.exists())
//					endogeneousVarFile.add(new SPLGeofileBuilder().setFile(pathF).buildGeofile());
//			} catch (IllegalArgumentException | TransformException | IOException | InvalidGeoFormatException | GSIllegalRangedData e2) {
//				e2.printStackTrace();
//			}
//		}
//		
//		
//		// SETUP THE LOCALIZER
//		// 2) SPUniformLocalizer est un SPLocalizer qui utilise une SPLinker avec une ISpatialDistribution uniforme 
//
//		//SPUniformLocalizer localizer = new SPUniformLocalizer(new SpllPopulation(population, sfGeoms));
//		// cf. Bangkok ... 
//		IGSGeofile<? extends AGeoEntity<? extends IValue>, IValue> geoFile = null;
//
//		SPLocalizer localizer = new SPLocalizer(population, geoFile);
//
//		// SETUP GEOGRAPHICAL MATCHER
//		// use of the IRIS attribute of the population
//		if (sfCensus != null)
//			localizer.setMatcher(sfCensus, gen.getStringOfCensusIdInCSVfile(), gen.getStringOfCensusIdInShapefile());
//		
//		// SETUP REGRESSION
//		if (endogeneousVarFile != null && !endogeneousVarFile.isEmpty())
//			try {
//				if (gen.getSpatialContingencyId() != null && !gen.getSpatialContingencyId().isEmpty()) {
//					localizer.setMapper(endogeneousVarFile.get(0), gen.getSpatialContingencyId());
//				
//				}
//				else if (sfCensus != null)
//					localizer.setMapper(endogeneousVarFile, new ArrayList<>(), 
//						new LMRegressionOLS(), new SPLUniformNormalizer(0, SPLRasterFile.DEF_NODATA));
//				
//			} catch (IndexOutOfBoundsException | IllegalRegressionException e) {
//				e.printStackTrace();
//			} 
//
//		//localize the population
//		return localizer.localisePopulation();
//	}
	
	
	private static IPopulation socializePopulation(IScope scope, GamaPopGenerator gen, IPopulation population) {
		SpinPopulation socializedPop = new SpinPopulation<>(population);
		
		for(Entry<String,ISpinNetworkGenerator<? extends ADemoEntity>> e : gen.getNetworkGenerators().entrySet()) {
			socializedPop.addNetwork(e.getKey(), e.getValue().generate(population));
		}	
		
		return socializedPop;
	}	
	
}
