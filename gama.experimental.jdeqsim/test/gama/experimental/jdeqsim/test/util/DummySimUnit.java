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

package gama.experimental.jdeqsim.test.util;

import gama.core.runtime.IScope;
import gama.experimental.jdeqsim.core.SchedulingUnit;
import gama.experimental.jdeqsim.core.message.Message;
import gama.experimental.jdeqsim.core.unit.Scheduler;

public class DummySimUnit extends SchedulingUnit {

	public DummySimUnit(IScope scope, Scheduler scheduler) {
		super(scope, null, scheduler);
	}

	public void handleMessage(Message m) {
	}

}
