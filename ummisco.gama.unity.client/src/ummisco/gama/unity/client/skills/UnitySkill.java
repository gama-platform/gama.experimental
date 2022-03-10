package ummisco.gama.unity.client.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import com.thoughtworks.xstream.XStream;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.network.skills.INetworkSkill;
import ummisco.gama.serializer.factory.StreamConverter;
import ummisco.gama.serializer.gamaType.converters.ConverterScope;
import ummisco.gama.unity.client.connector.UnityMQTTConnector;
import ummisco.gama.unity.client.data.Student;
import ummisco.gama.unity.client.data.type.rgbColor;
import ummisco.gama.unity.client.messages.ColorTopicMessage;
import ummisco.gama.unity.client.messages.CreateTopicMessage;
import ummisco.gama.unity.client.messages.DestroyTopicMessage;
import ummisco.gama.unity.client.messages.GetTopicMessage;
import ummisco.gama.unity.client.messages.ItemAttributes;
import ummisco.gama.unity.client.messages.MonoActionTopicMessage;
import ummisco.gama.unity.client.messages.MoveTopicMessage;
import ummisco.gama.unity.client.messages.NotificationTopicMessage;
import ummisco.gama.unity.client.messages.PluralActionTopicMessage;
import ummisco.gama.unity.client.messages.PositionTopicMessage;
import ummisco.gama.unity.client.messages.PropertyTopicMessage;
import ummisco.gama.unity.client.messages.ReplayMessage;
import ummisco.gama.unity.client.messages.SetTopicMessage;
import ummisco.gama.unity.client.messages.UIActionMessage;
import ummisco.gama.unity.client.mqtt.Utils;
import ummisco.gama.unity.client.wox.serial.WoxSerializer;

/**
 * UnitySkill : This class is intended to define the minimal set of behaviours
 * required from an agent that is able to communicate with unity angine in order
 * to visulaize GAMA simulations. Each member that has a meaning in GAML is
 * annotated with the respective tags (vars, getter, setter, init, action &
 * args)
 *
 */

@SuppressWarnings("unchecked")

@doc("The unity skill is intended to define the minimal set of behaviors required for agents that are able "
		+ "to communicate with the unity engine in order to visualize GAMA simulations in different terminals")
/*
 * @vars ({ @variable ( name = IUnitySkill.UNITY_LOCATION, type = IType.POINT,
 * doc = @doc ("Agent's location at unity scene")),
 * 
 * @variable ( name = IUnitySkill.UNITY_ROTATION, type = IType.POINT, doc = @doc
 * ("Agent rotation at unity scene")),
 * 
 * @variable ( name = IUnitySkill.UNITY_SCALE, type = IType.POINT, init =
 * "{0,0,0}", doc = @doc ("Agent's scale at unity scenet")),
 * 
 * @variable ( name = IUnitySkill.UNITY_SPEED, type = IType.FLOAT, init = "1.0",
 * doc = @doc ("Agent's speed at unity scene (in meter/second)")),
 * 
 * @variable ( name = IUnitySkill.UNITY_CREATED, type = IType.BOOL, init =
 * IKeyword.FALSE, doc = @doc
 * ("true if the agent is created into unity scene")),
 * 
 * @variable ( name = IUnitySkill.UNITY_ROTATE, type = IType.BOOL, init =
 * IKeyword.FALSE, doc = @doc ("true if agent's rotation is enabled")),
 * 
 * @variable ( name = IUnitySkill.UNITY_ROTATE, type = IType.BOOL, init =
 * IKeyword.FALSE, doc = @doc ("true if agent's rotation is enabled")), })
 */
@skill(name = IUnitySkill.SKILL_NAME, concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
//public class UnitySkill extends NetworkSkill implements IUnitySkill  {
public class UnitySkill extends Skill implements IUnitySkill {

	public static String allContent = "";
	final static String REGISTERED_AGENTS = "registred_agents";
	final static String REGISTRED_SERVER = "registred_servers";
	private final UnitySerializer unitySerializer = new UnitySerializer();

	static {
		DEBUG.ON();
	}

	public UnitySkill() {
		super();
	}

	public static final String BROKER_URL = "tcp://localhost:1883";
	// public static final String BROKER_URL = "tcp://195.221.248.15:1935";

	public static final String SERVER_URL = "195.221.248.15";
	public static final int SERVER_PORT = 1935;

	public static String DEFAULT_USER = "gama_demo";
	public static String DEFAULT_PASSWORD = "gama_demo";

	// public static final MqttConnectOptions options = new MqttConnectOptions();

	public UnityMQTTConnector connector = null;

	// @Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.getAgent();
		return " ";
	}

	@action(name = IUnitySkill.CONNECT, args = {
			@arg(name = IUnitySkill.WITHNAME, type = IType.STRING, optional = true, doc = @doc("ID of the agent (its name) for the simulation")),
			@arg(name = IUnitySkill.LOGIN, type = IType.STRING, optional = true, doc = @doc("Login")),
			@arg(name = IUnitySkill.PASSWORD, type = IType.STRING, optional = true, doc = @doc("password")),
			@arg(name = IUnitySkill.SERVER_URL, type = IType.STRING, optional = false, doc = @doc("server URL")),
			@arg(name = INetworkSkill.PORT, type = IType.INT, doc = @doc("Port number")), }, doc = @doc(value = "Generates a client ID and connects it to the Mqtt server.", returns = "The client generated identifier.", examples = {
					@example("") }))
	public void connectMqttClient(final IScope scope) {

		// -----------------

		final IAgent agt = scope.getAgent();

		final String serverURL = (String) scope.getArg(IUnitySkill.SERVER_URL, IType.STRING);
		final String login = (String) scope.getArg(IUnitySkill.LOGIN, IType.STRING);
		final String password = (String) scope.getArg(IUnitySkill.PASSWORD, IType.STRING);
		final Integer port = (Integer) scope.getArg(IUnitySkill.PORT, IType.INT);
		final String topicName = (String) scope.getArg(IUnitySkill.WITHNAME, IType.STRING);

		agt.setAttribute(IUnitySkill.SERVER_URL, serverURL);
		agt.setAttribute(IUnitySkill.LOGIN, login);
		agt.setAttribute(IUnitySkill.PASSWORD, password);
		agt.setAttribute(IUnitySkill.PORT, port);
		agt.setAttribute(IUnitySkill.WITHNAME, topicName);

		try {
			if (connector == null)
				connector = new UnityMQTTConnector(serverURL, Integer.toString(port), login, password);
		} catch (final MqttException e) {
			e.printStackTrace();
		}

		// -----------------

		/*
		 * final String clientId = Utils.getMacAddress() + "-" +
		 * scope.getAgent().getName() + "-pub";
		 * 
		 * DEBUG.LOG("The agent Name is  " + scope.getAgent().getName());
		 * 
		 * try { options.setCleanSession(true); client = new MqttClient(BROKER_URL,
		 * clientId); // options.setUserName(DEFAULT_USER); //
		 * options.setPassword(DEFAULT_PASSWORD.toCharArray());
		 * options.setCleanSession(true); client.connect(options); DEBUG.LOG("Client : "
		 * + clientId + " connected with success!"); } catch (final MqttException e) {
		 * e.printStackTrace(); System.exit(1); }
		 * scope.getSimulation().postDisposeAction(scope1 -> { try { if
		 * (client.isConnected()) { client.disconnect(); } } catch (final Exception e) {
		 * 
		 * e.printStackTrace(); } return null; });
		 * 
		 * return clientId;
		 */
	}

	@action(name = "send_unity_message", args = {
			@arg(name = "scene_manager", type = IType.STRING, optional = true, doc = @doc("The game object name")),
			@arg(name = IUnitySkill.CONTENT, type = IType.NONE, optional = false, doc = @doc("The emessage content")),
			@arg(name = IUnitySkill.TOPIC, type = IType.STRING, optional = true, doc = @doc("The emessage topict")) }, doc = @doc(value = "The generic form of a message to send to Unity engine. ", returns = "true if it is in the base.", examples = {
					@example("") }))
	public void sendUnityMqttMessage(final IScope scope) {

		final String sender = scope.getAgent().getName();
		final String objectName = scope.getArg("scene_manager", IType.NONE) != null
				? (String) scope.getArg("scene_manager", IType.NONE)
				: IUnitySkill.UNITY_SCENE_MANAGER;
		final String topic = scope.getArg(IUnitySkill.TOPIC, IType.STRING) != null
				? (String) scope.getArg(IUnitySkill.TOPIC, IType.STRING)
				: IUnitySkill.TOPIC_MAIN;
		final Object content = scope.getArg("content", IType.NONE);

		getConnector(scope);

		try {
			connector.sendMessage(scope, topic, sender, objectName, content);
		} catch (MqttException e) {
			e.printStackTrace();
		}

		// connector.sendMessage(scope.getAgent(),"littosim", topicMessage);
		// connector.send(scope.getAgent(), "littosim", topicMessage);
		// connector.send(scope.getAgent(), IUnitySkill.TOPIC_MAIN, topicMessage);
		// publishUnityMessage(scope, client, IUnitySkill.TOPIC_MAIN, topicMessage);

	}

	@action(name = "send_unity_message_wox", args = {
			@arg(name = "scene_manager", type = IType.STRING, optional = true, doc = @doc("The game object name")),
			@arg(name = IUnitySkill.CONTENT, type = IType.NONE, optional = false, doc = @doc("The emessage content")),
			@arg(name = IUnitySkill.TOPIC, type = IType.STRING, optional = true, doc = @doc("The emessage topict")) }, doc = @doc(value = "The generic form of a message to send to Unity engine. ", returns = "true if it is in the base.", examples = {
					@example("") }))
	public void sendUnityMqttWoxMessage(final IScope scope) {

		final String sender = scope.getAgent().getName();
		final String objectName = scope.getArg("scene_manager", IType.NONE) != null
				? (String) scope.getArg("scene_manager", IType.NONE)
				: IUnitySkill.UNITY_SCENE_MANAGER;
		final String topic = scope.getArg(IUnitySkill.TOPIC, IType.STRING) != null
				? (String) scope.getArg(IUnitySkill.TOPIC, IType.STRING)
				: IUnitySkill.TOPIC_MAIN;
		final Object content = scope.getArg("content", IType.NONE);

		getConnector(scope);

		try {
			connector.sendMessage(scope, topic, sender, objectName, content);
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}

	public void getConnector(final IScope scope) {
		/*
		 * if(connector == null) { final IAgent agt = scope.getAgent(); final String url
		 * = (String) agt.getAttribute(INetworkSkill.SERVER_URL); final Integer port =
		 * (Integer) agt.getAttribute(INetworkSkill.PORT);
		 * DEBUG.OUT("serverURL is "+url); DEBUG.OUT("port is "+port);
		 * DEBUG.OUT("createServerKey is "+createServerKey(url, port));
		 * 
		 * connector = (MQTTConnector)
		 * (this.getRegisteredServers(scope)).get(createServerKey(url, port));
		 * 
		 * }
		 */
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "getUnityField", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attribute", type = IType.STRING, optional = false, doc = @doc("The field name")), }, doc = @doc(value = "Get a unity game object field value", returns = "void", examples = {
					@example("") }))
	public void getUnityField(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final String attribute = (String) scope.getArg("attribute", IType.STRING);

		final GetTopicMessage topicMessage = new GetTopicMessage(scope, sender, receiver, objectName, attribute);

		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_GET, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityFields", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributes", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")) }, doc = @doc(value = "Set a set of fields of a unity game object.", returns = "void", examples = {
					@example("") }))
	public void setUnityField(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final Map<String, Object> attributes = (Map<String, Object>) scope.getArg("attributes", IType.MAP);

		final ArrayList<ItemAttributes> items = new ArrayList();
		for (final Map.Entry<?, ?> entry : attributes.entrySet()) {
			final ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}
		// TODO: change to support GamaMap in Unity side.
		final SetTopicMessage topicMessage = new SetTopicMessage(scope, sender, receiver, objectName, items);

		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_SET, topicMessage);

	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityProperty", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "propertyName", type = IType.STRING, optional = false, doc = @doc("The property name")),
			@arg(name = "propertyValue", type = IType.NONE, optional = false, doc = @doc("The property value")) }, doc = @doc(value = "Set a property value.", returns = "void", examples = {
					@example("") }))
	public void setUnityProperty(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final String propertyName = (String) scope.getArg("propertyName", IType.STRING);
		final Object propertyValue = scope.getArg("propertyValue", IType.NONE);

		final PropertyTopicMessage topicMessage = new PropertyTopicMessage(scope, sender, receiver, objectName,
				propertyName, propertyValue);

		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_PROPERTY, topicMessage);

	}

	@action(name = "callUnityMonoAction", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "actionName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attribute", type = IType.NONE, optional = false, doc = @doc("The attribute list and their values")) }, doc = @doc(value = "Call a unity game object method that has one parameter", returns = "void", examples = {
					@example("") }))
	public void callUnityMonoAction(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final String actionName = (String) scope.getArg("actionName", IType.STRING);
		final Object attribute = scope.getArg("attribute", IType.NONE);

		final MonoActionTopicMessage topicMessage = new MonoActionTopicMessage(scope, sender, receiver, objectName,
				actionName, attribute);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_MONO_FREE, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "callUnityPluralAction", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "actionName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributes", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")) }, doc = @doc(value = "Call a unity game object method that has several parameters.", returns = "void", examples = {
					@example("") }))
	public void callUnityPluralAction(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final String actionName = (String) scope.getArg("actionName", IType.STRING);
		final Map<String, String> attributes = (Map<String, String>) scope.getArg("attributes", IType.MAP);

		final ArrayList<ItemAttributes> items = new ArrayList();
		for (final Map.Entry<?, ?> entry : attributes.entrySet()) {
			final ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}
		final PluralActionTopicMessage topicMessage = new PluralActionTopicMessage(scope, sender, receiver, objectName,
				actionName, items);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_MULTIPLE_FREE, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityColor", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "color", type = IType.COLOR, optional = false, doc = @doc("The color name")), }, doc = @doc(value = "Set a unity game object color", returns = "void", examples = {
					@example("") }))
	public void setUnityColor(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);

		final GamaColor col = scope.hasArg("color") ? (GamaColor) scope.getArg("color", IType.COLOR)
				: new GamaColor(255, 0, 255);
		final rgbColor color = new rgbColor(col.getRed(), col.getGreen(), col.getBlue());

		final ColorTopicMessage topicMessage = new ColorTopicMessage(scope, sender, receiver, objectName,
				color.getRed(), color.getGreen(), color.getBlue());

		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_COLOR, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityPosition", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "position", type = IType.POINT, optional = false, doc = @doc("the new position to set for the object")),

	}, doc = @doc(value = "Set the position of a unity game object", returns = "void", examples = { @example("") }))
	public void setUnityPosition(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : null;
		final PositionTopicMessage topicMessage = new PositionTopicMessage(scope, sender, receiver, objectName,
				position);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_POSITION, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "unityMove", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "position", type = IType.POINT, optional = false, doc = @doc("The position values (x,y,z)")),
			@arg(name = "speed", type = IType.INT, optional = true, doc = @doc("speed")),
			@arg(name = "smoothMove", type = IType.BOOL, optional = true, doc = @doc("If true, the move will be towards the target position, but with adding force (according to the specified speed). "
					+ "So, the object may not stop at the destination position."
					+ " If false, the object will stop moving when the target position is reached.")), }, doc = @doc(value = "Set the position of a unity game object", returns = "void", examples = {
							@example("") }))
	public synchronized void unityMove(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final boolean smoothMove = scope.hasArg("smoothMove") ? (boolean) scope.getArg("smoothMove", IType.BOOL) : true;
		final double speed = scope.hasArg("speed") ? (double) scope.getArg("speed", IType.FLOAT)
				: IUnitySkill.DAFAULT_SPEED;
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : null;

		final MoveTopicMessage topicMessage = new MoveTopicMessage(scope, sender, receiver, objectName, position, speed,
				smoothMove);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_MOVE, topicMessage);

		// DEBUG.LOG("New message sent to Unity. Topic: " + IUnitySkill.TOPIC_MOVE + "
		// Number: " + serializeMessage(scope, topicMessage));
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "newUnityObject", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "type", type = IType.STRING, optional = false, doc = @doc("The object type")),
			@arg(name = "color", type = IType.COLOR, optional = true, doc = @doc("The object color")),
			@arg(name = "position", type = IType.POINT, optional = true, doc = @doc("The object position")), }, doc = @doc(value = "Create a new unity game object on the scene and set its initial color and position. Supported fomes are: Capsule, Cube, Cylinder and Sphere", returns = "void", examples = {
					@example("") }))
	public synchronized void newUnityObject(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final String type = (String) scope.getArg("type", IType.STRING);

		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT)
				: new GamaPoint(0, 0, 0);
		final GamaColor col = scope.hasArg("color") ? (GamaColor) scope.getArg("color", IType.COLOR)
				: new GamaColor(255, 0, 255);
		final rgbColor color = new rgbColor(col.getRed(), col.getGreen(), col.getBlue());

		DEBUG.LOG(" -----------> " + col.stringValue(scope));

		final CreateTopicMessage topicMessage = new CreateTopicMessage(scope, sender, receiver, objectName, type, color,
				position);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_CREATE_OBJECT, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "destroyUnityObject", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")) }, doc = @doc(value = "Destroy a unity game object", returns = "void", examples = {
					@example("") }))
	public synchronized void destroyUnityObject(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);

		final DestroyTopicMessage topicMessage = new DestroyTopicMessage(scope, sender, receiver, objectName);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_DESTROY_OBJECT, topicMessage);
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "unityNotificationSubscribe", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "notificationId", type = IType.STRING, optional = false, doc = @doc("notificationId: the notification ID to communicate when notifying an agent by unity")),
			@arg(name = "fieldType", type = IType.STRING, optional = false, doc = @doc("fieldType: whether it is a field or a property in the game object")),
			@arg(name = "fieldName", type = IType.STRING, optional = false, doc = @doc("fieldName: The field name")),
			@arg(name = "fieldValue", type = IType.NONE, optional = false, doc = @doc("fieldValue: The field value")),
			@arg(name = "fieldOperator", type = IType.STRING, optional = false, doc = @doc("fieldOperator: The comparaison operator")), }, doc = @doc(value = "Subscribe to the notification mechanism, allowing unity to notify Gama when the condition on the specified field has been met.", returns = "void", examples = {
					@example("") }))
	public synchronized void unityNotificationSubscribe(final IScope scope) {
		final String sender = scope.getAgent().getName();
		final String notificationId = (String) scope.getArg("notificationId", IType.STRING);
		final String receiver = (String) scope.getArg("objectName", IType.STRING);
		final String objectName = (String) scope.getArg("objectName", IType.STRING);
		final String fieldType = (String) scope.getArg("fieldType", IType.STRING);
		final String fieldName = (String) scope.getArg("fieldName", IType.STRING);
		final Object fieldValue = scope.getArg("fieldValue", IType.NONE);
		final String fieldOperator = (String) scope.getArg("fieldOperator", IType.STRING);

		final NotificationTopicMessage topicMessage = new NotificationTopicMessage(scope, sender, receiver,
				notificationId, objectName, fieldType, fieldName, fieldValue, fieldOperator);
		publishUnityMessage(scope, connector.getClient(), IUnitySkill.TOPIC_POSITION, topicMessage);
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "disconnectMqttClient", doc = @doc(value = "Disconnect the client from the Mqtt server.", returns = "true if it is in the base.", examples = {
			@example("") }))
	public String disconnectMqttClient(final IScope scope) {
		final String clientId = Utils.getMacAddress() + "-" + scope.getAgent().getName() + "-pub";
		try {

			if (connector.getClient().isConnected()) {
				connector.getClient().disconnect();
			}
			DEBUG.LOG("Client : " + clientId + " disconnected with success!");
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return clientId;
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "subscribe_to_topic", args = {
			@arg(name = "topic", type = IType.STRING, optional = false, doc = @doc("Topic Name")) }, doc = @doc(value = "Subscribe a client to a topic", returns = "true if success, false otherwise", examples = {
					@example("") }))
	public String SubscribeToTopic(final IScope scope) {
		Utils.getMacAddress();
		scope.getAgent().getName();
		final String topic = (String) scope.getArg("topic", IType.STRING);
		try {
			connector.subscribeToGroup(topic);
			DEBUG.LOG(scope.getAgent().getName() + " is now listening to " + topic);
		} catch (final MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return "Subscribed to the topic: " + topic;
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "get_unity_message", doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public GamaMap<String, String> getUnityMessage(final IScope scope) {
		String msg = connector.getNextMessage();
		//msg = "<object type=\"MaterialUI.UIActionMessage\" dotnettype=\"MaterialUI.UIActionMessage, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"0\"><field name=\"topic\" type=\"string\" value=\"UITopic\" /><field name=\"messageTime\" type=\"long\" value=\"1646750887\" /><field name=\"elementId\" type=\"string\" value=\"button1\" /><field name=\"actionCode\"><object type=\"int\" value=\"11\" id=\"1\" /></field><field name=\"content\" type=\"string\" value=\" \" /></object>";
		HashMap<String, String> mappingDic = new HashMap<String, String>();
		mappingDic.put("\"MaterialUI.UIActionMessage\"", "\"ummisco.gama.unity.client.messages.UIActionMessage\"");
		
		Object obj = WoxSerializer.deserializeFromString(msg, mappingDic);
		UIActionMessage UIMsg = (UIActionMessage) obj;
		
		System.out.println(" Class name : " + UIActionMessage.class.getName());
		
		System.out.println(" topic --> " + UIMsg.topic);
		System.out.println(" messageTime --> " + UIMsg.messageTime);
		System.out.println(" messageNumber --> " + UIMsg.messageNumber);
		System.out.println(" elementId --> " + UIMsg.elementId);
		System.out.println(" actionCode --> " + UIMsg.actionCode);
		System.out.println(" content --> " + UIMsg.content);
				
		GamaMap<String, String> MapMsg = UIMsg.ToHashMap();
		
		return MapMsg;
	}

	// TODO
	@action(name = "clear_topic", args = {
			@arg(name = "topic", type = IType.STRING, optional = false, doc = @doc("Topic Name")) }, doc = @doc(value = "Clear the topic messages", returns = "nothing.", examples = {
					@example("") }))
	public synchronized void clearTopic(final IScope scope) {
		final String topic = (String) scope.getArg("topic", IType.STRING);
		try {
			connector.clearAllMessages(topic);
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}

	// TODO
	@action(name = "clear_messages", doc = @doc(value = "Clear the topic messages", returns = "nothing.", examples = {
			@example("") }))
	public synchronized void clearMessages(final IScope scope) {
		try {
			connector.clearAllMessages();
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}

	// TODO: Review this action with better description and genericity support.
	// Action should return a pair "key"::value
	@action(name = "get_unity_filtered", args = {
			@arg(name = "topic", type = IType.STRING, optional = false) }, doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
					@example("") }))
	public synchronized String getTopicNextMessage(final IScope scope) {
		final String topic = (String) scope.getArg("topic", IType.STRING);
		final String message = connector.getNextMessage(topic);
		if (message != null) {
			final ConverterScope cScope = new ConverterScope(scope);
			final XStream xstream = StreamConverter.loadAndBuild(cScope);
			final ReplayMessage notifMsg = (ReplayMessage) xstream.fromXML(message);
			return notifMsg.fieldValue;
		} else {
			return "null";
		}
	}

	@action(name = "has_next_message_topic", args = {
			@arg(name = "topic", type = IType.STRING, optional = false) }, doc = {
					@doc("Check if there are more messages on a specific topic") })
	public synchronized boolean hasTopicNextMessage(final IScope scope) {
		final String topic = (String) scope.getArg("topic", IType.STRING);
		boolean result = false;
		result = connector.hasNextMessage(topic);
		return result;
	}

	@action(name = "has_next_message", doc = { @doc("Check if there are more messages on a specific topic") })
	public synchronized boolean hasNextMessage(final IScope scope) {
		boolean result = false;
		result = connector.hasNextMessage();
		return result;
	}

	public static XStream getXStream(final IScope scope) {
		final ConverterScope cScope = new ConverterScope(scope);
		final XStream xstream = StreamConverter.loadAndBuild(cScope);
		return xstream;
	}

	public synchronized void publishUnityMessage(final IScope scope, final MqttClient client, final String topic,
			final Object message) {
		final String messageString = serializeMessage(scope, message);
		// messageString.replace("class=", "xsi:type=");
		DEBUG.OUT("The message with replace is : \n " + messageString);
		// DEBUG.OUT("The shape to send is \n "+messageString);
		try {
			final MqttTopic unityTopic = client.getTopic(topic);
			final MqttMessage mqttMessage = new MqttMessage();
			mqttMessage.setPayload(messageString.getBytes());
			unityTopic.publish(mqttMessage);
		} catch (final MqttPersistenceException e) {
			e.printStackTrace();
		} catch (final MqttException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public String serializeMessage(final IScope scope, final Object message) {
		final UnitySerializer unitySerializer = new UnitySerializer();
		unitySerializer.SetSerializer(getXStream(scope));
		return unitySerializer.agentShapeToXML(message);
	}

}