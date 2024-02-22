package gama.experimental.apsf.spaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import gama.experimental.apsf.exception.UnBreakableParticle;
import gama.experimental.apsf.template.Template;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.gaml.species.GamlSpecies;

// les méthodes de cette classe sont inspirées du MicrobesEnvironnement de sworm

public class Apsf {
	public static float DEFAULT_WORLD_DIMENSION = 20; // taille en cm
	public static int DIVISION_PER_LEVEL = 10;

	private final double dimension;
	private int division_per_level;
	private Agglomerate spaceRoot;
	private Map<String, Template> templates;
	private final IAgent macroAgent;
	private GamlSpecies mySpecies;

	public void setDefaultSpecies(final GamlSpecies sp) {
		this.mySpecies = sp;
	}

	public GamlSpecies getDefaultSpecies() {
		return this.mySpecies;
	}

	public IAgent getUnderworldAgent() {
		return macroAgent;
	}

	public int getDivisionPerLevel() {
		return division_per_level;
	}

	public void setDivisionPerLevel(final int division_per_level) {
		this.division_per_level = division_per_level;
	}

	public Apsf(final IAgent agt) {
		this.dimension = DEFAULT_WORLD_DIMENSION;
		this.templates = new HashMap<>();
		this.macroAgent = agt;
	}

	public Apsf(final double dim, final int div, final IAgent agt) {
		this.dimension = dim;
		this.division_per_level = div;
		this.macroAgent = agt;
	}

	public Template getTemplateWithName(final String name) {
		return this.templates.get(name);
	}

	public double getDimension() {
		return dimension;
	}

	public Agglomerate getAPSF() {
		return spaceRoot;
	}

	public void defineTemplateTree(final Template t) {
		this.templates = new HashMap<>();
		final ArrayList<Template> tps = t.getAllSubTemplate();
		for (final Template tt : tps) {
			templates.put(tt.getTemplateName(), tt);
		}
		final SoilLocation loc = new SoilLocation(0, 0, 0, 0, this);
		this.spaceRoot = new Agglomerate(t);
		this.spaceRoot.setLocation(loc);
	}

	public ArrayList<Template> findBranchTo(final Template t) {
		if (t == this.getAPSF().getTemplate()) {
			final ArrayList<Template> res = new ArrayList<>();
			res.add(t);
			return res;
		}
		final Collection<Template> tt = templates.values();
		for (final Template tm : tt) {
			if (tm.isParentOf(t.getTemplateName())) {
				final ArrayList<Template> res = findBranchTo(tm);
				res.add(0, t);
				return res;
			}
		}
		return null;
	}

	SoilLocation chooseLocationInside(final Agglomerate p, final String type, final boolean sampled) {
		final Particle pp = p.getTemplate().getSamplerWithCharacteristics(type);

		/*
		 * ArrayList<Particle> ppp = ((Agglomerate)p).getTemplate().getCellsWithCharacteristics(type);
		 *
		 * int idx = ((int)(Math.random() * 1000)) % ppp.size(); pp = ppp.get(idx);
		 */
		final SoilLocation s2 = p.getSubParticleLocation(pp.getPI(), pp.getPJ(), pp.getPK());
		return s2;

	}

	public Particle getOneParticleWithCharacteristics(final IScope scope, final Template t, int scale,
			final String type, final boolean sampled) {
		scale = scale + 1;
		Particle choosedP = this.getAPSF();
		final ArrayList<Template> lst = findBranchTo(t);
		final int lstSize = lst.size();
		int i = lstSize - 1;
		while (i >= 0 && scale >= lstSize - i) {
			String choosenType = type;
			if (i > 0 && lstSize - i != scale) {

				choosenType = lst.get(i - 1).getTemplateName();
			}
			final SoilLocation s2 = chooseLocationInside((Agglomerate) choosedP, choosenType, sampled);
			choosedP = getParticleAtLocation(scope, s2);
			i = i - 1;
		}
		if (scale == lstSize - i) { return choosedP; }
		int currentScale = lstSize;
		while (currentScale < scale) {
			final SoilLocation s2 = chooseLocationInside((Agglomerate) choosedP, choosedP.getTemplateName(), sampled);
			choosedP = getParticleAtLocation(scope, s2);
			currentScale = currentScale + 1;
		}

		final SoilLocation s2 = chooseLocationInside((Agglomerate) choosedP, type, sampled);
		choosedP = getParticleAtLocation(scope, s2);
		return choosedP;
	}

	public Particle getParticleAtLocation(final IScope scope, final SoilLocation loc) {
		Particle p = spaceRoot.findParticle(scope, loc);
		while (p instanceof Agglomerate && p.getLocation().getScale() != loc.getScale()) {
			final SoilLocation mloc = SoilLocation.changeScale(loc, p.getLocation().getScale() + 1);
			final Particle pp = ((Agglomerate) p).getTemplate().getNewParticleInstance(scope, mloc);
			pp.setParent((Agglomerate) p);
			try {
				p.putParticle(scope, pp);
			} catch (final UnBreakableParticle e) {
				p = spaceRoot.findParticle(scope, loc);

			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p = pp;
		}
		return p;
	}

	public double countTotalOrganicMatter() {
		return countTotalMatter(IParticle.ORGANIC_MATTER_PARTICLE);

	}

	public double countTotalSandMatter() {
		return countTotalMatter(IParticle.SAND_PARTICLE);

	}

	public double countOrganicMatterAtSizeScale(final int sizeScale) {
		return countMatterAtSizeScale(sizeScale, IParticle.ORGANIC_MATTER_PARTICLE);
	}

	public double countSandMatterAtSizeScale(final int sizeScale) {
		return countMatterAtSizeScale(sizeScale, IParticle.SAND_PARTICLE);
	}

	public double countPorousMatterAtSizeScale(final int sizeScale) {
		return countMatterAtSizeScale(sizeScale, IParticle.WHITE_PARTICLE);
	}

	public double countTotalMatter(final String name) {
		double res = 0;
		int i = 0;
		while (i < 4) {
			final double tmpRes = countMatterAtSizeScale(i, name);
			res += tmpRes;
			i++;
		}
		return res;
	}

	public double countMatterAtSizeScale(final int sizeScale, final String name) {
		final long nbTCell = (long) Math.pow(spaceRoot.getTemplate().getNumberOfCell(), sizeScale + 1);
		final long nbCellFound = countMatterAtSizeScale(sizeScale, name, spaceRoot.getTemplate(), 0);
		return (double) nbCellFound / nbTCell;
	}

	private long countMatterAtSizeScale(final int sizeScale, final String name, final Template t,
			final int currentScale) {
		long sum = 0;
		if (sizeScale == currentScale) {
			sum = t.getNbCellsOfTemplateWithName(name);
		} else {
			final Enumeration<Template> e = t.getSubTemplates();
			while (e.hasMoreElements()) {
				final Template subT = e.nextElement();
				final long rate = t.getNbCellsOfTemplateWithName(subT.getTemplateName());
				sum += rate * countMatterAtSizeScale(sizeScale, name, subT, currentScale + 1);
			}
		}
		return sum;
	}

}
