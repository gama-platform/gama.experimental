/*********************************************************************************************
 *
 * 'INetworkSkill.java, in plugin ummisco.gama.network, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2018 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/

package ummisco.gama.unity.skills;

public interface IUnitySkill {

	public static final String SKILL_NAME = "unity";

	// Topics hierarchy
	public final static String TOPIC_ASK = "ask";
	public final static String TOPIC_SET = "set";
	public final static String TOPIC_GET = "get";

	// publish topics
	// subscribe topics
	public static final String TOPIC_MAIN = "Unity";
	public static final String TOPIC_MONO_FREE = "monoFree";
	public static final String TOPIC_MULTIPLE_FREE = "multipleFree";
	public static final String TOPIC_POSITION = "position";
	public static final String TOPIC_PROPERTY = "property";

	public static final String TOPIC_MOVE = "move";
	public static final String TOPIC_COLOR = "color";
	public final static String TOPIC_REPLAY = "replay";
	public final static String TOPIC_NOTIFICATION = "subscribeToNotification";

	public final static String TOPIC_NOTIFICATION_RECEIVED = "notification";
	public final static String TOPIC_LITTOSIM = "littosim";

	// scene manipulation topics
	public final static String TOPIC_CREATE_OBJECT = "create";
	public final static String TOPIC_DESTROY_OBJECT = "destroy";

	public static final String TOPIC_GAMA = "Gama";

	// Agent Properties
	public static final String UNITY_LOCATION = "unity_location";
	public static final String UNITY_ROTATION = "unity_rotation";
	public static final String UNITY_ROTATE = "unity_rotate";
	public static final String UNITY_SCALE = "unity_scale";
	public static final String UNITY_SPEED = "unity_speed";
	public static final String UNITY_CREATED = "unity_created";

	public static final double DAFAULT_SPEED = 1.0;
}
