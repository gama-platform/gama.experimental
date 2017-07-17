/*
* unBreakableParticle.java : microbes.environment.EnvException
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
package ummisco.gama.apsf.exception;


public class UnBreakableParticle extends APSFException {

	public UnBreakableParticle() {
		super("This particle cannot be broken down into small particle.");
		// TODO Auto-generated constructor stub
	}

	public UnBreakableParticle(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public UnBreakableParticle(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public UnBreakableParticle(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
