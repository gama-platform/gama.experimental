/*
 * AlreadyCreatedParticle.java : microbes.environment.EnvException Copyright (C) 2003-2006 Nicolas Marilleau
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package gama.experimental.apsf.exception;

public class AlreadyCreatedParticle extends APSFException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public AlreadyCreatedParticle() {
		super("the particle already exists");
	}

	public AlreadyCreatedParticle(final String arg0) {
		super(arg0);
	}

	public AlreadyCreatedParticle(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public AlreadyCreatedParticle(final Throwable arg0) {
		super(arg0);
	}

}
