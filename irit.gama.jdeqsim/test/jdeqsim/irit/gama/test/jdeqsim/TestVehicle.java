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
public class TestVehicle extends GenericTest {

	// Start all tests
	public static void test() {
		Logger.flush();
		TestVehicle.testMaxSpeed();
	}

	// Test if the max speed is well setted
	public static void testMaxSpeed() {
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
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p, 10.00, 7.0);

		GAMA.getRuntimeScope().getClock().setStartingDate(date10000);
		scheduler.execute(GAMA.getRuntimeScope());

		double expectedTime = (20000.0 / 10.00) * 1000.0;
		GamaDate expectedDate = date500.plus(expectedTime, ChronoUnit.MILLIS);
		GamaDate lastEvent = scheduler.getLastEventTime();

		// The last event time should be equals 20km of road at 100km/h
		assert (expectedDate.equals(lastEvent));
	}

	// Test if the max speed is well setted
	public static void testCapacity() {
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
		new Vehicle(GAMA.getRuntimeScope(), null, scheduler, p, 10.00, 7.0);

		GAMA.getRuntimeScope().getClock().setStartingDate(date10000);
		scheduler.execute(GAMA.getRuntimeScope());

		double expectedTime = (20000.0 / 10.00) * 1000.0;
		GamaDate expectedDate = date500.plus(expectedTime, ChronoUnit.MILLIS);
		GamaDate lastEvent = scheduler.getLastEventTime();

		// The last event time should be equals 20km of road at 100km/h
		assert (expectedDate.equals(lastEvent));
	}

}
