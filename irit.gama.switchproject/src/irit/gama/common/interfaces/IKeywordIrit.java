/*******************************************************************************************************
 *
 * IKeywordIrit.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common.interfaces;

/**
 * Keywords of IRIT
 * 
 * @author Jean-François Erdelyi
 */
public interface IKeywordIrit {
	/**
	 * Queue and stack keywords
	 */
	public static final String QUEUE = "queue";
	public static final String STACK = "stack";

	public static final String QUEUE_OPERATOR = "Queue-related operators";
	public static final String STACK_OPERATOR = "Stack-related operators";

	public static final int DEQUE_TYPE = 666;
	public static final int QUEUE_TYPE = 667;
	public static final int STACK_TYPE = 668;

	public static final String PUSH = "push";

	/**
	 * Scheduling keywords
	 */
	public static final String EVENT_MANAGER = "event_manager";

	public static final String EVENT = "event";
	public static final String EVENTS = "events";

	public static final String EVENT_DATE = "event_date";

	public static final String THE_ACTION = "the_action";
	public static final String WITH_ARGUMENTS = "with_arguments";
	public static final String AT = "at";
	public static final String REFER_TO = "refer_to";
	public static final String CALLER = "caller";

	public static final String SIZE_BY_AGENT = "size_by_agent";

	public static final String SCHEDULING = "scheduling";

	/**
	 * Logger keywords
	 */
	public static final String LOGGING = "logging";

	public static final String FLUSH = "flush";
	public static final String LOGBOOK = "logbook";
	public static final String LOG_DATA = "log_data";
	public static final String FILE_NAME = "file_name";
	public static final String SECTION = "section";
	public static final String ENTRY = "entry";
	public static final String VALUE = "value";
	public static final String DATE = "date";
	public static final String X = "x";
	public static final String Y = "y";

	/**
	 * IDM
	 */
	public static final String IDM = "idm";
	public static final String VEHICLE_LENGTH = "length";
	public static final String IDM_DESIRED_SPEED = "desired_speed";
	public static final String IDM_SPACING = "spacing";
	public static final String IDM_REACTION_TIME = "reaction_time";
	public static final String IDM_MAX_ACCELERATION = "max_acceleration";
	public static final String IDM_DESIRED_DECELERATION = "desired_deceleration";

	public static final String IDM_ACCELERATION = "acceleration";
	public static final String IDM_DELTA_SPEED = "delta_speed";
	public static final String IDM_ACTUAL_GAP = "actual_gap";
	public static final String IDM_DESIRED_MINIMUM_GAP = "desired_minimum_gap";

	/**
	 * Event Queue Road
	 */
	public static final String EVENT_QUEUE = "event_queue";
	public static final String BPR_ALPHA = "alpha";
	public static final String BPR_BETA = "beta";
	public static final String EVENT_QUEUE_VOLUME = "volume";
	public static final String EVENT_QUEUE_LENGTH = "length";

	/**
	 * Roads
	 */
	public static final String ROAD = "road";
	public static final String ROAD_NODE = "road_node";
	public static final String ROADS_IN = "roads_in";
	public static final String ROADS_OUT = "roads_out";
	public final static String AGENTS = "all_agents";

	public final static String ALL_ENTITIES = "all_entities";
	public final static String NODE_IN = "node_in";
	public final static String NODE_OUT = "node_out";
	public final static String MAXSPEED = "maxspeed";

}
