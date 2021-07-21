/*******************************************************************************************************
*
* DummySimUnit.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gama.test.jdeqsim.util;

import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.scheduler.message.Message;
import irit.gama.core.sim_unit.SimUnit;
import msi.gama.runtime.IScope;

public class DummySimUnit extends SimUnit {

	public DummySimUnit(IScope scope, Scheduler scheduler) {
		super(scope, scheduler);
	}

	public void handleMessage(Message m) {
	}

}
