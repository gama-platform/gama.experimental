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
import irit.gama.core.message.MessageFactory;
import irit.gama.core.message.def.DeadlockPreventionMessage;
import irit.gama.core.message.def.EndLegMessage;
import irit.gama.core.message.def.EndRoadMessage;
import irit.gama.core.message.def.EnterRoadMessage;
import irit.gama.core.message.def.LeaveRoadMessage;
import irit.gama.core.message.def.StartingLegMessage;
import irit.gama.core.unit.Person;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.runtime.GAMA;

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

		MessageFactory.disposeEndLegMessage(new EndLegMessage(null, null, null, null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null, null, null, null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null, null, null, null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null, null, null, null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null, null, null, null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null, null, null, null));

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

		MessageFactory.disposeEndLegMessage(new EndLegMessage(null, null, null, null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null, null, null, null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null, null, null, null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null, null, null, null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null, null, null, null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null, null, null, null));

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

		MessageFactory.disposeEndLegMessage(new EndLegMessage(null, null, null, null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null, null, null, null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null, null, null, null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null, null, null, null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null, null, null, null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null, null, null, null));

		MessageFactory.getEndLegMessage(null, null, null, null, null);
		MessageFactory.getEnterRoadMessage(null, null, null, null, null);
		MessageFactory.getStartingLegMessage(null, null, null, null, null);
		MessageFactory.getLeaveRoadMessage(null, null, null, null, null);
		MessageFactory.getEndRoadMessage(null, null, null, null, null);
		MessageFactory.getDeadlockPreventionMessage(null, null, null, null, null);

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
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null, DATE_0);
		Person person = new Person();
		Vehicle vehicle = new Vehicle(null, null, scheduler, person);

		assert (MessageFactory.getEndLegMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getEnterRoadMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getStartingLegMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getLeaveRoadMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getEndRoadMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getDeadlockPreventionMessage(null, null, scheduler, vehicle, null)
				.getScheduler() == scheduler);

		assert (MessageFactory.getEndLegMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getEnterRoadMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getStartingLegMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getLeaveRoadMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getEndRoadMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getDeadlockPreventionMessage(null, null, scheduler, vehicle, null)
				.getVehicle() == vehicle);
	}

	// check initialization using rest
	public static void testMessageFactory6() {
		MessageFactory.GC_ALL_MESSAGES();
		Param.GC_MESSAGES = false;
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null, DATE_0);
		Person person = new Person();
		Vehicle vehicle = new Vehicle(null, null, scheduler, person);

		assert (MessageFactory.getEndLegMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getEnterRoadMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getStartingLegMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getLeaveRoadMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getEndRoadMessage(null, null, scheduler, vehicle, null).getScheduler() == scheduler);
		assert (MessageFactory.getDeadlockPreventionMessage(null, null, scheduler, vehicle, null)
				.getScheduler() == scheduler);

		assert (MessageFactory.getEndLegMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getEnterRoadMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getStartingLegMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getLeaveRoadMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getEndRoadMessage(null, null, scheduler, vehicle, null).getVehicle() == vehicle);
		assert (MessageFactory.getDeadlockPreventionMessage(null, null, scheduler, vehicle, null)
				.getVehicle() == vehicle);
	}

}
