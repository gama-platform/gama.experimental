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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import irit.gama.common.Param;
import irit.gama.common.logger.Logger;
import irit.gama.core.plan.Activity;
import irit.gama.core.plan.Leg;
import irit.gama.core.unit.Person;
import irit.gama.core.unit.Road;
import irit.gama.core.unit.Scheduler;
import irit.gama.core.unit.Vehicle;
import msi.gama.runtime.GAMA;
import msi.gama.util.GamaDate;

// Full coverage except one thing: 
// in road, enterRequest -> if the road has been full recently then find out, when the next gap arrives   
public class TestMessages extends GenericTest {

	// Start all tests
	public static void test() {
		Logger.flush();
		TestMessages.testOneCar();
		TestMessages.testTwoCars();
		TestMessages.testTwoLegs();
		TestMessages.testFullRoad();
		TestMessages.testMultiRoad();
	}

	// Test with just one car (majority of messages are used)
	public static void testOneCar() {
		GamaDate date500 = new GamaDate(GAMA.getRuntimeScope(), 500);
		GamaDate date1000 = new GamaDate(GAMA.getRuntimeScope(), 1000);
		GamaDate date10000 = new GamaDate(GAMA.getRuntimeScope(), 10000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);

		ArrayList<Road> roads = new ArrayList<>();
		Road r1 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		Road r2 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		Road r3 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		roads.add(r2);

		Person p = new Person();
		p.addActivity(new Activity(date500, 0, r1, null));
		p.addLeg(new Leg(roads));
		p.addActivity(new Activity(date1000, 0, r3, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p);

		GAMA.getRuntimeScope().getClock().setStartingDate(date10000);
		scheduler.execute(GAMA.getRuntimeScope());

		double expectedTime = (20000.0 / 27.78) * 1000.0;
		GamaDate expectedDate = date500.plus(expectedTime, ChronoUnit.MILLIS);
		GamaDate lastEvent = scheduler.getLastEventTime();

		// The last event time should be equals 20km of road at 100km/h
		assert (expectedDate.equals(lastEvent));
	}

	// Test with two cars (for some case where the road is not completely empty)
	public static void testTwoCars() {
		GamaDate date499 = new GamaDate(GAMA.getRuntimeScope(), 499);
		GamaDate date500 = new GamaDate(GAMA.getRuntimeScope(), 500);
		GamaDate date999 = new GamaDate(GAMA.getRuntimeScope(), 999);
		GamaDate date1000 = new GamaDate(GAMA.getRuntimeScope(), 1000);
		GamaDate date10000 = new GamaDate(GAMA.getRuntimeScope(), 10000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);

		ArrayList<Road> roads = new ArrayList<>();
		Road r1 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		Road r2 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		// roads.add(r2);

		Person p1 = new Person();
		p1.addActivity(new Activity(date499, 0, r1, null));
		p1.addLeg(new Leg(roads));
		p1.addActivity(new Activity(date999, 0, r2, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p1);

		Person p2 = new Person();
		p2.addActivity(new Activity(date500, 0, r1, null));
		p2.addLeg(new Leg(roads));
		p2.addActivity(new Activity(date1000, 0, r2, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p2);

		GAMA.getRuntimeScope().getClock().setStartingDate(date10000);
		scheduler.execute(GAMA.getRuntimeScope());

		double expectedTime = (10000.0 / 27.78) * 1000.0;
		GamaDate expectedDate = date500.plus(expectedTime, ChronoUnit.MILLIS);
		GamaDate lastEvent = scheduler.getLastEventTime();

		Logger.print();
		Logger.printByVehicle();
		Logger.flush();
		Param.DEBUG_ON = false;

		// The last event time should be equals 20km of road at 100km/h
		assert (expectedDate.equals(lastEvent));
	}

	// Test one car with two legs (to check end leg message with another leg)
	public static void testTwoLegs() {
		Param.DEBUG_ON = true;
		Param.DEBUG_LEVEL = Param.LogLevel.scheduleOnly;

		GamaDate date500 = new GamaDate(GAMA.getRuntimeScope(), 500);
		GamaDate date1500 = new GamaDate(GAMA.getRuntimeScope(), 1500);
		GamaDate date2500 = new GamaDate(GAMA.getRuntimeScope(), 2500);
		GamaDate date10000 = new GamaDate(GAMA.getRuntimeScope(), 10000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);

		ArrayList<Road> roads1 = new ArrayList<>();
		ArrayList<Road> roads2 = new ArrayList<>();
		Road r1 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		Road r2 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 10000.0);
		Road r3 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 5000.0);
		Road r4 = new Road(GAMA.getRuntimeScope(), null, scheduler, 27.78, 36000, 1, 1000.0);
		// roads1.add(r2);
		roads2.add(r3);
		// roads2.add(r4);

		Person p = new Person();
		p.addActivity(new Activity(date500, 0, r1, null));
		p.addLeg(new Leg(roads1));
		p.addActivity(new Activity(date1500, 0, r2, null));
		p.addLeg(new Leg(roads2));
		p.addActivity(new Activity(date2500, 0, r4, null));

		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p);

		GAMA.getRuntimeScope().getClock().setStartingDate(date10000);
		scheduler.execute(GAMA.getRuntimeScope());

		double expectedTime = (15000.0 / 27.78) * 1000.0;
		GamaDate expectedDate = date1500.plus(expectedTime, ChronoUnit.MILLIS);
		GamaDate lastEvent = scheduler.getLastEventTime();
		Logger.print();

		// The last event time should be equals 15km of road at 100km/h (R2 + R3)
		assert (expectedDate.equals(lastEvent));
	}

	// Check the behavior if road is full (dead lock message)
	public static void testFullRoad() {
		GamaDate date500 = new GamaDate(GAMA.getRuntimeScope(), 500);
		GamaDate date1000 = new GamaDate(GAMA.getRuntimeScope(), 1000);
		GamaDate date1000000 = new GamaDate(GAMA.getRuntimeScope(), 1000000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);

		ArrayList<Road> roads = new ArrayList<>();
		Road r1 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 20.0);
		Road r2 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 10000.0);
		Road r3 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 10.0);
		Road r4 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 10000000.0);
		Road r5 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 10000.0);
		r4.setMaxNumberOfCarsOnRoad(1);
		roads.add(r2);
		roads.add(r3);
		roads.add(r4);
		roads.add(r5);

		Person p1 = new Person();
		p1.addActivity(new Activity(date500, 0, r1, null));
		p1.addLeg(new Leg(roads));
		p1.addActivity(new Activity(date1000, 0, r5, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p1);

		Person p2 = new Person();
		p2.addActivity(new Activity(date500, 0, r1, null));
		p2.addLeg(new Leg(roads));
		p2.addActivity(new Activity(date1000, 0, r5, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p2);

		Person p3 = new Person();
		p3.addActivity(new Activity(date500, 0, r1, null));
		p3.addLeg(new Leg(roads));
		p3.addActivity(new Activity(date1000, 0, r5, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p3);

		GAMA.getRuntimeScope().getClock().setStartingDate(date1000000);
		scheduler.execute(GAMA.getRuntimeScope());

		Logger.print();
		Logger.printByVehicle();
	}

	// Check the behavior if road is full (dead lock message)
	public static void testMultiRoad() {
		GamaDate date500 = new GamaDate(GAMA.getRuntimeScope(), 500);
		GamaDate date501 = new GamaDate(GAMA.getRuntimeScope(), 501);
		GamaDate date502 = new GamaDate(GAMA.getRuntimeScope(), 502);
		GamaDate date1000 = new GamaDate(GAMA.getRuntimeScope(), 1000);
		GamaDate date1000000 = new GamaDate(GAMA.getRuntimeScope(), 1000000);
		GAMA.getRuntimeScope().getClock().setStartingDate(DATE_0);
		Scheduler scheduler = new Scheduler(GAMA.getRuntimeScope(), null);

		ArrayList<Road> roads = new ArrayList<>();
		Road r10 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 21.0);
		Road r11 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 15.0);
		Road r2 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 15.0);
		Road r3 = new Road(GAMA.getRuntimeScope(), null, scheduler, 10.00, 36000, 1, 10000.0);
		roads.add(r2);
		roads.add(r3);

		Person p1 = new Person();
		p1.addActivity(new Activity(date500, 0, r10, null));
		p1.addLeg(new Leg(roads));
		p1.addActivity(new Activity(date1000, 0, r3, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p1);

		Person p11 = new Person();
		p11.addActivity(new Activity(date500, 0, r10, null));
		p11.addLeg(new Leg(roads));
		p11.addActivity(new Activity(date1000, 0, r3, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p11);

		Person p2 = new Person();
		p2.addActivity(new Activity(date501, 0, r10, null));
		p2.addLeg(new Leg(roads));
		p2.addActivity(new Activity(date1000, 0, r3, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p2);

		Person p3 = new Person();
		p3.addActivity(new Activity(date502, 0, r10, null));
		p3.addLeg(new Leg(roads));
		p3.addActivity(new Activity(date1000, 0, r3, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p3);

		Person p4 = new Person();
		p4.addActivity(new Activity(date502, 0, r11, null));
		p4.addLeg(new Leg(roads));
		p4.addActivity(new Activity(date1000, 0, r3, null));
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p4);

		GAMA.getRuntimeScope().getClock().setStartingDate(date1000000);
		scheduler.execute(GAMA.getRuntimeScope());

		Logger.print();
		Logger.printByVehicle();
	}

}
