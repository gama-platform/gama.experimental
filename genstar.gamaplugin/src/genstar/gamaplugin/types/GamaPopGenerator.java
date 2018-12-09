/*********************************************************************************************
 *
 * 'GamaRegression.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/

package genstar.gamaplugin.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.configuration.dictionary.AttributeDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.attribute.record.RecordAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.io.GSSurveyWrapper;
import msi.gama.common.interfaces.IValue;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import spin.SpinNetwork;
import spin.SpinPopulation;
import spin.algo.generator.ISpinNetworkGenerator;
import spll.io.SPLVectorFile;
import spll.popmapper.distribution.ISpatialDistribution;
import spll.popmapper.distribution.SpatialDistributionFactory;



// TODO var à revoir completement
@vars({
	@variable(name = "attributes", type = IType.LIST, of = IType.STRING, doc = {@doc("Returns the list of attribute names") }),
	@variable(name = "census_files", type = IType.LIST, of = IType.STRING, doc = {@doc("Returns the list of census files") }), 
	@variable(name = "generation_algo", type = IType.STRING, doc = {@doc("Returns the name of the generation algorithm") }),
	@variable(name = "mappers", type = IType.MAP, doc = {@doc("Returns the list of mapper") }),
	@variable(name = "spatial_file", type = IType.STRING, doc = {@doc("Returns the spatial file used to localize entities") }),
	@variable(name = "spatial_mapper_file", type = IType.LIST, of = IType.STRING, doc = {@doc("Returns the list of spatial files used to map the entities to areas") }),
	@variable(name = "spatial_matcher_file", type = IType.STRING, doc = {@doc("Returns the spatial file used to match entities and areas") })
})
public class GamaPopGenerator implements IValue {

	IPopulation<? extends ADemoEntity, ?> generatedPopulation;
	
	//////////////////////////////////////////////
	// Attirbute for the Gospl generation
	//////////////////////////////////////////////
	
	String generationAlgorithm;
	List<GSSurveyWrapper> inputFiles;
	AttributeDictionary inputAttributes ;
	
	//////////////////////////////////////////////
	// Attirbute for the Gospl generation
	//////////////////////////////////////////////
	boolean spatializePopulation;	
	
	String stringOfCensusIdInCSVfile;
	String stringOfCensusIdInShapefile;
	
	String pathCensusGeometries;
	String pathNestedGeometries;

	Double minDistanceLocalize;
	Double maxDistanceLocalize;
	boolean localizeOverlaps;
	
	String spatialDistribution;
	String crs;

	List<String> pathAncilaryGeofiles;
	
//	
//	Map<String, IAttribute<? extends core.metamodel.value.IValue>> inputKeyMap ;
//	
//	String spatialContingencyId;
//	
//	List<String> pathsRegressionData;
//	
//	pathsRegressionData = new ArrayList<>();
//	inputKeyMap = new HashMap<>();	

	//////////////////////////////////////////////
	// Attribute for the Spin generation
	//////////////////////////////////////////////
	HashMap<String, ISpinNetworkGenerator<? extends ADemoEntity>> networkGenerators;
	Map<ADemoEntity, IAgent> mapEntitiesAgent;


	public GamaPopGenerator() {
		generationAlgorithm = "IS";		
		inputFiles = new ArrayList<>();
		inputAttributes = new AttributeDictionary();
		
		minDistanceLocalize = 0.0;
		maxDistanceLocalize = 0.0;
		localizeOverlaps = false;
		pathAncilaryGeofiles = new ArrayList<>();	
	
		networkGenerators = new HashMap<>();		
		mapEntitiesAgent = new HashMap<>();
	}
	
	
	@Override
	public String serialize(boolean includingBuiltIn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	public AttributeFactory getAttf() {
		return AttributeFactory.getFactory();
	}

	public List<GSSurveyWrapper> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(List<GSSurveyWrapper> inputFiles) {
		this.inputFiles = inputFiles;
	}

	public AttributeDictionary getInputAttributes() {
		return inputAttributes;
	}

	public void setInputAttributes(AttributeDictionary inputAttributes) {
		this.inputAttributes = inputAttributes;
	}

//	public Map<String, IAttribute<? extends core.metamodel.value.IValue>> getInputKeyMap() {
//		return inputKeyMap;
//	}
//
//	public void setInputKeyMap(Map<String, IAttribute<? extends core.metamodel.value.IValue>> inputKeyMap) {
//		this.inputKeyMap = inputKeyMap;
//	}

	public void setGenerationAlgorithm(String generationAlgorithm) {
		this.generationAlgorithm = generationAlgorithm;
	}

	public void setPathNestedGeometries(String pathGeometries) {
		this.pathNestedGeometries = pathGeometries;
		// TODO add test si the File exist ?
		if (pathGeometries != null)
			setSpatializePopulation(true);
		else
			setSpatializePopulation(false);
	}	


//	public List<String> getPathsRegressionData() {
//		return pathsRegressionData;
//	}
//
//	public void setPathsRegressionData(List<String> pathsRegressionData) {
//		this.pathsRegressionData = pathsRegressionData;
//	}


	public String getStringOfCensusIdInCSVfile() {
		return stringOfCensusIdInCSVfile;
	}

	public void setStringOfCensusIdInCSVfile(String stringOfCensusIdInCSVfile) {
		this.stringOfCensusIdInCSVfile = stringOfCensusIdInCSVfile;
	}

	public String getStringOfCensusIdInShapefile() {
		return stringOfCensusIdInShapefile;
	}

	public void setStringOfCensusIdInShapefile(String stringOfCensusIdInShapefile) {
		this.stringOfCensusIdInShapefile = stringOfCensusIdInShapefile;
	}

	public boolean isSpatializePopulation() {
		return spatializePopulation;
	}

	public void setSpatializePopulation(boolean spatializePopulation) {
		this.spatializePopulation = spatializePopulation;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}
//
//	public String getSpatialContingencyId() {
//		return spatialContingencyId;
//	}
//
//	public void setSpatialContingencyId(String spatialContingencyId) {
//		this.spatialContingencyId = spatialContingencyId;
//	}
	
	@getter("attributes")
	public IList<String> getAttributeName(){
		IList<String> atts = GamaListFactory.create(Types.STRING);
		for (Attribute<? extends core.metamodel.value.IValue> a : this.getInputAttributes().getAttributes())
			atts.add(a.getAttributeName());
		return atts;
	}
	
	@getter("census_files")
	public IList<String> getCensusFile(){
		IList<String> f = GamaListFactory.create(Types.STRING);
		for (GSSurveyWrapper a : this.getInputFiles()) f.add(a.getRelativePath().toString());
		return f;
	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@getter("mappers")
//	public GamaMap getMappers(){
//		GamaMap<String, String> lm =GamaMapFactory.create(Types.STRING, Types.STRING);
//		for (String a : this.getInputKeyMap().keySet()) {
//			lm.put(a, this.getInputKeyMap().get(a).getAttributeName());
//		}
//		return lm;
//	}
//	
//	@getter("spatial_mapper_file")
//	public IList<String> getSpatialMapper(){
//		return GamaListFactory.create(GAMA.getRuntimeScope(), Types.STRING, this.getPathsRegressionData());
//	}
//	@getter("spatial_matcher_file")
//	public String getPathCensusGeometries() {
//		return pathCensusGeometries;
//	}


	@getter("generation_algo")
	public String getGenerationAlgorithm() {
		return generationAlgorithm;
	}
	
	@getter("spatial_file")
	public String getPathNestedGeometries() {
		return pathNestedGeometries;
	}

	public Collection<RecordAttribute<Attribute<? extends core.metamodel.value.IValue>, 
				Attribute<? extends core.metamodel.value.IValue>>> getRecordAttributes() {
		return inputAttributes.getRecords();
		
	}

	public void setSpatialMapper(String stringOfCensusIdInCSVfile, String stringOfCensusIdInShapefile) {
		this.stringOfCensusIdInCSVfile = stringOfCensusIdInCSVfile;
		this.stringOfCensusIdInShapefile = stringOfCensusIdInShapefile;
	}

	public void setPathCensusGeometries(String stringPathToCensusShapefile) {
		this.pathCensusGeometries = stringPathToCensusShapefile;
		
		if (pathCensusGeometries != null)
			setSpatializePopulation(true);
		else
			setSpatializePopulation(false);		
	}

	public String getPathCensusGeometries() {
		return pathCensusGeometries;
	}


	public void setLocalizedAround(Double min, Double max, boolean overlaps) {
		setMinDistanceLocalize(min);
		setMaxDistanceLocalize(max);
		setLocalizeOverlaps(overlaps);
	}


	public Double getMinDistanceLocalize() {
		return minDistanceLocalize;
	}


	public void setMinDistanceLocalize(Double minDistanceLocalize) {
		this.minDistanceLocalize = minDistanceLocalize;
	}


	public Double getMaxDistanceLocalize() {
		return maxDistanceLocalize;
	}


	public void setMaxDistanceLocalize(Double maxDistanceLocalize) {
		this.maxDistanceLocalize = maxDistanceLocalize;
	}


	public boolean isLocalizeOverlaps() {
		return localizeOverlaps;
	}


	public void setLocalizeOverlaps(boolean localizeOverlaps) {
		this.localizeOverlaps = localizeOverlaps;
	}


	public String getSpatialDistribution() {
		return spatialDistribution;
	}


	public void setSpatialDistribution(String spatialDistribution) {
		this.spatialDistribution = spatialDistribution;
	}	
	
	@SuppressWarnings("rawtypes")
	// TODO add more distribution...
	public ISpatialDistribution getSpatialDistribution(SPLVectorFile sfGeometries) {
		if("area".equals(spatialDistribution)) {
			return SpatialDistributionFactory.getInstance().getAreaBasedDistribution(sfGeometries);			
		}
		
		return SpatialDistributionFactory.getInstance().getUniformDistribution();
	}
	

	public List<String> getPathAncilaryGeofiles() {
		return pathAncilaryGeofiles;
	}

	public void addAncilaryGeoFiles(String pathToFile) {
		pathAncilaryGeofiles.add(pathToFile);
	}

	public void setPathAncilaryGeofiles(List<String> pathAncilaryGeofiles) {
		this.pathAncilaryGeofiles = pathAncilaryGeofiles;
	}


	public boolean isSocialPopulation() {
		return !networkGenerators.isEmpty();
	}


//	public void addNetwork(String graphName, SpinNetwork network) {
//		networks.put(graphName, network);
//	}	
	
	public void addNetworkGenerator(String graphName, ISpinNetworkGenerator<? extends ADemoEntity> graphGenerator) {
		networkGenerators.put(graphName, graphGenerator);
	}
	
	public HashMap<String, ISpinNetworkGenerator<? extends ADemoEntity>> getNetworkGenerators() {
		return networkGenerators;
	}


	public void setNetworksGenerator(HashMap<String, ISpinNetworkGenerator<? extends ADemoEntity>> networksGenerator) {
		this.networkGenerators = networksGenerator;
	}

	public ISpinNetworkGenerator<? extends ADemoEntity> getNetworkGenerator(String networkName) {
		return networkGenerators.get(networkName);
	}	
	
	public SpinNetwork getNetwork(String networkName) {
		if(isSocialPopulation()) {
			return ((SpinPopulation<?>)generatedPopulation).getNetwork(networkName);	
		} else 
			return null;
	}

	public void setGeneratedPopulation(IPopulation<? extends ADemoEntity, ?> population) {
		this.generatedPopulation = population;
	}
	public IPopulation<? extends ADemoEntity, ?> getGeneratedPopulation() {
		return generatedPopulation;
	}
	
	public void addAgent(ADemoEntity e, IAgent a) {
		mapEntitiesAgent.put(e, a);	
	}

	public IAgent getAgent(ADemoEntity e) {
		return mapEntitiesAgent.get(e);
	}
	
	public Collection<IAgent> getAgents() {
		return mapEntitiesAgent.values();
	}
	
/*	
    public void setRecordAttribute(RecordAttribute<Attribute<? extends core.metamodel.value.IValue>, 
				Attribute<? extends core.metamodel.value.IValue>> record) {
		inputAttributes.addRecords(record);
	}
*/	

}
