package ummisco.gama.unity.skills;

import java.util.ArrayList;

import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;


import com.thoughtworks.xstream.XStream;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.topology.ITopology;
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
import msi.gama.util.GamaMap;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.mqtt.external.connector.MQTTConnector;
import ummisco.gama.serializer.factory.StreamConverter;
import ummisco.gama.serializer.gamaType.converters.ConverterScope;
import ummisco.gama.unity.data.type.rgbColor;
import ummisco.gama.unity.messages.ColorTopicMessage;
import ummisco.gama.unity.messages.CreateTopicMessage;
import ummisco.gama.unity.messages.CreatedAgentMessage;
import ummisco.gama.unity.messages.DestroyTopicMessage;
import ummisco.gama.unity.messages.GamaUnityMessage;
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
import ummisco.gama.unity.mqtt.SubscribeCallback;
import ummisco.gama.unity.mqtt.Utils;


/**
 * UnitySkill : This class is intended to define the minimal set of behaviours required from an agent that is able to
 * communicate with unity angine in order to visulaize GAMA simulations. Each member that has a meaning in GAML is annotated with the respective tags (vars, getter, setter, init,
 * action & args)
 *
 */

@doc ("The unity skill is intended to define the minimal set of behaviors required for agents that are able "
		+ "to communicate with the unity engine in order to visualize GAMA simulations in different terminals")

@vars ({ @variable (
		name = IKeyword.LOCATION,
		type = IType.POINT,
		depends_on = IKeyword.SHAPE,
		doc = @doc ("Represents the current position of the agent")),
	@variable (name = UnitySkill.UNITY_ROTATION,
		type = IType.POINT,
		depends_on = IKeyword.SHAPE,
		doc = @doc ("Represents the current rotation of the agent")),
	@variable (name = UnitySkill.UNITY_SCALE,
		type = IType.POINT,
		depends_on = IKeyword.SHAPE,
		init = "{0,0,0}",
		doc = @doc ("Represents the current scale of the agent")),
		@variable (
				name = IKeyword.SPEED,
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("Represents the speed of the agent (in meter/second)")),
	})
@skill(name = UnitySkill.SKILL_NAME, concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
public class UnitySkill extends Skill {
	
	public static String allContent = "";
	static {
		DEBUG.OFF();	
	}

	public static final String BROKER_URL = "tcp://localhost:1883";
	//public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
	
	
	
	public static final String SKILL_NAME = "unity";
	public static final String UNITY_SPEED = "speed";
	public static final String UNITY_ROTATION = "rotation";
	public static final String UNITY_SCALE = "scale";
	
	//public static final String BROKER_URL = "tcp://195.221.248.15:1935";
	//public static final String DEFAULT_PASSWORD = "gama_demo";
	//public static final String DEFAULT_USER = "gama_demo";
	
	public static final MqttConnectOptions options = new MqttConnectOptions();

	public static MqttClient client = null;
	public static SubscribeCallback subscribeCallback = new SubscribeCallback();
	
	public static MQTTConnector connector;
	

	
	
	// @Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		return " ";
	}

	@action ( 
			name = "connectMqttClient", 
			doc = @doc ( 
						value = "Generates a client ID and connects it to the Mqtt server.", 
						returns = "The client generated identifier.", 
						examples = { @example("") }))
	public static String connectMqttClient(final IScope scope) {
		String clientId = Utils.getMacAddress() + "-" + scope.getAgent().getName() + "-pub";
		
		
		DEBUG.LOG("The agent Name is  "+scope.getAgent().getName());
		
		
		try {
			client = new MqttClient(BROKER_URL, clientId);
			options.setCleanSession(true);
		//	options.setPassword(DEFAULT_PASSWORD.toCharArray());
		//	options.setUserName(DEFAULT_USER);
			
			
			
		//	final MqttConnectOptions connOpts = new MqttConnectOptions();
		//	connOpts.setCleanSession(true);
			//subscribeCallback = new SubscribeCallback();
			//client.setCallback(subscribeCallback);
			//connOpts.setCleanSession(true);
			//connOpts.setKeepAliveInterval(30);
			//client.connect(connOpts);
			
			
			
			
		//	
		//	public static String DEFAULT_LOCAL_NAME = "gama-" + Calendar.getInstance().getTimeInMillis() + "@";
		//	
			
			
		//	options.setWill(client.getTopic("home/LWT"), "I'm gone :(".getBytes(), 0, false);
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
	
	
	//TODO: Youcef-> Review this action to remove some attributes, make it more generic and fix data structure issues
	@action ( 
				name = "send_unity_message", 
				args = { @arg ( 
								name = "objectName", 
								type = IType.STRING,
								optional = false, 
								doc = @doc("The game object name")),
						@arg ( 
								name = "content", 
								type = IType.NONE, 
								optional = false, 
								doc = @doc("The emessage content")) }, 
				doc = @doc ( 
							value = "The generic form of a message to send to Unity engine. ", 
							returns = "true if it is in the base.", 
							examples = { @example("") }))
	public static Boolean sendUnityMqttMessage(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		Object content = (Object) scope.getArg("content", IType.NONE);
		
		GamaMessage messageUnity = new GamaMessage(scope, sender, objectName, content);
	
		final String stringMessage = getXStream(scope).toXML(messageUnity);
		
		allContent += "\n"+stringMessage;
		
		DEBUG.LOG("The message is: ");
		DEBUG.LOG(stringMessage);
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_MAIN);
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
			
			return true;
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	
	//TODO: Youcef-> Review this action with better description and genericity support
	@action(
			name = "getUnityField", 
			args = { @arg (
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg (
							name = "attribute", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The field name")), }, 
			doc = @doc (
					value = "Get a unity game object field value", 
					returns = "void", 
					examples = { @example("") }))
	public static void getUnityField(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String attribute = (String) scope.getArg("attribute", IType.STRING);

		GetTopicMessage topicMessage = new GetTopicMessage(scope, sender, receiver, objectName, attribute);
		
		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_GET);

		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action (
			name = "setUnityFields", 
			args = { @arg (
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg (
							name = "attributes", 
							type = IType.MAP, 
							optional = false, 
							doc = @doc("The attribute list and their values")) }, 
			doc = @doc (
							value = "Set a set of fields of a unity game object.", 
							returns = "void", 
							examples = { @example("") }))
	public static void setUnityField(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		Map<String, Object> attributes = (Map<String, Object>) scope.getArg("attributes", IType.MAP);

		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : attributes.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}
		//TODO: change to support GamaMap in Unity side.
		SetTopicMessage setMessage = new SetTopicMessage(scope, sender, receiver, objectName, items);
				
		final String stringMessage = getXStream(scope).toXML(setMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_SET);
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}

	
	
	


	//TODO: Just to do tests
	@action ( 
				name = "doTest", 
				args = { @arg ( 
								name = "thisIsTest", 
								type = IType.NONE,
								optional = false, 
								doc = @doc("The sender")) 
				}, 
				doc = @doc ( 
							value = "  ", 
							returns = " ", 
							examples = { @example("") }))
	public static Boolean doTest(final IScope scope) 
	{
		Object sender = (Object) scope.getArg("thisIsTest", IType.NONE);

		System.out.println("Type is -> "+sender.getClass());
		
		if (sender.getClass() == Integer.class) {
		    System.out.println("This is an Integer");
		} 
		else if (sender.getClass() == String.class) {
		    System.out.println("This is a String");
		}
		else if (sender.getClass() == Float.class) {
		    System.out.println("This is a Float");
		}else if (sender.getClass() == Double.class){
		    System.out.println("This is a Double");
		}else if (sender.getClass() == GamaMap.class) {
		    System.out.println("This is a Map");
		}else if (sender.getClass() == GamaPoint.class) {
		    System.out.println("This is a GamaPoint");
		}
		
		
		return false;
	}
	
	
	
	
	
	
	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "setUnityProperty", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg (
							name = "propertyName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The property name")),
					@arg ( 
							name = "propertyValue", 
							type = IType.NONE, 
							optional = false, 
							doc = @doc("The property value")) }, 
			doc = @doc ( 
						value = "Set a property value.", 
						returns = "void", 
						examples = { @example("") }))
	public static void setUnityProperty(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String propertyName = (String) scope.getArg("propertyName", IType.STRING);
		Object propertyValue = (Object) scope.getArg("propertyValue", IType.NONE);

		PropertyTopicMessage topicMessage = new PropertyTopicMessage(scope, sender, receiver, objectName, propertyName,
				propertyValue);
	
		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_PROPERTY);
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}

	}

	@action	( 
				name = "callUnityMonoAction", 
				args = { @arg ( 
								name = "objectName", 
								type = IType.STRING, 
								optional = false, 
								doc = @doc("The game object name")),
						@arg (
								name = "actionName", 
								type = IType.STRING, 
								optional = false, 
								doc = @doc("The game object name")),
						@arg ( 
								name = "attribute", 
								type = IType.NONE, 
								optional = false, 
								doc = @doc("The attribute list and their values")) }, 
				doc = @doc ( 
							value = "Call a unity game object method that has one parameter", 
							returns = "void", 
							examples = { @example("") }))
	public static void callUnityMonoAction(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String actionName = (String) scope.getArg("actionName", IType.STRING);
		Object attribute = (Object) scope.getArg("attribute", IType.NONE);

		MonoActionTopicMessage topicMessage = new MonoActionTopicMessage(scope, sender, receiver, objectName,
				actionName, attribute);
	
		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_MONO_FREE);
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action	(	
			name = "callUnityPluralAction", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg (
							name = "actionName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg ( 
							name = "attributes", 
							type = IType.MAP, 
							optional = false, 
							doc = @doc("The attribute list and their values")) }, 
			doc = @doc ( 
						value = "Call a unity game object method that has several parameters.",
						returns = "void", 
						examples = { @example("") }))
	public static void callUnityPluralAction(final IScope scope) 
	{
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
	
		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_MULTIPLE_FREE);
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action (	
			name = "setUnityColor", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg ( 
							name = "color", 
							type = IType.COLOR, 
							optional = false, 
							doc = @doc("The color name")), }, 
			doc = @doc ( 
						value = "Set a unity game object color", 
						returns = "void", 
						examples = { @example("") }))
	public static void setUnityColor(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		
		final GamaColor col = scope.hasArg("color") ? (GamaColor) scope.getArg("color", IType.COLOR) : new GamaColor(255, 0, 255);
		final rgbColor  color = new rgbColor(col.getRed(), col.getGreen(), col.getBlue());

		ColorTopicMessage topicMessage = new ColorTopicMessage(scope, sender, receiver, objectName, color.getRed(), color.getGreen(), color.getBlue());

		final String stringMessage = getXStream(scope).toXML(topicMessage);
	
		allContent += "\n"+stringMessage;
		
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_COLOR);
	
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "setUnityPosition", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg (
							name = "position",
							type = IType.POINT,
							optional = false,
							doc = @doc ("the new position to set for the object")),		
			
			}, 
			doc = @doc ( 
						value = "Set the position of a unity game object", 
						returns = "void", 
						examples = { @example("") }))
	public static void setUnityPosition(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : null;

		PositionTopicMessage topicMessage = new PositionTopicMessage(scope, sender, receiver, objectName, position);
		
		
		

		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_POSITION);

		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action	( 
			name = "unityMove", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg ( 
							name = "position", 
							type = IType.POINT, 
							optional = false, 
							doc = @doc("The position values (x,y,z)")),
					@arg ( 
							name = "speed", 
							type = IType.INT, 
							optional = true, 
							doc = @doc("speed")),
					@arg ( 
							name = "smoothMove", 
							type = IType.BOOL, 
							optional = true, 
							doc = @doc("If true, the move will be towards the target position, but with adding force (according to the specified speed). "
									+ "So, the object may not stop at the destination position."
									+ " If false, the object will stop moving when the target position is reached.")),
					}, 
			doc = @doc ( 
						value = "Set the position of a unity game object", 
						returns = "void", 
						examples = { @example("") }))
	public static synchronized void unityMove(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		final boolean smoothMove = scope.hasArg("smoothMove") ? (boolean) scope.getArg("smoothMove", IType.BOOL) : true;
		final double speed = scope.hasArg("speed") ? (double) scope.getArg("speed", IType.FLOAT) : IUnitySkill.DAFAULT_SPEED;
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : null;
		
		MoveTopicMessage topicMessage = new MoveTopicMessage(scope, sender, receiver, objectName, position, speed, smoothMove);

		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_MOVE);

		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		DEBUG.LOG("New message sent to Unity. Topic: " + unityTopic.getName() + "   Number: " + stringMessage);
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "newUnityObject", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The game object name")),
					@arg ( 
							name = "type", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("The object type")),
					@arg ( 
							name = "color", 
							type = IType.COLOR, 
							optional = true, 
							doc = @doc("The object color")),
					@arg ( 
							name = "position", 
							type = IType.POINT, 
							optional = true, 
							doc = @doc("The object position")), }, 
			doc = @doc ( 
						value = "Create a new unity game object on the scene and set its initial color and position. Supported fomes are: Capsule, Cube, Cylinder and Sphere", 
						returns = "void", 
						examples = { @example("") }))
	public static synchronized void newUnityObject(final IScope scope) 
	{
		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String type = (String) scope.getArg("type", IType.STRING);
		
		final GamaPoint position = scope.hasArg("position") ? (GamaPoint) scope.getArg("position", IType.POINT) : new GamaPoint(0,0,0);
		final GamaColor col = scope.hasArg("color") ? (GamaColor) scope.getArg("color", IType.COLOR) : new GamaColor(255, 0, 255);
		final rgbColor  color = new rgbColor(col.getRed(), col.getGreen(), col.getBlue());
	
		DEBUG.LOG(" -----------> "+col.stringValue(scope)); 
	
		
		CreateTopicMessage topicMessage = new CreateTopicMessage(scope, sender, receiver, objectName, type, color,
				position);

		final String stringMessage = getXStream(scope).toXML(topicMessage);

		allContent += "\n"+stringMessage;
		DEBUG.LOG("The new message is : "+stringMessage);
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_CREATE_OBJECT);

		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	//TODO: Youcef-> Review this action with better description and genericity support
		@action ( 
				name = "destroyUnityObject", 
				args = { @arg ( 
								name = "objectName", 
								type = IType.STRING, 
								optional = false, 
								doc = @doc("The game object name")) }, 
				doc = @doc ( 
							value = "Destroy a unity game object", 
							returns = "void", 
							examples = { @example("") }))
		public static synchronized void destroyUnityObject(final IScope scope) 
		{
			String sender = (String) scope.getAgent().getName();
			String receiver = (String) scope.getArg("objectName", IType.STRING);
			String objectName = (String) scope.getArg("objectName", IType.STRING);
		
			DestroyTopicMessage topicMessage = new DestroyTopicMessage(scope, sender, receiver, objectName);

			final String stringMessage = getXStream(scope).toXML(topicMessage);
		
			allContent += "\n"+stringMessage;
			
			final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_DESTROY_OBJECT);

			try {
				MqttMessage message = new MqttMessage();
				message.setPayload(stringMessage.getBytes());
				unityTopic.publish(message);
				
				
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "unityNotificationSubscribe", 
			args = { @arg ( 
							name = "objectName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc ( "The game object name" ) ),
					@arg ( 
							name = "notificationId", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc ( "notificationId: the notification ID to communicate when notifying an agent by unity")),
					@arg ( 
							name = "fieldType", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("fieldType: whether it is a field or a property in the game object")),
					@arg ( 
							name = "fieldName", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("fieldName: The field name")),
					@arg ( 
							name = "fieldValue", 
							type = IType.NONE, 
							optional = false, 
							doc = @doc("fieldValue: The field value")),
					@arg ( 
							name = "fieldOperator", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc ( "fieldOperator: The comparaison operator")), }, 
			doc = @doc ( 
						value = "Subscribe to the notification mechanism, allowing unity to notify Gama when the condition on the specified field has been met.", 
						returns = "void", 
						examples = { @example("") }))
	public synchronized void unityNotificationSubscribe(final IScope scope) 
	{
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


		final String stringMessage = getXStream(scope).toXML(topicMessage);
		
		allContent += "\n"+stringMessage;
		
		final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_NOTIFICATION);

		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "disconnectMqttClient", 
			doc = @doc ( 
						value = "Disconnect the client from the Mqtt server.", 
						returns = "true if it is in the base.", 
						examples = { @example("") }))
	public static String disconnectMqttClient(final IScope scope) {
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

	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "subscribe_To_Topic", 
			args = {@arg ( 
							name = "topic", 
							type = IType.STRING, 
							optional = false, 
							doc = @doc("Topic Name")) }, 
			doc =  @doc ( 
						value = "Subscribe a client to a topic", 
						returns = "true if success, false otherwise", 
						examples = { @example("") }))
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
	
	
	//TODO Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "get_unity_message", 
			doc = @doc(value = "Get the next received mqtt message.", 
			returns = "The message content if there is a received message, null otherwise.", 
			examples = { @example("") }))
	public synchronized String getUnityMessage(final IScope scope) {
		return subscribeCallback.getNextMessage();
	}

	//TODO: Youcef-> Review this action with better description and genericity support. Action should return a pair "key"::value
	@action ( 
			name = "get_unity_replay", 
			doc = @doc(value = "Get the next received mqtt message.", 
			returns = "The message content if there is a received message, null otherwise.", 
			examples = { @example("") }))
	public synchronized String getReplayUnityMessage(final IScope scope) {
			
			String message = subscribeCallback.getNextReplayMessage();
		if(message!= null) {
			
			final ConverterScope cScope = new ConverterScope(scope);
			final XStream xstream = StreamConverter.loadAndBuild(cScope);
			final ReplayMessage notifMsg = (ReplayMessage) xstream.fromXML(message);
			return notifMsg.fieldValue;
		}else {
			return "null";
		}
			
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "get_unity_notification", 
			doc = @doc(value = "Get the next received mqtt notification message.", 
			returns = "The message content if there is a received message, null otherwise.", 
			examples = { @example("") }))
	public synchronized String getUnityNotificationMessage(final IScope scope) {
		return subscribeCallback.getNextNotificationMessage();
	}

	//TODO: Youcef-> Review this action with better description and genericity support
	@operator ( 
			value = "isNotificationTrue", 
			doc = { @doc("Check if the notification has been received") }, 
			category = { IOperatorCategory.LOGIC })
	public static synchronized boolean isNotificationTrue(final IScope scope, String notificationId) {
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
	
	
	
	

	//TODO: Youcef-> Review this action with better description and genericity support
	@action ( 
			name = "newCreatedAgent", 
			doc = @doc(value = "Check if there is a new created agent and return it's position and name", 
			returns = "Check if there is a new created agent and return it's position and name", 
			examples = { @example("") }))
	public synchronized CreatedAgentMessage newCreatedAgent(final IScope scope) {
		
		DEBUG.LOG("subscribeCallback.createdMailBox.size()  is:  " + subscribeCallback.createdMailBox.size());
		
		if (subscribeCallback.createdMailBox.size() > 0) {

			MqttMessage msg = subscribeCallback.createdMailBox.get(0); 
			String message = msg.toString();
			DEBUG.LOG("The received Message is : " + message);
			final ConverterScope cScope = new ConverterScope(scope);
			final XStream xstream = StreamConverter.loadAndBuild(cScope);
			final CreatedAgentMessage notifMsg = (CreatedAgentMessage) xstream.fromXML(message);
			subscribeCallback.createdMailBox.remove(0);
			return notifMsg;

		} else {
			return null;
		}
	}
	
	


	@action ( 
			name = "getAllActionsMessage", 
			doc = @doc(value = ".", 
			returns = "", 
			examples = { @example("") }))
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
	
	
	
	
	
	/*
	
	@getter (IKeyword.SPEED)
	public double getSpeed(final IAgent agent) {
		if (agent == null) { return 0.0; }
		return (Double) agent.getAttribute(IKeyword.SPEED);
	}

	
	@setter (IKeyword.SPEED)
	public void setSpeed(final IAgent agent, final double s) {
		if (agent == null) { return; }		
		agent.setAttribute(IKeyword.SPEED, s);
		
		if(client!=null) {
			ArrayList<ItemAttributes> items = new ArrayList<ItemAttributes>();
			ItemAttributes it = new ItemAttributes(UnitySkill.UNITY_SPEED, s);
			items.add(it);
				
			SetTopicMessage setMessage = new SetTopicMessage(agent.getScope(), agent.getName(), agent.getName(), agent.getName(), items);
		
			final String stringMessage = getXStream(agent.getScope()).toXML(setMessage);
			allContent += "\n"+stringMessage;
			DEBUG.LOG("The new message is : "+stringMessage);
			try {
				final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_SET);
				MqttMessage message = new MqttMessage();
				message.setPayload(stringMessage.getBytes());
				unityTopic.publish(message);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	*/
	
	/*
	
	@setter (IKeyword.LOCATION)
	public void setLocation(final IAgent agent, final ILocation p) {
		if (agent == null) { return; }
		agent.setLocation(p);
		
		if(client!=null) {
			GamaPoint loc = new GamaPoint(p.getX(), p.getY(), p.getZ());
			PositionTopicMessage topicMessage = new PositionTopicMessage(agent.getScope(), agent.getName(), agent.getName(), agent.getName(), loc);
			final String stringMessage = getXStream(agent.getScope()).toXML(topicMessage);
			allContent += "\n"+stringMessage;
			DEBUG.LOG("The new message is : "+stringMessage);
			try {
				final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_POSITION);
				MqttMessage message = new MqttMessage();
				message.setPayload(stringMessage.getBytes());
				unityTopic.publish(message);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	}

	
	
	@setter (UnitySkill.UNITY_ROTATION)
	public void setRotation(final IAgent agent, final GamaPoint p) {
		if (agent == null) { return; }
	//	agent.setRotation(p);
		if(client!=null && p!=null) {
			GamaPoint loc = new GamaPoint(p.getX(), p.getY(), p.getZ());
			PropertyTopicMessage topicMessage = new PropertyTopicMessage(agent.getScope(), agent.getName(), agent.getName(), agent.getName(), "localEulerAngles", loc);
			final String stringMessage = getXStream(agent.getScope()).toXML(topicMessage);
			allContent += "\n"+stringMessage;
			DEBUG.LOG("The new message is : "+stringMessage);
			try {
				final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_PROPERTY);
				MqttMessage message = new MqttMessage();
				message.setPayload(stringMessage.getBytes());
				unityTopic.publish(message);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	@setter (UnitySkill.UNITY_SCALE)
	public void setScal(final IAgent agent, final ILocation p) {
		if (agent == null) { return; }
	//	agent.setScale(p);
	
		if(client!=null && p!=null ) {
			GamaPoint loc = new GamaPoint(p.getX(), p.getY(), p.getZ());
			PropertyTopicMessage topicMessage = new PropertyTopicMessage(agent.getScope(), agent.getName(), agent.getName(), agent.getName(), "localScale", loc);
			final String stringMessage = getXStream(agent.getScope()).toXML(topicMessage);
			allContent += "\n"+stringMessage;
			DEBUG.LOG("The new message is : "+stringMessage);
			try {
				final MqttTopic unityTopic = client.getTopic(IUnitySkill.TOPIC_PROPERTY);
				MqttMessage message = new MqttMessage();
				message.setPayload(stringMessage.getBytes());
				unityTopic.publish(message);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

*/
	

	
	
	
	
	
	
}