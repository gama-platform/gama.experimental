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
	
	public final static String CONNECT = "connect_unity";
	public final static String SERVER_URL = "to";
	public final static String LOGIN = "login";
	public final static String PASSWORD = "password";
	public final static String WITHNAME = "with_name";
	public final static String PORT = "port";
	
	public final static String TOPIC = "topic";
	public static final String CONTENT = "content";

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

	// Messages fields.
	public static final String MSG_UNREAD = "unread";
	public static final String MSG_SENDER = "sender";
	public static final String MSG_RECEIVERS = "receivers";
	public static final String MSG_CONTENTS = "contents";
	public static final String MSG_FIELD_TYPE = "fieldType";
	public static final String MSG_FIELD_NAME = "fieldName";
	public static final String MSG_FIELD_VALUE = "fieldValue";
	public static final String MSG_FIELD_OPERATOR = "fieldOperator";
	public static final String MSG_EMISSION_TIMESTAMP = "emissionTimeStamp";
	public static final String MSG_RECEPTION_TIMESTAMP = "receptionTimeStamp";

	public static final String MSG_OBJECT_NAME = "objectName";
	public static final String MSG_ATTRIBUTES = "attributes";
	public static final String MSG_ATTRIBUTE = "attribute";
	public static final String MSG_PROPERTY_NAME = "propertyName";
	public static final String MSG_VALUE_TYPE = "valueType";
	public static final String MSG_VALUE = "value";
	public static final String MSG_POSITION = "position";
	public static final String MSG_COLOR = "color";
	public static final String MSG_METHODE_NAME = "methodeName";
	
	public static final String MSG_NOTIFICATION_ID = "notificationId";
	public static final String MSG_SPEED = "speed";
	public static final String MSG_SMOOTH_MOVE = "smoothMove";
	public static final String MSG_X = "x";
	public static final String MSG_Y = "y";
	public static final String MSG_Z = "z";
	
	public static final String MSG_RED = "red";
	public static final String MSG_GREEN = "green";
	public static final String MSG_BLUE = "blue";
	
	public static final String MSG_UNITY_ACTION = "unityAction";
	public static final String MSG_UNITY_OBJECT = "unityObject";
	public static final String MSG_UNITY_ATTRIBUTE = "unityAttribute";
	public static final String MSG_UNITY_TOPIC = "unityTopic";
	//public static final String MSG_SMOOTH_MOVE = "smoothMove";
	//public static final String MSG_SMOOTH_MOVE = "smoothMove";
	//public static final String MSG_SMOOTH_MOVE = "smoothMove";
	
	
	// Littosim fields
	public static final String MSG_TYPE = "type";
	public static final String MSG_NAME = "name";

	// Classes names
	public static final String CLASS_REPLAY_MESSAGE = "ummisco.gama.unity.messages.ReplayMessage";
	public static final String CLASS_NOTIFICATION_MESSAGE = "ummisco.gama.unity.messages.NotificationMessage";


	public static final double DAFAULT_SPEED = 1.0;
	
	
	// Unity managers
	public static final String UNITY_SCENE_MANAGER = "GamaManager";
}
