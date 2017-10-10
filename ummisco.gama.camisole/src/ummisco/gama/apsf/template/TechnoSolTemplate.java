/*
 * SoilTemplate.java : ummisco.gama.environment
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

import java.util.Collection;
import java.util.Vector;

import ummisco.gama.apsf.spaces.Agglomerate;
import ummisco.gama.apsf.spaces.OrganicMatter;
import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.spaces.SandParticle;
import ummisco.gama.apsf.spaces.SoilLocation;
import ummisco.gama.apsf.spaces.TechnoSoilHorizon;
import ummisco.gama.apsf.spaces.WhiteParticle;


public class TechnoSolTemplate extends Template {

	public final static String DEFAULT_NAME = "Soil template";

	private int ecartType = 2;
	private int esperance = 5;

	private TechnoSoilHorizon[] horizons;

	private double gaussDistribution(double x, double ecartType,
			double esperance) {
		double temp = (x - esperance) / ecartType;
		// System.out.println("test :"+temp+" "+Math.exp((-1/2)*(temp*temp
		// ))+" "+(-1/2)*(temp*temp )+" "+-1/2*temp*temp);
		return ((1 / (ecartType * Math.sqrt(2 * Math.PI))))
				* Math.exp((-1 / 2.0) * (temp * temp));
	}

	int gaussNbCells(double value, double ecartType, double esperance) {

		int it;
		double integral = gaussDistribution(0, ecartType, esperance);
		int i = 0;
		while (integral < value) {
			i++;
			integral = integral + gaussDistribution(i, ecartType, esperance)
					+ 0.001;
			// System.out.println("int?gral"+ integral+" " +value+
			// " "+gaussDistribution(i, ecartType, esperance));
		}
		int dur = (int) Math.round(i);
		return dur;
	}

	void pushWhiteParticle(int x, int y, int z, int number) {
		float ll = 0;

		int total = 0;
		int distance = 0;
		while (total < number) {

			int i = x - distance;

			if (i < 0)
				i = 0;

			for (; i < size && i <= x + distance && total < number; i++) {

				int j = y - distance;
				if (j < 0)
					j = 0;
				for (; j < size && j <= y + distance && total < number; j++) {
					int k = z - distance;
					if (k < 0)
						k = 0;

					for (; k < size && k <= z + distance && total < number; k++) {
						if (this.getParticle(i, j, k) instanceof Agglomerate) {
							Particle pp = new WhiteParticle();
							pp.setLocation(new SoilLocation(i, j, k, 1,null));
							this.setTemplate(pp, i, j, k, ll);
							total++;
						}
					}
				}
			}
			distance++;
			if (distance == 5)
				number = 0;
		}
	}

	public void generateTemplateFinal() {
		// initialisation du template en le remplissant de fractal
		int debutHorizon = 0, finHorizon = 0;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				debutHorizon = 0;
				finHorizon = 0;
				for (int l = 0; l < horizons.length; l++) {
					finHorizon = finHorizon+horizons[l].getDepth();
					for (int k = debutHorizon; k < finHorizon; k++) {
						Particle pp;
						Template tmp = null;
						tmp = this.horizons[l].getHorizon();
						pp = new Agglomerate(tmp);
						pp.setLocation(new SoilLocation(i, j, k, 1,null));
						this.setTemplate(pp, i, j, k, 0);
					}
					debutHorizon = debutHorizon+horizons[l].getDepth();
				}
			}
	}

	public TechnoSolTemplate(String name, int size, TechnoSoilHorizon[] h) {
		super(name, 0, 1, 0, size);
		this.horizons = h;
		for (int i = 0; i < horizons.length; i++) {
			System.out.println("size horizon = " + horizons[i].getDepth());
		}
		this.generateTemplateFinal();
	}

	@Override
	public void generateTemplate() {
		// TODO Auto-generated method stub

	}

}