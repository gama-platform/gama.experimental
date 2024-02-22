/*
* SandTemplate.java : gama.experimental.environment
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

import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SandParticle;
import gama.experimental.apsf.spaces.SoilLocation;

public class SandTemplate extends Template {
	
	public final static String SAND_AGGLOMERATE=" agglomerate";
	public final static String DEFAULT_NAME="sand template";
	
	public static String getTemplateWithWormName(String n)
	{
		return n+SAND_AGGLOMERATE;
	}
	 public SandTemplate( int size) {
		super(DEFAULT_NAME,1, 0, 0, size);
		// TODO Auto-generated constructor stub
	}
	 public SandTemplate() {
			super(DEFAULT_NAME,1, 0, 0, 10);
			// TODO Auto-generated constructor stub
		}

	@Override
	public void generateTemplate() {
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++)
				for(int k=0;k<size;k++)
				{
					Particle pp=new SandParticle();
					pp.setLocation(new SoilLocation(i,j,k,1,null));
					this.setTemplate(pp,i,j,k,0);
				}

	}

}
