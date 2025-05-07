/*******************************************************************************************************
*
* TestScheduler.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package jdeqsim.irit.gama.test.jdeqsim;

import irit.gama.core.SchedulingUnit;
import irit.gama.core.message.Message;
import irit.gama.core.unit.Scheduler;
import irit.gama.test.jdeqsim.util.DummyMessage;
import irit.gama.test.jdeqsim.util.DummyMessage1;
import irit.gama.test.jdeqsim.util.DummySimUnit;
import msi.gama.runtime.GAMA;
import msi.gama.util.GamaDate;

public class TestScheduler extends GenericTest {

	public static void test() {
		TestScheduler.testSchedule1();

		TestScheduler.testUnschedule1();
		TestScheduler.testUnschedule2();
	}

	// the time at the end of the simulation is equal to the time of the last
	// message processed
	public static void testSchedule1() {
		GamaDate date9000 = new GamaDate(GAMA.getRuntimeScope(), 9000000);
		GamaDate date10000 = new GamaDate(GAMA.getRuntimeScope(), 10000000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);

		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);
		SchedulingUnit sm1 = new DummySimUnit(GAMA.getRuntimeScope(), scheduler);

		Message m1 = new DummyMessage(sm1, date9000);
		sm1.sendMessage(m1);

		GAMA.getRuntimeScope().getClock().setStartingDate(date10000);
		scheduler.execute(GAMA.getRuntimeScope());
		assert (date9000.equals(scheduler.getLastEventTime()));
	}

	// a message is scheduled and unscheduled before starting the simulation
	// this causes the simulation to stop immediatly (because no messages in queue)
	public static void testUnschedule1() {
		GamaDate date1 = new GamaDate(GAMA.getRuntimeScope(), 1000);
		GamaDate date2 = new GamaDate(GAMA.getRuntimeScope(), 2000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);

		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);
		SchedulingUnit sm1 = new DummySimUnit(GAMA.getRuntimeScope(), scheduler);

		Message m1 = new DummyMessage(sm1, date1);
		sm1.sendMessage(m1);
		scheduler.unschedule(m1);

		GAMA.getRuntimeScope().getClock().setStartingDate(date2);
		scheduler.execute(GAMA.getRuntimeScope());
		assert (DATE_0.equals(scheduler.getLastEventTime()));
	}

	// We shedule two messages, but the first message deletes upon handling the
	// message the second message.
	// This results in that the simulation stops not at time 10, but immediatly at
	// time 1.
	public static void testUnschedule2() {
		GamaDate date1 = new GamaDate(GAMA.getRuntimeScope(), 1000);
		GamaDate date10 = new GamaDate(GAMA.getRuntimeScope(), 10000);
		GamaDate date20 = new GamaDate(GAMA.getRuntimeScope(), 20000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);

		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);
		SchedulingUnit sm1 = new DummySimUnit(GAMA.getRuntimeScope(), scheduler);

		Message m1 = new DummyMessage(sm1, date10);
		sm1.sendMessage(m1);

		DummyMessage1 m2 = new DummyMessage1(sm1, scheduler, date1);
		m2.messageToUnschedule = m1;
		sm1.sendMessage(m2);

		GAMA.getRuntimeScope().getClock().setStartingDate(date20);
		scheduler.execute(GAMA.getRuntimeScope());
		assert (date1.equals(scheduler.getLastEventTime()));
	}

}
