package ummisco.gama.camisole.skills;

import java.util.List;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import ummisco.gama.apsf.spaces.Particle;

@vars({ 
	@variable(name = IAPSFProcessSkill.FOLLOWED_PARTICLE_INT, type = IType.NONE, doc = @doc("followed particle in the soil")),
	@variable(name = IAPSFProcessSkill.FOLLOWED_PARTICLE, type = IType.AGENT, doc = @doc("followed particle in the soil")),
	@variable(name = IAPSFProcessSkill.LOCAL_PROCESSES, type = IType.AGENT, doc = @doc("soil processes in the same particle"))

})

@skill(name = IAPSFProcessSkill.SKILL_NAME, concept = { IConcept.SKILL })
public class APSFProcessSkill extends Skill{
	
	
	private Particle getParticle(IAgent agt)
	{
		Particle p =(Particle) agt.getAttribute(IAPSFProcessSkill.FOLLOWED_PARTICLE_INT);
		return p;
	}
	
	@getter(IAPSFProcessSkill.FOLLOWED_PARTICLE)
	public IAgent getParticleAgent(IAgent scope)
	{
		System.out.println("coucou toto est la "+ getParticle(scope).getAgent());
		return getParticle(scope).getAgent();
	}
	
	@getter(IAPSFProcessSkill.LOCAL_PROCESSES)
	public List<IAgent> getLocalProcesses(IAgent scope)
	{
		return getParticle(scope).getAssociatedProcesses();
	}
	

	
	

}
