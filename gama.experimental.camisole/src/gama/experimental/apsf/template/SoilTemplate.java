/*
 * SoilTemplate.java : gama.experimental.environment
 * Copyright (C) 2003-2007 Nicolas Marilleau
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
package gama.experimental.apsf.template;

import java.util.ArrayList;

import gama.experimental.apsf.spaces.Agglomerate;
import gama.experimental.apsf.spaces.OrganicMatter;
import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SandParticle;
import gama.experimental.apsf.spaces.SoilLocation;
import gama.experimental.apsf.spaces.WhiteParticle;
import gama.experimental.camisole.RandomGenerator;

public class SoilTemplate extends Template {

	public final static String DEFAULT_NAME = "Soil template";

	private int ecartType = 2;
	private int esperance = 5;
/*
	private double gaussDistribution(double x, double ecartType,
			double esperance) {
		double temp = (x - esperance) / ecartType;
		// System.out.println("test :"+temp+" "+Math.exp((-1/2)*(temp*temp
		// ))+" "+(-1/2)*(temp*temp )+" "+-1/2*temp*temp);
		return ((1 / (ecartType * Math.sqrt(2 * Math.PI))))
				* Math.exp((-1 / 2.0) * (temp * temp));
	}*/

	
	int gaussNbCells( double ecartType, double esperance)	{
		return Math.round((float)RandomGenerator.getNormalGen(esperance,ecartType).nextDouble());
	}
	
	

	// ajouter des pores (WhiteParticle)
	void pushWhiteParticle(int x, int y, int z, int number) {
		float ll = 0;

		int total = 0;
		int distance = 0;
		while (total < number) {

			int i = x - distance;

			if (i < 0)
				i = 0;

			for (; i < size && i <= x + distance && total < number; i++) {

				int j = y - distance;
				if (j < 0)
					j = 0;
				for (; j < size && j <= y + distance && total < number; j++) {
					int k = z - distance;
					if (k < 0)
						k = 0;
                     
					for (; k < size && k <= z + distance && total < number; k++) {
						if (this.getParticle(i, j, k) instanceof Agglomerate) {
							Particle pp = new WhiteParticle();
							pp.setLocation(new SoilLocation(i, j, k, 1,null));
							this.setTemplate(pp, i, j, k, ll);
							total++;
						}
					}
				}
			}
			distance++;
			if (distance == 5)
				number = 0;
		}
	}
    
	ArrayList<Agglomerate> getAllAgglomerate()
	{
		ArrayList<Agglomerate> aggl = new ArrayList<Agglomerate>();
		for(int i=0;i<size;i++)
			for(int j =0;j<size;j++ )
				for(int k = 0 ; k<size;k++)
				{
					Particle p = getParticle(i, j, k);
					if(p instanceof Agglomerate)
					{
						aggl.add((Agglomerate)p);
					}
				}
		return aggl;
	}
	
	public void generateTemplateFinal(Template subTemplate) {
		double og = this.organic;
		subTemplate = subTemplate==null?this:subTemplate;
		
		// initialisation du template en le remplissant de fractal
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				for (int k = 0; k < size; k++) {
					Particle pp;
                    pp = new Agglomerate(subTemplate);
					pp.setLocation(new SoilLocation(i, j, k, 1,null));
					this.setTemplate(pp, i, j, k, 0);

				}
		// dépots des cavités
		// Distribution aléatoire des Pores 
		int nbCavite = (int) ((1 - solid - fractal - organic) * (size * size * size));
		int nbCaviteCree = 0;
		while (nbCaviteCree < nbCavite) {
			int nb = gaussNbCells(ecartType, esperance);
			// position aléatoire
			int i = (int) Math.round(Math.random() * size) % size;
			int j = (int) Math.round(Math.random() * (size)) % size;
			int k = (int) Math.round(Math.random() * (size)) % size;
			pushWhiteParticle(i, j, k, nb);
			nbCaviteCree += nb;
		}
		
		// dépot de la matiere organique
		int nbOrganic = (int) ((organic) * (size * size * size));
		int nbOrganicCree = 0;
		
		ArrayList<Agglomerate> aggl  = getAllAgglomerate();
	
		
		
		while (nbOrganicCree < nbOrganic && aggl.size()>0) {
			int l = (int) Math.round(Math.random() * (aggl.size()-1));
			Agglomerate a = aggl.get(l);
			aggl.remove(l);
			int i = a.getPI();
			int j =  a.getPJ();
			int k =  a.getPK();
				Particle pp;
				pp = new OrganicMatter();
				pp.setLocation(new SoilLocation(i, j, k, 1,null));
				this.setTemplate(pp, i, j, k, 0);
				nbOrganicCree++;
			
		}
		
		// dépot des minéraux

		int nbSand = (int) ((solid) * (size * size * size));
		int nbSandCree = 0;

		 aggl  = getAllAgglomerate();
		while (nbSandCree < nbSand && aggl.size()>0) {
			
			int l = (int) Math.round(Math.random() * (aggl.size()-1));
			Agglomerate a = aggl.get(l);
			aggl.remove(l);
			int i = a.getPI();
			int j =  a.getPJ();
			int k =  a.getPK();
				Particle pp;
				pp = new SandParticle();
				pp.setLocation(new SoilLocation(i, j, k, 1,null));
				this.setTemplate(pp, i, j, k, 0);
				nbSandCree++;
			
		}
	}

	public SoilTemplate(String name, float solid, float fractal, float organic,
			int size, int ecartType, int esperance) {
		super(name, solid, fractal, organic, size);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(this);
	}

	public SoilTemplate(String name, float solid, float fractal, float organic,
			int size, int ecartType, int esperance, Template sub) {
		super(name, solid, fractal, organic, size, sub);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(sub);
	}

	public SoilTemplate(float solid, float fractal, float organic, int size,
			int ecartType, int esperance, Template sub) {
		super(DEFAULT_NAME, solid, fractal, organic, size, sub);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(sub);
	}

	public SoilTemplate(float solid, float fractal, float organic, int size,
			int ecartType, int esperance) {
		super(DEFAULT_NAME, solid, fractal, organic, size);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(this);
	}

	public SoilTemplate() {
		super(DEFAULT_NAME, 0, 0, 0, 10);
		this.ecartType = 0;
		this.esperance = 0;
	}

	@Override
	public void generateTemplate() {
		// TODO Auto-generated method stub

	}

}
