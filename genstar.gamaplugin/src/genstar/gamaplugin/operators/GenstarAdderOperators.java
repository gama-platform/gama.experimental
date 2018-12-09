package genstar.gamaplugin.operators;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.io.GSSurveyWrapper;
import core.metamodel.value.IValue;
import core.util.excpetion.GSIllegalRangedData;
import genstar.gamaplugin.types.GamaPopGenerator;
import genstar.gamaplugin.utils.GenStarGamaUtils;
import msi.gama.common.util.FileUtils;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.IList;
import msi.gaml.types.IType;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenstarAdderOperators {
	@operator(value = "add_census_file", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add a census data file defined by its path (string), its type (\"ContingencyTable\", \"GlobalFrequencyTable\", \"LocalFrequencyTable\" or  \"Sample\"), its separator (string), the index of the first row of data (int) and the index of the first column of data (int) to a population_generator",
	examples = @example(value = "add_census_file(pop_gen, \"../data/Age_Couple.csv\", \"ContingencyTable\", \";\", 1, 1)", test = false))
	public static GamaPopGenerator addCensusFile(IScope scope, GamaPopGenerator gen, String path, String type, String csvSeparator, int firstRowIndex, int firstColumnIndex) throws GamaRuntimeException {
		Path completePath = Paths.get(FileUtils.constructAbsoluteFilePath(scope, path, false));
		gen.getInputFiles().add(new GSSurveyWrapper(completePath, GenStarGamaUtils.toSurveyType(type), csvSeparator.isEmpty() ? ',':csvSeparator.charAt(0), firstRowIndex, firstColumnIndex));
		return gen;
	}
	
//	@operator(value = "add_spatial_file", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
//	@doc(value = "add a spatial data file to locate the entities (nested geometries) defined by its path (string) to a population_generator",
//	examples = @example(value = "add_spatial_file(pop_gen, \"../data/buildings.shp\")", test = false))
//	public static GamaPopGenerator addGeographicFile(IScope scope, GamaPopGenerator gen, String path) throws GamaRuntimeException {
//		String completePath = FileUtils.constructAbsoluteFilePath(scope, path, false);
//		gen.setPathNestedGeometries(completePath);
//		return gen;
//	}
//	
//	@operator(value = "add_regression_file", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
//	@doc(value = "add a spatial regression data file defined by its path (string) to a population_generator",
//	examples = @example(value = "add_regression_file(pop_gen, \"../data/landuse.tif\")", test = false))
//	public static GamaPopGenerator addSpatialRegressionFile(IScope scope, GamaPopGenerator gen, String path) throws GamaRuntimeException {
//		String completePath = FileUtils.constructAbsoluteFilePath(scope, path, false);
//		gen.getPathsRegressionData().add(completePath);
//		return gen;
//	}
//	
//	@operator(value = "add_spatial_contingency_file", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
//	@doc(value = "add a spatial contingency data file defined by its path (string) and the name of the attribute containing the number used to place the entities to a population_generator",
//	examples = @example(value = "add_spatial_contingency_file(pop_gen, \"../data/district.shp\", \"POP\")", test = false))
//	public static GamaPopGenerator addSpatialContingencyFile(IScope scope, GamaPopGenerator gen, String path, String contingencyId) throws GamaRuntimeException {
//		String completePath = FileUtils.constructAbsoluteFilePath(scope, path, false);
//		gen.getPathsRegressionData().add(completePath);
//		gen.setSpatialContingencyId(contingencyId);
//		return gen;
//	}
//	
//	
//	@operator(value = "add_spatial_matcher", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
//	@doc(value = "add a spatial matcher data (link between the entities and the space) file defined by its path (string), the name of the key attribute in the entities and the name of the key attribute in the geographic file to a population_generator",
//	examples = @example(value = "add_spatial_matcher(pop_gen, \"../data/iris.shp\", \"iris\",\"IRIS\")", test = false))
//	public static GamaPopGenerator addSpatialMatcher(IScope scope, GamaPopGenerator gen, String path, String idInCensusFile, String isInShapefile) throws GamaRuntimeException {
//		String completePath = FileUtils.constructAbsoluteFilePath(scope, path, false);
//		gen.setPathCensusGeometries(completePath);
//		gen.setStringOfCensusIdInCSVfile(idInCensusFile);
//		gen.setStringOfCensusIdInShapefile(isInShapefile);
//		return gen;
//	}

	
	
	@operator(value = "add_mapper", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add a mapper between source of data for a attribute to a population_generator. A mapper is defined by the name of the attribute, the datatype of attribute (type), the corresponding value (map<list,list>) and the type of attribute (\"unique\" or \"range\")",
	examples = @example(value = " add_mapper(pop_gen, \"Age\", int, [[\"0 to 18\"]::[\"1 to 10\",\"11 to 18\"], [\"18 to 100\"]::[\"18 to 50\",\"51 to 100\"] , \"range\");", test = false))
	public static GamaPopGenerator addMapper(IScope scope, GamaPopGenerator gen, String referentAttributeName, IType dataType, GamaMap values ) {
		return addMapper(scope, gen,referentAttributeName, dataType, values, false);
	}
	

	// TODO : remove the type ... 
	@operator(value = "add_mapper", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add a mapper between source of data for a attribute to a population_generator. A mapper is defined by the name of the attribute, the datatype of attribute (type), the corresponding value (map<list,list>) and the type of attribute (\"unique\" or \"range\")",
	examples = @example(value = " add_mapper(pop_gen, \"Age\", int, [[\"0 to 18\"]::[\"1 to 10\",\"11 to 18\"], [\"18 to 100\"]::[\"18 to 50\",\"51 to 100\"] , \"range\");", test = false))
	public static GamaPopGenerator addMapper(IScope scope, GamaPopGenerator gen, String referentAttributeName, IType dataType, GamaMap values, Boolean ordered) {
		if (gen == null) {
			gen = new GamaPopGenerator();
		}
		if (referentAttributeName == null) return gen;
		
		AttributeFactory attf = AttributeFactory.getFactory();		

		Attribute<? extends IValue> referentAttribute = gen.getInputAttributes().getAttribute(referentAttributeName);
		
		if(referentAttribute != null) {	
			// TODO : from GamaMap to Map ? 
			Map<Collection<String>, Collection<String>> mapper = new Hashtable<>();
			for (Object k : values.keySet()) {
				Object v = values.get(scope, k);
				if (k instanceof Collection && v instanceof Collection) {
					Collection<String> key = new HashSet<String>((Collection)k);
					Collection<String> val = new HashSet<String>((Collection)v);
					mapper.put(key, val);
				}
			}
			
			try {	
				String name = referentAttribute.getAttributeName() + "_" + (gen.getInputAttributes().getAttributes().size() + 1);
				gen.getInputAttributes().addAttributes(attf.createSTSMappedAttribute(name, GenStarGamaUtils.toDataType(dataType, ordered), referentAttribute, mapper));

				// We lose an information in the case it is an aggregatre 
				// Si keys ont 1 seules element -> aggregation
				
			//	dd.addAttributes(attf.createRangeAggregatedAttribute("Age_2", new GSDataParser()
			//			.getRangeTemplate(mapperA1.keySet().stream().collect(Collectors.toList())),
			//			referentAgeAttribute, mapperA1));
				
			} catch (GSIllegalRangedData e) {
				e.printStackTrace();
			}				
		}
		return gen;
	}
	

	
	@operator(value = "add_attribute", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add an attribute defined by its name (string), its datatype (type), its list of values (list) to a population_generator",
			examples = @example(value = "add_attribute(pop_gen, \"Sex\", string,[\"Man\", \"Woman\"])", test = false))
	public static GamaPopGenerator addAttribute(IScope scope, GamaPopGenerator gen, String name, IType dataType, IList value) {
		return addAttribute(scope, gen, name, dataType, value, false);
	}

	@operator(value = "add_attribute", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add an attribute defined by its name (string), its datatype (type), its list of values (list) and attributeType name (type of the attribute among \"range\" and \"unique\") to a population_generator", 
			examples = @example(value = "add_attribute(pop_gen, \"iris\", string, liste_iris, \"unique\")", test = false))
	public static GamaPopGenerator addAttribute(IScope scope, GamaPopGenerator gen, String name, IType dataType, IList value, String record, IType recordType) {
		return addAttribute(scope, gen, name, dataType, value, false, record, recordType);
	}	

	@operator(value = "add_attribute", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add an attribute defined by its name (string), its datatype (type), its list of values (list) to a population_generator",
			examples = @example(value = "add_attribute(pop_gen, \"Sex\", string,[\"Man\", \"Woman\"])", test = false))
	public static GamaPopGenerator addAttribute(IScope scope, GamaPopGenerator gen, String name, IType dataType, IList value, Boolean ordered, String record, IType recordType) {
		GamaPopGenerator genPop = addAttribute(scope, gen, name, dataType, value, ordered);
		genPop.getInputAttributes().addRecords();
		try {
			genPop.getInputAttributes().addRecords(
				gen.getAttf().createRecordAttribute(
					record, 
					GenStarGamaUtils.toDataType(recordType,false)/*GSEnumDataType.Integer*/, 
					genPop.getInputAttributes().getAttribute(name)
				)
			);
		} catch (GSIllegalRangedData e) {
			GamaRuntimeException.error("Wrong type for the record", scope);
		}
	
		return genPop;
		
	}	
	
	@operator(value = "add_attribute", can_be_const = true, category = { "Gen*" }, concept = { "Gen*"})
	@doc(value = "add an attribute defined by its name (string), its datatype (type), its list of values (list) and record name (name of the attribute to record) to a population_generator", 
			examples = @example(value = "add_attribute(pop_gen, \"iris\", string,liste_iris, \"unique\", \"P13_POP\")", test = false))
	public static GamaPopGenerator addAttribute(IScope scope, GamaPopGenerator gen, String name, IType dataType, IList value, Boolean ordered) {
		if (gen == null) {
			gen = new GamaPopGenerator();
		}
		
		try {
			Attribute<? extends IValue> newAttribute = 
					gen.getAttf().createAttribute(name, GenStarGamaUtils.toDataType(dataType,ordered), value);
			
			gen.getInputAttributes().addAttributes(newAttribute);
			 
			 // TODO : à revoir les records ..........
		//	 if (record != null && ! record.isEmpty()) {
		//		 gen.getInputAttributes().addRecords()
		//				 gen.getAttf()
		//				 .createIntegerRecordAttribute(record,newAttribute,Collections.emptyMap()));
			
			/*Attribute<? extends IValue> attIris = gen.getAttf()
					.createAttribute(name, toDataType(dataType,ordered), value);
			 gen.getInputAttributes().addAttributes(attIris);
			 if (record != null && ! record.isEmpty()) {
				 gen.getRecordAttributes().addAttributes(gen.getAttf()
						 .createRecordAttribute("population", GSEnumDataType.Integer,
								 attIris, Collections.emptyMap()));*/
		//	 }
		} catch (GSIllegalRangedData e) {
			e.printStackTrace();
		}
		return gen;
	}
}