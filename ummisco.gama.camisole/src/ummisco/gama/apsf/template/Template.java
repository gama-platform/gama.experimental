    /*
* FractalTemplate.java : ummisco.gama.environment
* Copyright (C) 2003-2006 Nicolas Marilleau
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package ummisco.gama.apsf.template;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
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
import msi.gaml.types.IType;
import ummisco.gama.apsf.spaces.Agglomerate;
import ummisco.gama.apsf.spaces.Apsf;
import ummisco.gama.apsf.spaces.OrganicMatter;
import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.spaces.SoilLocation;
import ummisco.gama.apsf.spaces.WhiteParticle;
import ummisco.gama.camisole.skills.IApsfParticleSkill;
import ummisco.gama.camisole.skills.IApsfSkill;

public abstract class Template  {
	
	public static String TEMPLATE_DTD="<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Canvas [" +
	"\n<!ELEMENT Canvas (Cell*)>"+
	"\n<!ATTLIST SpaceElement Name CDATA #REQUIRED>"+		
	"\n<!ATTLIST SpaceElement Class CDATA #REQUIRED>"+		
	"\n<!ATTLIST SpaceElement Solid CDATA #REQUIRED>"+		
	"\n<!ATTLIST SpaceElement Fractal CDATA #REQUIRED>"+		
	"\n<!ATTLIST SpaceElement Organic CDATA #REQUIRED>"+		
	"\n<!ATTLIST SpaceElement SideSize CDATA #REQUIRED>"+		
	
	"\n<!ELEMENT Cell (#PCDATA)>" +
	"\n<!ATTLIST Cell Type CDATA #REQUIRED>"+		
	"\n<!ATTLIST Cell SubTemplatename CDATA #IMPLIED>"+		
	"]>";
	
	public static float DEFAULT_vMSolid=2.6f;
	public static float DEFAULT_vMOrganicMatter=0.8f;
	private static float EPSILON=25f;
	public static final int DEFAULT_SIZE=20;
	private String templateName;
	private Map<String, Particle> samplers = new HashMap<String, Particle>();
	private Map<String, Map<Integer,GamlSpecies>> processes = new HashMap<String, Map<Integer,GamlSpecies>>();
	
	
	private double count=0;//jr
	
	private Particle [][][] template;
	/**minerals cells density in the fractal*/
	protected double solid ;
	/**sub fractal density in the fractal*/
	protected double fractal ;
	/**organic matter density in the fractal*/
	protected double organic;
	/**Empty particle density in the fractal*/
	private double empty;
	
	
	/**masse volumique des solides du canevas*/
	protected float solidMatterVolumicMass=DEFAULT_vMSolid;
	
	/**masse volumique des OM du canevas*/
	protected float organicMatterVolumicMass=DEFAULT_vMOrganicMatter;
	
	/**masse volumique lie */
	protected double organicMatterTotalVolumicMass=-1;
	/**masse volumique lie */
	protected double solidMatterTotalVolumicMass=-1;	

	/**sub template of this template*/	
	protected Hashtable<String, Template> subTemplate=new Hashtable<String, Template>();
	//protected Hashtable nbParticles;
	protected int size;
	
	public abstract void  generateTemplate();
	
	
	public Particle getSamplerWithCharacteristics(String a)
	{
		Particle p =  this.samplers.get(a);
		if(p == null)
		{
			ArrayList<Particle>ppp= getCellsWithCharacteristics(a);

			int idx = ((int)(Math.random() * 1000)) % ppp.size();
			p = ppp.get(idx);
			this.samplers.put(a, p);
		}
		
		
		return p;
	}
	public void setSamplerWithCharacteristics(String a, Particle p)
	{
		this.samplers.put(a,p);
	}
	
	public void addProcess(GamlSpecies species,String particleType, int scale)
	{
		
		Map<Integer,GamlSpecies> data = this.processes.get(particleType);
		if(data == null)
		{
			data = new HashMap<Integer,GamlSpecies>();
			this.processes.put(particleType, data);
		}
		Integer k = new Integer(scale);
		if(data.containsKey(k))
			data.remove(k);
		data.put(k, species);
	}
	
	public IAgent createProcessesForParticle(IScope scope, Particle p)
	{
		Map<Integer,GamlSpecies> data = this.processes.get(p.getTemplateName());
		GamlSpecies processName = null;
		if(data != null)
			processName = data.get(new Integer(p.getLocation().getScale()));
		if(processName == null)
			processName = (GamlSpecies)(p.getWorld().getDefaultSpecies());
		if(processName == null)
			return null;
		Map<String,Object> values = new HashMap<String,Object>();
		values.put(IApsfParticleSkill.FOLLOWED_PARTICLE, p);
		
		ArrayList<Map<String,Object>> list = new ArrayList<>();
		list.add(values);
		IList<IAgent> magt=  processName.getPopulation(scope).createAgents(scope, 1, list, false, true);
		IAgent agt = magt.get(0);
		p.setAgent(agt);
		return agt;
	}
	
	public boolean isParentOf(String s)
	{
		Enumeration<String> all = this.subTemplate.keys();
		while(all.hasMoreElements())
			if(all.nextElement().equals(s))
				return true;
		return false;
	}
	
	public ArrayList<Particle> getCellsWithCharacteristics(String type)
	{
		ArrayList<Particle> res = new ArrayList<>();
		for(Particle[][] a:this.template)
			for(Particle[] b:a)
				for(Particle c:b)
					{
						if(c.getTemplateName().equals(type)) {
							res.add(c);
						}
					}
					
						
		return res;
	}
	
	
	public int getNumberOfCell() {
		return template==null?0:(template.length*template[0].length*template[0][0].length);
	}
	
	public double getOrganicMatterTotalVolumicMass(int scale, Apsf app)
	{
		organicMatterTotalVolumicMass= getOrganicMatterTotalVolumicMass(0,1,app);
		return organicMatterTotalVolumicMass/Math.pow(app.getDivisionPerLevel(),scale);
	}
	public double getRateOfTemplateWithName(String name)
	{
		double res  = ((double)getNbCellsOfTemplateWithName(name))/getNumberOfCell();
		return res;
	}
	
	public int getNbCellsOfTemplateWithName(String name)
	{
		int counter =0;
		for(Particle[][] a:template)
			for(Particle[] b:a)
				for(Particle c:b)
				{	
					if(c.getTemplateName().equals(name))
					{
							counter++;
					}
				}	
		return counter;
	}
	
	
	public double getEmptyParticleRate(Apsf app)
	{
		double nbCell=0;
		if(empty!=-1)
			return empty;
		for(int i=0; i<template.length;i++)
			for(int j=0;j<template[i].length;j++)
				for(int k=0;k<template[i][j].length;k++)
				{
					if(template[i][j][k] instanceof WhiteParticle)
					{
						nbCell++;
					}
				}
		this.empty=nbCell/app.getDivisionPerLevel();
		return this.empty;
	}
	
	/**
	 * 
	 * @param p
	 * @param scale
	 * @param fractalCount
	 * @return
	 */
	public double getOrganicMatterTotalVolumicMass(int p, double fractalCount, Apsf app)
	{	
		if(p>EPSILON)
			return 0;
		double res=0;
		Template subTemplate=this.getSubTemplate();
		double cellSizeFactor=Math.pow(app.getDimension(),3);
		double tt=organicMatterVolumicMass*fractalCount*organic*cellSizeFactor;
		//res+=subA.getTemplate().getOrganicMatterTotalVolumicMass(p+1, scale+1,fractalCount*fractal);
		res+=subTemplate.getOrganicMatterTotalVolumicMass(p+1, fractalCount*fractal,app);
		//	System.out.println("avant somme "+res+" "+fractal);
		res+=tt;
		
		return res;
	}
	
	public double getPorosity(Apsf app)
	{
		return getPorosity(0,1, app);
	}
	
	
	/**
	 * 
	 * @param p
	 * @param scale
	 * @param fractalCount
	 * @return
	 */
	public double getPorosity(int p, double fractalCount, Apsf app)
	{	
		if(p>EPSILON)
			return getEmptyParticleRate(app);
		double res=0;
		Template subTemplate=this.getSubTemplate();
		
/*		double cellVolume= SoilLocation.getCellSize(p,MicrobesEnvironment.WORLD_DIMENSION);
		double cellSizeFactor=Math.pow(MicrobesEnvironment.WORLD_DIMENSION,3);
	*/	
		
	//	System.out.println("scale "+ p+" cellsize "+cellVolume );
	//	System.out.println("calcul "+(1-organic-solid-fractal)+" " +fractalCount);
		double tt=fractalCount*getEmptyParticleRate(app);
	//	System.out.println("resultat "+tt);
		
		//*cellVolume;
		//res+=subA.getTemplate().getOrganicMatterTotalVolumicMass(p+1, scale+1,fractalCount*fractal);
		double subPoro=subTemplate.getPorosity(p+1, fractalCount*fractal,app);
		//	System.out.println("avant somme "+res+" "+fractal);
	//	System.out.println("Sub porosity "+subTemplate.getTemplateName()+" "+subPoro);
		res+=subPoro;
		res+=tt;
		
		return res;
	}
	
	

	public Template getSubTemplate()
	{
		return this.subTemplate.elements().nextElement();
	}
	
	public Template getSubTemplateWithName(String mname)
	{
		return subTemplate.get(mname);
	}
	
	public Enumeration<Template> getSubTemplates()
	{
		return this.subTemplate.elements();
	}
	
	public ArrayList<Template> getAllSubTemplate()
	{
		return getAllSubTemplate(this);
	}
	private ArrayList<Template> getAllSubTemplate(Template t)
	{
		ArrayList<Template> res = new ArrayList<Template>();
		Collection<Template >  coll =t.subTemplate.values();
		res.add(t);
		if(coll.size() == 1&& coll.contains(t))
		{
			return res;
		}
		for(Template tt : this.subTemplate.values())
		{
			ArrayList<Template> subR = tt.getAllSubTemplate(tt);
			for(Template ttt : subR)
				if(!res.contains(ttt))
					res.add(ttt);
		}
		return res;
	}
	
	public Vector<String> getNeededSubTemplates()
	{
		Vector<String> resultat=new Vector<String>() ;
		//System.out.println("needed"+template);
		if(template==null)
		return resultat;
		
		for(int i=0; i<template.length;i++)
			for(int j=0;j<template[i].length;j++)
				for(int k=0;k<template[i][j].length;k++)
				{
					if(template[i][j][k] instanceof Agglomerate )
					{
						if(!resultat.contains(((Agglomerate)template[i][j][k]).getTemplateName())&&! this.subTemplate.containsKey(((Agglomerate)template[i][j][k]).getTemplateName()))
						{
						resultat.add(((Agglomerate)template[i][j][k]).getTemplateName());
						
						}
					}
				}
	//	System.out.println("Resultat "+resultat);			
		return resultat;
	}
	
	public double getSolidMatterTotalVolumicMass(int scale, Apsf app)
	{
	//	if(solidMatterTotalVolumicMass==-1)
			solidMatterTotalVolumicMass= getSolidMatterTotalVolumicMass(0,1,1,app);
		return solidMatterTotalVolumicMass/Math.pow(app.getDivisionPerLevel(),scale);
	}
	
	public double getSolidMatterTotalVolumicMass(int p, int scale, double fractalCount, Apsf app)
	{
		if(p>EPSILON)
			return 0;
		double res=0;
		Template subTemplate=this.getSubTemplate();
		
		double cellSizeFactor=Math.pow(app.getDimension(),3);
		double tt=solidMatterVolumicMass*fractalCount*solid*cellSizeFactor;
		
			res+=subTemplate.getSolidMatterTotalVolumicMass(p+1, scale+1,fractalCount*fractal,app);
		return res;
	}
	
	public void setTemplate(Particle p, int i, int j, int k, double qte)
	{
		//TParticle tp=new TParticle(p,qte);
		organicMatterTotalVolumicMass=-1;
		template[i][j][k]=p;
		p.setMatrixCoordinate(i, j,k);
		
	}
	
	
	public Collection getParticles()
	{
		Vector result=new Vector();
		for(int i=0; i<template.length;i++)
			for(int j=0;j<template[i].length;j++)
				for(int k=0;k<template[i][j].length;k++)
					result.add(template[i][j][k]);
		return result;
	}

	 public Particle getParticle(int i, int j, int k)
	{
		
		// System.out.println(template[i][j][k]);
		return template[i][j][k];
	}
	public Class getParticleClass(int i, int j, int k)
	{
		return template[i][j][k].getClass();
	}
	/*public float getParticleOrganicPercent(int i, int j, int k)
	{
		return template[i][j][k].getOrganicMatterQt();
	}*/
	
	public Particle getNewParticleInstance(IScope scope, SoilLocation lc)
	{
		int [] ll=Particle.getMatrixId(lc,lc.getScale());
		Particle pp = getNewParticleInstance(ll[0],ll[1], ll[2]);
		pp.setLocation(lc);
		IAgent agt = createProcessesForParticle(scope,pp);
		pp.setAgent(agt);
		return pp;
	}
	
	public Particle getNewParticleInstance(int i, int j, int k)
	{
		Particle p=template[i][j][k].clone();
		return p;
	}
	
	public void initialise()
	{
		for(int i=0; i<template.length;i++)
			for(int j=0;j<template[i].length;j++)
				for(int k=0;k<template[i][j].length;k++)
					template[i][j][k]=null;
	}
	
	public boolean isInAccordanceWithTemplate(Particle p, int i, int j, int k)
	{
		return p.getClass().isInstance(template[i][j][k]);
	}

	public void addSubTemplate(Template t)
	{
		Template findedTemplate=this.subTemplate.get(t.getTemplateName());
		if(findedTemplate==null)
		{
			this.subTemplate.put(t.getTemplateName(), t);
		}

		
	}

	public void initAgglomerate(Hashtable<String, Template> templates)
	{
		for(int i=0; i<template.length;i++)
			for(int j=0;j<template[i].length;j++)
				for(int k=0;k<template[i][j].length;k++)
				{
					if(template[i][j][k] instanceof Agglomerate)
					{
						((Agglomerate)template[i][j][k]).setTemplate(templates.get(((Agglomerate)template[i][j][k]).getTemplateName()));
					}
				}
	}
	
	private void addSubTemplate(Template [] t)
	{
		for(int i=0; i<t.length;i++)
		{
			this.addSubTemplate(t[i]);
		}
		
	}
	/**
	 * initialise the template
	 * @param solid mineral density
	 * @param fractal fractal density
	 * @param organic organic matter density
	 * @param size side cube size
	 */
	private void initialize(String name,float solid, float fractal, float organic,  int size)
	{
		this.size=size;
		this.solid=solid;
		this.fractal=fractal;
		this.organic =organic;
		template=new Particle[size][size][size];
		this.subTemplate=new Hashtable<String, Template>();
		this.templateName=name;
		empty=-1;
	}
	
	/**
	 * constructor of the template. It is a recursive template.
	 * @param solid mineral density
	 * @param fractal fractal density
	 * @param organic organic matter density
	 * @param size side cube size
	 * 
	 */
	protected Template(String name,float solid, float fractal, float organic,  int size) {
		super();
		initialize(name,solid, fractal, organic,size);
		this.addSubTemplate(this);
	//	nbParticles=new Hashtable();
		generateTemplate();
	}
	
	
	
	/**
	  * constructor of the template. It is a recursive template.
	 * @param solid mineral density
	 * @param fractal fractal density
	 * @param organic organic matter density
	 * @param size side cube size
	 * @param t subTemplate List
	 * 
	 */
	protected Template(String name,float solid, float fractal, float organic,  int size, Template [] t) {
		super();
		initialize(name,solid, fractal, organic,size);
		this.addSubTemplate(t);
		generateTemplate();
	}
	
	/**
	 * constructor of the template. It is a recursive template.
	 * @param solid mineral density
	 * @param fractal fractal density
	 * @param organic organic matter density
	 * @param size side cube size
	 * @param t subTemplate
	 * 
	 */
	protected Template(String name,float solid, float fractal, float organic,  int size, Template  t) {
		super();
		t = t!=null?t:this;
		initialize(name,solid, fractal, organic,size);
		this.addSubTemplate(t);
		generateTemplate();
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public double getFractal() {
		return fractal;
	}

	public void setFractal(double fractal) {
		this.fractal = fractal;
	}

	public double getOrganic() {
		return organic;
	}

	public void setOrganic(double organic) {
		this.organic = organic;
	}

	public double getSolid() {
		return solid;
	}

	public void setSolid(double solid) {
		this.solid = solid;
	}

	/**
	 * @return the organicMatterVolumicMass
	 */
	public float getOrganicMatterVolumicMass() {
		return organicMatterVolumicMass;
	}

	/**
	 * @param organicMatterVolumicMass the organicMatterVolumicMass to set
	 */
	public void setOrganicMatterVolumicMass(float organicMatterVolumicMass) {
		this.organicMatterVolumicMass = organicMatterVolumicMass;
	}

	/**
	 * @return the solidVolumicMass
	 */
	public float getSolidVolumicMass() {
		return solidMatterVolumicMass;
	}

	/**
	 * @param solidVolumicMass the solidVolumicMass to set
	 */
	public void setSolidVolumicMass(float solidVolumicMass) {
		this.solidMatterVolumicMass = solidVolumicMass;
	}

	/**
	 * @return the templateName
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * @param templateName the templateName to set
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	public String toString()
	{
		String result="";
		result+="<Canvas Class='"+this.getClass().getName()+"' Name='"+this.getTemplateName()+"' Solid='"+solid+"' Fractal='"+fractal+"' Organic='"+organic+"' SideSize='"+size+"'>";				
		
		for(int i=0; i<template.length;i++)
			for(int j=0;j<template[i].length;j++)
				for(int k=0;k<template[i][j].length;k++)
					if( template[i][j][k] instanceof  Agglomerate )
					{
						Agglomerate aggl=(Agglomerate) template[i][j][k];
						result+="<Cell Type='Fractal' x='"+i+"' y='"+j+"' z='"+k+"' SubTemplateName='"+aggl.getTemplateName()+"' ></Cell>";
					}
					else
					{
						result+="<Cell Type='"+template[i][j][k].getClass().getName()+"' x='"+i+"' y='"+j+"' z='"+k+"' ></Cell>";
					}
		result+="</Canvas>";
		return result;
		
	}

	
	
}
