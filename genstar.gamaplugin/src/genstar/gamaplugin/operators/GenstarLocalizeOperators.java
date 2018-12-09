package genstar.gamaplugin.operators;

import genstar.gamaplugin.types.GamaPopGenerator;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;


public class GenstarLocalizeOperators {
	@operator(value = "add_spatial_mapper", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	public static GamaPopGenerator addSpatialMapper(IScope scope, GamaPopGenerator gen, String stringOfCensusIdInCSVfile, String stringOfCensusIdInShapefile) {
		if(gen.getPathCensusGeometries() == null ) {
			throw GamaRuntimeException.error("Cannot set a spatial Mapper when the Census Shapefile has not been set.", scope);
		}
		gen.setSpatialMapper(stringOfCensusIdInCSVfile, stringOfCensusIdInShapefile);		
		
		return gen;
	}	
	
	@operator(value = "add_spatial_distribution", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	public static GamaPopGenerator addSpatialDistribution(IScope scope, GamaPopGenerator gen, String distribution) {
		gen.setSpatialDistribution(distribution);
		return gen;
	}	
	
	@operator(value = "add_ancilary_geofile", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	public static GamaPopGenerator addAncilaryGeoFiles(IScope scope, GamaPopGenerator gen, String pathToFile) {
		gen.addAncilaryGeoFiles(pathToFile);
		return gen;
	}		

	@operator(value = "localize_around_at", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	public static GamaPopGenerator localize_around_at(IScope scope, GamaPopGenerator gen, Double max) {
		return localize_around_at(scope, gen, 0.0, max, false);
	}		
	
	@operator(value = "localize_around_at", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	public static GamaPopGenerator localize_around_at(IScope scope, GamaPopGenerator gen, Double min, Double max) {
		return localize_around_at(scope,gen,min,max,false);
	}		
	
	@operator(value = "localize_around_at", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	public static GamaPopGenerator localize_around_at(IScope scope, GamaPopGenerator gen, Double min, Double max, boolean overlaps) {
		gen.setLocalizedAround(min, max, overlaps);		
		return gen;
	}	
	
	@operator(value = "localize_on_geometries", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add an attribute defined by its name (string), its datatype (type), its list of values (list) to a population_generator",
			examples = @example(value = "add_attribute(pop_gen, \"Sex\", string,[\"Man\", \"Woman\"])", test = false))
	public static GamaPopGenerator localize_on_geometries(IScope scope, GamaPopGenerator gen, String stringPathToGeometriesShapefile) {
		gen.setSpatializePopulation(true);
		gen.setPathNestedGeometries(stringPathToGeometriesShapefile);
		
		return gen;
	}

	@operator(value = "localize_on_census", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add an attribute defined by its name (string), its datatype (type), its list of values (list) to a population_generator",
			examples = @example(value = "add_attribute(pop_gen, \"Sex\", string,[\"Man\", \"Woman\"])", test = false))
	public static GamaPopGenerator localize_on_census(IScope scope, GamaPopGenerator gen, String stringPathToCensusShapefile) {
		gen.setSpatializePopulation(true);
		gen.setPathCensusGeometries(stringPathToCensusShapefile);
		
		return gen;
	}
}
