package ummisco.gama.apsf.spaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.tools.JavaFileManager.Location;

import com.kitfox.svg.A;
import com.sun.javafx.css.converters.SizeConverter;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gaml.species.GamlSpecies;
import ummisco.gama.apsf.exception.UnBreakableParticle;
import ummisco.gama.apsf.template.EmptyTemplate;
import ummisco.gama.apsf.template.Template;

// les méthodes de cette classe sont inspirées du MicrobesEnvironnement de sworm


public class Apsf {
	public static float DEFAULT_WORLD_DIMENSION=20; // taille en cm
	public static int DIVISION_PER_LEVEL=10;
	
	private double dimension;
	private int division_per_level;
	private Agglomerate spaceRoot;
	private Map<String, Template> templates;
	private IAgent macroAgent;
	private GamlSpecies mySpecies;
	
	public void setDefaultSpecies(GamlSpecies sp)
	{
		this.mySpecies = sp;
	}
	public GamlSpecies getDefaultSpecies()
	{
		return this.mySpecies;
	}
	
	public IAgent getUnderworldAgent()
	{
		return macroAgent;
	}
	
	public int getDivisionPerLevel() {
		return division_per_level;
	}

	public void setDivisionPerLevel(int division_per_level) {
		this.division_per_level = division_per_level;
	}

	public Apsf(IAgent agt)
	{
		this.dimension = DEFAULT_WORLD_DIMENSION;
		this.templates = new HashMap<>();
		this.macroAgent = agt;
	}
	
	public Apsf(double dim, int div,IAgent agt)
	{
		this.dimension = dim;
		this.division_per_level  = div;
		this.macroAgent = agt;
	}
	
	public Template getTemplateWithName(String name)
	{
		return this.templates.get(name);
	}
	
	public double getDimension()
	{
		return dimension;
	}
	
	public Agglomerate getAPSF() {
		return spaceRoot;
	}
	
	public void defineTemplateTree(Template t)
	{
		this.templates = new HashMap<>();
		ArrayList<Template> tps = t.getAllSubTemplate();
		for(Template tt : tps)
			templates.put(tt.getTemplateName(), tt);
		SoilLocation loc = new SoilLocation(0,0,0,0,this);
		this.spaceRoot = new Agglomerate(t);
		this.spaceRoot.setLocation(loc);
	}
	
	
	public ArrayList<Template> findBranchTo(Template t)
	{
		if(t== this.getAPSF().getTemplate())
		{
			ArrayList<Template> res = new ArrayList<>();
			res.add(t);
			return res;
		}
		Collection<Template> tt =templates.values();
		for(Template tm:tt)
		{
			if(tm.isParentOf(t.getTemplateName()))
			{
				ArrayList<Template> res =findBranchTo(tm);
				res.add(0, t);
				return res;
			}
		}
		return null;
	}
	
	SoilLocation chooseLocationInside(Agglomerate p,  String type,boolean sampled )
	{
		Particle pp = ((Agglomerate)p).getTemplate().getSamplerWithCharacteristics(type);

		/*ArrayList<Particle> ppp = ((Agglomerate)p).getTemplate().getCellsWithCharacteristics(type);

		int idx = ((int)(Math.random() * 1000)) % ppp.size();
		pp = ppp.get(idx);
*/
		SoilLocation s2 = ((Agglomerate)p).getSubParticleLocation( pp.getPI(),pp.getPJ(),pp.getPK() );
		return s2;
		
	}
	
	public Particle getOneParticleWithCharacteristics(IScope scope, Template t, int scale, String type,boolean sampled )
	{
		scale = scale + 1; 
		Particle choosedP = this.getAPSF();
		ArrayList<Template> lst = findBranchTo(t);
		int j = 0;
		int lstSize = lst.size();
		int i = lstSize - 1;
		ArrayList<Particle> res = new ArrayList<>();
		while(i>=0 && scale >=(lstSize - i))
		{
			Template temp = lst.get(i);
			String choosenType = type;
			if( i > 0 && (lstSize - i) != scale  )
			{
				
				choosenType = lst.get(i-1).getTemplateName();
			}
			SoilLocation s2 = chooseLocationInside((Agglomerate)choosedP,choosenType, sampled);
			choosedP = getParticleAtLocation(scope,s2);
			i = i - 1;
		}
		if(scale ==(lstSize - i))
			return choosedP;
		int currentScale = lstSize;
		while(currentScale<scale)
		{
			SoilLocation s2 = chooseLocationInside((Agglomerate)choosedP,choosedP.getTemplateName(),sampled);
			choosedP = getParticleAtLocation(scope,s2);
			currentScale = currentScale +1;
		}

		SoilLocation s2 = chooseLocationInside((Agglomerate)choosedP,type,sampled);
		choosedP = getParticleAtLocation(scope,s2);
		return choosedP;
	}
	
	public Particle getParticleAtLocation(IScope scope, SoilLocation loc)
	{
		Particle p =spaceRoot.findParticle(scope,loc);
		while(p instanceof Agglomerate&&((SoilLocation)p.getLocation()).getScale()!=loc.getScale())
		{
			SoilLocation mloc = SoilLocation.changeScale(loc, p.getLocation().getScale()+1);
			Particle pp =((Agglomerate)p).getTemplate().getNewParticleInstance(scope,mloc);
			pp.setParent((Agglomerate)p);
			try
			{
				p.putParticle(scope,pp);
			}
			catch(UnBreakableParticle e)
			{
				p =spaceRoot.findParticle(scope,loc);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p = pp;
		}
		return p;
	}
	public double countTotalOrganicMatter()
	{
		return countTotalMatter(IParticle.ORGANIC_MATTER_PARTICLE);
		
	}
	public double countTotalSandMatter()
	{
		return countTotalMatter(IParticle.SAND_PARTICLE);
		
	}
	
	public double countOrganicMatterAtSizeScale(int sizeScale)
	{
		return countMatterAtSizeScale(sizeScale,IParticle.ORGANIC_MATTER_PARTICLE);
	}
	public double countSandMatterAtSizeScale(int sizeScale)
	{
		return countMatterAtSizeScale(sizeScale,IParticle.SAND_PARTICLE);
	}
	public double countPorousMatterAtSizeScale(int sizeScale)
	{
		return countMatterAtSizeScale(sizeScale,IParticle.WHITE_PARTICLE);
	}
	
	
	public double countTotalMatter(String name )
	{
		double res = 0;
		int i = 0;
		while(i<4)
		{
			double tmpRes = countMatterAtSizeScale(i,name);
			res += tmpRes;
			i++;
		}
		return res;
	}
	
	public double countMatterAtSizeScale(int sizeScale, String name)
	{
		long nbTCell = ((long) Math.pow(spaceRoot.getTemplate().getNumberOfCell(), sizeScale+1));
		long nbCellFound = countMatterAtSizeScale(sizeScale,name,spaceRoot.getTemplate(),0);
		return (double)nbCellFound/nbTCell;
	}
	
	private long countMatterAtSizeScale(int sizeScale,String name, Template t, int currentScale)
	{
		long sum = 0;
		if(sizeScale==currentScale)
			sum = t.getNbCellsOfTemplateWithName(name);
		else
			{
				Enumeration<Template> e = t.getSubTemplates();
				while(e.hasMoreElements())
				{
					Template subT=e.nextElement();
					long rate = t.getNbCellsOfTemplateWithName(subT.getTemplateName());
					sum += rate*countMatterAtSizeScale(sizeScale,name,subT,currentScale + 1);
				}
			}
		return sum;
	}

	
	
}
