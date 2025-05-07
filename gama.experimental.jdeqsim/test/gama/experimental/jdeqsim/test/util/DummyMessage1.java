/*******************************************************************************************************
*
* DummyMessage1.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package gama.experimental.jdeqsim.test.util;

import gama.core.util.GamaDate;
import gama.experimental.jdeqsim.core.SchedulingUnit;
import gama.experimental.jdeqsim.core.message.Message;
import gama.experimental.jdeqsim.core.unit.Scheduler;

public class DummyMessage1 extends Message {

	public Message messageToUnschedule = null;

	public DummyMessage1(SchedulingUnit receivingUnit, Scheduler scheduler, GamaDate messageArrivalTime) {
		this.receivingUnit = receivingUnit;
		this.messageArrivalTime = messageArrivalTime;
		this.scheduler = scheduler;
	}

	@Override
	public void handleMessage() {
		scheduler.unschedule(messageToUnschedule);
	}
}
