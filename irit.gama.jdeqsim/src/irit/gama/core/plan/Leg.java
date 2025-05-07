/*******************************************************************************************************
 *
 * Leg.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.plan;

import java.util.ArrayList;
import java.util.List;

import irit.gama.core.IPlanElement;
import irit.gama.core.unit.Road;

/**
 * Leg implementation
 * 
 * @author Jean-François Erdelyi
 */
public class Leg implements IPlanElement {

	List<Road> route = new ArrayList<>();

	public Leg(List<Road> roads) {
		route = roads;
	}

	public void addRoad(Road road) {
		this.route.add(road);
	}

	public List<Road> getRoads() {
		return route;
	}

	public Road getFirstRoad() {
		return route.get(0);
	}
}
