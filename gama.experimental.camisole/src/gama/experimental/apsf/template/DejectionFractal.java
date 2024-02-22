/*
* DejectionFractal.java : microbes.environment
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
/*
* DejectionFractal.java : microbes.environment
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

import gama.experimental.apsf.spaces.Agglomerate;
import gama.experimental.apsf.spaces.OrganicMatter;
import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SandParticle;
import gama.experimental.apsf.spaces.SoilLocation;
import gama.experimental.apsf.spaces.WhiteParticle;

/**
 * @author nicolas
 *
 */
public class DejectionFractal extends Template {
	public final static String DEFAULT_NAME="dejection template";


	public void generateTemplate()
	{
		int nbPore=(int)((size*size*size)*(1-solid-fractal-organic));
		
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++)
				for(int k=0;k<size;k++)
				{
					float ll=0;
					double srd=Math.random()*(fractal+solid);
					Particle pp;
					if(nbPore>0)
					{
						pp=new WhiteParticle();
						nbPore--;
					}
					else
					if(srd<fractal)
						{

						ll=(float)(0.3/(1000*organic));
							pp=new Agglomerate(this);
						}
					else
						if(fractal<=srd&&srd<(fractal+solid))
							pp=new SandParticle();
						else
						{
							ll=(float)(0.7/(1000*organic));
							pp=new OrganicMatter();
						}
					pp.setLocation(new SoilLocation(i,j,k,1,null));
					this.setTemplate(pp,i,j,k,ll);
				}
	// 	return template;

	}
	
	public  DejectionFractal(float solid, float fractal, float organic, int size) {
		super(DEFAULT_NAME,solid, fractal, organic,size);
		System.out.println("deection "+solid+" "+fractal+" "+organic);
	}
	public  DejectionFractal() {
		super(DEFAULT_NAME,0, 0, 0,10);
		System.out.println("deection "+solid+" "+fractal+" "+organic);
	}
	
	public  DejectionFractal(String name,float solid, float fractal, float organic, int size) {
		super(name,solid, fractal, organic,size);
		System.out.println(name+" "+solid+" "+fractal+" "+organic);
	}
	



}
