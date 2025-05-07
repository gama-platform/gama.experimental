/*******************************************************************************************************
 *
 * SimUnit.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.experimental.jdeqsim.core;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.experimental.jdeqsim.core.message.Message;
import gama.experimental.jdeqsim.core.unit.Scheduler;

/**
 * Unit using the scheduler
 * 
 * @author Jean-François Erdelyi
 */
public abstract class SchedulingUnit extends SkillUnit {
	private Scheduler scheduler;

	public SchedulingUnit(IScope scope, IAgent relativeAgent, Scheduler scheduler) {
		super(scope, relativeAgent);
		this.scheduler = scheduler;
	}

	public Message sendMessage(Message m) {
		return scheduler.schedule(m);
	}

	public Scheduler getScheduler() {
		return scheduler;
	}
}
