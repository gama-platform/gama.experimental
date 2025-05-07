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

package gama.experimental.jdeqsim.core.unit;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.experimental.jdeqsim.core.SkillUnit;
import gama.experimental.jdeqsim.core.plan.Activity;
import gama.experimental.jdeqsim.core.plan.Leg;
import gama.experimental.jdeqsim.core.plan.Plan;

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

	public boolean addActivity(Activity activity) {
		if (selectedPlan == null) {
			selectedPlan = new Plan();
		}
		return selectedPlan.addActivity(activity);
	}

	public boolean addLeg(Leg leg) {
		if (selectedPlan == null) {
			selectedPlan = new Plan();
		}
		return selectedPlan.addLeg(leg);
	}

	public void removePlan() {
		selectedPlan = null;
	}

	public Plan getSelectedPlan() {
		return selectedPlan;
	}

	public void setToLocation(GamaPoint iLocation) {
		if (relativeAgent != null) {
			relativeAgent.setLocation(iLocation);
		}
	}

}
