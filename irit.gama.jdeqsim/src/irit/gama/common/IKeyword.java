/*******************************************************************************************************
 *
 * IKeyword.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common;

/**
 * GAMA keywords JDEQSIM
 * 
 * @author Jean-François Erdelyi
 */
public interface IKeyword {
	public static final String JDQSIM_SIMUNIT = "jdeqsimunit";
	public static final String JDQSIM_SIMUNITTEST = "jdeqsimunittest";
	public static final String JDQSIM_ROAD = "jdeqsimroad";
	public static final String JDQSIM_VEHICLE = "jdeqsimvehicle";
	public static final String JDQSIM_PERSON = "jdeqsimperson";
	public static final String JDQSIM_SCHEDULER = "jdeqsimscheduler";

	public static final String SCHEDULER = "scheduler";
	public static final String FREESPEED = "freespeed";
	public static final String MAXSPEED = "maxspeed";
	public static final String SIZE = "size";
	public static final String CAPACITY = "capacity";
	public static final String FLOW_CAPACITY = "flow_capacity";
	public static final String MAX_CAPACITY = "max_capacity";
	public static final String PROMISE_CAPACITY = "promise_capacity";
	public static final String NO_LANES = "no_lanes";
	public static final String LENGTH = "length";
	public static final String SCHEDULING = "scheduling";
	public static final String OWNER = "owner";
	public static final String CURRENT_ROAD = "current_road";
	public static final String LEG_INDEX = "leg_index";
	public static final String LINK_INDEX = "link_index";
	public static final String CURRENT_ROUTE = "current_route";
	public static final String CORE_DEFINITION = "core_definition";

	public static final String ACTIVITY_DATE = "activity_date";
	public static final String ACTIVITY_DURATION = "activity_duration";
	public static final String ACTIVITY_ROAD = "activity_road";
	public static final String ACTIVITY_BUILDING = "activity_building";
	public static final String LEG = "leg";
	public static final String ADD_ACTIVITY = "add_activity";
	public static final String ADD_LEG = "add_leg";
}
