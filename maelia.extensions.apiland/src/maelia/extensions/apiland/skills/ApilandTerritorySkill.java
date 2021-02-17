package maelia.extensions.apiland.skills;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.opengis.referencing.FactoryException;

import fr.inra.sad.bagap.apiland.capfarm.model.ConstraintSystemFactory;
import fr.inra.sad.bagap.apiland.capfarm.model.CoverFactory;
import fr.inra.sad.bagap.apiland.capfarm.model.Farm;
import fr.inra.sad.bagap.apiland.capfarm.model.GenericConstraintSystem;
import fr.inra.sad.bagap.apiland.capfarm.model.constraint.ConstraintBuilder;
import fr.inra.sad.bagap.apiland.capfarm.model.territory.Territory;
import fr.inra.sad.bagap.apiland.capfarm.model.territory.TerritoryFactory;
import fr.inra.sad.bagap.apiland.capfarm.simul.farm.CfmFarmManager;
import fr.inra.sad.bagap.apiland.capfarm.simul.farm.CfmFarmSimulator;
import fr.inra.sad.bagap.apiland.capfarm.simul.output.ConsoleOutput;
import fr.inra.sad.bagap.apiland.capfarm.simul.output.FarmShapefileOutput;
import fr.inra.sad.bagap.apiland.core.time.Instant;
import maelia.extensions.apiland.skills.ouput.GamaOutput;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.ITyped;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.common.util.FileUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.projection.IProjection;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Files;
import msi.gaml.skills.Skill;
import msi.gaml.statements.SaveStatement;
import msi.gaml.types.IType;


@vars({ 
	@variable(name = ApilandTerritorySkill.SHAPEFILE_PATH, type = IType.STRING, init = "",
			doc = @doc ("the path to the plot shapefile - if not provides, GAMA will use a temporary file")),
	@variable(name = ApilandTerritorySkill.PROBA_FOLDER_TIMES, type = IType.STRING, init = "",
			doc = @doc ("the path to the proba times folder")),
	@variable(name = ApilandTerritorySkill.FARMS, type = IType.LIST, of = IType.STRING, init = "[]",
	doc = @doc ("the list of farms (their id) of the territory"))
	})
@skill(name = "apiland_territory",
concept = { "agriculture", IConcept.SKILL },
doc = @doc ("A skill that provides primitives for crop allocation for plots using the apiland framework (constraint satisfaction problem)"))
public class ApilandTerritorySkill extends Skill {

	public static final String SHAPEFILE_PATH = "shapefile_path";
	public static final String FARMS = "farms";
	public static final String PROBA_FOLDER_TIMES = "proba_folder_times";
	
	private Territory territory;
	private Map<String, Farm> farms;
	
	@getter(ApilandTerritorySkill.SHAPEFILE_PATH)
	public String getShapefilePath(final IAgent agent) {
		if (agent == null) { return ""; }
		return (String) agent.getAttribute(SHAPEFILE_PATH);
	}	
	
	@getter(ApilandTerritorySkill.FARMS)
	public IList<String> getFarms(final IAgent agent) {
		if (agent == null) { return GamaListFactory.EMPTY_LIST; }
		return (IList<String>) agent.getAttribute(FARMS);
	}	
	
	
	
	@setter (ApilandTerritorySkill.SHAPEFILE_PATH)
	public void setShapefile(final IAgent agent, final String path) {
		if (agent == null) { return; }
		agent.setAttribute(SHAPEFILE_PATH, path);
		if (Files.exist_file(agent.getScope(), path)) 
			buildTerritory(agent.getScope(),Instant.get(2020),GamaListFactory.EMPTY_LIST);
	}
	
	
	@getter(ApilandTerritorySkill.PROBA_FOLDER_TIMES)
	public String getprobaFolderPath(final IAgent agent) {
		if (agent == null) { return ""; }
		return (String) agent.getAttribute(PROBA_FOLDER_TIMES);
	}	
	
	
	@setter (ApilandTerritorySkill.PROBA_FOLDER_TIMES)
	public void setProbaFolderfile(final IAgent agent, final String path) {
		if (agent == null) { return; }
		agent.setAttribute(PROBA_FOLDER_TIMES, path);
	}
	
	
	public static Instant toApilandDate(GamaDate date) {
		return Instant.get(date.getDay(), date.getMonth(), date.getYear());
	}

	
	private static final Set<String> NON_SAVEABLE_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList(IKeyword.PEERS,
			IKeyword.LOCATION, IKeyword.HOST, IKeyword.AGENTS, IKeyword.MEMBERS, IKeyword.SHAPE));

	
	public static String type(final ITyped var) {
		switch (var.getGamlType().id()) {
			case IType.BOOL:
				return "Boolean";
			case IType.INT:
				return "Integer";
			case IType.FLOAT:
				return "Double";
			default:
				return "String";
		}
	}
	

	private static void  toShapeFile(IScope scope, File newFile, IList<? extends IShape> shapes) {
		final StringBuilder specs = new StringBuilder(shapes.size() * 20);
		final String geomType = SaveStatement.getGeometryType(shapes);
		specs.append("geometry:" + geomType);
		try {
			SpeciesDescription species =
					shapes instanceof IPopulation ? ((IPopulation) shapes).getSpecies().getDescription() : null;
			final Map<String, IExpression> attributes = GamaMapFactory.create();
				if ((species == null) && (shapes.get(0) instanceof IAgent)) {
				species = shapes.getGamlType().getContentType().getSpecies();
			 }
			 
			 if (species != null) {
				 for (final String var : species.getAttributeNames()) {
					if (!NON_SAVEABLE_ATTRIBUTE_NAMES.contains(var)) {
						attributes.put(var, species.getVarExpr(var, false));
					}
				}
			 } else {
				 
					 
			}
			for (final String e : attributes.keySet()) {
				final IExpression var = attributes.get(e);
				String name = e.replaceAll("\"", "");
				name = name.replaceAll("'", "");
				final String type = type(var);
				specs.append(',').append(name).append(':').append(type);
			}
			
			final boolean useNoSpecific = GamaPreferences.External.LIB_USE_DEFAULT.getValue();
			String code;
			IProjection gis = null;
			if (!useNoSpecific) {
				code = "EPSG:" + GamaPreferences.External.LIB_OUTPUT_CRS.getValue();
				try {
					gis = scope.getSimulation().getProjectionFactory().forSavingWith(scope, code);
				} catch (final FactoryException e1) {
					throw GamaRuntimeException.error(
							"The code " + code + " does not correspond to a known EPSG code. GAMA is unable to save the file",
							scope);
				}
			} else {
				gis = scope.getSimulation().getProjectionFactory().getWorld();
				if (gis == null || gis.getInitialCRS(scope) == null) {
					final boolean alreadyprojected = GamaPreferences.External.LIB_PROJECTED.getValue();
					if (alreadyprojected) {
						code = "EPSG:" + GamaPreferences.External.LIB_TARGET_CRS.getValue();
					} else {
						code = "EPSG:" + GamaPreferences.External.LIB_INITIAL_CRS.getValue();
					}
					try {
						gis = scope.getSimulation().getProjectionFactory().forSavingWith(scope, code);
					} catch (final FactoryException e1) {
						throw GamaRuntimeException.error("The code " + code
								+ " does not correspond to a known EPSG code. GAMA is unable to save the file", scope);
					}
				}
			}
			// }
			SaveStatement.saveShapeFile(scope, newFile, shapes, specs.toString(), attributes, gis);
			
		} catch (final GamaRuntimeException e) {
			throw e;
		} catch (final Throwable e) {
			throw GamaRuntimeException.create(e, scope);
		}

	}
	
	private void buildTerritory(IScope scope, Instant startingDate, IList<IAgent> plots) {
		String path = getShapefilePath(scope.getAgent());
		File newFile = null;
		if (!Files.exist_file(scope, path)) {
			try {
				newFile = File.createTempFile("apilandShape", ".shp");
				toShapeFile(scope, newFile, plots);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			path = FileUtils.constructAbsoluteFilePath(scope, path, true);
		}

		territory = TerritoryFactory.init(path, startingDate);
	}
	
	
	@action(name = "add_farm", args = {
			@arg(name = "farm_id", type = IType.STRING, optional = false, doc = @doc("id of the farm"))},
			doc = @doc (
					value = "action add a farm to a territory",
					examples = { @example ("do add_farm farm_id: \"0\";") }))
	public void primAddfarm(final IScope scope) throws GamaRuntimeException {
		String farmId = (String) scope.getArg("farm_id", IType.STRING);
		if (farms == null) 
			farms = new Hashtable();
		// création d'une ferme
		Farm farm = new Farm(farmId);
		if (territory == null)
			buildTerritory(scope,Instant.get(2020),GamaListFactory.EMPTY_LIST );
		
		TerritoryFactory.init(territory, farm);
		
		farms.put(farmId, farm);
	}
	
	@action(name = "next_covers", args = {
			@arg(name = "farm_id", type = IType.STRING, optional = true, doc = @doc("id of the farm - optional - if not provided, computes the next covers for all the farms of the territory")),
			@arg(name = "starting_date", type = IType.DATE, optional = false, doc = @doc("Starting date")),
			@arg(name = "ending_date", type = IType.DATE, optional = false, doc = @doc("Starting date")) },
			doc = @doc (
					value = "action the next covers for one farm or all the farms of a territory from a starting date to an ending date",
					examples = { @example ("do next_covers starting_date: date(2020,1,1) ending_date:date(2024,1,1);") }))
	public IMap<String, IMap<String,IList<String>>> primNextCovers(final IScope scope) throws GamaRuntimeException {
		Instant startingDate = toApilandDate((GamaDate) scope.getArg("starting_date", Types.DATE));
		Instant endingDate = toApilandDate((GamaDate) scope.getArg("ending_date", Types.DATE));
		String farmid = scope.hasArg("farm_id") ? (String) scope.getArg("farm_id", IType.STRING) : null;
		
		territory.setTime(startingDate);
		
		
		IMap<String, IMap<String,IList<String>>> results = GamaMapFactory.create();
		
		if (farmid == null) {
			for (String id: farms.keySet())  {
				results.put(id, runSimulation(scope,id,startingDate,endingDate));
			}
		} else {
			results.put(farmid, runSimulation(scope,farmid,startingDate,endingDate));
		}
		
				
		return results;
	
	}	
	
	
	IMap<String,IList<String>> runSimulation(IScope scope, String farmid,Instant startingDate, Instant endingDate) {
		Farm farm = farms.get(farmid);
		// initialisation du simulateur
		CfmFarmManager sm = new CfmFarmManager(farm);
		sm.setPath(scope.getModel().getFilePath());
		sm.setTime(startingDate, endingDate);
		if (Files.exist_folder(scope, getprobaFolderPath(scope.getAgent()))) {
			sm.setProbaTimeFolder( FileUtils.constructAbsoluteFilePath(scope, getprobaFolderPath(scope.getAgent()), true));
		}
		sm.setSuccess(1);

		// définition des sorties
		GamaOutput out = new GamaOutput();
		
		//sm.addOutput(new ConsoleOutput());
	//	sm.addOutput(new FarmShapefileOutput());
		sm.addOutput(out);
		
		
		// création et lancement du simulateur
		CfmFarmSimulator s = new CfmFarmSimulator(sm);
		s.allRun();
		return out.getRes();
	}
	
	
	
	
	@action(name = "set_historic", args = {
			@arg(name = "farm_id", type = IType.STRING, optional = false, doc = @doc("id of the farm")),
			@arg(name = "historic", type = IType.STRING, optional = false, doc = @doc("path to historic file"))},
			doc = @doc (
					value = "action to set the historic file for a farm (defined by its id)",
					examples = { @example ("do set_historic farm_id: \"0\" historic: \"..\\includes\\historic.txt;") }))
	
	public boolean primWithHistoric(final IScope scope) throws GamaRuntimeException {
		String farmId = (String) scope.getArg("farm_id", IType.STRING);
		String historicFile = (String) scope.getArg("historic", IType.STRING);
		if (Files.exist_file(scope, historicFile)) {
			farms.get(farmId).setHistoric(FileUtils.constructAbsoluteFilePath(scope, historicFile, true));
			return true;
		}
		return false;

	}
	
	@action(name = "set_covers_file", args = {
			@arg(name = "farm_id", type = IType.STRING, optional = false, doc = @doc("id of the farm")),
			@arg(name = "group_path", type = IType.STRING, optional = false, doc = @doc("path to system file")),
			@arg(name = "cover_path", type = IType.STRING, optional = false, doc = @doc("path to cover file"))},
			doc = @doc (
					value = "action to set the cover file (constraints) and group file for a farm (defined by its id)",
					examples = { @example ("do set_covers_file farm_id: \"0\" group_path: \"..\\includes\\group.txt\" cover_path: \"..\\includes\\constriants.csv\";") }))
	public void initCoversGroup(final IScope scope) {
		String farmId = (String) scope.getArg("farm_id", IType.STRING);
		String group = FileUtils.constructAbsoluteFilePath(scope, scope.getStringArg("group_path"), true);
		String cover = FileUtils.constructAbsoluteFilePath(scope, scope.getStringArg("cover_path"), true);
		// création d'un type de système
		CoverFactory.init(farms.get(farmId), cover, group); // intégration des couverts
	}
	
	@action(name = "set_system_file", args = {
			@arg(name = "farm_id", type = IType.STRING, optional = false, doc = @doc("id of the farm")),
			@arg(name = "system_path", type = IType.STRING, optional = false, doc = @doc("path to system file"))},
			doc = @doc (
					value = "action to set the system file for a farm (defined by its id)",
					examples = { @example ("do set_system_file farm_id: \"0\" system_path: \"..\\includes\\system.txt;") }))
	
	public void initSystems(final IScope scope) {
		String farmId = (String) scope.getArg("farm_id", IType.STRING);
		String systemFile = FileUtils.constructAbsoluteFilePath(scope, scope.getStringArg("system_path"), true);
		
		// création d'un type de système
		GenericConstraintSystem system = new GenericConstraintSystem(new File(systemFile).getName().replace(".csv", ""));	
		ConstraintSystemFactory.importSystem(system, systemFile);
		new ConstraintBuilder(farms.get(farmId)).build(system);
	}

	
	
}
