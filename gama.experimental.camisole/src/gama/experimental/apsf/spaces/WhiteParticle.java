/*
* WhiteParticle.java : environment
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


public class WhiteParticle extends Particle {

	public WhiteParticle() {
		super();
		porosity=1;
		// TODO Auto-generated constructor stub
	}	
	public WhiteParticle(WhiteParticle p) {
		super(p);
		porosity=1; 
		this.agent =p.agent;
	}
	@Override
	public Particle clone() {
		// TODO Auto-generated method stub
		return new WhiteParticle(this);
	}
	public void setParent(Agglomerate parent) {
		super.setParent(parent);
		this.organicMatterWeight=0;
		this.solidWeight=0;
	}
	@Override
	public String getTemplateName() {
		return IParticle.WHITE_PARTICLE;
	}
}
