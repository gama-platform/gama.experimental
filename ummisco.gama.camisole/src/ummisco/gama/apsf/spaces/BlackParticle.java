/*
* BlackParticle.java : environment
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


public abstract class BlackParticle extends Particle {
	public  static int SAND_PARTICLE=0;
	public   static  int ORGANIC_MATTER=1;
	private int particleType ;
	public BlackParticle() {
		super();
		this.particleType=SAND_PARTICLE;
		// TODO Auto-generated constructor stub
	}
	public BlackParticle(int type) {
		super();
		this.particleType=type;
		// TODO Auto-generated constructor stub
	}

	public BlackParticle(BlackParticle p) {
		super( p);
		this.particleType=p.particleType;
		this.agent = p.agent;
		// TODO Auto-generated constructor stub
	}

}
