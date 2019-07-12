package ummisco.gama.camisole.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
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

@vars({ @variable(name = IApsfSkill.APSF_SOIL, type = IType.NONE, doc = @doc("Contains the virtual soil")),
	@variable(name = IApsfSkill.APSF_SOIL_FACTORY, type = IType.NONE, doc = @doc("Contains the virtual soil")),
	@variable(name = IApsfSkill.DEVELOPPED_TEMPLATE, type = IType.MAP, doc = @doc("Contains the virtual soil")),
	@variable(name = IApsfSkill.SOIL_DIVIDING, type = IType.INT, doc = @doc("soil dividing")),
	@variable(name = IApsfSkill.SOIL_SIZE, type = IType.FLOAT, doc = @doc("size of the soil in m")),
	@variable(name = IApsfSkill.COUPLED_MODEL, type = IType.LIST, doc = @doc("models")) })
@skill(name = IApsfSkill.SKILL_NAME, concept = { IConcept.SKILL })

public class ApsfSkill extends Skill {
	


	private Apsf getAPSFSoil(IAgent agent)
	{
		Apsf inMM = (Apsf) agent.getAttribute(IApsfSkill.APSF_SOIL);
		return inMM;
	}
	
	
	private Map<Template, Agglomerate> getDeveloppedTemplate(IAgent agent)
	{
		Map<Template, Agglomerate> inMM = (Map<Template, Agglomerate>) agent.getAttribute(IApsfSkill.DEVELOPPED_TEMPLATE);
		if(inMM==null)
		{
			inMM = new HashMap<Template, Agglomerate>();
			agent.setAttribute(IApsfSkill.DEVELOPPED_TEMPLATE, inMM);
		}
		return inMM;
	}
	
	private SoilFactory getAPSFSoilFactory(IAgent agent)
	{
		SoilFactory inMM = (SoilFactory) agent.getAttribute(IApsfSkill.APSF_SOIL_FACTORY);
		if(inMM==null)
		{
			inMM = new SoilFactory();
			agent.setAttribute(IApsfSkill.APSF_SOIL_FACTORY, inMM);
		}
		return inMM;
	}
	
	public ArrayList<InputData> getOrganicInputlist(IAgent agent)
	{
		ArrayList<InputData> inMM = (ArrayList<InputData>) agent.getAttribute(IApsfSkill.GRANULOMETRIC_DATA_OM);
		if(inMM==null)
		{
			inMM = new ArrayList<InputData>();
			agent.setAttribute(IApsfSkill.GRANULOMETRIC_DATA_OM, inMM);
		}
		return inMM;
	}
	
	@action(name = IApsfSkill.GRANULOMETRIC_DEFINE_COMMAND, args = {
			@arg(name = IApsfSkill.MINERAL_MATTER, type = IType.FLOAT, optional=true, doc = @doc("quantity of minerals")),
			@arg(name = IApsfSkill.ORGANIC_MATTER, type = IType.FLOAT, optional=true, doc = @doc("quantity of organic matter")),
			@arg(name = IApsfSkill.MAX_BOUNDARY, type = IType.FLOAT, optional=true, doc = @doc("max boundary")),
			@arg(name = IApsfSkill.MIN_BOUNDARY, type = IType.FLOAT, optional=true, doc = @doc("min boundary")),
			@arg(name = IApsfSkill.BOUNDARY, type = IType.FLOAT, optional=true, doc = @doc("boundary of granulometric data"))}, doc = @doc(value = "", returns = "", examples = {
					@example("") }))
	public void defineGranulometricData(IScope scope)
	{
		final IAgent agent = scope.getAgent();
		Object umineralMatter = scope.getArg(IApsfSkill.MINERAL_MATTER, IType.FLOAT);
		Object uorganicMatter = scope.getArg(IApsfSkill.ORGANIC_MATTER, IType.FLOAT);
		Object umaxBoundary = scope.getArg(IApsfSkill.MAX_BOUNDARY, IType.FLOAT);
		Object uminBoundary = scope.getArg(IApsfSkill.MIN_BOUNDARY, IType.FLOAT);
		Object uboundary = scope.getArg(IApsfSkill.BOUNDARY, IType.LIST);
		double minBoundary = 0;
		double maxBoundary = Double.MAX_VALUE;
		
		if(uminBoundary != null&& ((Double)uminBoundary).doubleValue()!= 0)
			minBoundary = Math.max(minBoundary, ((Double)uminBoundary).doubleValue()*100);
		if(umaxBoundary != null &&((Double)umaxBoundary).doubleValue()!= 0)
			maxBoundary = Math.min(maxBoundary, ((Double)umaxBoundary).doubleValue()*100);
				
		if(uboundary != null && ((GamaList<Double>)uboundary).size() >= 2)
		{
			GamaList<Double> bd = (GamaList<Double>)uboundary;
			double min = bd.get(0).floatValue();
			double max = bd.get(1).floatValue();
			minBoundary = Math.min(minBoundary, min);
			maxBoundary = Math.min(maxBoundary, max);
		}
		
		SoilFactory factory = getAPSFSoilFactory(agent); 
		if(umineralMatter != null && ((Double)umineralMatter)!=0)
		{
			float mineralMatter= ((Double)umineralMatter).floatValue()*1000;
			factory.addMineralGranulometricScale(mineralMatter, minBoundary, maxBoundary);
			System.out.println("mineral\t" + mineralMatter+ "\tmin:"+minBoundary+"\tmax:"+maxBoundary );
		}
	
		if(uorganicMatter != null && ((Double)uorganicMatter).floatValue()!=0)
		{
			float organicMatter= ((Double)uorganicMatter).floatValue()*1000;
			factory.addOMGranulometricScale(organicMatter, minBoundary, maxBoundary);
			System.out.println("organic\t" + organicMatter+ "\tmin:"+minBoundary+"\tmax:"+maxBoundary );

		}
	}
	
	
	@action(name = IApsfSkill.SOIL_DEFINE_COMMAND, args = {
			@arg(name = IApsfSkill.SOIL_SIZE, type = IType.FLOAT, optional=false, doc = @doc("width of the cube")),
			@arg(name = IApsfSkill.SOIL_DIVIDING, type = IType.INT, optional=false, doc = @doc("soil spliting factor")),
			@arg(name = IApsfSkill.DEFAULT_SPECIES, type = IType.SPECIES, optional=false, doc = @doc("default particle species")),
			@arg(name = IApsfSkill.NUMBER_OF_TRY, type = IType.INT, optional=false, doc = @doc("number of try")),
			@arg(name = IApsfSkill.BULK_DENSITY, type = IType.FLOAT, optional=false, doc = @doc("max boundary"))}, doc = @doc(value = "", returns = "", examples = {
					@example("") }))
	public void configureAndBuild(IScope scope)
	{
		final IAgent agent = scope.getAgent();
		Object soilSize = scope.getArg(IApsfSkill.SOIL_SIZE, IType.FLOAT);
		GamlSpecies defaultSpecies = (GamlSpecies)scope.getArg(IApsfSkill.DEFAULT_SPECIES, IType.SPECIES);
		Object soilDividing = scope.getArg(IApsfSkill.SOIL_DIVIDING, IType.INT);
		Object soilDensity = scope.getArg(IApsfSkill.BULK_DENSITY, IType.FLOAT);
		Object iteration = scope.getArg(IApsfSkill.NUMBER_OF_TRY, IType.INT);
		
		double width =  soilSize==null?20.0:(((Double)soilSize).doubleValue())*100;
		int divide =  soilDividing==null?10:(((Integer)soilDividing).intValue());
		float bulk =  soilDensity==null?20:(((Double)soilDensity).floatValue());
		int nbtry =  iteration==null||(((Integer)iteration).intValue())==0?20000:(((Integer)iteration).intValue());
		SoilFactory soil = getAPSFSoilFactory(agent);
		Apsf virtualSoil = soil.compileAndBuild(bulk, width, divide,nbtry, agent);
		agent.setAttribute(IApsfSkill.APSF_SOIL, virtualSoil);
		virtualSoil.setDefaultSpecies(defaultSpecies);
	//	agent.setAttribute(IApsfSkill.DEFAULT_SPECIES_VAR, defaultSpecies);
	}
	
	
	
	@action (
			name = IApsfSkill.SOIL_TEMPLATE_LIST_COMMAND, args = {},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	
	public IList<String> getCreatedTemplate(IScope scope)
	{
		final IAgent agent = scope.getAgent();
		
		Apsf soil = (Apsf) (agent.getAttribute(IApsfSkill.APSF_SOIL));
		GamaList<String> res =(GamaList<String>) GamaListFactory.create();
		
		for(Template t :soil.getAPSF().getTemplate().getAllSubTemplate())
		{
			res.add(t.getTemplateName());
		}
		return res;
	}
	
	public static String userToApsf(String s)
	{
		if(s.equalsIgnoreCase(IApsfParticleSkill.POROUS))
			return IParticle.WHITE_PARTICLE;
		if(s.equalsIgnoreCase(IApsfParticleSkill.ORGANIC))
			return IParticle.ORGANIC_MATTER_PARTICLE;
		if(s.equalsIgnoreCase(IApsfParticleSkill.MINERAL))
			return IParticle.SAND_PARTICLE;
		return s;
	}
	
	public static String apsfToUser(Particle s)
	{
		if(s.getTemplateName().equalsIgnoreCase(IParticle.WHITE_PARTICLE))
			return IApsfParticleSkill.POROUS;
		if(s.getTemplateName().equalsIgnoreCase(IParticle.ORGANIC_MATTER_PARTICLE))
			return IApsfParticleSkill.ORGANIC;
		if(s.getTemplateName().equalsIgnoreCase( IParticle.SAND_PARTICLE))
			return IApsfParticleSkill.MINERAL;
		return s.getTemplateName();
	}
	
	@action(name = IApsfSkill.ASSOCIATE_PROCESS_TO_TEMPLATE_COMMAND, args = {
			@arg(name = IApsfSkill.TEMPLATE_NAME, type = IType.STRING, optional=false, doc = @doc("name of the template")),
			@arg(name = IApsfSkill.AT_SCALE, type = IType.INT, optional=false, doc = @doc("name of the template")),
			@arg(name = IApsfSkill.PROCESS_NAME, type = IType.SPECIES, optional=false, doc = @doc("name of the process species")),
			@arg(name = IApsfSkill.PARTICLE_NAME, type = IType.STRING, optional=false, doc = @doc("porous, sand, organic"))}, doc = @doc(value = "", returns = "", examples = {
					@example("") }))
	public void associateSpeciesToTemplate(IScope scope)
	{
		final IAgent agent  = scope.getAgent();
		String templateName = (String) scope.getArg(IApsfSkill.TEMPLATE_NAME, IType.STRING);
		int scale = ((Integer) scope.getArg(IApsfSkill.AT_SCALE, IType.INT)).intValue();
		GamlSpecies processName  = (GamlSpecies) scope.getArg(IApsfSkill.PROCESS_NAME, IType.SPECIES);
		String particleName = (String) scope.getArg(IApsfSkill.PARTICLE_NAME, IType.STRING);
		
		List<Object[]> processes = (List<Object[]>) agent.getAttribute(IApsfSkill.COUPLED_MODEL);
		if(processes == null)
		{
			processes = new ArrayList<Object[]>();
			agent.setAttribute(IApsfSkill.COUPLED_MODEL, processes);
		}
		
		
		Apsf soil = this.getAPSFSoil(agent);
		ArrayList<Template> templateNames = soil.getAPSF().getTemplate().getAllSubTemplate();
		if(!this.containsTemplate(templateNames, templateName))
			return; 
		Template tt = soil.getTemplateWithName(templateName);
		tt.addProcess(processName, userToApsf(particleName), scale);
		Object[] ttmp = {processName,tt,particleName,new Integer(scale) };
		processes.add(ttmp);

	}
	@action(name = "apply_processes")
	public void initializeProcesses(IScope scope)
	{
		final IAgent agent  = scope.getAgent();
		Apsf soil = this.getAPSFSoil(agent);
		
		Map<Template, Agglomerate> ss = getDeveloppedTemplate(agent);
		List<Object[]> processes = (List<Object[]>) agent.getAttribute(IApsfSkill.COUPLED_MODEL);
		for(Object[] ttmp:processes)
		{
			Agglomerate a = ss.get(ttmp[1]);
			Particle ppx = soil.getOneParticleWithCharacteristics(scope,(Template) ttmp[1], ((Integer)ttmp[3]).intValue(), userToApsf((String)ttmp[2]),true);
			a = ppx.getParent();
			ss.put((Template) ttmp[1], a);
			a.deploySubParticles(scope);
	
		}
	}
	
	private boolean containsTemplate(ArrayList<Template> templateNames, String name)
	{
		boolean res = false;
		name = name.toUpperCase();
		for(Template t:templateNames)
		{
			if(t.getTemplateName().toUpperCase().equals(name))
				return true;
		}
		if(name.equals(IApsfParticleSkill.MINERAL.toUpperCase())||name.equals(IApsfParticleSkill.ORGANIC.toUpperCase())||name.equals(IApsfParticleSkill.POROUS.toUpperCase()))
			return true;
		return false;
	}
	
	
	
	

}
