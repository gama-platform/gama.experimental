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

package gama.experimental.jdeqsim.core.plan;

import java.util.ArrayList;
import java.util.List;

import gama.experimental.jdeqsim.core.IPlanElement;

/**
 * Plan implementation
 * 
 * @author Jean-François Erdelyi
 */
public class Plan {

	private ArrayList<IPlanElement> plan = new ArrayList<>();

	public boolean addActivity(Activity activity) {
		return plan.add(activity);
	}

	public boolean addLeg(Leg leg) {
		return plan.add(leg);
	}

	public List<? extends IPlanElement> getPlanElements() {
		return plan;
	}

}
