package gama.experimental.camisole.skills;

import java.util.List;

import gama.experimental.apsf.spaces.Particle;
import gama.core.metamodel.agent.IAgent;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;

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
