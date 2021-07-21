/*******************************************************************************************************
 *
 * Param.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common;

public class Param {

	/**
	 * Config parameters
	 */

	// Flow capacity factor
	public static double FLOW_CAPACITY_FACTOR = 1.0;

	// Storage capacity factor
	public static double STORAGE_CAPACITY_FACTOR = 1.0;

	// In [veh/h] per lane, can be scaled with flow capacity factor
	public static double MINIMUM_IN_FLOW_CAPACITY = 1800.0;

	// In m
	public static double CAR_SIZE = 7.5;

	// In m/s
	public static double GAP_TRAVEL_SPEED = 15.0;

	// stuckTime is used for deadlock prevention. when a car waits for more than
	// 'stuckTime' for entering next road, it will enter the next.
	// In s
	public static double SQUEEZE_TIME = 1800.0;

	/**
	 * Other parameters
	 */

	// in seconds
	public static double CAPACITY_PERIOD = 3600.0;

	// Garbage collector ON/OFF for messages
	public static boolean GC_MESSAGES = false;
}
