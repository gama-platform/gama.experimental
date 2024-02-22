/*
* SoilLocation.java : environment
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
package gama.experimental.apsf.spaces;

import javax.vecmath.Point3d;

public class SoilLocation {
	public final static String SEPARATOR_LOCATION=":";
	// les coordonnées (x,y,z) sont les coord dans la structure finale 
	private int x;
	private int y;
	private int z;
	private int scale;// l'echelle à laquelle appartient le canevas
	
	private Apsf world;
	
	public SoilLocation(int x, int y, int z, int scale, Apsf app) {
		//long max = getMaxCoordinateAccordingToScale(scale,app);
		
		//super();
		// TODO Auto-generated constructor stub
		this.x = x ; //(int) ((x+max) % max);
		this.y = y ; //(int) ((y+max) % max);
		this.z = z; //(int) ((z+max) % max);
		this.scale=scale;
		this.world=app;
	}
	public SoilLocation(Apsf app) {
		super();
		this.world=app;
		// TODO Auto-generated constructor stub
	}

	
	public Object parseString(String arg0) {
		String [] parameters=arg0.split(SEPARATOR_LOCATION);
		
		x=Integer.valueOf(parameters[0]).intValue();
		y=Integer.valueOf(parameters[1]).intValue();
		z=Integer.valueOf(parameters[2]).intValue();
		scale=Integer.valueOf(parameters[3]).intValue();
		return this;

		
	}

	public Point3d getAbsoluteCoordinate()
	//(fx, fy, fz) position en centimetre dans la structure 
	// (x,y,z, scale) les coordonnées des cellules dans la structure  
	{
		double max = (double) (SoilLocation.getMaxCoordinateAccordingToScale(scale, this.world));
		
		double fx=this.x/max*world.getDimension();
		double fy=this.y/max*world.getDimension();
		double fz=this.z/max*world.getDimension();

		Point3d xyz=new Point3d(fx,fy,fz);
		return xyz;
	}
	

	
	
	@Override
	public String toString() {
		
		return x+SEPARATOR_LOCATION+y+SEPARATOR_LOCATION+z+SEPARATOR_LOCATION+scale;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int getScale() {
		return scale;
	}
	
	public static long fact(int c,int value)
	{
		return (long) Math.pow(value,c) ;
	}
	
	public boolean equals(Object b)
	{
		if(b instanceof SoilLocation)
		{
			SoilLocation loc=(SoilLocation)b;
			if(loc.scale==this.scale&&loc.x==this.x&&loc.y==this.y&&loc.z==this.z)
				return true;
		}
		return false;
	}
	
	public long getMaxCoordinateAccordingToScale(Apsf app)
	{
		return getMaxCoordinateAccordingToScale(this.scale,app);
	}
	
	public static double getCellSize(int scale, Apsf app)
	{
		return app.getDimension()/getMaxCoordinateAccordingToScale(scale,app);
		
	}
	
	public static int nbCellPerLevel(Apsf app)
	{
		return app.getDivisionPerLevel()*app.getDivisionPerLevel()*app.getDivisionPerLevel(); // DIVISION_PER_LEVEL*DIVISION_PER_LEVEL*DIVISION_PER_LEVEL;
	}
	public static long getMaxCoordinateAccordingToScale(int s, Apsf app)
	{
		return fact(s,app.getDivisionPerLevel());
	}
	
	
	public static SoilLocation changeScale(SoilLocation s, int scale)
	{
		if(s.getScale()==scale)
			return s;
		
		double level1 =fact(scale,s.world.DIVISION_PER_LEVEL);		
		double level2 =fact(s.getScale(),s.world.DIVISION_PER_LEVEL);	
		//attention, le 0.1 est pour palier au erreur de calcul
		int x=(int)(((double)s.getX()/level2*level1)+0.00001); //(level1/level2)); //level2*level1);
		int y=(int)(((double)s.getY()/level2*level1)+0.00001); ///level2*level1);
		int z=(int)(((double)s.getZ()/level2*level1)+0.00001);
		
		return new SoilLocation(x,y,z,scale,s.world);
	}
	public Apsf getWorld() {
		return world;
	}
	
	public void setWorld(Apsf app)
	{
		this.world = app;
	}

}
