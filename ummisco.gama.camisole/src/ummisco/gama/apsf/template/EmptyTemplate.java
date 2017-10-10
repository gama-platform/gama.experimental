/*
* WhiteTemplate.java : ummisco.gamaenvironment
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
package ummisco.gama.apsf.template;

import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.spaces.SoilLocation;
import ummisco.gama.apsf.spaces.WhiteParticle;

public class EmptyTemplate extends Template {
	public final static String DEFAULT_NAME="Empty template";
	public EmptyTemplate( int size) {
		super(DEFAULT_NAME,0, 0, 0, size);
		// TODO Auto-generated constructor stub
	}

	public EmptyTemplate() {
		super(DEFAULT_NAME,0, 0, 0, 10);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void generateTemplate() {
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++)
				for(int k=0;k<size;k++)
				{
					Particle pp=new WhiteParticle();
					pp.setLocation(new SoilLocation(i,j,k,1,null));
					this.setTemplate(pp,i,j,k,0);
				}

	}

}
