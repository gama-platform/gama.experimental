/*
* TemplateFactory.java : ummisco.gama.environment.template
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


public  class TemplateFactory {
	private Template[] templateList;
	private int size;
	private final static int TEMPLATES_NB=5;
	public final static int SOIL_TEMPLATE=0;
	public final static int DEJECTION_TEMPLATE=1;
	public final static int SAND_TEMPLATE=2;
	public final static int EMPTY_TEMPLATE=3;
	public final static int FRACTAL_TEMPLATE=4;
	
	public TemplateFactory(int size)
	{
		if(templateList==null)
		{
			templateList=new Template[TEMPLATES_NB];	
		}
		this.size=size;
		
	}
	
	/*public Template getFractalTemplate(int templateType)
	{
		return templateList[templateType];
	}
	*/
	public void initialiseSoilTemplate(float solid, float fractal, float organic, int ecartType, int esperance)
	{
		
		templateList[SOIL_TEMPLATE]=new SoilTemplate(solid,fractal,organic,size,ecartType,esperance);
	}
	public void initialiseFractalTemplate(float solid, float fractal, float organic, Template t)
	{
		templateList[FRACTAL_TEMPLATE]=new FractalTemplate(solid,fractal,organic,size,t);
	}
	public void initialiseDejectionTemplate(float solid, float fractal, float organic)
	{
		templateList[DEJECTION_TEMPLATE]=new DejectionFractal(solid,fractal,organic,size);
	}
	
	public void initialiseSandTemplate()
	{
		templateList[SAND_TEMPLATE]=new SandTemplate(size);
	}
	public void initialiseEmptyTemplate()
	{
		templateList[EMPTY_TEMPLATE]=new EmptyTemplate(size);
	}
}
