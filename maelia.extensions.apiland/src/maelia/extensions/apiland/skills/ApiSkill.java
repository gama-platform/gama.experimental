package maelia.extensions.apiland.skills;

import java.io.File;

import fr.inra.sad.bagap.apiland.capfarm.CAPFarm;
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
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;


@vars({ 
	@variable(name = IKeyword.SPEED, type = IType.FLOAT, init = "1.0"),
	@variable(name = IKeyword.HEADING, type = IType.INT, init = "rnd(359)")
	})
@skill(name = "APILandExtension")
public class ApiSkill extends Skill {

	private static final Instant start = Instant.get(1, 7, 2020);
	private static final Instant end = Instant.get(1, 7, 2024);
	private static Farm farm;	
	
//	@getter(IKeyword.SPEED)
//	public Integer getHeading(final IAgent agent) {
//		return 32;
//	}	
	
//	@action(name = "helloWorld", args = {})
//	public void primMoveRandomly(final IScope scope) throws GamaRuntimeException {
//		System.out.println("Hello");
//	}

	@action(name = "firstCAPFarm", args = { 
		@arg(name = "shapefile", type = IType.STRING, optional = false, doc = @doc("")),
		@arg(name = "constraints", type = IType.STRING, optional = false, doc = @doc("")),
		@arg(name = "covers", type = IType.STRING, optional = false, doc = @doc("")),	
		@arg(name = "groups", type = IType.STRING, optional = false, doc = @doc("")),
		@arg(name = "historic", type = IType.STRING, optional = false, doc = @doc("")),
		//@arg(name = "next_covers", type = IType.STRING, optional = false, doc = @doc("")),
		@arg(name = "proba_times_folder", type = IType.STRING, optional = false, doc = @doc("")),		
		@arg(name = "farm", type = IType.STRING, optional = true, doc = @doc(""))
	})
	public GamaMap firstCAPFarm(final IScope scope) throws GamaRuntimeException {
		final String shpFile = (String) (scope.hasArg("shapefile") ? scope.getArg("shapefile", IType.STRING) : null);		
		final String constraintFile = (String) (scope.hasArg("constraints") ? scope.getArg("constraints", IType.STRING) : null);		
		final String coverFile = (String) (scope.hasArg("covers") ? scope.getArg("covers", IType.STRING) : null);		
		final String groupFile = (String) (scope.hasArg("groups") ? scope.getArg("groups", IType.STRING) : null);
		final String historicFile = (String) (scope.hasArg("historic") ? scope.getArg("historic", IType.STRING) : null);		
		//		final String nextCoverFile = (String) (scope.hasArg("next_covers") ? scope.getArg("next_covers", IType.STRING) : null);		
		final String probaTimesFolder = (String) (scope.hasArg("proba_times_folder") ? scope.getArg("proba_times_folder", IType.STRING) : null);		
		final String farmid = (String) (scope.hasArg("farm") ? scope.getArg("farm", IType.STRING) : null);		
		
		CAPFarm.t = start;

		// g�n�ration du shapefile initiale
		//CfmUtil.generateShapefile("F:/maelia/data/ea1", "1");
		
		
		return script(scope, shpFile, farmid,coverFile, groupFile, historicFile, constraintFile, probaTimesFolder); 
		//return scriptOneFarmOneType(shpFile,constraintFile, coverFile,farm);
		
	}	
	
	
	private static GamaMap script(final IScope scope, final String shpFile, final String farmid, String coverfile, String groupFile, String historicFile, String constraintFile, String probaTimesFolder) {

		// integration du territoire
		Territory territory = TerritoryFactory.init(shpFile, start);

		// cr�ation d'une ferme
		farm = new Farm(farmid);

		// int�gration du territoire d'une ferme
		TerritoryFactory.init(territory, farm);

//		initConstraints(farmid,coverfile,nextCoverFile);
		initConstraints(constraintFile, coverfile, groupFile);

		// mise en place d'un historique
		farm.setHistoric(historicFile);

		
		// initialisation du simulateur
		CfmFarmManager sm = new CfmFarmManager(farm);
		sm.setPath(scope.getModel().getFilePath());
		sm.setTime(start, end);
		sm.setProbaTimeFolder(probaTimesFolder);
		sm.setSuccess(1);

		// d�finition des sorties
		GamaOutput out = new GamaOutput();
		
		sm.addOutput(new ConsoleOutput());
		sm.addOutput(new FarmShapefileOutput());
		sm.addOutput(out);
		
		
		// cr�ation et lancement du simulateur
		CfmFarmSimulator s = new CfmFarmSimulator(sm);
		s.allRun();

				
		return (GamaMap) out.getRes();
		// v�rification des contraintes
		//farm.checkConstraintSystem(start, end, true);
	}
	
//	private static void initConstraints(String farmCode, String coverfile, String nextCoverFile) {
//		// int�gration des couverts
//		CoverFactory.init(farm,coverfile, null);
//
//		// mise en place des contraintes
//		ConstraintBuilder cb = new ConstraintBuilder(farm);
//		
//		cb.setCode("ALL0");
//		cb.setType(ConstraintType.NextCover);
//		cb.setParams(nextCoverFile);
//		cb.build();
//		
//		cb.setCode("ALL1");
//		cb.setType(ConstraintType.Duration);
//		cb.setDomain("1");
//		cb.build();
//
//		// 3e contrainte : min et max de r�p�tition d'une culture dans une s�quence (en prenant en comtpe l'historique)
//		cb.setCode("ALL2");
//		cb.setCover("orgeI","CouvRg"); // Si ligne pas pr�sente, c'est vrai pour toutes les cultures
//		cb.setType(ConstraintType.Repetition);
//		cb.setDomain("[2,3]"); // Il faut que cette contrainte soit coh�rente avec ce qui est d�fini dans next_covers ([min,max]) Si min = max, on met une seule valeur. Si on veut que min --> "[2,]"
//		cb.build();
//		
//		farm.getConstraintSystem().display();
//		ConstraintSystemFactory.exportSystem(farm.getConstraintSystem(), "C:/Users/rmisslin/Dropbox/inra/maelia/capfarm/workspace_capfarm/MAELIA_1.3.6_GAMA_1.8.1//includes/vendee_capfarm/capfarm/csp/system_production.csv");
//	}
	
	private static void initConstraints(String systemFile, String cover, String group) {
		// cr�ation d'un type de syst�me
		GenericConstraintSystem system = new GenericConstraintSystem(new File(systemFile).getName().replace(".csv", ""));	
		ConstraintSystemFactory.importSystem(system, systemFile);
		CoverFactory.init(farm, cover, group); // int�gration des couverts
		new ConstraintBuilder(farm).build(system);
	}

}
