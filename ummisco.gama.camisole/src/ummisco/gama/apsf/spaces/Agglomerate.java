/*
 * Agglomerate.java : environment Copyright (C) 2003-2006 Nicolas Marilleau
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
package ummisco.gama.apsf.spaces;

import java.util.Enumeration;
import java.util.Hashtable;

import msi.gama.runtime.IScope;
import ummisco.gama.apsf.exception.APSFException;
import ummisco.gama.apsf.exception.AlreadyCreatedParticle;
import ummisco.gama.apsf.exception.UnBreakableParticle;
import ummisco.gama.apsf.template.Template;

public class Agglomerate extends Particle {

	private Template template;
	// private String templateName;
	private final Hashtable<String, Particle> subParticle;
	private double referenceOMWeight;
	private double referenceSolidWeight;

	// precomptuted mass
	private double organicMatterMassPerCell;
	private double solidMassPerCell;
	private double fractalMassPerCell;

	public void deploySubParticles(final IScope agt) {
		final int nbCellPerLevel = template.getSize();
		for (int i = 0; i < nbCellPerLevel; i++) {
			for (int j = 0; j < nbCellPerLevel; j++) {
				for (int k = 0; k < nbCellPerLevel; k++) {
					final SoilLocation s = getSubParticleLocation(i, j, k);
					this.getWorld().getParticleAtLocation(agt, s);
				}
			}
		}
	}

	public SoilLocation getSubParticleLocation(final int i, final int j, final int k) {
		final SoilLocation se = SoilLocation.changeScale(this.location, this.location.getScale() + 1);
		return new SoilLocation(se.getX() + i, se.getY() + j, se.getZ() + k, se.getScale(), this.getWorld());
	}

	@Override
	public void setParent(final Agglomerate parent) {
		super.setParent(parent);
		final int nbCell = this.getWorld().getDivisionPerLevel();
		this.organicMatterWeight =
				(parent.referenceOMWeight - parent.organicMatterMassPerCell * parent.getTemplate().getOrganic()
						* this.getWorld().getDivisionPerLevel()) / (parent.getTemplate().getFractal() * nbCell);
		this.solidWeight = (parent.referenceSolidWeight
				- parent.solidMassPerCell * parent.getTemplate().getSolid() * this.getWorld().getDivisionPerLevel())
				/ (parent.getTemplate().getFractal() * nbCell);
		// System.out.println("MASS "+organicMatterWeight+" " +solidWeight);
		precomputeMatters();

	}

	public float countSolidQuantity() {
		float result = 0;
		final Enumeration<Particle> e = subParticle.elements();
		while (e.hasMoreElements()) {
			final Object o = e.nextElement();
			if (o instanceof OrganicMatter) {
				result++;
			}
			if (o instanceof Agglomerate) {
				result += ((Agglomerate) o).countSolidQuantity();
			}
		}
		return result / (this.getWorld().getDivisionPerLevel() * this.getWorld().getDivisionPerLevel()
				* this.getWorld().getDivisionPerLevel());
	}

	public float countDeployedOrganicQuantityInFractal() {
		float result = 0;
		final Enumeration<Particle> e = subParticle.elements();
		while (e.hasMoreElements()) {
			final Object o = e.nextElement();
			if (o instanceof Agglomerate) {
				result += ((Agglomerate) o).getOrganicMatter();
			}
		}
		return result / (this.getWorld().getDivisionPerLevel() * this.getWorld().getDivisionPerLevel()
				* this.getWorld().getDivisionPerLevel());
	}

	public int countDeployedFractal() {
		int result = 0;
		final Enumeration<Particle> e = subParticle.elements();
		while (e.hasMoreElements()) {
			final Object o = e.nextElement();
			if (o instanceof Agglomerate) {
				result++;
			}
		}
		return result;
	}

	public int countDeployedOrganicMatter() {
		int result = 0;
		final Enumeration<Particle> e = subParticle.elements();
		while (e.hasMoreElements()) {
			final Particle o = e.nextElement();
			final Class<?> c = this.getTemplate().getParticleClass(o.getPI(), o.getPJ(), o.getPK());

			if (c.equals(OrganicMatter.class)) {
				result++;
			}
		}
		return result;
	}

	public Enumeration<Particle> getSubParticle() {
		return subParticle.elements();
	}

	public Agglomerate() {
		super();

		this.subParticle = new Hashtable<>();
		this.organicMatterWeight = 0;
		this.referenceOMWeight = 0;

	}

	public Agglomerate(final Template ft) {
		super();

		this.subParticle = new Hashtable<>();
		this.template = ft;
		this.organicMatterWeight = 0;
		this.referenceOMWeight = 0;
	}

	public Agglomerate(final Agglomerate a) {
		super(a);
		this.organicMatterWeight = a.organicMatterWeight;
		this.subParticle = new Hashtable<>();
		this.template = a.template;
		this.referenceOMWeight = a.referenceOMWeight;
		this.agent = a.agent;
	}

	@Override
	public void setAttribute(final String arg0, final Object arg1) {}

	@Override
	public Object getAttribute(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return level + SEPARATOR_PARTICLE + (int) size + SEPARATOR_PARTICLE + pI + SEPARATOR_PARTICLE + pJ
				+ SEPARATOR_PARTICLE + pK + SEPARATOR_PARTICLE + organicMatterWeight + SEPARATOR_PARTICLE + solidWeight
				+ SEPARATOR_PARTICLE + referenceOMWeight + SEPARATOR_PARTICLE + referenceSolidWeight
				+ SEPARATOR_PARTICLE + organicMatterMassPerCell + SEPARATOR_PARTICLE + solidMassPerCell
				+ SEPARATOR_PARTICLE + fractalMassPerCell + SEPARATOR_PARTICLE + this.getTemplateName();
	}

	@Override
	public Object parseString(final String arg0) {
		final String[] parameters = arg0.split(SEPARATOR_PARTICLE);
		level = Integer.valueOf(parameters[0]).intValue();
		pI = Integer.valueOf(parameters[2]).intValue();
		pJ = Integer.valueOf(parameters[3]).intValue();
		pK = Integer.valueOf(parameters[4]).intValue();
		organicMatterWeight = Float.valueOf(parameters[5]).floatValue();
		solidWeight = Float.valueOf(parameters[6]).floatValue();
		referenceOMWeight = Float.valueOf(parameters[7]).floatValue();
		referenceSolidWeight = Float.valueOf(parameters[8]).floatValue();
		organicMatterMassPerCell = Float.valueOf(parameters[9]).floatValue();
		solidMassPerCell = Float.valueOf(parameters[10]).floatValue();
		fractalMassPerCell = Float.valueOf(parameters[11]).floatValue();
		// templateName=parameters[12];

		return this;
	}

	public Particle findParticle(final IScope scope, final SoilLocation pLoc) {
		if (pLoc.equals(this.getLocation())) { return this; }

		int[] loc = new int[3];
		final SoilLocation currentLoc = this.getLocation();
		loc = Particle.getMatrixId(pLoc, currentLoc.getScale() + 1);
		final String key = Particle.getKey(loc[0], loc[1], loc[2]);
		if (pLoc.getScale() == currentLoc.getScale() + 1) {
			final Particle sub = this.subParticle.get(key);
			if (sub != null) {
				return sub;
			} else {
				return this;
			}
		} else {
			final Particle sub = this.subParticle.get(key);
			if (sub != null) {
				if (sub instanceof Agglomerate) {
					return ((Agglomerate) sub).findParticle(scope, pLoc);
				} else {
					return sub;
				}
			} else {
				return this;
			}
		}
	}

	public void removeSubParticle(final Particle p) {
		subParticle.remove(p.getKey());
		porosityHaveBeenModified = true;
	}

	@Override
	public void putParticle(final IScope scope, final Particle p) throws APSFException {
		porosityHaveBeenModified = true;
		int[] loc;
		final SoilLocation currentLoc = this.getLocation();
		final SoilLocation pLoc = p.getLocation();
		loc = Particle.getMatrixId(pLoc, currentLoc, currentLoc.getScale() + 1);

		if (pLoc.getScale() == currentLoc.getScale() + 1) {
			p.setMatrixCoordinate(loc[0], loc[1], loc[2]);
			final Particle ptmp = this.subParticle.get(p.getKey());

			if (ptmp != null) {
				// la particule existe d�ja, on ne faire rien
				// return;
				throw new AlreadyCreatedParticle();
			}
			// if(!(this.template.isInAccordanceWithTemplate(p,loc[0],loc[1],loc[2])))
			// throw new UnBreakableParticle();

			p.setMatrixCoordinate(loc[0], loc[1], loc[2]);
			this.subParticle.put(p.getKey(), p);
			p.setParent(this);
		} else {
			final String key = Particle.getKey(loc[0], loc[1], loc[2]);
			Particle sub = this.subParticle.get(key);
			if (sub != null) {
				if (!(sub instanceof Agglomerate)) {
					// System.out.println("nes pas agglo");
					throw new UnBreakableParticle();
				}
				sub.putParticle(scope, p);
			} else {
				final SoilLocation s = SoilLocation.changeScale(p.getLocation(), currentLoc.getScale() + 1);
				final SoilLocation newLoc = new SoilLocation(s.getX() + loc[0], s.getY() + loc[1], s.getZ() + loc[2],
						currentLoc.getScale() + 1, s.getWorld());
				sub = this.getTemplate().getNewParticleInstance(scope, newLoc);// createSubAgglo();
				sub.putParticle(scope, p);
			}
		}

	}

	public int getNbCellWithType(final Class<WhiteParticle> particleType) {
		int result = 0;
		final Enumeration<Particle> it = this.subParticle.elements();
		// compute the number of pore for this scale
		while (it.hasMoreElements()) {
			final Particle p = it.nextElement();
			if (p.getClass().equals(particleType)) {
				if (this.template.getParticleClass(p.getPI(), p.getPJ(), p.getPK()).equals(particleType)) {
					// result--;
				} else {
					result++;
				}
			} else if (this.template.getParticleClass(p.getPI(), p.getPJ(), p.getPK()).equals(particleType)) {
				result--;
			}
		}
		// System.out.println("nb Particle trouvee interm�diaire "+ result+" " +particleType);
		// count the number of pores in the canvas
		final int nbCellPerLevel = template.getSize();
		for (int i = 0; i < nbCellPerLevel; i++) {
			for (int j = 0; j < nbCellPerLevel; j++) {
				for (int k = 0; k < nbCellPerLevel; k++) {
					if (template.getParticleClass(i, j, k).equals(particleType)) {
						result++;
					}
				}
			}
		}

		// System.out.println("nb Particle trouvee "+ result+" " +particleType+ " "+resultTemp);
		return result;
	}

	public double getPorosityInFractalCells() {
		double result = 0;

		final Hashtable<String, Integer> nbFractalWithTemplate = new Hashtable<>();
		final Hashtable<String, Template> templates = new Hashtable<>();
		final Enumeration<Particle> it = this.subParticle.elements();
		// compute the number of pore for this scale
		while (it.hasMoreElements()) {
			final Particle p = it.nextElement();
			final Particle tP = this.template.getParticle(p.getPI(), p.getPJ(), p.getPK());
			if (p instanceof Agglomerate) {
				result += ((Agglomerate) p).getPorosity();
			}

			if (tP instanceof Agglomerate) {
				final Template temp = ((Agglomerate) tP).getTemplate();

				if (templates.containsKey(temp.getTemplateName())) {
					final Integer count = nbFractalWithTemplate.get(temp.getTemplateName());
					nbFractalWithTemplate.remove(temp.getTemplateName());
					nbFractalWithTemplate.put(temp.getTemplateName(), new Integer(count.intValue() - 1));
				} else {
					nbFractalWithTemplate.put(temp.getTemplateName(), new Integer(-1));
					templates.put(temp.getTemplateName(), temp);
				}
			}
		}

		final int nbCellPerLevel = template.getSize();
		for (int i = 0; i < nbCellPerLevel; i++) {
			for (int j = 0; j < nbCellPerLevel; j++) {
				for (int k = 0; k < nbCellPerLevel; k++) {
					final Particle p = template.getParticle(i, j, k);
					if (p instanceof Agglomerate) {
						final String tempName = ((Agglomerate) p).getTemplateName();
						if (templates.containsKey(tempName)) {
							final Integer count = nbFractalWithTemplate.get(tempName);
							nbFractalWithTemplate.remove(tempName);
							nbFractalWithTemplate.put(tempName, new Integer(count.intValue() + 1));
						} else {
							nbFractalWithTemplate.put(tempName, new Integer(1));
							templates.put(tempName, ((Agglomerate) p).getTemplate());
						}
					}
				}
			}
		}
		final Enumeration<String> keys = templates.keys();

		while (keys.hasMoreElements()) {
			final String key = keys.nextElement();
			final Template template = templates.get(key);
			final Integer qte = nbFractalWithTemplate.get(key);
			result += qte.intValue() * template.getPorosity(this.location.getWorld());
		}
		return result / this.getWorld().getDivisionPerLevel();
	}

	public double getPorosityAtCurrentSubSpaceLevel() {
		final int nbWhite = getNbCellWithType(WhiteParticle.class);
		final double cellSizeFactor = this.getWorld().getDivisionPerLevel();
		return nbWhite / cellSizeFactor;
	}

	@Override
	public double getPorosity() {
		// if(!porosityHaveBeenModified)
		// return this.porosity;
		double result = getPorosityAtCurrentSubSpaceLevel();
		// System.out.println("PorosityAtCurrentSubSpaceLevel "+result);
		final double fractalPorosity = getPorosityInFractalCells();
		// System.out.println("Fractal Porosity "+fractalPorosity);
		porosityHaveBeenModified = false;
		result += fractalPorosity;
		this.porosity = result;
		return result;
	}

	public double getOrganicMatter() {
		return organicMatterWeight;
	}

	protected void setOrganicMatter(final float organicMatter) {
		this.organicMatterWeight = organicMatter;
		this.referenceOMWeight = organicMatter;
	}

	@Override
	public Particle clone() {
		// TODO Auto-generated method stub
		return new Agglomerate(this);
	}

	public Template getTemplate() {
		return template;
	}

	public double getReferenceOMQte() {
		return referenceOMWeight;
	}

	public void setReferenceOMQte(final double referenceOMQte) {
		this.referenceOMWeight = referenceOMQte;
	}

	public double getReferenceMineralWeight() {
		return referenceSolidWeight;
	}

	public void setReferenceMineralWeight(final double referenceMineralWeight) {
		this.referenceSolidWeight = referenceMineralWeight;
	}

	public double getReferenceOMWeight() {
		return referenceOMWeight;
	}

	public void setReferenceOMWeight(final double referenceOMWeight) {
		this.referenceOMWeight = referenceOMWeight;
	}

	/**
	 * @return the fractalMass
	 */
	public double getFractalMass() {
		return fractalMassPerCell;
	}

	/**
	 * @param fractalMass
	 *            the fractalMass to set
	 */
	public void setFractalMass(final double fractalMass) {
		this.fractalMassPerCell = fractalMass;
	}

	/**
	 * @return the organicMatterMass
	 */
	public double getOrganicMatterMass() {
		return organicMatterMassPerCell;
	}

	/**
	 * @param organicMatterMass
	 *            the organicMatterMass to set
	 */
	public void setOrganicMatterMass(final double organicMatterMass) {
		this.organicMatterMassPerCell = organicMatterMass;
	}

	/**
	 * @return the solidMass
	 */
	public double getSolidMass() {
		return solidMassPerCell;
	}

	/**
	 * @param solidMass
	 *            the solidMass to set
	 */
	public void setSolidMass(final double solidMass) {
		this.solidMassPerCell = solidMass;
	}

	@Override
	public void initMatters(final double OM, final double solidMatter) {
		this.organicMatterWeight = OM;
		this.solidWeight = solidMatter;
		precomputeMatters();
	}

	private void precomputeMatters() {
		this.referenceOMWeight = this.organicMatterWeight;
		this.referenceSolidWeight = this.solidWeight;
		double cellSize = 0;
		if (this.getParent() == null) {
			cellSize = SoilLocation.getCellSize(1, this.getWorld());
		} else {
			final SoilLocation ft = this.getParent().getLocation();
			cellSize = SoilLocation.getCellSize(ft.getScale() + 2, this.getWorld());
		}
		final double cellVol = cellSize * cellSize * cellSize;
		/* compute the mass of organic matter */
		organicMatterMassPerCell = getTemplate().getOrganicMatterVolumicMass() * cellVol;
		/* compute the mass of solid matter */
		solidMassPerCell = getTemplate().getSolidVolumicMass() * cellVol;
		/* compute the mass of sub fractal */
		fractalMassPerCell = referenceOMWeight + referenceSolidWeight - this.getWorld().getDivisionPerLevel()
				* (getTemplate().getOrganic() * organicMatterMassPerCell + getTemplate().getSolid() * solidMassPerCell);
	}

	@Override
	public String getTemplateName() {
		return template == null ? null : template.getTemplateName();
	}

	public void setTemplate(final Template t) {
		this.template = t;
	}
}
