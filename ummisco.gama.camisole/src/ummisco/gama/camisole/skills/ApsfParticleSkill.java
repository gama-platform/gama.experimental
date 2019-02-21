package ummisco.gama.camisole.skills;

import msi.gama.precompiler.IConcept;

import java.util.ArrayList;

import javax.vecmath.Point3d;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.spaces.SoilLocation;


@vars({ 
	@variable(name = IApsfParticleSkill.FOLLOWED_PARTICLE_INT, type = IType.NONE, doc = @doc("followed particle in the soil")),
	@variable(name = IKeyword.LOCATION, type = IType.POINT, doc = @doc("location")),
	@variable(name = IApsfParticleSkill.MY_NEIGHBORS, type =IType.LIST, doc = @doc("neighbors of cell")),
	@variable(name = IApsfParticleSkill.MY_NEIGHBORS_LOCAL, type =IType.LIST, doc = @doc("local neighbors of cell")),
	@variable(name = IKeyword.SIZE, type = IType.FLOAT, doc = @doc("location")),
	@variable(name = IApsfParticleSkill.I, type = IType.INT, doc = @doc("i location of particle in the soil")),
	@variable(name = IApsfParticleSkill.J, type = IType.INT, doc = @doc("j location of particle in the soil")),
	@variable(name = IApsfParticleSkill.K, type = IType.INT, doc = @doc("k location of particle in the soil")),
	@variable(name = IApsfParticleSkill.SCALE, type = IType.INT, doc = @doc("scale location of particle in the soil")),
	@variable(name = IApsfParticleSkill.ORGANIC_MATTER, type = IType.FLOAT, doc = @doc("quantity of organic matter in the particle")),
	@variable(name = IApsfParticleSkill.PARTICLE_TYPE, type = IType.STRING, doc = @doc("scale location of particle in the soil"))
	})

@skill(name = IApsfParticleSkill.SKILL_NAME, concept = { IConcept.SKILL })
public class ApsfParticleSkill extends Skill {
	
	
	private boolean isBuiltinAttribute(IAgent agt, String name)
	{
		String data [ ] = {IApsfParticleSkill.FOLLOWED_PARTICLE_INT,
				IKeyword.LOCATION,
				IApsfParticleSkill.MY_NEIGHBORS,
				IApsfParticleSkill.MY_NEIGHBORS_LOCAL,
				IKeyword.SIZE,IApsfParticleSkill.I,
				IApsfParticleSkill.J,
				IApsfParticleSkill.K,
				IApsfParticleSkill.SCALE,
				IApsfParticleSkill.ORGANIC_MATTER,
				IApsfParticleSkill.PARTICLE_TYPE};
		for(String s:data)
			if(s.equals(name))
				return true;
		return false;
	}
	
	private Particle getParticle(IAgent agt)
	{
		Particle p =(Particle) agt.getAttribute(IApsfParticleSkill.FOLLOWED_PARTICLE_INT);
		return p;
	}
	
	@getter(IApsfParticleSkill.I)
	public int getI(IAgent scope)
	{
		Particle p =  getParticle(scope);
		if(p == null)
			return -1;
		return p.getPI();
	}
	
	@getter (
			value = IKeyword.LOCATION,
			initializer = true)
	public GamaPoint getParticuleLocation(IAgent scope)
	{
		Particle p =  getParticle(scope);
		if(p == null)
			return null;
		Point3d loc3D = p.getLocation().getAbsoluteCoordinate();
		IAgent agt = p.getWorld().getUnderworldAgent();
		ILocation lc = agt.getLocation();
		double width = p.getWorld().getDimension() / 200;
		double sz = p.getAbsoluteSize() / 2;
		return new GamaPoint((loc3D.x+sz) / 100.0 - width  + lc.getX(),(loc3D.y+sz)  / 100.0 - width + lc.getY(),(loc3D.z+sz)  / 100.0 - width + lc.getZ());
	}
	@getter (
			value = IKeyword.SIZE,
			initializer = true)
	public float getParticuleSize(IAgent scope)
	{
		Particle p =  getParticle(scope);
		if(p == null)
			return 0.0f;
		return (float) p.getAbsoluteSize()/100;
	}
	
	@getter(IApsfParticleSkill.J)
	public int getJ(IAgent scope)
	{
		Particle p =  getParticle(scope);
		if(p == null)
			return -1;
		return p.getPJ();
	}
	
	@getter(IApsfParticleSkill.K)
	public int getK(IAgent scope)
	{
		Particle p =  getParticle(scope);
		if(p == null)
			return -1;
		return p.getPK();
	}
	
	@getter(IApsfParticleSkill.SCALE)
	public int getScale(IAgent scope)
	{
		Particle p =  getParticle(scope);
		if(p == null)
			return -1;
		return p.getLevel();
	}
	
	@getter(IApsfParticleSkill.ORGANIC_MATTER)
	public float getOrganicMatter(IScope scope)
	{
		Particle p =  getParticle(scope.getAgent());
		if(p == null)
			return 0;
		else
			return (float)p.getOrganicMatterWeight();
	}
	@setter(IApsfParticleSkill.ORGANIC_MATTER)
	public void setOrganicMatter(final IAgent scope, double value)
	{
		Particle p =  getParticle(scope.getAgent());
		if(p == null)
			return ;
		p.setOrganicMatterWeight(value);
		
	}
	
	
	@getter(IApsfParticleSkill.PARTICLE_TYPE)
	public String getParticleType(IScope scope)
	{
		Particle p =  getParticle(scope.getAgent());
		if(p == null)
			return null;
		else
			return ApsfSkill.apsfToUser(p);

	}
	
	public IList<IAgent> getNeighbors(IScope scope, boolean local)
	{
		IList<IAgent> res =GamaListFactory.create(scope, Types.AGENT);
		Particle p =  getParticle(scope.getAgent());
		SoilLocation s = p.getLocation();
		int max = (int)SoilLocation.getMaxCoordinateAccordingToScale(s.getScale(),s.getWorld());
		int nbPerCub = s.getWorld().getDivisionPerLevel();
		for(int i=-1;i<=1;i++)
			for(int j=-1;j<=1;j++)
				for(int k=-1;k<=1;k++)
				{
					int mx = 0;
					int my = 0;
					int mz = 0;
					
					if(local)
						{
						 mx = torusCoordinate(s.getX()+ i,max,nbPerCub);
						 my = torusCoordinate(s.getY()+ j,max,nbPerCub);
						 mz = torusCoordinate(s.getZ()+ k,max,nbPerCub);
						}
					else
					{
						 mx = torusCoordinate(s.getX()+ i,max);
						 my = torusCoordinate(s.getY()+ j,max);
						 mz = torusCoordinate(s.getZ()+ k,max);
					}
					SoilLocation ss = new SoilLocation(mx,my,mz,s.getScale(),p.getWorld());
					Particle p2 = p.getWorld().getParticleAtLocation(scope, ss);
					res.add(p2.getAgent());
				}
		return res;
	}

	@getter(IApsfParticleSkill.MY_NEIGHBORS)
	public IList<IAgent> getNeighborsGlobal(IScope scope)
	{
		return getNeighbors(scope,false);
	}
	@getter(IApsfParticleSkill.MY_NEIGHBORS_LOCAL)
	public IList<IAgent> getNeighborsLocal(IScope scope)
	{
		return getNeighbors(scope,true);
	}
	
	private int torusCoordinate(int val, int range)
	{
		if(val < 0)
			return range + val;
		if(val >=range)
			return val - range;
		return val;
	}
	private int torusCoordinate(int val, int range, int localTorus)
	{
		int res1 = torusCoordinate(val,range);
		return res1%localTorus;
	}
	
	GamaPoint getCellLocation(IScope scope)
	{
		Particle p = getParticle(scope.getAgent());
		SoilLocation loc = p.getLocation();
		Point3d xyz = loc.getAbsoluteCoordinate();
		GamaPoint res = new GamaPoint(xyz.x,xyz.y,xyz.z);
		return res;
	}
	
	
	
	
	

}
