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

package irit.gama.test.jdeqsim.util;

import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.Message;
import msi.gama.util.GamaDate;

public class DummyMessage extends Message {

	public DummyMessage(SchedulingUnit receivingUnit, GamaDate messageArrivalTime) {
		this.receivingUnit = receivingUnit;
		this.messageArrivalTime = messageArrivalTime;
	}

	@Override
	public void handleMessage() {
	}
}
