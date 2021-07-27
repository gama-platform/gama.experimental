/*******************************************************************************************************
 *
 * Plan.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
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

/**
 * Plan implementation
 * 
 * @author Jean-François Erdelyi
 */
public class Plan {

	private ArrayList<IPlanElement> plan = new ArrayList<>();

	public void addActivity(Activity activity) {
		plan.add(activity);
	}

	public void addLeg(Leg leg) {
		plan.add(leg);
	}

	public List<? extends IPlanElement> getPlanElements() {
		return plan;
	}

}
