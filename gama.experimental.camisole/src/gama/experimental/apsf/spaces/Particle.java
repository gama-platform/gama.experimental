/*
 * Particle.java : environment Copyright (C) 2003-2006 Nicolas Marilleau
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
/*
 * Particle.java : environment Copyright (C) 2003-2006 Nicolas Marilleau
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package gama.experimental.apsf.spaces;

import java.util.ArrayList;
import java.util.List;

import gama.experimental.apsf.exception.APSFException;
import gama.experimental.apsf.exception.UnBreakableParticle;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;

/**
 * @author marilleauni
 *
 */
public abstract class Particle {
	public final static String SEPARATOR_PARTICLE = ":";
	public static boolean GRAVITY_PROCESS = false;
	public final static String DEFAULT_WHO_GENERATE = "soil";
	protected int level;
	protected double size;
	protected boolean porosityHaveBeenModified;
	private String whoGenerate = DEFAULT_WHO_GENERATE;

	/** organic matter quantity in centimeters */
	protected double organicMatterWeight;
	/** mineral quantity in centimeters */
	protected double solidWeight;
	/** Porosity of the particle */
	protected double porosity;

	protected int pI, pJ, pK; // coord dans le cannevas

	protected IAgent agent;

	protected List<IAgent> associatedProcesses;

	private Agglomerate parent;

	protected SoilLocation location;

	public IAgent getAgent() {
		return this.agent;
	}

	public List<IAgent> getAssociatedProcesses() {
		return this.associatedProcesses;
	}

	public void setAgent(final IAgent ag) {
		this.agent = ag;
	}

	public void addProcesses(final IAgent agt) {
		this.associatedProcesses.add(agt);
	}

	/**
	 *
	 */
	public Particle() {
		super();
		porosityHaveBeenModified = true;
		porosity = 0;
		whoGenerate = DEFAULT_WHO_GENERATE;
		this.associatedProcesses = new ArrayList<>();
	}

	public void reduceMatters() {
		if (this.getParent() != null) {
			this.getParent().reduceMatters(this);
		}
		porosityHaveBeenModified = true;
	}

	public void reduceMatters(final Particle p) {
		if (this.getParent() != null) {
			this.getParent().reduceMatters(p);
			// System.out.println("reduced om "+p.getOrganicMatterWeight());
		}

		this.organicMatterWeight -= p.getOrganicMatterWeight();
		this.solidWeight -= p.getSolidWeight();
		porosityHaveBeenModified = true;
	}

	public void initMatters(final double om, final double sm) {
		this.organicMatterWeight = om;
		this.solidWeight = sm;
	}

	public void addMatters() {
		if (this.getParent() != null) {
			this.getParent().addMatters(this);
		}
		porosityHaveBeenModified = true;
	}

	public void addMatters(final Particle p) {
		if (this.getParent() != null) {
			this.getParent().addMatters(p);
		}
		// System.out.println("added om "+p.getOrganicMatterWeight());
		this.organicMatterWeight += p.getOrganicMatterWeight();
		this.solidWeight += p.getSolidWeight();
		porosityHaveBeenModified = true;
	}

	public Particle(final Particle p) {
		this();
		porosityHaveBeenModified = true;
		this.level = p.level;
		this.size = p.size;
		this.pI = p.pI;
		this.pJ = p.pJ;
		this.pK = p.pK;
	}

	public void putParticle(final IScope scope, final Particle arg0) throws APSFException {
		throw new UnBreakableParticle();
	}

	@Override
	public abstract Particle clone();

	public double getAbsoluteSize() {
		final long max = SoilLocation.getMaxCoordinateAccordingToScale(this.location.getScale(), this.getWorld());
		final double res = this.getWorld().getDimension() / max;
		return res;
		// return
		// 1.0/((SoilLocation)this.getLocation()).fact(((SoilLocation)this.getLocation()).getScale(),this.getWorld().getDivisionPerLevel())*this.getWorld().getDimension();
	}

	public void setMatrixCoordinate(final int x, final int y, final int z) {
		// Coordonnées dans le canevas
		pI = x;
		pJ = y;
		pK = z;
	}

	public SoilLocation getLocation() {
		return this.location;
	}

	public double getVolume() {
		return this.getAbsoluteSize() * this.getAbsoluteSize() * this.getAbsoluteSize();

	}

	public double getSurface() {
		return 6 * this.getAbsoluteSize() * this.getAbsoluteSize();

	}

	/*
	 * public void setMatrixCoordinate(Particle p) { SoilLocation currLoc=(SoilLocation)this.getLocation(); SoilLocation
	 * pLoc=(SoilLocation)p.getLocation(); float x=currLoc.getX()-pLoc.getX(); float y=currLoc.getY()-pLoc.getY(); float
	 * z=currLoc.getZ()-pLoc.getZ();
	 * 
	 * int i,j,k; for(i=0;(i)*(size/DIVISION_PER_LEVEL)<x;i++); for(j=0;(j)*(size/DIVISION_PER_LEVEL)<y;j++);
	 * for(k=0;(k)*(size/DIVISION_PER_LEVEL)<z;k++);
	 * 
	 * this.setMatrixCoordinate(i,j,k);
	 * 
	 * }
	 */
	//
	// private static int getMatrixId(int v,int scale, int scaleMin, Apsf app)
	// {
	// int factor=(int)SoilLocation.fact(scale,app.getDivisionPerLevel());
	// while(scale>scaleMin)
	// {
	// factor=factor/app.getDivisionPerLevel();
	// v=v%factor;
	// scale--;
	// }
	// return v/scale;
	// }
	public static int[] getMatrixId(final SoilLocation p, final int scale) {
		final SoilLocation newLoc = SoilLocation.changeScale(p, scale);
		final SoilLocation newLoc2 = SoilLocation.changeScale(p, scale - 1);

		final SoilLocation newLoc3 = SoilLocation.changeScale(newLoc2, scale);

		// System.out.println(newLoc+" "+newLoc2+" "+newLoc3);

		final int[] res = new int[3];
		res[0] = newLoc.getX() - newLoc3.getX();
		res[1] = newLoc.getY() - newLoc3.getY();
		res[2] = newLoc.getZ() - newLoc3.getZ();

		return res;
		// SoilLocation newLoc2=SoilLocation.changeScale(p,scale-1);

		// return getMatrixId( newLoc,newLoc2,scale);
	}

	public static int[] getMatrixId(final SoilLocation p, final SoilLocation agglo, final int scale) {
		final int[] r = new int[3];
		final int[] r2 = new int[3];
		final int[] result = new int[3];
		final SoilLocation newLoc = SoilLocation.changeScale(p, scale);
		final SoilLocation newLoc2 = SoilLocation.changeScale(agglo, scale);
		result[0] = newLoc.getX() - newLoc2.getX();
		result[1] = newLoc.getY() - newLoc2.getY();
		result[2] = newLoc.getZ() - newLoc2.getZ();
		/*
		 * r2=getMatrixId(agglo,scale); r=getMatrixId(p,scale); result[0]=r2[0]-r[0]; result[1]=r2[0]-r[1];
		 * result[2]=r2[0]-r[2];
		 */
		return result;
	}

	public String getKey() {
		return "(" + pI + "," + pJ + "," + pK + ")";
	}

	public static String getKey(final int pI, final int pJ, final int pK) {
		return "(" + pI + "," + pJ + "," + pK + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GISTool.GISEnvironment.Face#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(final String arg0, final Object arg1) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GISTool.GISEnvironment.Face#getAttribute(java.lang.String)
	 */
	public Object getAttribute(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GISTool.GISEnvironment.Face#parseString(java.lang.String)
	 */
	public Object parseString(final String arg0) {
		final String[] parameters = arg0.split(SEPARATOR_PARTICLE);

		level = Integer.valueOf(parameters[0]).intValue();
		size = Integer.valueOf(parameters[1]).intValue();
		pI = Integer.valueOf(parameters[2]).intValue();
		pJ = Integer.valueOf(parameters[3]).intValue();
		pK = Integer.valueOf(parameters[4]).intValue();
		this.whoGenerate = parameters[5];
		return this;
	}

	@Override
	public String toString() {
		return level + SEPARATOR_PARTICLE + size + SEPARATOR_PARTICLE + pI + SEPARATOR_PARTICLE + pJ
				+ SEPARATOR_PARTICLE + pK + SEPARATOR_PARTICLE + this.whoGenerate;
	}

	public int getLevel() {
		return level;
	}

	public int getPI() {
		return pI;
	}

	public int getPK() {
		return pK;
	}

	public int getPJ() {
		return pJ;
	}

	public String getWhoGenerate() {
		return this.whoGenerate;
	}

	public void setWhoGenerate(final String g) {
		this.whoGenerate = g;
	}

	public double getSize() {
		return size;
	}

	public Agglomerate getParent() {
		return parent;
	}

	public void setParent(final Agglomerate parent) {
		this.parent = parent;
		this.organicMatterWeight = parent.getOrganicMatterMass();
		this.solidWeight = parent.getSolidMass();
		this.getLocation().setWorld(parent.getLocation().getWorld());
	}

	public double getParticleSize(final SoilLocation loc) {
		final int max = (int) loc.getMaxCoordinateAccordingToScale(loc.getWorld());
		return this.getWorld().getDimension() / (double) max;
	}

	/**
	 * @return the organicMatterWeight
	 */
	public double getOrganicMatterWeight() {
		return organicMatterWeight;
	}

	/**
	 * @param organicMatterWeight
	 *            the organicMatterWeight to set
	 */
	public void setOrganicMatterWeight(final double organicMatterWeight) {
		this.organicMatterWeight = organicMatterWeight;
	}

	/**
	 * @return the solidWeight
	 */
	public double getSolidWeight() {
		return solidWeight;
	}

	/**
	 * @param solidWeight
	 *            the solidWeight to set
	 */
	public void setSolidWeight(final double solidWeight) {
		this.solidWeight = solidWeight;
	}

	public double getPorosity() {
		return porosity;
	}

	public void setLocation(final SoilLocation location) {
		this.location = location;
	}

	public Apsf getWorld() {
		return this.getLocation().getWorld();
	}

	public abstract String getTemplateName();

}
