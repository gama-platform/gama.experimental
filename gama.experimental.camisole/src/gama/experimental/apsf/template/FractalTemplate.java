/*
* FractalTemplate.java : gama.experimental.environment.template
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
* FractalTemplate.java : gama.experimental.environment.template
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
import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SoilLocation;

/**
 * @author nicolas
 *
 */
	public class FractalTemplate extends Template {
	public static final String DEFAULT_NAME="Fractal_template";
	
	public FractalTemplate(String name, float solid, float fractal, float organic, int size, Template subT) {
		super(name,solid, fractal, organic, size,  subT);
		
		// TODO Auto-generated constructor stub
	}

	public FractalTemplate() {
		super(DEFAULT_NAME,0, 0, 0, 10);
		
		// TODO Auto-generated constructor stub
	}
	public FractalTemplate( float solid, float fractal, float organic, int size, Template subT) {
		super(DEFAULT_NAME,solid, fractal, organic, size,  subT);
		
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gama.experimental.environment.template.Template#generateTemplate()
	 */
	@Override
	public void generateTemplate() {
		double og=organic/(this.size*this.size*this.size);
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++)
				for(int k=0;k<size;k++)
				{
					Particle pp=new Agglomerate(this.getSubTemplate());
					pp.setLocation(new SoilLocation(i,j,k,1,null));
					this.setTemplate(pp,i,j,k,og);
				}

	}
}
