/*
 * SoilTemplate.java : gama.experimental.environment Copyright (C) 2003-2007 Nicolas Marilleau
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
package gama.experimental.apsf.template;

import gama.experimental.apsf.spaces.Agglomerate;
import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SoilLocation;
import gama.experimental.apsf.spaces.TechnoSoilHorizon;
import gama.experimental.apsf.spaces.WhiteParticle;

public class TechnoSolTemplate extends Template {

	public final static String DEFAULT_NAME = "Soil template";

	private final TechnoSoilHorizon[] horizons;

	private double gaussDistribution(final double x, final double ecartType, final double esperance) {
		final double temp = (x - esperance) / ecartType;
		// System.out.println("test :"+temp+" "+Math.exp((-1/2)*(temp*temp
		// ))+" "+(-1/2)*(temp*temp )+" "+-1/2*temp*temp);
		return 1 / (ecartType * Math.sqrt(2 * Math.PI)) * Math.exp(-1 / 2.0 * (temp * temp));
	}

	int gaussNbCells(final double value, final double ecartType, final double esperance) {

		double integral = gaussDistribution(0, ecartType, esperance);
		int i = 0;
		while (integral < value) {
			i++;
			integral = integral + gaussDistribution(i, ecartType, esperance) + 0.001;
			// System.out.println("int?gral"+ integral+" " +value+
			// " "+gaussDistribution(i, ecartType, esperance));
		}
		final int dur = (int) Math.round(i);
		return dur;
	}

	void pushWhiteParticle(final int x, final int y, final int z, int number) {
		final float ll = 0;

		int total = 0;
		int distance = 0;
		while (total < number) {

			int i = x - distance;

			if (i < 0) {
				i = 0;
			}

			for (; i < size && i <= x + distance && total < number; i++) {

				int j = y - distance;
				if (j < 0) {
					j = 0;
				}
				for (; j < size && j <= y + distance && total < number; j++) {
					int k = z - distance;
					if (k < 0) {
						k = 0;
					}

					for (; k < size && k <= z + distance && total < number; k++) {
						if (this.getParticle(i, j, k) instanceof Agglomerate) {
							final Particle pp = new WhiteParticle();
							pp.setLocation(new SoilLocation(i, j, k, 1, null));
							this.setTemplate(pp, i, j, k, ll);
							total++;
						}
					}
				}
			}
			distance++;
			if (distance == 5) {
				number = 0;
			}
		}
	}

	public void generateTemplateFinal() {
		// initialisation du template en le remplissant de fractal
		int debutHorizon = 0, finHorizon = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				debutHorizon = 0;
				finHorizon = 0;
				for (final TechnoSoilHorizon horizon : horizons) {
					finHorizon = finHorizon + horizon.getDepth();
					for (int k = debutHorizon; k < finHorizon; k++) {
						Particle pp;
						Template tmp = null;
						tmp = horizon.getHorizon();
						pp = new Agglomerate(tmp);
						pp.setLocation(new SoilLocation(i, j, k, 1, null));
						this.setTemplate(pp, i, j, k, 0);
					}
					debutHorizon = debutHorizon + horizon.getDepth();
				}
			}
		}
	}

	public TechnoSolTemplate(final String name, final int size, final TechnoSoilHorizon[] h) {
		super(name, 0, 1, 0, size);
		this.horizons = h;
		for (final TechnoSoilHorizon horizon : horizons) {
			System.out.println("size horizon = " + horizon.getDepth());
		}
		this.generateTemplateFinal();
	}

	@Override
	public void generateTemplate() {
		// TODO Auto-generated method stub

	}

}