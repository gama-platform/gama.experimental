/*******************************************************************************************************
*
* TestMessageFactory.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package jdeqsim.irit.gama.test.jdeqsim;

import irit.gama.common.Param;
import irit.gama.core.scheduler.MessageFactory;
import irit.gama.core.scheduler.Scheduler;
import irit.gama.core.scheduler.message.DeadlockPreventionMessage;
import irit.gama.core.scheduler.message.EndLegMessage;
import irit.gama.core.scheduler.message.EndRoadMessage;
import irit.gama.core.scheduler.message.EnterRoadMessage;
import irit.gama.core.scheduler.message.LeaveRoadMessage;
import irit.gama.core.scheduler.message.StartingLegMessage;
import irit.gama.core.sim_unit.Person;
import irit.gama.core.sim_unit.Vehicle;

public class TestMessageFactory extends GenericTest {

	public static void test() {
		TestMessageFactory.testMessageFactory1();
		TestMessageFactory.testMessageFactory2();
		TestMessageFactory.testMessageFactory3();
		TestMessageFactory.testMessageFactory5();
		TestMessageFactory.testMessageFactory6();
	}

	// check if gc turned on
	public static void testMessageFactory1() {
		MessageFactory.GC_ALL_MESSAGES();
		Param.GC_MESSAGES = true;
		MessageFactory.disposeEndLegMessage(new EndLegMessage(null, null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null, null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null, null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null, null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null, null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null, null));

		assert (0 == MessageFactory.getEndLegMessageQueue().size());
		assert (0 == MessageFactory.getEnterRoadMessageQueue().size());
		assert (0 == MessageFactory.getStartingLegMessageQueue().size());
		assert (0 == MessageFactory.getLeaveRoadMessageQueue().size());
		assert (0 == MessageFactory.getEndRoadMessageQueue().size());
		assert (0 == MessageFactory.getEndLegMessageQueue().size());
	}

	// check when gc turned off
	public static void testMessageFactory2() {
		MessageFactory.GC_ALL_MESSAGES();
		Param.GC_MESSAGES = false;
		MessageFactory.disposeEndLegMessage(new EndLegMessage(null, null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null, null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null, null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null, null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null, null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null, null));

		assert (1 == MessageFactory.getEndLegMessageQueue().size());
		assert (1 == MessageFactory.getEnterRoadMessageQueue().size());
		assert (1 == MessageFactory.getStartingLegMessageQueue().size());
		assert (1 == MessageFactory.getLeaveRoadMessageQueue().size());
		assert (1 == MessageFactory.getEndRoadMessageQueue().size());
		assert (1 == MessageFactory.getEndLegMessageQueue().size());
	}

	// check check use of Message factory
	public static void testMessageFactory3() {
		MessageFactory.GC_ALL_MESSAGES();
		Param.GC_MESSAGES = false;
		MessageFactory.disposeEndLegMessage(new EndLegMessage(null, null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null, null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null, null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null, null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null, null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null, null));

		MessageFactory.getEndLegMessage(null, null);
		MessageFactory.getEnterRoadMessage(null, null);
		MessageFactory.getStartingLegMessage(null, null);
		MessageFactory.getLeaveRoadMessage(null, null);
		MessageFactory.getEndRoadMessage(null, null);
		MessageFactory.getDeadlockPreventionMessage(null, null);

		assert (0 == MessageFactory.getEndLegMessageQueue().size());
		assert (0 == MessageFactory.getEnterRoadMessageQueue().size());
		assert (0 == MessageFactory.getStartingLegMessageQueue().size());
		assert (0 == MessageFactory.getLeaveRoadMessageQueue().size());
		assert (0 == MessageFactory.getEndRoadMessageQueue().size());
		assert (0 == MessageFactory.getEndLegMessageQueue().size());
	}

	// check initialization using constructer
	public static void testMessageFactory5() {
		MessageFactory.GC_ALL_MESSAGES();
		Param.GC_MESSAGES = true;
		Scheduler scheduler = new Scheduler(DATE_0);
		Person person = new Person(null, scheduler);
		Vehicle vehicle = new Vehicle(null, scheduler, person);

		assert (MessageFactory.getEndLegMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getEnterRoadMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getStartingLegMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getLeaveRoadMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getEndRoadMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).scheduler == scheduler);

		assert (MessageFactory.getEndLegMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getEnterRoadMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getStartingLegMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getLeaveRoadMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getEndRoadMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).vehicle == vehicle);
	}

	// check initialization using rest
	public static void testMessageFactory6() {
		MessageFactory.GC_ALL_MESSAGES();
		Param.GC_MESSAGES = false;
		Scheduler scheduler = new Scheduler(DATE_0);
		Person person = new Person(null, scheduler);
		Vehicle vehicle = new Vehicle(null, scheduler, person);

		assert (MessageFactory.getEndLegMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getEnterRoadMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getStartingLegMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getLeaveRoadMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getEndRoadMessage(scheduler, vehicle).scheduler == scheduler);
		assert (MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).scheduler == scheduler);

		assert (MessageFactory.getEndLegMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getEnterRoadMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getStartingLegMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getLeaveRoadMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getEndRoadMessage(scheduler, vehicle).vehicle == vehicle);
		assert (MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).vehicle == vehicle);
	}

}
