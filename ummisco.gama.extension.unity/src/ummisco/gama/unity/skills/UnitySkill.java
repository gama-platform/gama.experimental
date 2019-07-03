package ummisco.gama.unity.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import com.thoughtworks.xstream.XStream;
import com.vividsolutions.jts.geom.Polygon;

import msi.gama.common.geometry.GamaGeometryFactory;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.agent.MinimalAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.network.common.IConnector;
import ummisco.gama.network.mqtt.MQTTConnector;
import ummisco.gama.network.skills.INetworkSkill;
import ummisco.gama.network.skills.NetworkSkill;
import ummisco.gama.serializer.factory.StreamConverter;
import ummisco.gama.serializer.gamaType.converters.ConverterScope;
import ummisco.gama.unity.data.type.rgbColor;
import ummisco.gama.unity.messages.ColorTopicMessage;
import ummisco.gama.unity.messages.CreateTopicMessage;
import ummisco.gama.unity.messages.DestroyTopicMessage;
import ummisco.gama.unity.messages.GetTopicMessage;
import ummisco.gama.unity.messages.ItemAttributes;
import ummisco.gama.unity.messages.MonoActionTopicMessage;
import ummisco.gama.unity.messages.MoveTopicMessage;
import ummisco.gama.unity.messages.NotificationMessage;
import ummisco.gama.unity.messages.NotificationTopicMessage;
import ummisco.gama.unity.messages.PluralActionTopicMessage;
import ummisco.gama.unity.messages.PositionTopicMessage;
import ummisco.gama.unity.messages.PropertyTopicMessage;
import ummisco.gama.unity.messages.ReplayMessage;
import ummisco.gama.unity.messages.SetTopicMessage;
import ummisco.gama.unity.messages.littosimMessage;
import ummisco.gama.unity.mqtt.SubscribeCallback;
import ummisco.gama.unity.mqtt.Utils;

/**
 * UnitySkill : This class is intended to define the minimal set of behaviours
 * required from an agent that is able to communicate with unity angine in order
 * to visulaize GAMA simulations. Each member that has a meaning in GAML is
 * annotated with the respective tags (vars, getter, setter, init, action &
 * args)
 *
 */

@doc("The unity skill is intended to define the minimal set of behaviors required for agents that are able "
		+ "to communicate with the unity engine in order to visualize GAMA simulations in different terminals")
@vars({ @variable(name = IUnitySkill.UNITY_LOCATION, type = IType.POINT, doc = @doc("Agent's location at unity scene")),
		@variable(name = IUnitySkill.UNITY_ROTATION, type = IType.POINT, doc = @doc("Agent rotation at unity scene")),
		@variable(name = IUnitySkill.UNITY_SCALE, type = IType.POINT, init = "{0,0,0}", doc = @doc("Agent's scale at unity scenet")),
		@variable(name = IUnitySkill.UNITY_SPEED, type = IType.FLOAT, init = "1.0", doc = @doc("Agent's speed at unity scene (in meter/second)")),
		@variable(name = IUnitySkill.UNITY_CREATED, type = IType.BOOL, init = IKeyword.FALSE, doc = @doc("true if the agent is created into unity scene")),
		@variable(name = IUnitySkill.UNITY_ROTATE, type = IType.BOOL, init = IKeyword.FALSE, doc = @doc("true if agent's rotation is enabled")), 
		@variable(name = IUnitySkill.UNITY_ROTATE, type = IType.BOOL, init = IKeyword.FALSE, doc = @doc("true if agent's rotation is enabled")), 
		})
@skill(name = IUnitySkill.SKILL_NAME, concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
public class UnitySkill extends NetworkSkill {

	public static String allContent = "";
	final static String REGISTERED_AGENTS = "registred_agents";
	final static String REGISTRED_SERVER = "registred_servers";
	private UnitySerializer unitySerializer = new UnitySerializer();

	static {
		DEBUG.ON();
	}

	public UnitySkill() {
		super();
	}

	 public static final String BROKER_URL = "tcp://localhost:1883";
	//public static final String BROKER_URL = "tcp://195.221.248.15:1935";

	public static final String SERVER_URL = "195.221.248.15";
	public static final int SERVER_PORT = 1935;

	public static String DEFAULT_USER = "gama_demo";
	public static String DEFAULT_PASSWORD = "gama_demo";

	public static final MqttConnectOptions options = new MqttConnectOptions();

	public static MqttClient client = null;
	public static SubscribeCallback subscribeCallback = new SubscribeCallback();

	public MQTTUnityConnector connector;

	// @Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		return " ";
	}

	// -------------------- - - - -
	// --------------------------------------------------------------------------------------------------
	// -------------------------------------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@action(name = INetworkSkill.CONNECT_TOPIC, args = {
			@arg(name = INetworkSkill.PROTOCOL, type = IType.STRING, doc = @doc("protocol type (UDP, TCP, MQTT (by default)): the possible value ares '"
					+ INetworkSkill.UDP_SERVER + "', '" + INetworkSkill.UDP_CLIENT + "', '" + INetworkSkill.TCP_SERVER
					+ "', '" + INetworkSkill.TCP_CLIENT + "', otherwise the MQTT protocol is used.")),
			@arg(name = INetworkSkill.PORT, type = IType.INT, doc = @doc("Port number")),
			@arg(name = INetworkSkill.WITHNAME, type = IType.STRING, optional = true, doc = @doc("name of the agent on the server")),
			@arg(name = INetworkSkill.LOGIN, type = IType.STRING, optional = true, doc = @doc("login for the connection to the server")),
			@arg(name = INetworkSkill.PASSWORD, type = IType.STRING, optional = true, doc = @doc("password associated to the login")),
			@arg(name = INetworkSkill.SERVER_URL, type = IType.STRING, optional = true, doc = @doc("server URL (localhost or a server URL)")) }, doc = @doc(value = "Action used by a networking agent to connect to a server or as a server.", examples = {
					@example(" do connect to:\"localhost\" protocol:\"udp_server\" port:9876 with_name:\"Server\"; "),
					@example(" do connect to:\"localhost\" protocol:\"udp_client\" port:9876 with_name:\"Client\";"),
					@example(" do connect  with_name:\"any_name\";") }))
	@Override
	public void connectToServer(final IScope scope) throws GamaRuntimeException {

		System.out.println("Ceci est le tes");

		if (!scope.getSimulation().getAttributes().keySet().contains(REGISTRED_SERVER)) {
			this.startSkill(scope);
		}

		final IAgent agt = scope.getAgent();
		final String serverURL = scope.hasArg(INetworkSkill.SERVER_URL)
				? (String) scope.getArg(INetworkSkill.SERVER_URL, IType.STRING)
				: SERVER_URL;
		final String login = scope.hasArg(INetworkSkill.LOGIN)
				? (String) scope.getArg(INetworkSkill.LOGIN, IType.STRING)
				: DEFAULT_USER;
		final String password = scope.hasArg(INetworkSkill.PASSWORD)
				? (String) scope.getArg(INetworkSkill.PASSWORD, IType.STRING)
				: DEFAULT_PASSWORD;
		final String networkName = (String) scope.getArg(INetworkSkill.WITHNAME, IType.STRING);
		final String protocol = (String) scope.getArg(INetworkSkill.PROTOCOL, IType.STRING);
		final Integer port = scope.hasArg(INetworkSkill.PORT) ? (Integer) scope.getArg(INetworkSkill.PORT, IType.INT)
				: SERVER_PORT;

		DEBUG.OUT("--> paprametre  serverURL -> " + serverURL);

		// final Map<String, IConnector> myConnectors = new HashMap<String,
		// IConnector>();// this.getRegisteredServers(scope);
		final Map<String, IConnector> myConnectors = this.getRegisteredServers(scope);
		IConnector connector = myConnectors.get(serverURL);

		DEBUG.OUT("--> create to MQTT Broker " + login + " " + password);

		if (connector == null) {
			DEBUG.OUT("create to MQTT Broker " + login + " " + password);
			connector = new MQTTConnector(scope);
			if (serverURL != null) {
				connector.configure(IConnector.SERVER_URL, serverURL);
			}
			if (login != null) {
				connector.configure(IConnector.LOGIN, login);
			}
			if (password != null) {
				connector.configure(IConnector.PASSWORD, password);
			}
			myConnectors.put(serverURL, connector);
		}

		if (agt.getAttribute(INetworkSkill.NET_AGENT_NAME) == null) {
			agt.setAttribute(INetworkSkill.NET_AGENT_NAME, networkName);
		}

		List<String> serverList = (List<String>) agt.getAttribute(INetworkSkill.NET_AGENT_SERVER);
		if (serverList == null) {
			serverList = new ArrayList<>();
			agt.setAttribute(INetworkSkill.NET_AGENT_SERVER, serverList);
		}

		connector.connect(agt);
		serverList.add(serverURL);

		// register connected agent to global groups;
		for (final String grp : INetworkSkill.DEFAULT_GROUP) {
			connector.joinAGroup(agt, grp);
		}
	}

	@action(name = "send", args = {
			@arg(name = IKeyword.TO, type = IType.NONE, optional = true, doc = @doc("The agent, or server, to which this message will be sent to")),
			@arg(name = GamaMessage.CONTENTS, type = IType.NONE, optional = false, doc = @doc("The contents of the message, an arbitrary object")) })
	@Override
	public GamaMessage primSendMessage(final IScope scope) throws GamaRuntimeException {
		DEBUG.OUT("--> Message sent from UnitySkill");
		final IAgent sender = scope.getAgent();
		Object receiver = scope.getArg("to", IType.NONE);
		if (receiver == null)
			receiver = sender;
		final Object contents = effectiveContents(scope, scope.getArg(GamaMessage.CONTENTS, IType.NONE));
		if (contents == null) {
			return null;
		}
		final GamaMessage message = createNewMessage(scope, sender, receiver, contents);
		effectiveSend(scope, message, receiver);
		return message;
	}
	// ----------------- - - - - - -
	// ---------------------------------------------------------------------------------------------
	// ------------------------------- ------ - - - -
	// --------------------------------- -------------------------------------------

	@action(name = "connectMqttClient", doc = @doc(value = "Generates a client ID and connects it to the Mqtt server.", returns = "The client generated identifier.", examples = {
			@example("") }))
	public String connectMqttClient(final IScope scope) {
		connector = new MQTTUnityConnector(scope);

		String clientId = Utils.getMacAddress() + "-" + scope.getAgent().getName() + "-pub";

		DEBUG.LOG("The agent Name is  " + scope.getAgent().getName());

		try {
			options.setCleanSession(true);
			client = new MqttClient(BROKER_URL, clientId);
			//options.setUserName(DEFAULT_USER);
			//options.setPassword(DEFAULT_PASSWORD.toCharArray());
			options.setCleanSession(true);
			client.connect(options);
			DEBUG.LOG("Client : " + clientId + " connected with success!");
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
		scope.getSimulation().postDisposeAction(scope1 -> {
			try {
				if (client.isConnected())
					client.disconnect();
			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		});

		return clientId;
	}
	
	
	@action(name = "send_unity_message", args = {
			@arg(name = "scene_manager", type = IType.STRING, optional = true, doc = @doc("The game object name")),
			@arg(name = "content", type = IType.NONE, optional = false, doc = @doc("The emessage content")) }, doc = @doc(value = "The generic form of a message to send to Unity engine. ", returns = "true if it is in the base.", examples = {
					@example("") }))
	public Boolean sendUnityMqttMessage(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String objectName = ((Object) scope.getArg("scene_manager", IType.NONE) != null) ? (String) scope.getArg("scene_manager", IType.NONE) :  IUnitySkill.UNITY_SCENE_MANAGER;
		Object content = (Object) scope.getArg("content", IType.NONE);
		
		
		MinimalAgent mAgent = (MinimalAgent) scope.getArg("content", IType.NONE);
		
				
		System.out.println("The Envlope is "+ scope);
		
		
		GamaShape gs = (GamaShape) mAgent.getGeometry();
		
		System.out.println("The geometry to send is: "+gs.getGeometry());
						
		UnityAgent UAgent = new UnityAgent();
		UAgent.getUnityAgent(mAgent);
					
		//GamaMessage topicMessage = new GamaMessage(scope, sender, objectName, content);
		
		GamaMessage topicMessage = new GamaMessage(scope, sender, objectName, UAgent);

		publishUnityMessage(scope, client, IUnitySkill.TOPIC_MAIN, topicMessage);

		return false;
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "getUnityField", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attribute", type = IType.STRING, optional = false, doc = @doc("The field name")), }, doc = @doc(value = "Get a unity game object field value", returns = "void", examples = {
					@example("") }))
	public void getUnityField(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String attribute = (String) scope.getArg("attribute", IType.STRING);

		GetTopicMessage topicMessage = new GetTopicMessage(scope, sender, receiver, objectName, attribute);

		publishUnityMessage(scope, client, IUnitySkill.TOPIC_GET, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityFields", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributes", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")) }, doc = @doc(value = "Set a set of fields of a unity game object.", returns = "void", examples = {
					@example("") }))
	public void setUnityField(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		Map<String, Object> attributes = (Map<String, Object>) scope.getArg("attributes", IType.MAP);

		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : attributes.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}
		// TODO: change to support GamaMap in Unity side.
		SetTopicMessage topicMessage = new SetTopicMessage(scope, sender, receiver, objectName, items);

		publishUnityMessage(scope, client, IUnitySkill.TOPIC_SET, topicMessage);

	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityProperty", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "propertyName", type = IType.STRING, optional = false, doc = @doc("The property name")),
			@arg(name = "propertyValue", type = IType.NONE, optional = false, doc = @doc("The property value")) }, doc = @doc(value = "Set a property value.", returns = "void", examples = {
					@example("") }))
	public void setUnityProperty(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String propertyName = (String) scope.getArg("propertyName", IType.STRING);
		Object propertyValue = (Object) scope.getArg("propertyValue", IType.NONE);

		PropertyTopicMessage topicMessage = new PropertyTopicMessage(scope, sender, receiver, objectName, propertyName,
				propertyValue);

		publishUnityMessage(scope, client, IUnitySkill.TOPIC_PROPERTY, topicMessage);

	}

	@action(name = "callUnityMonoAction", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "actionName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attribute", type = IType.NONE, optional = false, doc = @doc("The attribute list and their values")) }, doc = @doc(value = "Call a unity game object method that has one parameter", returns = "void", examples = {
					@example("") }))
	public void callUnityMonoAction(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String actionName = (String) scope.getArg("actionName", IType.STRING);
		Object attribute = (Object) scope.getArg("attribute", IType.NONE);

		MonoActionTopicMessage topicMessage = new MonoActionTopicMessage(scope, sender, receiver, objectName,
				actionName, attribute);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_MONO_FREE, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "callUnityPluralAction", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "actionName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributes", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")) }, doc = @doc(value = "Call a unity game object method that has several parameters.", returns = "void", examples = {
					@example("") }))
	public void callUnityPluralAction(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String actionName = (String) scope.getArg("actionName", IType.STRING);
		Map<String, String> attributes = (Map<String, String>) scope.getArg("attributes", IType.MAP);

		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : attributes.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}
		PluralActionTopicMessage topicMessage = new PluralActionTopicMessage(scope, sender, receiver, objectName,
				actionName, items);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_MULTIPLE_FREE, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityColor", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "color", type = IType.COLOR, optional = false, doc = @doc("The color name")), }, doc = @doc(value = "Set a unity game object color", returns = "void", examples = {
					@example("") }))
	public void setUnityColor(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);

		final GamaColor col = scope.hasArg("color") ? (GamaColor) scope.getArg("color", IType.COLOR)
				: new GamaColor(255, 0, 255);
		final rgbColor color = new rgbColor(col.getRed(), col.getGreen(), col.getBlue());

		ColorTopicMessage topicMessage = new ColorTopicMessage(scope, sender, receiver, objectName, color.getRed(),
				color.getGreen(), color.getBlue());

		publishUnityMessage(scope, client, IUnitySkill.TOPIC_COLOR, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "setUnityPosition", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "position", type = IType.POINT, optional = false, doc = @doc("the new position to set for the object")),

	}, doc = @doc(value = "Set the position of a unity game object", returns = "void", examples = { @example("") }))
	public void setUnityPosition(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : null;
		PositionTopicMessage topicMessage = new PositionTopicMessage(scope, sender, receiver, objectName, position);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_POSITION, topicMessage);
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
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		final boolean smoothMove = scope.hasArg("smoothMove") ? (boolean) scope.getArg("smoothMove", IType.BOOL) : true;
		final double speed = scope.hasArg("speed") ? (double) scope.getArg("speed", IType.FLOAT)
				: IUnitySkill.DAFAULT_SPEED;
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : null;

		MoveTopicMessage topicMessage = new MoveTopicMessage(scope, sender, receiver, objectName, position, speed,
				smoothMove);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_MOVE, topicMessage);

		DEBUG.LOG("New message sent to Unity. Topic: " + IUnitySkill.TOPIC_MOVE + "   Number: "
				+ serializeMessage(scope, topicMessage));
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
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String type = (String) scope.getArg("type", IType.STRING);

		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT)
				: new GamaPoint(0, 0, 0);
		final GamaColor col = scope.hasArg("color") ? (GamaColor) scope.getArg("color", IType.COLOR)
				: new GamaColor(255, 0, 255);
		final rgbColor color = new rgbColor(col.getRed(), col.getGreen(), col.getBlue());

		DEBUG.LOG(" -----------> " + col.stringValue(scope));

		CreateTopicMessage topicMessage = new CreateTopicMessage(scope, sender, receiver, objectName, type, color,
				position);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_CREATE_OBJECT, topicMessage);
	}

	// TODO: Youcef-> Review this action with better description and genericity
	// support
	@action(name = "destroyUnityObject", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")) }, doc = @doc(value = "Destroy a unity game object", returns = "void", examples = {
					@example("") }))
	public synchronized void destroyUnityObject(final IScope scope) {
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);

		DestroyTopicMessage topicMessage = new DestroyTopicMessage(scope, sender, receiver, objectName);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_DESTROY_OBJECT, topicMessage);
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
		String sender = (String) scope.getAgent().getName();
		String notificationId = (String) scope.getArg("notificationId", IType.STRING);
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String fieldType = (String) scope.getArg("fieldType", IType.STRING);
		String fieldName = (String) scope.getArg("fieldName", IType.STRING);
		Object fieldValue = (Object) scope.getArg("fieldValue", IType.NONE);
		String fieldOperator = (String) scope.getArg("fieldOperator", IType.STRING);

		NotificationTopicMessage topicMessage = new NotificationTopicMessage(scope, sender, receiver, notificationId,
				objectName, fieldType, fieldName, fieldValue, fieldOperator);
		publishUnityMessage(scope, client, IUnitySkill.TOPIC_POSITION, topicMessage);
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "disconnectMqttClient", doc = @doc(value = "Disconnect the client from the Mqtt server.", returns = "true if it is in the base.", examples = {
			@example("") }))
	public String disconnectMqttClient(final IScope scope) {
		String clientId = Utils.getMacAddress() + "-" + scope.getAgent().getName() + "-pub";
		try {

			if (client.isConnected())
				client.disconnect();
			DEBUG.LOG("Client : " + clientId + " disconnected with success!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return clientId;
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "subscribe_To_Topic", args = {
			@arg(name = "topic", type = IType.STRING, optional = false, doc = @doc("Topic Name")) }, doc = @doc(value = "Subscribe a client to a topic", returns = "true if success, false otherwise", examples = {
					@example("") }))
	public String SubscribeToTopic(final IScope scope) {
		String clientId = Utils.getMacAddress() + "-" + scope.getAgent().getName() + "-pub";
		final String topic = (String) scope.getArg("topic", IType.STRING);
		try {
			client.setCallback(subscribeCallback);
			// client.connect();
			client.subscribe(topic);
			DEBUG.LOG("Subscriber is now listening to " + topic);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return "Subscribed to the topic: " + topic;
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "get_unity_message", doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public synchronized String getUnityMessage(final IScope scope) {
		return subscribeCallback.getNextMessage();
	}

	// TODO
	@action(name = "clearTopic", args = {
			@arg(name = "topic", type = IType.STRING, optional = false, doc = @doc("Topic Name")) }, doc = @doc(value = "Clear the topic messages", returns = "nothing.", examples = {
					@example("") }))
	public synchronized void clearTopic(final IScope scope) {
		final String topic = (String) scope.getArg("topic", IType.STRING);
		/*
		 * final MqttTopic unityTopic = client.getTopic(topic); try { MqttMessage
		 * message = new MqttMessage(new byte[0]); message.setRetained(true);
		 * unityTopic.publish(message); //client.publish(topic, new byte[0],0,true); }
		 * catch (MqttPersistenceException e) { e.printStackTrace(); } catch
		 * (MqttException e) { e.printStackTrace(); }
		 */
		try {
			DEBUG.LOG(
					"-------------------------------------------------------------------------------> Topic to clear is "
							+ topic);
			subscribeCallback.clearTopicMessages(topic);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	// TODO: Review this action with better description and genericity support.
	// Action should return a pair "key"::value
	@action(name = "get_unity_replay", doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public synchronized String getReplayUnityMessage(final IScope scope) {
		String message = subscribeCallback.getNextReplayMessage();
		if (message != null) {
			final ConverterScope cScope = new ConverterScope(scope);
			final XStream xstream = StreamConverter.loadAndBuild(cScope);
			final ReplayMessage notifMsg = (ReplayMessage) xstream.fromXML(message);
			return notifMsg.fieldValue;
		} else {
			return "null";
		}
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "get_unity_notification", doc = @doc(value = "Get the next received mqtt notification message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public synchronized String getUnityNotificationMessage(final IScope scope) {
		return subscribeCallback.getNextNotificationMessage();
	}

	// TODO: Review this action with better description and genericity support
	@operator(value = "isNotificationTrue", doc = { @doc("Check if the notification has been received") }, category = {
			IOperatorCategory.LOGIC })
	public synchronized boolean isNotificationTrue(final IScope scope, String notificationId) {
		DEBUG.LOG("subscribeCallback.notificationMailBox.size()  is:  " + subscribeCallback.notificationMailBox.size());
		if (subscribeCallback.notificationMailBox.size() > 0) {

			for (MqttMessage msg : subscribeCallback.notificationMailBox) {
				String message = msg.toString();
				final ConverterScope cScope = new ConverterScope(scope);
				final XStream xstream = StreamConverter.loadAndBuild(cScope);
				final NotificationMessage notifMsg = (NotificationMessage) xstream.fromXML(message);

				if (notifMsg.notificationId.equals(notificationId)) {
					subscribeCallback.notificationMailBox.remove(0);
					return true;
				}
			}
		} else {
			return false;
		}
		return false;
	}

	// TODO: Review this action with better description and genericity support
	@action(name = "getLittosimMessage", doc = @doc(value = "Check if there is a new created agent and return it's position and name", returns = "Check if there is a new created agent and return it's position and name", examples = {
			@example("") }))
	public synchronized Map<String, String> getLittosimMessage(final IScope scope) {

		Map<String, String> messageReceived = null;

		DEBUG.LOG("subscribeCallback.littosimMailBox.size()  is:  " + subscribeCallback.littosimMailBox.size());

		if (subscribeCallback.littosimMailBox.size() > 0) {

			MqttMessage msg = subscribeCallback.littosimMailBox.get(0);
			String message = msg.toString();
			DEBUG.LOG("The received Message is : " + message);
			final ConverterScope cScope = new ConverterScope(scope);
			final XStream xstream = StreamConverter.loadAndBuild(cScope);
			final littosimMessage notifMsg = (littosimMessage) xstream.fromXML(message);
			subscribeCallback.littosimMailBox.remove(0);

			messageReceived = notifMsg.getMapMsg();

			return messageReceived;

		} else {
			return null;
		}
	}

	// TODO: Review this action with better description and genericity support
	@operator(value = "hasMoreMessageOnTopic", doc = {
			@doc("Check if there are more messages on a specific topic") }, category = { IOperatorCategory.LOGIC })
	public static synchronized boolean hasMoreMessageOnTopic(final IScope scope, String topic) {

		switch (topic) {
		case IUnitySkill.TOPIC_REPLAY:
			if (subscribeCallback.replayMailBox.size() > 0) {
				return true;
			}
			break;
		case IUnitySkill.TOPIC_NOTIFICATION:
			if (subscribeCallback.notificationMailBox.size() > 0) {
				return true;
			}
			break;
		case IUnitySkill.TOPIC_LITTOSIM:
			if (subscribeCallback.littosimMailBox.size() > 0) {
				return true;
			}
			break;
		default:
			if (subscribeCallback.mailBox.size() > 0) {
				return true;
			}
			break;
		}
		return false;
	}

	@action(name = "getAllActionsMessage", doc = @doc(value = ".", returns = "", examples = { @example("") }))
	public synchronized String getAllActionsMessage(final IScope scope) {
		String text = allContent;
		allContent = "";
		return text;
	}

	public static XStream getXStream(final IScope scope) {
		final ConverterScope cScope = new ConverterScope(scope);
		final XStream xstream = StreamConverter.loadAndBuild(cScope);
		return xstream;
	}

	@getter(IUnitySkill.UNITY_SPEED)
	public double getSpeed(final IAgent agent) {
		if (agent == null) {
			return 0.0;
		}
		return (Double) agent.getAttribute(IUnitySkill.UNITY_SPEED);
	}

	@setter(IUnitySkill.UNITY_SPEED)
	public void setSpeed(final IAgent agent, final double s) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IUnitySkill.UNITY_SPEED, s);
		if (isAgentCreatedInUnity(agent)) {
			if (client != null) {
				ArrayList<ItemAttributes> items = new ArrayList<ItemAttributes>();
				ItemAttributes it = new ItemAttributes(IUnitySkill.UNITY_SPEED, s);
				items.add(it);
				SetTopicMessage topicMessage = new SetTopicMessage(agent.getScope(), agent.getName(), agent.getName(),
						agent.getName(), items);
				publishUnityMessage(agent.getScope(), client, IUnitySkill.TOPIC_SET, topicMessage);
			}
		}
	}

	public boolean isAgentCreatedInUnity(final IAgent agent) {
		if (agent.getAttribute(IUnitySkill.UNITY_CREATED) == null)
			return false;
		if ((boolean) agent.getAttribute(IUnitySkill.UNITY_CREATED))
			return true;
		return false;
	}

	@setter(IUnitySkill.UNITY_LOCATION)
	public void setUnityLocation(final IAgent agent, ILocation p) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IUnitySkill.UNITY_LOCATION, p);
		if (isAgentCreatedInUnity(agent)) {
			if (p == null)
				p = agent.getLocation();
			if (client != null) {
				GamaPoint loc = new GamaPoint(p.getX(), p.getY(), p.getZ());
				PositionTopicMessage topicMessage = new PositionTopicMessage(agent.getScope(), agent.getName(),
						agent.getName(), agent.getName(), loc);
				publishUnityMessage(agent.getScope(), client, IUnitySkill.TOPIC_POSITION, topicMessage);
			}
		}
	}

	@getter(IUnitySkill.UNITY_LOCATION)
	public GamaPoint getUnityLocation(final IAgent agent) {
		if (agent == null) {
			return new GamaPoint(0, 0, 0);
		}
		return (GamaPoint) agent.getAttribute(IUnitySkill.UNITY_LOCATION);
	}

	@setter(IUnitySkill.UNITY_CREATED)
	public void setUnityCreated(final IAgent agent, final boolean isCreated) {
		agent.setAttribute(IUnitySkill.UNITY_CREATED, isCreated);
	}

	@setter(IUnitySkill.UNITY_ROTATION)
	public void setUnityRotation(final IAgent agent, final GamaPoint p) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IUnitySkill.UNITY_ROTATION, p);
		if (isAgentCreatedInUnity(agent)) {
			if (client != null && p != null) {
				GamaPoint loc = new GamaPoint(p.getX(), p.getY(), p.getZ());
				PropertyTopicMessage topicMessage = new PropertyTopicMessage(agent.getScope(), agent.getName(),
						agent.getName(), agent.getName(), "localEulerAngles", loc);
				publishUnityMessage(agent.getScope(), client, IUnitySkill.TOPIC_PROPERTY, topicMessage);
			}
		}
	}

	@getter(IUnitySkill.UNITY_ROTATION)
	public GamaPoint getUnityRotation(final IAgent agent) {
		if (agent == null) {
			return new GamaPoint(0, 0, 0);
		}
		return (GamaPoint) agent.getAttribute(IUnitySkill.UNITY_ROTATION);
	}

	@getter(IUnitySkill.UNITY_ROTATE)
	public boolean getUnityRotate(final IAgent agent) {
		if (agent == null) {
			return false;
		}
		return (boolean) agent.getAttribute(IUnitySkill.UNITY_ROTATE);
	}

	@setter(IUnitySkill.UNITY_ROTATE)
	public void setUnityRotate(final IAgent agent, final boolean isRotate) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IUnitySkill.UNITY_ROTATE, isRotate);
		if (isAgentCreatedInUnity(agent)) {
			if (client != null) {
				ArrayList<ItemAttributes> items = new ArrayList<ItemAttributes>();
				ItemAttributes it = new ItemAttributes(IUnitySkill.UNITY_ROTATE, isRotate);
				items.add(it);
				SetTopicMessage setMessage = new SetTopicMessage(agent.getScope(), agent.getName(), agent.getName(),
						agent.getName(), items);
				publishUnityMessage(agent.getScope(), client, IUnitySkill.TOPIC_SET, setMessage);
			}
		}
	}

	@setter(IUnitySkill.UNITY_SCALE)
	public void setScale(final IAgent agent, final ILocation p) {
		if (agent == null) {
			return;
		}
		agent.setAttribute(IUnitySkill.UNITY_SCALE, p);
		if (isAgentCreatedInUnity(agent)) {
			if (client != null && p != null) {
				GamaPoint loc = new GamaPoint(p.getX(), p.getY(), p.getZ());
				PropertyTopicMessage topicMessage = new PropertyTopicMessage(agent.getScope(), agent.getName(),
						agent.getName(), agent.getName(), "localScale", loc);
				publishUnityMessage(agent.getScope(), client, IUnitySkill.TOPIC_PROPERTY, topicMessage);
			}
		}
	}

	@getter(IUnitySkill.UNITY_SCALE)
	public GamaPoint getUnityScale(final IAgent agent) {
		if (agent == null) {
			return new GamaPoint(0, 0, 0);
		}
		return (GamaPoint) agent.getAttribute(IUnitySkill.UNITY_SCALE);
	}

	public void publishUnityMessage(final IScope scope, MqttClient client, String topic, Object message) {
		String messageString = serializeMessage(scope, message);
		//messageString.replace("class=", "xsi:type=");
		System.out.println("The message with replace is : \n "+messageString);
		//System.out.println("The shape to send is \n "+messageString);
		try {
			final MqttTopic unityTopic = client.getTopic(topic);
			MqttMessage mqttMessage = new MqttMessage();
			mqttMessage.setPayload(messageString.getBytes());
			unityTopic.publish(mqttMessage);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String serializeMessage(final IScope scope, Object message) {
		unitySerializer.SetSerializer(getXStream(scope));
		return unitySerializer.agentShapeToXML(message);
		//return unitySerializer.toXML(message);
		//return getXStream(scope).toXML(message);
	}

}