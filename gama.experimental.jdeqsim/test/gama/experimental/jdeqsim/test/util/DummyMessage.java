/*******************************************************************************************************
*
* DummyMessage.java, in plugin irit.gama.jdeqsim,
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

public class DummyMessage extends Message {

	public DummyMessage(SchedulingUnit receivingUnit, GamaDate messageArrivalTime) {
		this.receivingUnit = receivingUnit;
		this.messageArrivalTime = messageArrivalTime;
	}

	@Override
	public void handleMessage() {
	}
}
