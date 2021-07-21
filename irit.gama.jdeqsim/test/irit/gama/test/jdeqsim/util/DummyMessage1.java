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

package irit.gama.test.jdeqsim.util;

import irit.gama.core.scheduler.message.Message;

public class DummyMessage1 extends Message {

	public Message messageToUnschedule = null;

	@Override
	public void handleMessage() {
		this.getReceivingUnit().getScheduler().unschedule(messageToUnschedule);
	}
}
