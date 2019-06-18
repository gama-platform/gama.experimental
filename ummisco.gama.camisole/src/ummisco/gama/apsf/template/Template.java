/*
 * FractalTemplate.java : ummisco.gama.environment Copyright (C) 2003-2006 Nicolas Marilleau
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
package ummisco.gama.apsf.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gaml.species.GamlSpecies;
import ummisco.gama.apsf.spaces.Agglomerate;
import ummisco.gama.apsf.spaces.Apsf;
import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.spaces.SoilLocation;
import ummisco.gama.apsf.spaces.WhiteParticle;
import ummisco.gama.camisole.skills.IAPSFProcessSkill;
import ummisco.gama.camisole.skills.IApsfParticleSkill;

public abstract class Template {

	public static String TEMPLATE_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Canvas ["
			+ "\n<!ELEMENT Canvas (Cell*)>" + "\n<!ATTLIST SpaceElement Name CDATA #REQUIRED>"
			+ "\n<!ATTLIST SpaceElement Class CDATA #REQUIRED>" + "\n<!ATTLIST SpaceElement Solid CDATA #REQUIRED>"
			+ "\n<!ATTLIST SpaceElement Fractal CDATA #REQUIRED>" + "\n<!ATTLIST SpaceElement Organic CDATA #REQUIRED>"
			+ "\n<!ATTLIST SpaceElement SideSize CDATA #REQUIRED>" +

			"\n<!ELEMENT Cell (#PCDATA)>" + "\n<!ATTLIST Cell Type CDATA #REQUIRED>"
			+ "\n<!ATTLIST Cell SubTemplatename CDATA #IMPLIED>" + "]>";

	public static float DEFAULT_vMSolid = 2.6f;
	public static float DEFAULT_vMOrganicMatter = 0.8f;
	private static float EPSILON = 25f;
	public static final int DEFAULT_SIZE = 20;
	private String templateName;
	private final Map<String, Particle> samplers = new HashMap<>();
	private final Map<String, Map<Integer, GamlSpecies>> processes = new HashMap<>();

	private final double count = 0;// jr

	private Particle[][][] template;
	/** minerals cells density in the fractal */
	protected double solid;
	/** sub fractal density in the fractal */
	protected double fractal;
	/** organic matter density in the fractal */
	protected double organic;
	/** Empty particle density in the fractal */
	private double empty;

	/** masse volumique des solides du canevas */
	protected float solidMatterVolumicMass = DEFAULT_vMSolid;

	/** masse volumique des OM du canevas */
	protected float organicMatterVolumicMass = DEFAULT_vMOrganicMatter;

	/** masse volumique lie */
	protected double organicMatterTotalVolumicMass = -1;
	/** masse volumique lie */
	protected double solidMatterTotalVolumicMass = -1;

	/** sub template of this template */
	protected Hashtable<String, Template> subTemplate = new Hashtable<>();
	// protected Hashtable nbParticles;
	protected int size;

	public abstract void generateTemplate();

	public Particle getSamplerWithCharacteristics(final String a) {
		Particle p = this.samplers.get(a);
		if (p == null) {
			final ArrayList<Particle> ppp = getCellsWithCharacteristics(a);

			final int idx = (int) (Math.random() * 1000) % ppp.size();
			p = ppp.get(idx);
			this.samplers.put(a, p);
		}

		return p;
	}

	public void setSamplerWithCharacteristics(final String a, final Particle p) {
		this.samplers.put(a, p);
	}

	public void addProcess(final GamlSpecies species, final String particleType, final int scale) {

		Map<Integer, GamlSpecies> data = this.processes.get(particleType);
		if (data == null) {
			data = new HashMap<>();
			this.processes.put(particleType, data);
		}
		final Integer k = new Integer(scale);
		if (data.containsKey(k)) {
			data.remove(k);
		}
		data.put(k, species);
	}

	public IAgent createProcessesForParticle(final IScope scope, final Particle p) {
		final Map<Integer, GamlSpecies> data = this.processes.get(p.getTemplateName()); // Shouldnt it be a
																						// List<Integer,
																						// List<GamlSpecies>> ?
		GamlSpecies processName = null;
		if (data != null) {
			processName = data.get(new Integer(p.getLocation().getScale()));
		}
		if (processName == null) {
			processName = p.getWorld().getDefaultSpecies();
		}
		if (processName == null) { return null; }
		Map<String, Object> values = new HashMap<>();
		values.put(IApsfParticleSkill.FOLLOWED_PARTICLE_INT, p);

		ArrayList<Map<String, Object>> list = new ArrayList<>();
		list.add(values);
		IList<IAgent> magt = processName.getPopulation(scope).createAgents(scope, 1, list, false, true);
		final IAgent agt = magt.get(0);
		p.setAgent(agt);

		if (data != null) {
//			for (final GamlSpecies spe : data.get(new Integer(p.getLocation().getScale()))) {
				final GamlSpecies spe = data.get(new Integer(p.getLocation().getScale()));
				processName = spe;
				System.out.println("modification " + processName.getName());
				values = new HashMap<>();
				values.put(IAPSFProcessSkill.FOLLOWED_PARTICLE_INT, p);

				list = new ArrayList<>();
				list.add(values);
				magt = processName.getPopulation(scope).createAgents(scope, 1, list, false, true);
				p.addProcesses(magt.get(0));
//			}

			// agt = magt.get(0);
		}

		return agt;
	}

	public boolean isParentOf(final String s) {
		final Enumeration<String> all = this.subTemplate.keys();
		while (all.hasMoreElements()) {
			if (all.nextElement().equals(s)) { return true; }
		}
		return false;
	}

	public ArrayList<Particle> getCellsWithCharacteristics(final String type) {
		final ArrayList<Particle> res = new ArrayList<>();
		for (final Particle[][] a : this.template) {
			for (final Particle[] b : a) {
				for (final Particle c : b) {
					if (c.getTemplateName().equals(type)) {
						res.add(c);
					}
				}
			}
		}

		return res;
	}

	public int getNumberOfCell() {
		return template == null ? 0 : template.length * template[0].length * template[0][0].length;
	}

	public double getOrganicMatterTotalVolumicMass(final int scale, final Apsf app) {
		organicMatterTotalVolumicMass = getOrganicMatterTotalVolumicMass(0, 1, app);
		return organicMatterTotalVolumicMass / Math.pow(app.getDivisionPerLevel(), scale);
	}

	public double getRateOfTemplateWithName(final String name) {
		final double res = (double) getNbCellsOfTemplateWithName(name) / getNumberOfCell();
		return res;
	}

	public int getNbCellsOfTemplateWithName(final String name) {
		int counter = 0;
		for (final Particle[][] a : template) {
			for (final Particle[] b : a) {
				for (final Particle c : b) {
					if (c.getTemplateName().equals(name)) {
						counter++;
					}
				}
			}
		}
		return counter;
	}

	public double getEmptyParticleRate(final Apsf app) {
		double nbCell = 0;
		if (empty != -1) { return empty; }
		for (final Particle[][] element : template) {
			for (final Particle[] element2 : element) {
				for (final Particle element3 : element2) {
					if (element3 instanceof WhiteParticle) {
						nbCell++;
					}
				}
			}
		}
		this.empty = nbCell / app.getDivisionPerLevel();
		return this.empty;
	}

	/**
	 *
	 * @param p
	 * @param scale
	 * @param fractalCount
	 * @return
	 */
	public double getOrganicMatterTotalVolumicMass(final int p, final double fractalCount, final Apsf app) {
		if (p > EPSILON) { return 0; }
		double res = 0;
		final Template subTemplate = this.getSubTemplate();
		final double cellSizeFactor = Math.pow(app.getDimension(), 3);
		final double tt = organicMatterVolumicMass * fractalCount * organic * cellSizeFactor;
		// res+=subA.getTemplate().getOrganicMatterTotalVolumicMass(p+1, scale+1,fractalCount*fractal);
		res += subTemplate.getOrganicMatterTotalVolumicMass(p + 1, fractalCount * fractal, app);
		// System.out.println("avant somme "+res+" "+fractal);
		res += tt;

		return res;
	}

	public double getPorosity(final Apsf app) {
		return getPorosity(0, 1, app);
	}

	/**
	 *
	 * @param p
	 * @param scale
	 * @param fractalCount
	 * @return
	 */
	public double getPorosity(final int p, final double fractalCount, final Apsf app) {
		if (p > EPSILON) { return getEmptyParticleRate(app); }
		double res = 0;
		final Template subTemplate = this.getSubTemplate();

		/*
		 * double cellVolume= SoilLocation.getCellSize(p,MicrobesEnvironment.WORLD_DIMENSION); double
		 * cellSizeFactor=Math.pow(MicrobesEnvironment.WORLD_DIMENSION,3);
		 */

		// System.out.println("scale "+ p+" cellsize "+cellVolume );
		// System.out.println("calcul "+(1-organic-solid-fractal)+" " +fractalCount);
		final double tt = fractalCount * getEmptyParticleRate(app);
		// System.out.println("resultat "+tt);

		// *cellVolume;
		// res+=subA.getTemplate().getOrganicMatterTotalVolumicMass(p+1, scale+1,fractalCount*fractal);
		final double subPoro = subTemplate.getPorosity(p + 1, fractalCount * fractal, app);
		// System.out.println("avant somme "+res+" "+fractal);
		// System.out.println("Sub porosity "+subTemplate.getTemplateName()+" "+subPoro);
		res += subPoro;
		res += tt;

		return res;
	}

	public Template getSubTemplate() {
		return this.subTemplate.elements().nextElement();
	}

	public Template getSubTemplateWithName(final String mname) {
		return subTemplate.get(mname);
	}

	public Enumeration<Template> getSubTemplates() {
		return this.subTemplate.elements();
	}

	public ArrayList<Template> getAllSubTemplate() {
		return getAllSubTemplate(this);
	}

	private ArrayList<Template> getAllSubTemplate(final Template t) {
		final ArrayList<Template> res = new ArrayList<>();
		final Collection<Template> coll = t.subTemplate.values();
		res.add(t);
		if (coll.size() == 1 && coll.contains(t)) { return res; }
		for (final Template tt : this.subTemplate.values()) {
			final ArrayList<Template> subR = tt.getAllSubTemplate(tt);
			for (final Template ttt : subR) {
				if (!res.contains(ttt)) {
					res.add(ttt);
				}
			}
		}
		return res;
	}

	public Vector<String> getNeededSubTemplates() {
		final Vector<String> resultat = new Vector<>();
		// System.out.println("needed"+template);
		if (template == null) { return resultat; }

		for (final Particle[][] element : template) {
			for (final Particle[] element2 : element) {
				for (final Particle element3 : element2) {
					if (element3 instanceof Agglomerate) {
						if (!resultat.contains(((Agglomerate) element3).getTemplateName())
								&& !this.subTemplate.containsKey(((Agglomerate) element3).getTemplateName())) {
							resultat.add(((Agglomerate) element3).getTemplateName());

						}
					}
				}
			}
		}
		// System.out.println("Resultat "+resultat);
		return resultat;
	}

	public double getSolidMatterTotalVolumicMass(final int scale, final Apsf app) {
		// if(solidMatterTotalVolumicMass==-1)
		solidMatterTotalVolumicMass = getSolidMatterTotalVolumicMass(0, 1, 1, app);
		return solidMatterTotalVolumicMass / Math.pow(app.getDivisionPerLevel(), scale);
	}

	public double getSolidMatterTotalVolumicMass(final int p, final int scale, final double fractalCount,
			final Apsf app) {
		if (p > EPSILON) { return 0; }
		double res = 0;
		final Template subTemplate = this.getSubTemplate();

		res += subTemplate.getSolidMatterTotalVolumicMass(p + 1, scale + 1, fractalCount * fractal, app);
		return res;
	}

	public void setTemplate(final Particle p, final int i, final int j, final int k, final double qte) {
		// TParticle tp=new TParticle(p,qte);
		organicMatterTotalVolumicMass = -1;
		template[i][j][k] = p;
		p.setMatrixCoordinate(i, j, k);

	}

	public Collection<Particle> getParticles() {
		final Vector<Particle> result = new Vector<>();
		for (final Particle[][] element : template) {
			for (final Particle[] element2 : element) {
				for (final Particle element3 : element2) {
					result.add(element3);
				}
			}
		}
		return result;
	}

	public Particle getParticle(final int i, final int j, final int k) {

		// System.out.println(template[i][j][k]);
		return template[i][j][k];
	}

	public Class<? extends Particle> getParticleClass(final int i, final int j, final int k) {
		return template[i][j][k].getClass();
	}
	/*
	 * public float getParticleOrganicPercent(int i, int j, int k) { return template[i][j][k].getOrganicMatterQt(); }
	 */

	public Particle getNewParticleInstance(final IScope scope, final SoilLocation lc) {
		final int[] ll = Particle.getMatrixId(lc, lc.getScale());
		final Particle pp = getNewParticleInstance(ll[0], ll[1], ll[2]);
		pp.setLocation(lc);
		final IAgent agt = createProcessesForParticle(scope, pp);
		pp.setAgent(agt);
		return pp;
	}

	public Particle getNewParticleInstance(final int i, final int j, final int k) {
		final Particle p = template[i][j][k].clone();
		return p;
	}

	public void initialise() {
		for (int i = 0; i < template.length; i++) {
			for (int j = 0; j < template[i].length; j++) {
				for (int k = 0; k < template[i][j].length; k++) {
					template[i][j][k] = null;
				}
			}
		}
	}

	public boolean isInAccordanceWithTemplate(final Particle p, final int i, final int j, final int k) {
		return p.getClass().isInstance(template[i][j][k]);
	}

	public void addSubTemplate(final Template t) {
		final Template findedTemplate = this.subTemplate.get(t.getTemplateName());
		if (findedTemplate == null) {
			this.subTemplate.put(t.getTemplateName(), t);
		}

	}

	public void initAgglomerate(final Hashtable<String, Template> templates) {
		for (final Particle[][] element : template) {
			for (final Particle[] element2 : element) {
				for (final Particle element3 : element2) {
					if (element3 instanceof Agglomerate) {
						((Agglomerate) element3).setTemplate(templates.get(((Agglomerate) element3).getTemplateName()));
					}
				}
			}
		}
	}

	private void addSubTemplate(final Template[] t) {
		for (final Template element : t) {
			this.addSubTemplate(element);
		}

	}

	/**
	 * initialise the template
	 *
	 * @param solid
	 *            mineral density
	 * @param fractal
	 *            fractal density
	 * @param organic
	 *            organic matter density
	 * @param size
	 *            side cube size
	 */
	private void initialize(final String name, final float solid, final float fractal, final float organic,
			final int size) {
		this.size = size;
		this.solid = solid;
		this.fractal = fractal;
		this.organic = organic;
		template = new Particle[size][size][size];
		this.subTemplate = new Hashtable<>();
		this.templateName = name;
		empty = -1;
	}

	/**
	 * constructor of the template. It is a recursive template.
	 *
	 * @param solid
	 *            mineral density
	 * @param fractal
	 *            fractal density
	 * @param organic
	 *            organic matter density
	 * @param size
	 *            side cube size
	 *
	 */
	protected Template(final String name, final float solid, final float fractal, final float organic, final int size) {
		super();
		initialize(name, solid, fractal, organic, size);
		this.addSubTemplate(this);
		// nbParticles=new Hashtable();
		generateTemplate();
	}

	/**
	 * constructor of the template. It is a recursive template.
	 *
	 * @param solid
	 *            mineral density
	 * @param fractal
	 *            fractal density
	 * @param organic
	 *            organic matter density
	 * @param size
	 *            side cube size
	 * @param t
	 *            subTemplate List
	 *
	 */
	protected Template(final String name, final float solid, final float fractal, final float organic, final int size,
			final Template[] t) {
		super();
		initialize(name, solid, fractal, organic, size);
		this.addSubTemplate(t);
		generateTemplate();
	}

	/**
	 * constructor of the template. It is a recursive template.
	 *
	 * @param solid
	 *            mineral density
	 * @param fractal
	 *            fractal density
	 * @param organic
	 *            organic matter density
	 * @param size
	 *            side cube size
	 * @param t
	 *            subTemplate
	 *
	 */
	protected Template(final String name, final float solid, final float fractal, final float organic, final int size,
			Template t) {
		super();
		t = t != null ? t : this;
		initialize(name, solid, fractal, organic, size);
		this.addSubTemplate(t);
		generateTemplate();
	}

	public int getSize() {
		return size;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public double getFractal() {
		return fractal;
	}

	public void setFractal(final double fractal) {
		this.fractal = fractal;
	}

	public double getOrganic() {
		return organic;
	}

	public void setOrganic(final double organic) {
		this.organic = organic;
	}

	public double getSolid() {
		return solid;
	}

	public void setSolid(final double solid) {
		this.solid = solid;
	}

	/**
	 * @return the organicMatterVolumicMass
	 */
	public float getOrganicMatterVolumicMass() {
		return organicMatterVolumicMass;
	}

	/**
	 * @param organicMatterVolumicMass
	 *            the organicMatterVolumicMass to set
	 */
	public void setOrganicMatterVolumicMass(final float organicMatterVolumicMass) {
		this.organicMatterVolumicMass = organicMatterVolumicMass;
	}

	/**
	 * @return the solidVolumicMass
	 */
	public float getSolidVolumicMass() {
		return solidMatterVolumicMass;
	}

	/**
	 * @param solidVolumicMass
	 *            the solidVolumicMass to set
	 */
	public void setSolidVolumicMass(final float solidVolumicMass) {
		this.solidMatterVolumicMass = solidVolumicMass;
	}

	/**
	 * @return the templateName
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * @param templateName
	 *            the templateName to set
	 */
	public void setTemplateName(final String templateName) {
		this.templateName = templateName;
	}

	@Override
	public String toString() {
		String result = "";
		result += "<Canvas Class='" + this.getClass().getName() + "' Name='" + this.getTemplateName() + "' Solid='"
				+ solid + "' Fractal='" + fractal + "' Organic='" + organic + "' SideSize='" + size + "'>";

		for (int i = 0; i < template.length; i++) {
			for (int j = 0; j < template[i].length; j++) {
				for (int k = 0; k < template[i][j].length; k++) {
					if (template[i][j][k] instanceof Agglomerate) {
						final Agglomerate aggl = (Agglomerate) template[i][j][k];
						result += "<Cell Type='Fractal' x='" + i + "' y='" + j + "' z='" + k + "' SubTemplateName='"
								+ aggl.getTemplateName() + "' ></Cell>";
					} else {
						result += "<Cell Type='" + template[i][j][k].getClass().getName() + "' x='" + i + "' y='" + j
								+ "' z='" + k + "' ></Cell>";
					}
				}
			}
		}
		result += "</Canvas>";
		return result;

	}

}
