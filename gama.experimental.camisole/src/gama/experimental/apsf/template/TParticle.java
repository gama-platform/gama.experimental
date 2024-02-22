/*
* TParticle.java : gama.experimental.environment.template
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

public class TParticle {
	Particle particle;
	float organicMatterQt;
	public float getOrganicMatterQt() {
		return organicMatterQt;
	}
	public void setOrganicMatterQt(float organicMatterQt) {
		this.organicMatterQt = organicMatterQt;
	}

	public Particle getParticle() {
		return particle;
	}
	public void setParticle(Particle particle) {
		this.particle = particle;
	}
	public TParticle(Particle particleType, float organicMatterQt) {
		super();
		this.particle = particleType;
		this.organicMatterQt = organicMatterQt;
	}
	
}
