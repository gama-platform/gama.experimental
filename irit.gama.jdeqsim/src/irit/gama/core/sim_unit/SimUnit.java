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

package irit.gama.core.sim_unit;

import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.scheduler.message.Message;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaDate;

/**
 * The basic building block for all simulation units.
 *
 * @author rashid_waraich
 */
public abstract class SimUnit {
	protected Scheduler scheduler = null;
	protected IScope scope = null;

	/**
	 * Constructor
	 * 
	 * @param scheduler
	 */
	public SimUnit(IScope scope, Scheduler scheduler) {
		this.scope = scope;
		this.scheduler = scheduler;
	}

	/**
	 * Send message
	 * 
	 * @param m                  the message
	 * @param messageArrivalTime
	 */
	public void sendMessage(Message m, SimUnit targetUnit, GamaDate messageArrivalTime) {

//		m.setSendingUnit(this);
		// this info is set, but never used. Commenting it out for the time being,
		// especially since one can generate circumvent this method here.
		// would (evidently) be easy to re-instantiate since it just needs the "this"
		// pointer. kai, feb'18

		m.setReceivingUnit(targetUnit);
		m.setMessageArrivalTime(messageArrivalTime);
		scheduler.schedule(scope, m);
	}

	public Scheduler getScheduler() {
		return scheduler;
	}
}
