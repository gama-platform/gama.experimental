package maelia.extensions.apiland.skills;

import fr.inra.sad.bagap.apiland.capfarm.CAPFarm;
import fr.inra.sad.bagap.apiland.capfarm.model.CoverFactory;
import fr.inra.sad.bagap.apiland.capfarm.model.Farm;
import fr.inra.sad.bagap.apiland.capfarm.model.constraint.ConstraintBuilder;
import fr.inra.sad.bagap.apiland.capfarm.model.constraint.ConstraintType;
import fr.inra.sad.bagap.apiland.capfarm.model.territory.Territory;
import fr.inra.sad.bagap.apiland.capfarm.model.territory.TerritoryFactory;
import fr.inra.sad.bagap.apiland.capfarm.simul.farm.CfmFarmManager;
import fr.inra.sad.bagap.apiland.capfarm.simul.farm.CfmFarmSimulator;
import fr.inra.sad.bagap.apiland.capfarm.simul.output.ConsoleOutput;
import fr.inra.sad.bagap.apiland.capfarm.simul.output.FarmMemoryOutput;
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
import msi.gama.util.IMap;
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
		@arg(name = "next_covers", type = IType.STRING, optional = false, doc = @doc("")),
		@arg(name = "proba_times_folder", type = IType.STRING, optional = false, doc = @doc("")),		
		@arg(name = "farm", type = IType.STRING, optional = true, doc = @doc(""))				
	})
	public GamaMap firstCAPFarm(final IScope scope) throws GamaRuntimeException {
		final String shpFile = (String) (scope.hasArg("shapefile") ? scope.getArg("shapefile", IType.STRING) : null);		
		final String constraintFile = (String) (scope.hasArg("constraints") ? scope.getArg("constraints", IType.STRING) : null);		
		final String coverFile = (String) (scope.hasArg("covers") ? scope.getArg("covers", IType.STRING) : null);		
		final String nextCoverFile = (String) (scope.hasArg("next_covers") ? scope.getArg("next_covers", IType.STRING) : null);		
		final String probaTimesFolder = (String) (scope.hasArg("proba_times_folder") ? scope.getArg("proba_times_folder", IType.STRING) : null);		
		final String farmid = (String) (scope.hasArg("farm") ? scope.getArg("farm", IType.STRING) : "f0");		
		
		CAPFarm.t = start;

		// génération du shapefile initiale
		//CfmUtil.generateShapefile("F:/maelia/data/ea1", "1");
		
		
		return script(scope, shpFile, farmid,coverFile,nextCoverFile,probaTimesFolder); 
		//return scriptOneFarmOneType(shpFile,constraintFile, coverFile,farm);
		
	}	
	
	
	
	private static GamaMap script(final IScope scope, final String shpFile, final String farmid, String coverfile, String nextCoverFile, String probaTimesFolder) {

		// integration du territoire
		Territory territory = TerritoryFactory.init(shpFile, start);

		// création d'une ferme
		farm = new Farm(farmid);

		// intégration du territoire d'une ferme
		TerritoryFactory.init(territory, farm);

		initConstraints(farmid,coverfile,nextCoverFile);

		// initialisation du simulateur
		CfmFarmManager sm = new CfmFarmManager(farm);
		sm.setPath(scope.getModel().getFilePath());
		sm.setTime(start, end);
		sm.setProbaTimeFolder(probaTimesFolder);
		sm.setSuccess(1);

		// définition des sorties
		GamaOutput out = new GamaOutput();
		
		sm.addOutput(new ConsoleOutput());
		sm.addOutput(new FarmShapefileOutput());
		sm.addOutput(out);
		
		
		// création et lancement du simulateur
		CfmFarmSimulator s = new CfmFarmSimulator(sm);
		s.allRun();

				
		return (GamaMap) out.getRes();
		// vérification des contraintes
		//farm.checkConstraintSystem(start, end, true);
	}
	
	private static void initConstraints(String farmCode, String coverfile, String nextCoverFile) {
		// intégration des couverts
		CoverFactory.init(farm,coverfile, null);

		// mise en place des contraintes
		ConstraintBuilder cb = new ConstraintBuilder(farm);
		
		cb.setCode("ALL0");
		cb.setType(ConstraintType.NextCover);
		cb.setParams(nextCoverFile);
		cb.build();
		
		cb.setCode("ALL1");
		cb.setType(ConstraintType.Duration);
		cb.setDomain("1");
		cb.build();
		
		farm.getConstraintSystem().display();
	}

	
}
