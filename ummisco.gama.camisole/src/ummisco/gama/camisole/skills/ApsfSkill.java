package ummisco.gama.camisole.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.skills.Skill;
import msi.gaml.species.GamlSpecies;
import msi.gaml.types.IType;
import ummisco.gama.apsf.spaces.Agglomerate;
import ummisco.gama.apsf.spaces.Apsf;
import ummisco.gama.apsf.spaces.IParticle;
import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.template.Template;
import ummisco.gama.camisole.InputData;
import ummisco.gama.camisole.SoilFactory;

@vars ({ @variable (
		name = IApsfSkill.APSF_SOIL,
		type = IType.NONE,
		doc = @doc ("Contains the virtual soil")),
		@variable (
				name = IApsfSkill.APSF_SOIL_FACTORY,
				type = IType.NONE,
				doc = @doc ("Contains the virtual soil")),
		@variable (
				name = IApsfSkill.DEVELOPPED_TEMPLATE,
				type = IType.MAP,
				doc = @doc ("Contains the virtual soil")),
		@variable (
				name = IApsfSkill.SOIL_DIVIDING,
				type = IType.INT,
				doc = @doc ("soil dividing")),
		@variable (
				name = IApsfSkill.SOIL_SIZE,
				type = IType.FLOAT,
				doc = @doc ("size of the soil in m")),
		@variable (
				name = IApsfSkill.COUPLED_MODEL,
				type = IType.LIST,
				doc = @doc ("models")) })
@skill (
		name = IApsfSkill.SKILL_NAME,
		concept = { IConcept.SKILL })

public class ApsfSkill extends Skill {

	private Apsf getAPSFSoil(final IAgent agent) {
		final Apsf inMM = (Apsf) agent.getAttribute(IApsfSkill.APSF_SOIL);
		return inMM;
	}

	@SuppressWarnings ("unchecked")
	private Map<Template, Agglomerate> getDeveloppedTemplate(final IAgent agent) {
		Map<Template, Agglomerate> inMM =
				(Map<Template, Agglomerate>) agent.getAttribute(IApsfSkill.DEVELOPPED_TEMPLATE);
		if (inMM == null) {
			inMM = new HashMap<>();
			agent.setAttribute(IApsfSkill.DEVELOPPED_TEMPLATE, inMM);
		}
		return inMM;
	}

	private SoilFactory getAPSFSoilFactory(final IAgent agent) {
		SoilFactory inMM = (SoilFactory) agent.getAttribute(IApsfSkill.APSF_SOIL_FACTORY);
		if (inMM == null) {
			inMM = new SoilFactory();
			agent.setAttribute(IApsfSkill.APSF_SOIL_FACTORY, inMM);
		}
		return inMM;
	}

	@SuppressWarnings ("unchecked")
	public ArrayList<InputData> getOrganicInputlist(final IAgent agent) {
		ArrayList<InputData> inMM = (ArrayList<InputData>) agent.getAttribute(IApsfSkill.GRANULOMETRIC_DATA_OM);
		if (inMM == null) {
			inMM = new ArrayList<>();
			agent.setAttribute(IApsfSkill.GRANULOMETRIC_DATA_OM, inMM);
		}
		return inMM;
	}

	@SuppressWarnings ("unchecked")
	@action (
			name = IApsfSkill.GRANULOMETRIC_DEFINE_COMMAND,
			args = { @arg (
					name = IApsfSkill.MINERAL_MATTER,
					type = IType.FLOAT,
					optional = true,
					doc = @doc ("quantity of minerals")),
					@arg (
							name = IApsfSkill.ORGANIC_MATTER,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("quantity of organic matter")),
					@arg (
							name = IApsfSkill.MAX_BOUNDARY,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("max boundary")),
					@arg (
							name = IApsfSkill.MIN_BOUNDARY,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("min boundary")),
					@arg (
							name = IApsfSkill.BOUNDARY,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("boundary of granulometric data")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void defineGranulometricData(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final Object umineralMatter = scope.getArg(IApsfSkill.MINERAL_MATTER, IType.FLOAT);
		final Object uorganicMatter = scope.getArg(IApsfSkill.ORGANIC_MATTER, IType.FLOAT);
		final Object umaxBoundary = scope.getArg(IApsfSkill.MAX_BOUNDARY, IType.FLOAT);
		final Object uminBoundary = scope.getArg(IApsfSkill.MIN_BOUNDARY, IType.FLOAT);
		final Object uboundary = scope.getArg(IApsfSkill.BOUNDARY, IType.LIST);
		double minBoundary = 0;
		double maxBoundary = Double.MAX_VALUE;

		if (uminBoundary != null && ((Double) uminBoundary).doubleValue() != 0) {
			minBoundary = Math.max(minBoundary, ((Double) uminBoundary).doubleValue() * 100);
		}
		if (umaxBoundary != null && ((Double) umaxBoundary).doubleValue() != 0) {
			maxBoundary = Math.min(maxBoundary, ((Double) umaxBoundary).doubleValue() * 100);
		}

		if (uboundary != null && ((IList<Double>) uboundary).size() >= 2) {
			final IList<Double> bd = (IList<Double>) uboundary;
			final double min = bd.get(0).floatValue();
			final double max = bd.get(1).floatValue();
			minBoundary = Math.min(minBoundary, min);
			maxBoundary = Math.min(maxBoundary, max);
		}

		final SoilFactory factory = getAPSFSoilFactory(agent);
		if (umineralMatter != null && (Double) umineralMatter != 0) {
			final float mineralMatter = ((Double) umineralMatter).floatValue() * 1000;
			factory.addMineralGranulometricScale(mineralMatter, minBoundary, maxBoundary);
			System.out.println("mineral\t" + mineralMatter + "\tmin:" + minBoundary + "\tmax:" + maxBoundary);
		}

		if (uorganicMatter != null && ((Double) uorganicMatter).floatValue() != 0) {
			final float organicMatter = ((Double) uorganicMatter).floatValue() * 1000;
			factory.addOMGranulometricScale(organicMatter, minBoundary, maxBoundary);
			System.out.println("organic\t" + organicMatter + "\tmin:" + minBoundary + "\tmax:" + maxBoundary);

		}
	}

	@action (
			name = IApsfSkill.SOIL_DEFINE_COMMAND,
			args = { @arg (
					name = IApsfSkill.SOIL_SIZE,
					type = IType.FLOAT,
					optional = false,
					doc = @doc ("width of the cube")),
					@arg (
							name = IApsfSkill.SOIL_DIVIDING,
							type = IType.INT,
							optional = false,
							doc = @doc ("soil spliting factor")),
					@arg (
							name = IApsfSkill.DEFAULT_SPECIES,
							type = IType.SPECIES,
							optional = false,
							doc = @doc ("default particle species")),
					@arg (
							name = IApsfSkill.NUMBER_OF_TRY,
							type = IType.INT,
							optional = false,
							doc = @doc ("number of try")),
					@arg (
							name = IApsfSkill.BULK_DENSITY,
							type = IType.FLOAT,
							optional = false,
							doc = @doc ("max boundary")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void configureAndBuild(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final Object soilSize = scope.getArg(IApsfSkill.SOIL_SIZE, IType.FLOAT);
		final GamlSpecies defaultSpecies = (GamlSpecies) scope.getArg(IApsfSkill.DEFAULT_SPECIES, IType.SPECIES);
		final Object soilDividing = scope.getArg(IApsfSkill.SOIL_DIVIDING, IType.INT);
		final Object soilDensity = scope.getArg(IApsfSkill.BULK_DENSITY, IType.FLOAT);
		final Object iteration = scope.getArg(IApsfSkill.NUMBER_OF_TRY, IType.INT);

		final double width = soilSize == null ? 20.0 : ((Double) soilSize).doubleValue() * 100;
		final int divide = soilDividing == null ? 10 : ((Integer) soilDividing).intValue();
		final float bulk = soilDensity == null ? 20 : ((Double) soilDensity).floatValue();
		final int nbtry =
				iteration == null || ((Integer) iteration).intValue() == 0 ? 20000 : ((Integer) iteration).intValue();
		final SoilFactory soil = getAPSFSoilFactory(agent);
		final Apsf virtualSoil = soil.compileAndBuild(bulk, width, divide, nbtry, agent);
		agent.setAttribute(IApsfSkill.APSF_SOIL, virtualSoil);
		virtualSoil.setDefaultSpecies(defaultSpecies);
		// agent.setAttribute(IApsfSkill.DEFAULT_SPECIES_VAR, defaultSpecies);
	}

	@action (
			name = IApsfSkill.SOIL_TEMPLATE_LIST_COMMAND,
			args = {},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))

	public IList<String> getCreatedTemplate(final IScope scope) {
		final IAgent agent = scope.getAgent();

		final Apsf soil = (Apsf) agent.getAttribute(IApsfSkill.APSF_SOIL);
		final IList<String> res = GamaListFactory.create();

		for (final Template t : soil.getAPSF().getTemplate().getAllSubTemplate()) {
			res.add(t.getTemplateName());
		}
		return res;
	}

	public static String userToApsf(final String s) {
		if (s.equalsIgnoreCase(IApsfParticleSkill.POROUS)) { return IParticle.WHITE_PARTICLE; }
		if (s.equalsIgnoreCase(IApsfParticleSkill.ORGANIC)) { return IParticle.ORGANIC_MATTER_PARTICLE; }
		if (s.equalsIgnoreCase(IApsfParticleSkill.MINERAL)) { return IParticle.SAND_PARTICLE; }
		return s;
	}

	public static String apsfToUser(final Particle s) {
		if (s.getTemplateName().equalsIgnoreCase(IParticle.WHITE_PARTICLE)) { return IApsfParticleSkill.POROUS; }
		if (s.getTemplateName().equalsIgnoreCase(IParticle.ORGANIC_MATTER_PARTICLE)) {
			return IApsfParticleSkill.ORGANIC;
		}
		if (s.getTemplateName().equalsIgnoreCase(IParticle.SAND_PARTICLE)) { return IApsfParticleSkill.MINERAL; }
		return s.getTemplateName();
	}

	@SuppressWarnings ("unchecked")
	@action (
			name = IApsfSkill.ASSOCIATE_PROCESS_TO_TEMPLATE_COMMAND,
			args = { @arg (
					name = IApsfSkill.TEMPLATE_NAME,
					type = IType.STRING,
					optional = false,
					doc = @doc ("name of the template")),
					@arg (
							name = IApsfSkill.AT_SCALE,
							type = IType.INT,
							optional = false,
							doc = @doc ("name of the template")),
					@arg (
							name = IApsfSkill.PROCESS_NAME,
							type = IType.SPECIES,
							optional = false,
							doc = @doc ("name of the process species")),
					@arg (
							name = IApsfSkill.PARTICLE_NAME,
							type = IType.STRING,
							optional = false,
							doc = @doc ("porous, sand, organic")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void associateSpeciesToTemplate(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final String templateName = (String) scope.getArg(IApsfSkill.TEMPLATE_NAME, IType.STRING);
		final int scale = ((Integer) scope.getArg(IApsfSkill.AT_SCALE, IType.INT)).intValue();
		final GamlSpecies processName = (GamlSpecies) scope.getArg(IApsfSkill.PROCESS_NAME, IType.SPECIES);
		final String particleName = (String) scope.getArg(IApsfSkill.PARTICLE_NAME, IType.STRING);

		List<Object[]> processes = (List<Object[]>) agent.getAttribute(IApsfSkill.COUPLED_MODEL);
		if (processes == null) {
			processes = new ArrayList<>();
			agent.setAttribute(IApsfSkill.COUPLED_MODEL, processes);
		}

		final Apsf soil = this.getAPSFSoil(agent);
		final ArrayList<Template> templateNames = soil.getAPSF().getTemplate().getAllSubTemplate();
		if (!this.containsTemplate(templateNames, templateName)) { return; }
		final Template tt = soil.getTemplateWithName(templateName);
		tt.addProcess(processName, userToApsf(particleName), scale);
		final Object[] ttmp = { processName, tt, particleName, new Integer(scale) };
		processes.add(ttmp);

	}

	@SuppressWarnings ("unchecked")
	@action (
			name = "apply_processes")
	public void initializeProcesses(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final Apsf soil = this.getAPSFSoil(agent);

		final Map<Template, Agglomerate> ss = getDeveloppedTemplate(agent);
		final List<Object[]> processes = (List<Object[]>) agent.getAttribute(IApsfSkill.COUPLED_MODEL);
		for (final Object[] ttmp : processes) {
			Agglomerate a = ss.get(ttmp[1]);
			final Particle ppx = soil.getOneParticleWithCharacteristics(scope, (Template) ttmp[1],
					((Integer) ttmp[3]).intValue(), userToApsf((String) ttmp[2]), true);
			a = ppx.getParent();
			ss.put((Template) ttmp[1], a);
			a.deploySubParticles(scope);

		}
	}

	private boolean containsTemplate(final ArrayList<Template> templateNames, String name) {
		name = name.toUpperCase();
		for (final Template t : templateNames) {
			if (t.getTemplateName().toUpperCase().equals(name)) { return true; }
		}
		if (name.equals(IApsfParticleSkill.MINERAL.toUpperCase())
				|| name.equals(IApsfParticleSkill.ORGANIC.toUpperCase())
				|| name.equals(IApsfParticleSkill.POROUS.toUpperCase())) {
			return true;
		}
		return false;
	}

}
