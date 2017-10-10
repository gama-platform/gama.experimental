/*
* Agglomerate.java : environment
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
package ummisco.gama.apsf.spaces;

import java.util.Enumeration;
import java.util.Hashtable;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import ummisco.gama.apsf.exception.APSFException;
import ummisco.gama.apsf.exception.AlreadyCreatedParticle;
import ummisco.gama.apsf.exception.UnBreakableParticle;
import ummisco.gama.apsf.template.Template;


public class Agglomerate extends Particle {
	
	
	private Template template;
//	private String templateName;
	private Hashtable subParticle; 
	private double referenceOMWeight;
	private double referenceSolidWeight; 

	//precomptuted mass
	private double organicMatterMassPerCell;
	private double solidMassPerCell;
	private double fractalMassPerCell;
	
	
	public void deploySubParticles(IScope agt)
	{
		int nbCellPerLevel=template.getSize();
		for(int i = 0;i<nbCellPerLevel;i++)
			for(int j = 0;j<nbCellPerLevel;j++)
				for(int k = 0;k<nbCellPerLevel;k++)
				{
					SoilLocation s = getSubParticleLocation( i,  j,  k);
					this.getWorld().getParticleAtLocation(agt, s);
				}
	}
	
	public SoilLocation getSubParticleLocation(int i, int j, int k)
	{
		SoilLocation se = SoilLocation.changeScale(this.location, this.location.getScale()+1);
		return new SoilLocation(se.getX()+i,se.getY()+j,se.getZ()+k,se.getScale(),this.getWorld());
	}
	
	public void setParent(Agglomerate parent) {
		super.setParent(parent);
		int nbCell=this.getWorld().getDivisionPerLevel();
		this.organicMatterWeight=(parent.referenceOMWeight-(parent.organicMatterMassPerCell*parent.getTemplate().getOrganic())*this.getWorld().getDivisionPerLevel())/(parent.getTemplate().getFractal()*nbCell);
		this.solidWeight=(parent.referenceSolidWeight-(parent.solidMassPerCell*parent.getTemplate().getSolid())*this.getWorld().getDivisionPerLevel())/(parent.getTemplate().getFractal()*nbCell);
	//	System.out.println("MASS "+organicMatterWeight+" " +solidWeight);
		precomputeMatters();
		
		}

	
	public float countSolidQuantity()
	{
		float result=0;
		Enumeration e= subParticle.elements();
		while(e.hasMoreElements())
		{
			Object o=e.nextElement();
			if(o instanceof OrganicMatter)
				result++;
			if(o instanceof Agglomerate)
				result+=((Agglomerate)o).countSolidQuantity();
		}
		return result/(this.getWorld().getDivisionPerLevel()*this.getWorld().getDivisionPerLevel()*this.getWorld().getDivisionPerLevel());
	}

	public float countDeployedOrganicQuantityInFractal()
	{
		float result=0;
		Enumeration e= subParticle.elements();
		while(e.hasMoreElements())
		{
			Object o=e.nextElement();
			if(o instanceof Agglomerate)
				result+=((Agglomerate)o).getOrganicMatter();
		}
		return result/(this.getWorld().getDivisionPerLevel()*this.getWorld().getDivisionPerLevel()*this.getWorld().getDivisionPerLevel());
	}
	public int countDeployedFractal()
	{
		int result=0;
		Enumeration e= subParticle.elements();
		while(e.hasMoreElements())
		{
			Object o=e.nextElement();
			if(o instanceof Agglomerate)
				result++;
		}
		return result;
	}
	
	public int countDeployedOrganicMatter()
	{
		int result=0;
		Enumeration e= subParticle.elements();
		while(e.hasMoreElements())
		{
			Particle o=(Particle)e.nextElement();
			Class c=this.getTemplate().getParticleClass(o.getPI(), o.getPJ(), o.getPK());
			
			if(c.equals(OrganicMatter.class))
			{
				result++;
			}
		}
		return result;
	}
	public Enumeration getSubParticle()
	{
		return subParticle.elements();
	}
	
	
	public Agglomerate() 
	{
		super();

		this.subParticle=new Hashtable();
		this.organicMatterWeight=0;
		this.referenceOMWeight=0;
		
		
	}

	
	public Agglomerate(Template ft) 
	{
		super();

		this.subParticle=new Hashtable();
		this.template=ft;
		this.organicMatterWeight=0;
		this.referenceOMWeight=0;
	}
	
	public Agglomerate(Agglomerate a) 
	{
		super(a);
		this.organicMatterWeight=a.organicMatterWeight;
		this.subParticle=new Hashtable();
		this.template=a.template;
		this.referenceOMWeight=a.referenceOMWeight;
		this.agent = a.agent;
	}
	public void setAttribute(String arg0, Object arg1) {
	}

	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString()
	{
		return level+SEPARATOR_PARTICLE+
			(int)size+SEPARATOR_PARTICLE+
			pI+SEPARATOR_PARTICLE+
			pJ+SEPARATOR_PARTICLE+
			pK+SEPARATOR_PARTICLE+
			organicMatterWeight+SEPARATOR_PARTICLE+
			solidWeight+ SEPARATOR_PARTICLE+
			referenceOMWeight+ SEPARATOR_PARTICLE+
			referenceSolidWeight+ SEPARATOR_PARTICLE+ 
			organicMatterMassPerCell+ SEPARATOR_PARTICLE+
			solidMassPerCell+ SEPARATOR_PARTICLE+
			fractalMassPerCell+ SEPARATOR_PARTICLE+
			this.getTemplateName();
	}
	
	public Object parseString(String arg0) {
		String [] parameters=arg0.split(SEPARATOR_PARTICLE);
		level=Integer.valueOf(parameters[0]).intValue();
		pI=Integer.valueOf(parameters[2]).intValue();
		pJ=Integer.valueOf(parameters[3]).intValue();
		pK=Integer.valueOf(parameters[4]).intValue();
		organicMatterWeight=Float.valueOf(parameters[5]).floatValue();
		solidWeight=Float.valueOf(parameters[6]).floatValue();
		referenceOMWeight=Float.valueOf(parameters[7]).floatValue();
		referenceSolidWeight=Float.valueOf(parameters[8]).floatValue();
		organicMatterMassPerCell=Float.valueOf(parameters[9]).floatValue();
		solidMassPerCell=Float.valueOf(parameters[10]).floatValue();
		fractalMassPerCell=Float.valueOf(parameters[11]).floatValue();
	//	templateName=parameters[12];
		
		return this;
	}

	public Particle findParticle(IScope scope, SoilLocation pLoc)
	{
		if(pLoc.equals(this.getLocation()))
			return this;
			
		int[] loc=new int[3];
		SoilLocation currentLoc=(SoilLocation)this.getLocation();
		loc=Particle.getMatrixId(pLoc,currentLoc.getScale()+1);
		String key=Particle.getKey(loc[0],loc[1],loc[2]);
		if(pLoc.getScale()==currentLoc.getScale() + 1)
		{
			Particle sub=(Particle)this.subParticle.get(key);
			if(sub!=null)
			{
				return sub; 
			}
			else {
				return this;
			}
		}
		else
		{
			Particle sub=(Particle)this.subParticle.get(key);
			if(sub!=null)
			{
				if(sub instanceof Agglomerate)
					return ((Agglomerate)sub).findParticle(scope,pLoc);
				else
					return sub;
			}
			else
			{
				return this;
			}
		}
	}

	public void removeSubParticle(Particle p)
	{
		subParticle.remove(p.getKey());
		porosityHaveBeenModified=true;
	}

	@Override
	public void putParticle(IScope scope, Particle p) throws APSFException  
	{
		porosityHaveBeenModified=true;
		int[] loc;
		SoilLocation currentLoc=(SoilLocation)this.getLocation();
		SoilLocation pLoc=(SoilLocation)p.getLocation();
		loc=Particle.getMatrixId(pLoc,currentLoc,currentLoc.getScale()+1);
		
		if(pLoc.getScale()==currentLoc.getScale()+1)
			{
				p.setMatrixCoordinate(loc[0],loc[1],loc[2]);
				Particle ptmp=(Particle)this.subParticle.get(p.getKey());

				if(ptmp!=null)
				{
					//la particule existe d�ja, on ne faire rien
					//return;
					throw new AlreadyCreatedParticle();
				}
			//	if(!(this.template.isInAccordanceWithTemplate(p,loc[0],loc[1],loc[2])))
			//		throw new UnBreakableParticle();

				p.setMatrixCoordinate(loc[0],loc[1],loc[2]);
				this.subParticle.put(p.getKey(),p);
				p.setParent(this);
			}
		else
		{
			String key=Particle.getKey(loc[0],loc[1],loc[2]);
			Particle sub=(Particle)this.subParticle.get(key);
			if(sub!=null)
			{
				if(!(sub instanceof Agglomerate))
				{
					//System.out.println("nes pas agglo");
					throw new UnBreakableParticle();
				}
				sub.putParticle(scope,p);
			}
			else
			{
				SoilLocation s=SoilLocation.changeScale(((SoilLocation)(p.getLocation())),currentLoc.getScale()+1);
				SoilLocation newLoc=new SoilLocation(s.getX()+loc[0],s.getY()+loc[1],s.getZ()+loc[2],currentLoc.getScale()+1, s.getWorld());
				sub=this.getTemplate().getNewParticleInstance(scope,newLoc);//createSubAgglo();
				sub.putParticle(scope,p);
			}
		}
	
	}
	
	
	public int getNbCellWithType(Class particleType)
	{
		int result=0;
		Enumeration<Particle> it=this.subParticle.elements();
		//compute the number of pore for this scale
		while(it.hasMoreElements())
		{
			Particle p=it.nextElement();
			if(p.getClass().equals(particleType))
			{
				if(this.template.getParticleClass(p.getPI(), p.getPJ(), p.getPK()).equals(particleType))
				{
					//result--;
				}
				else
				{
					result++;
				}
			}
			else
				if(this.template.getParticleClass(p.getPI(), p.getPJ(), p.getPK()).equals(particleType))
				{
					result--;
				}
		}
		//System.out.println("nb Particle trouvee interm�diaire "+ result+" " +particleType);
		int resultTemp=0;
		//count the number of pores in the canvas
		int nbCellPerLevel=template.getSize();
		for(int i=0;i<nbCellPerLevel;i++)
			for(int j=0;j<nbCellPerLevel;j++)
				for(int k=0;k<nbCellPerLevel;k++)
					if(template.getParticleClass(i, j, k).equals(particleType))
					{
						resultTemp++;
						result++;
					}
		
	//	System.out.println("nb Particle trouvee "+ result+" " +particleType+ " "+resultTemp);
		return result;
	}

		
	public double getPorosityInFractalCells()
	{
		double result=0;
		
		Hashtable<String, Integer> nbFractalWithTemplate=new Hashtable<String, Integer>();
		Hashtable<String, Template> templates=new Hashtable<String, Template>();
		Enumeration<Particle> it=this.subParticle.elements();
		//compute the number of pore for this scale
		while(it.hasMoreElements())
		{
			Particle p=it.nextElement();
			Particle tP=this.template.getParticle(p.getPI(), p.getPJ(), p.getPK());
			if(p instanceof Agglomerate)
			{
				result+=((Agglomerate)p).getPorosity();
			}	
			
			if(tP instanceof Agglomerate)
			{
				Template temp=((Agglomerate)tP).getTemplate();
				
				if(templates.containsKey(temp.getTemplateName()))
				{
					Integer count=nbFractalWithTemplate.get(temp.getTemplateName());
					nbFractalWithTemplate.remove(temp.getTemplateName());
					nbFractalWithTemplate.put(temp.getTemplateName(), new Integer(count.intValue()-1));
				}
				else
				{
					nbFractalWithTemplate.put(temp.getTemplateName(), new Integer(-1));
					templates.put(temp.getTemplateName(),temp);
				}
			}
		}
		
		int nbCellPerLevel=template.getSize();
		for(int i=0;i<nbCellPerLevel;i++)
			for(int j=0;j<nbCellPerLevel;j++)
				for(int k=0;k<nbCellPerLevel;k++)
				{
					Particle p=template.getParticle(i, j, k);
					if(p instanceof Agglomerate)
					{
						String tempName=((Agglomerate)p).getTemplateName();
						if(templates.containsKey(tempName))
						{
							Integer count=nbFractalWithTemplate.get(tempName);
							nbFractalWithTemplate.remove(tempName);
							nbFractalWithTemplate.put(tempName, new Integer(count.intValue()+1));
						}
						else
						{
							nbFractalWithTemplate.put(tempName, new Integer(1));
							templates.put(tempName,((Agglomerate)p).getTemplate());
						}
					}
				}
		Enumeration<String> keys=templates.keys();
		
		while(keys.hasMoreElements())
		{
			String key=keys.nextElement();
			Template template=templates.get(key);
			Integer qte=nbFractalWithTemplate.get(key);
			result+=qte.intValue()* template.getPorosity(this.location.getWorld());
		}
		return result/this.getWorld().getDivisionPerLevel();
	}
	
	public double getPorosityAtCurrentSubSpaceLevel()
	{
		int nbWhite=getNbCellWithType(WhiteParticle.class);
		double cellSizeFactor=this.getWorld().getDivisionPerLevel();
		return nbWhite/cellSizeFactor;
	}
	
	public double getPorosity()
	{
	//	if(!porosityHaveBeenModified)
	//		return this.porosity;
		double result=getPorosityAtCurrentSubSpaceLevel();
	//	System.out.println("PorosityAtCurrentSubSpaceLevel "+result);
		double fractalPorosity=getPorosityInFractalCells();
	//	System.out.println("Fractal Porosity "+fractalPorosity);
		porosityHaveBeenModified=false;
		result+=fractalPorosity;
		this.porosity=result;
		return result;
	}

	public double getOrganicMatter() {
		return organicMatterWeight;
	}
	protected void setOrganicMatter(float organicMatter) {
		this.organicMatterWeight = organicMatter;
		this.referenceOMWeight=organicMatter;
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

	public void setReferenceOMQte(double referenceOMQte) {
		this.referenceOMWeight = referenceOMQte;
	}



	public double getReferenceMineralWeight() {
		return referenceSolidWeight;
	}


	public void setReferenceMineralWeight(double referenceMineralWeight) {
		this.referenceSolidWeight = referenceMineralWeight;
	}


	public double getReferenceOMWeight() {
		return referenceOMWeight;
	}


	public void setReferenceOMWeight(double referenceOMWeight) {
		this.referenceOMWeight = referenceOMWeight;
	}


	/**
	 * @return the fractalMass
	 */
	public double getFractalMass() {
		return fractalMassPerCell;
	}


	/**
	 * @param fractalMass the fractalMass to set
	 */
	public void setFractalMass(double fractalMass) {
		this.fractalMassPerCell = fractalMass;
	}


	/**
	 * @return the organicMatterMass
	 */
	public double getOrganicMatterMass() {
		return organicMatterMassPerCell;
	}


	/**
	 * @param organicMatterMass the organicMatterMass to set
	 */
	public void setOrganicMatterMass(double organicMatterMass) {
		this.organicMatterMassPerCell = organicMatterMass;
	}


	/**
	 * @return the solidMass
	 */
	public double getSolidMass() {
		return solidMassPerCell;
	}


	/**
	 * @param solidMass the solidMass to set
	 */
	public void setSolidMass(double solidMass) {
		this.solidMassPerCell = solidMass;
	}

	public void initMatters(double OM, double solidMatter)
	{
		this.organicMatterWeight=OM;
		this.solidWeight=solidMatter;
		precomputeMatters();
	}
	
	
	private void precomputeMatters()
	{
		this.referenceOMWeight= this.organicMatterWeight;
		this.referenceSolidWeight= this.solidWeight;
		double cellSize=0;
		if(this.getParent()==null)
			cellSize=SoilLocation.getCellSize(1, this.getWorld());
		else
		{
			SoilLocation ft=(SoilLocation)this.getParent().getLocation();
			cellSize=SoilLocation.getCellSize(ft.getScale()+2, this.getWorld());
		}
		double cellVol=cellSize*cellSize*cellSize;
		/*compute the mass of organic matter*/
		organicMatterMassPerCell=getTemplate().getOrganicMatterVolumicMass()*cellVol;
		/*compute the mass of solid matter*/
		solidMassPerCell=getTemplate().getSolidVolumicMass()*cellVol;
		/*compute the mass of sub fractal*/
		fractalMassPerCell=(referenceOMWeight+referenceSolidWeight)-(this.getWorld().getDivisionPerLevel()*(getTemplate().getOrganic()*organicMatterMassPerCell+getTemplate().getSolid()*solidMassPerCell));
	}


	public String getTemplateName() {	
		return template==null?null:template.getTemplateName();
	}

	public void setTemplate(Template t)
	{
		this.template = t;
	}
}
