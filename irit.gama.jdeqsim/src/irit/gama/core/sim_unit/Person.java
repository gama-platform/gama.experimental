/*******************************************************************************************************
 *
 * Person.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.sim_unit;

import irit.gama.core.Activity;
import irit.gama.core.Leg;
import irit.gama.core.Plan;
import irit.gama.core.scheduler.Scheduler;
import msi.gama.runtime.IScope;

public class Person extends SimUnit {

	private Plan selectedPlan = null;

	public Person(IScope scope, Scheduler scheduler) {
		super(scope, scheduler);
	}

	public void addActivity(Activity activity) {
		if (selectedPlan == null) {
			selectedPlan = new Plan();
		}
		selectedPlan.addActivity(activity);
	}

	public void addLeg(Leg leg) {
		if (selectedPlan == null) {
			selectedPlan = new Plan();
		}
		selectedPlan.addLeg(leg);
	}

	public void removePlan() {
		selectedPlan = null;
	}

	public Plan getSelectedPlan() {
		return selectedPlan;
	}

}
