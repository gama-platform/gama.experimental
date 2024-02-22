package gama.experimental.camisole.skills;

import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SoilLocation;

import javax.vecmath.Point3d;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@vars ({ @variable (
		name = IApsfParticleSkill.FOLLOWED_PARTICLE_INT,
		type = IType.NONE,
		doc = @doc ("followed particle in the soil")),
		@variable (
				name = IKeyword.LOCATION,
				type = IType.POINT,
				doc = @doc ("location")),
		@variable (
				name = IApsfParticleSkill.MY_NEIGHBORS,
				type = IType.LIST,
				doc = @doc ("neighbors of cell")),
		@variable (
				name = IApsfParticleSkill.MY_NEIGHBORS_LOCAL,
				type = IType.LIST,
				doc = @doc ("local neighbors of cell")),
		@variable (
				name = IKeyword.SIZE,
				type = IType.FLOAT,
				doc = @doc ("location")),
		@variable (
				name = IApsfParticleSkill.I,
				type = IType.INT,
				doc = @doc ("i location of particle in the soil")),
		@variable (
				name = IApsfParticleSkill.J,
				type = IType.INT,
				doc = @doc ("j location of particle in the soil")),
		@variable (
				name = IApsfParticleSkill.K,
				type = IType.INT,
				doc = @doc ("k location of particle in the soil")),
		@variable (
				name = IApsfParticleSkill.SCALE,
				type = IType.INT,
				doc = @doc ("scale location of particle in the soil")),
		@variable (
				name = IApsfParticleSkill.ORGANIC_MATTER,
				type = IType.FLOAT,
				doc = @doc ("quantity of organic matter in the particle")),
		@variable (
				name = IApsfParticleSkill.PARTICLE_TYPE,
				type = IType.STRING,
				doc = @doc ("scale location of particle in the soil")) })

@skill (
		name = IApsfParticleSkill.SKILL_NAME,
		concept = { IConcept.SKILL })
public class ApsfParticleSkill extends Skill {

	private boolean isBuiltinAttribute(final IAgent agt, final String name) {
		final String data[] = { IApsfParticleSkill.FOLLOWED_PARTICLE_INT, IKeyword.LOCATION,
				IApsfParticleSkill.MY_NEIGHBORS, IApsfParticleSkill.MY_NEIGHBORS_LOCAL, IKeyword.SIZE,
				IApsfParticleSkill.I, IApsfParticleSkill.J, IApsfParticleSkill.K, IApsfParticleSkill.SCALE,
				IApsfParticleSkill.ORGANIC_MATTER, IApsfParticleSkill.PARTICLE_TYPE };
		for (final String s : data) {
			if (s.equals(name)) { return true; }
		}
		return false;
	}

	private Particle getParticle(final IAgent agt) {
		final Particle p = (Particle) agt.getAttribute(IApsfParticleSkill.FOLLOWED_PARTICLE_INT);
		return p;
	}

	@getter (IApsfParticleSkill.I)
	public int getI(final IAgent scope) {
		final Particle p = getParticle(scope);
		if (p == null) { return -1; }
		return p.getPI();
	}

	@getter (
			value = IKeyword.LOCATION,
			initializer = true)
	public GamaPoint getParticuleLocation(final IAgent scope) {
		final Particle p = getParticle(scope);
		if (p == null) { return null; }
		final Point3d loc3D = p.getLocation().getAbsoluteCoordinate();
		final IAgent agt = p.getWorld().getUnderworldAgent();
		final GamaPoint lc = agt.getLocation();
		final double width = p.getWorld().getDimension() / 200;
		final double sz = p.getAbsoluteSize() / 2;
		return new GamaPoint((loc3D.x + sz) / 100.0 - width + lc.getX(), (loc3D.y + sz) / 100.0 - width + lc.getY(),
				(loc3D.z + sz) / 100.0 - width + lc.getZ());
	}

	@getter (
			value = IKeyword.SIZE,
			initializer = true)
	public float getParticuleSize(final IAgent scope) {
		final Particle p = getParticle(scope);
		if (p == null) { return 0.0f; }
		return (float) p.getAbsoluteSize() / 100;
	}

	@getter (IApsfParticleSkill.J)
	public int getJ(final IAgent scope) {
		final Particle p = getParticle(scope);
		if (p == null) { return -1; }
		return p.getPJ();
	}

	@getter (IApsfParticleSkill.K)
	public int getK(final IAgent scope) {
		final Particle p = getParticle(scope);
		if (p == null) { return -1; }
		return p.getPK();
	}

	@getter (IApsfParticleSkill.SCALE)
	public int getScale(final IAgent scope) {
		final Particle p = getParticle(scope);
		if (p == null) { return -1; }
		return p.getLevel();
	}

	@getter (IApsfParticleSkill.ORGANIC_MATTER)
	public float getOrganicMatter(final IScope scope) {
		final Particle p = getParticle(scope.getAgent());
		if (p == null) {
			return 0;
		} else {
			return (float) p.getOrganicMatterWeight();
		}
	}

	@setter (IApsfParticleSkill.ORGANIC_MATTER)
	public void setOrganicMatter(final IAgent scope, final double value) {
		final Particle p = getParticle(scope.getAgent());
		if (p == null) { return; }
		p.setOrganicMatterWeight(value);

	}

	@getter (IApsfParticleSkill.PARTICLE_TYPE)
	public String getParticleType(final IScope scope) {
		final Particle p = getParticle(scope.getAgent());
		if (p == null) {
			return null;
		} else {
			return ApsfSkill.apsfToUser(p);
		}

	}

	public IList<IAgent> getNeighbors(final IScope scope, final boolean local) {
		final IList<IAgent> res = GamaListFactory.create(scope, Types.AGENT);
		final Particle p = getParticle(scope.getAgent());
		final SoilLocation s = p.getLocation();
		final int max = (int) SoilLocation.getMaxCoordinateAccordingToScale(s.getScale(), s.getWorld());
		final int nbPerCub = s.getWorld().getDivisionPerLevel();
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					int mx = 0;
					int my = 0;
					int mz = 0;

					if (local) {
						mx = torusCoordinate(s.getX() + i, max, nbPerCub);
						my = torusCoordinate(s.getY() + j, max, nbPerCub);
						mz = torusCoordinate(s.getZ() + k, max, nbPerCub);
					} else {
						mx = torusCoordinate(s.getX() + i, max);
						my = torusCoordinate(s.getY() + j, max);
						mz = torusCoordinate(s.getZ() + k, max);
					}
					final SoilLocation ss = new SoilLocation(mx, my, mz, s.getScale(), p.getWorld());
					final Particle p2 = p.getWorld().getParticleAtLocation(scope, ss);
					res.add(p2.getAgent());
				}
			}
		}
		return res;
	}

	@getter (IApsfParticleSkill.MY_NEIGHBORS)
	public IList<IAgent> getNeighborsGlobal(final IScope scope) {
		return getNeighbors(scope, false);
	}

	@getter (IApsfParticleSkill.MY_NEIGHBORS_LOCAL)
	public IList<IAgent> getNeighborsLocal(final IScope scope) {
		return getNeighbors(scope, true);
	}

	private int torusCoordinate(final int val, final int range) {
		if (val < 0) { return range + val; }
		if (val >= range) { return val - range; }
		return val;
	}

	private int torusCoordinate(final int val, final int range, final int localTorus) {
		final int res1 = torusCoordinate(val, range);
		return res1 % localTorus;
	}

	GamaPoint getCellLocation(final IScope scope) {
		final Particle p = getParticle(scope.getAgent());
		final SoilLocation loc = p.getLocation();
		final Point3d xyz = loc.getAbsoluteCoordinate();
		final GamaPoint res = new GamaPoint(xyz.x, xyz.y, xyz.z);
		return res;
	}

}
