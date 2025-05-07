/*******************************************************************************************************
*
* TestMessageQueue.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package jdeqsim.irit.gama.test.jdeqsim;

import irit.gama.core.message.Message;
import irit.gama.core.message.MessageQueue;
import irit.gama.test.jdeqsim.util.DummyMessage;
import msi.gama.runtime.GAMA;
import msi.gama.util.GamaDate;

public class TestMessageQueue {
	public static void test() {
		TestMessageQueue.testPutMessage1();
		TestMessageQueue.testPutMessage2();
		TestMessageQueue.testPutMessage3();

		TestMessageQueue.testRemoveMessage1();
		TestMessageQueue.testRemoveMessage2();
		TestMessageQueue.testRemoveMessage3();

		TestMessageQueue.testMessagePriority();
	}

	public static void testPutMessage1() {
		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 2000));

		mq.putMessage(m1);
		mq.putMessage(m2);
		assert (2 == mq.getQueueSize());
		assert (mq.getNextMessage() == m1);
	}

	public static void testPutMessage2() {
		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 2000));

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));

		mq.putMessage(m1);
		mq.putMessage(m2);
		assert (2 == mq.getQueueSize());
		assert (mq.getNextMessage() == m2);
	}

	public static void testPutMessage3() {
		GamaDate date1 = new GamaDate(GAMA.getRuntimeScope(), 1000);

		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 2000));

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(date1);

		Message m3 = new DummyMessage(null, null);
		m3.setMessageArrivalTime(date1);

		mq.putMessage(m1);
		mq.putMessage(m2);
		mq.putMessage(m3);
		assert (3 == mq.getQueueSize());
		assert (mq.getNextMessage().getMessageArrivalTime().equals(date1));
	}

	public static void testRemoveMessage1() {
		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 2000));

		mq.putMessage(m1);
		mq.putMessage(m2);
		mq.removeMessage(m1);
		assert (1 == mq.getQueueSize());
		assert (mq.getNextMessage() == m2);
		assert (0 == mq.getQueueSize());
	}

	public static void testRemoveMessage2() {
		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 2000));

		mq.putMessage(m1);
		mq.putMessage(m2);
		mq.removeMessage(m2);
		assert (1 == mq.getQueueSize());
		assert (mq.getNextMessage() == m1);
		assert (0 == mq.getQueueSize());
	}

	public static void testRemoveMessage3() {
		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));

		mq.putMessage(m1);
		mq.putMessage(m2);
		mq.removeMessage(m1);
		assert (1 == mq.getQueueSize());
		assert (!mq.isEmpty());
		assert (mq.getNextMessage() == m2);
		assert (0 == mq.getQueueSize());
		assert (mq.isEmpty());
	}

	// a higher priority message will be at front of queue, if there are
	// several messages with same time
	public static void testMessagePriority() {
		MessageQueue mq = new MessageQueue();
		Message m1 = new DummyMessage(null, null);
		m1.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));
		m1.setPriority(10);

		Message m2 = new DummyMessage(null, null);
		m2.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));
		m2.setPriority(5);

		Message m3 = new DummyMessage(null, null);
		m3.setMessageArrivalTime(new GamaDate(GAMA.getRuntimeScope(), 1000));
		m3.setPriority(20);

		mq.putMessage(m1);
		mq.putMessage(m2);
		mq.putMessage(m3);

		assert (mq.getNextMessage() == m3);
		assert (mq.getNextMessage() == m1);
		assert (mq.getNextMessage() == m2);
		assert (0 == mq.getQueueSize());
		assert (mq.isEmpty());
	}

}
