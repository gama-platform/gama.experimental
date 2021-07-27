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

package irit.gama.core.unit;

import irit.gama.core.SkillUnit;
import irit.gama.core.plan.Activity;
import irit.gama.core.plan.Leg;
import irit.gama.core.plan.Plan;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;

/**
 * Person implementation this is the internal object using in the skill
 * JDEQSIMPersonSkill
 * 
 * @author Jean-François Erdelyi
 */
public class Person extends SkillUnit {

	private Plan selectedPlan = null;

	public Person() {
		super(null, null);
	}

	public Person(IScope scope, IAgent relativeAgent) {
		super(scope, relativeAgent);
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
