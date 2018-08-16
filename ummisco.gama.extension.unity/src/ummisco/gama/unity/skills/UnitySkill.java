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

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.unity.messages.ColorTopicMessage;
import ummisco.gama.unity.messages.CreateTopicMessage;
import ummisco.gama.unity.messages.GamaUnityMessage;
import ummisco.gama.unity.messages.GetTopicMessage;
import ummisco.gama.unity.messages.ItemAttributes;
import ummisco.gama.unity.messages.MonoActionTopicMessage;
import ummisco.gama.unity.messages.MoveTopicMessage;
import ummisco.gama.unity.messages.NotificationTopicMessage;
import ummisco.gama.unity.messages.PluralActionTopicMessage;
import ummisco.gama.unity.messages.PositionTopicMessage;
import ummisco.gama.unity.messages.PropertyTopicMessage;
import ummisco.gama.unity.messages.SetTopicMessage;
import ummisco.gama.unity.mqtt.SubscribeCallback;
import ummisco.gama.unity.mqtt.Utils;

/**
 * @author
 * @symbol(name = "StartUnity", kind = ISymbolKind.SINGLE_STATEMENT,
 *              with_sequence = false, concept = { IConcept.SYSTEM })
 * @inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT,
 *               ISymbolKind.LAYER })
 * @doc(value = "The statement allow to move the ball.", usages = {
 * @usage(value = "Move a Ball in unity scene", examples =
 *              { @example("StartUnity;") }) })
 */
@skill(name = UnitySkill.SKILL_NAME, concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
public class UnitySkill extends Skill {

	public static final String SKILL_NAME = "unity";
	public static final String BROKER_URL = "tcp://localhost:1883";

	public static final MqttConnectOptions options = new MqttConnectOptions();

	public static MqttClient client = null;
	public static SubscribeCallback subscribeCallback = new SubscribeCallback();
	public ArrayList<String> mailBox = new ArrayList<String>();

	// @Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		return " ";
	}

	@action(name = "connectMqttClient", args = {
			@arg(name = "idClient", type = IType.STRING, optional = false, doc = @doc("predicate name")) }, doc = @doc(value = "Generates a client ID and connects it to the Mqtt server.", returns = "true if it is in the base.", examples = {
					@example("") }))
	public static String connectMqttClient(final IScope scope) {
		String clientId = Utils.getMacAddress() + "-" + scope.getArg("idClient", IType.STRING) + "-pub";
		try {
			client = new MqttClient(BROKER_URL, clientId);
			options.setCleanSession(false);
			options.setWill(client.getTopic("home/LWT"), "I'm gone :(".getBytes(), 0, false);
			client.connect(options);
			DEBUG.LOG("Client : " + scope.getArg("idClient", IType.STRING) + " connected with success!");
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}

		scope.getSimulation().postDisposeAction(scope1 -> {
			try {
				if (client.isConnected())
					client.disconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		});

		return clientId;
	}
	
	//TODO 
	// add the actions bellow: 
	// ------------------------
	// setUnityFields --> Done
	// getUnityField --> Done
	// setUnityColor --> Done
	// setUnityPosition --> Done
	// callUnityPluralAction --> Done
	// callUnityMonoAction --> Done
	// setUnityProperty --> 
	// unityMove -->  Done

	@action(name = "send_unity_message", args = {
			@arg(name = "senderU", type = IType.STRING, optional = false, doc = @doc("The sender")),
			@arg(name = "actionU", type = IType.STRING, optional = false, doc = @doc("The method name on unity game object")),
			@arg(name = "objectU", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributeU", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")),
			@arg(name = "topicU", type = IType.STRING, optional = false, doc = @doc("The topic")) }, doc = @doc(value = "Send a message to unity.", returns = "true if it is in the base.", examples = {
					@example("") }))
	public static String sendUnityMqttMessage(final IScope scope) {

		String sender = (String) scope.getArg("senderU", IType.STRING);
		String action = (String) scope.getArg("actionU", IType.STRING);
		String object = (String) scope.getArg("objectU", IType.STRING);
		Map<String, String> attribute = (Map<String, String>) scope.getArg("attributeU", IType.MAP);
		String topic = (String) scope.getArg("topicU", IType.STRING);

		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : attribute.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}

		// GamaUnityMessage messageUnity = new GamaUnityMessage(scope, sender,
		// "receiver", action, object, attribute, value, "content");
		GamaUnityMessage messageUnity = new GamaUnityMessage(scope, sender, "receiver", action, object, items, topic,
				"content");

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(messageUnity);

		DEBUG.LOG(" --->>>>   the message --> " + stringMessage);

		final MqttTopic unityTopic = client.getTopic(topic);
		// final String stringMessage = "{sender:"+sender+", action:"+action+",
		// object:"+object+", attribute:"+attribute+", value:"+value+"}";
		try {
			// unityTopic.publish(new MqttMessage(stringMessage.getBytes()));

			// --------
			MqttMessage message = new MqttMessage();
			message.setPayload(stringMessage.getBytes());
			unityTopic.publish(message);
			// --------

		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		DEBUG.LOG("New message sent to Unity. Topic: " + unityTopic.getName() + "   Number: " + stringMessage);
		return "Message sent!";
	}

	@action(name = "getUnityField", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attribute", type = IType.STRING, optional = false, doc = @doc("The field name")),
			}, doc = @doc(value = "Get a unity game object field value", returns = "void", examples = {
					@example("") }))
	public static synchronized void getUnityField(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String attribute = (String) scope.getArg("attribute", IType.STRING);

		GetTopicMessage topicMessage = new GetTopicMessage(scope, sender, receiver, objectName, attribute);
		
		

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to Get Topic is : ");
		DEBUG.LOG(stringMessage);

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

	@action(name = "setUnityFields", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributes", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")) }, 
			doc = @doc(value = "Set a set of fields of a unity game object.", returns = "void", examples = {
					@example("") }))
	public static synchronized void setUnityField(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		Map<String, String> attributes = (Map<String, String>) scope.getArg("attributes", IType.MAP);

		
		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : attributes.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}

		SetTopicMessage setMessage = new SetTopicMessage(scope, sender, receiver, objectName, items);

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(setMessage);
		
		DEBUG.LOG("Message to send to set Topic is : ");
		DEBUG.LOG(stringMessage);

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
	
	
	@action(name = "setUnityProperty", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "propertyName", type = IType.STRING, optional = false, doc = @doc("The property name")) , 
			@arg(name = "propertyValue", type = IType.STRING, optional = false, doc = @doc("The property value")) },
			doc = @doc(value = "Set a property value.", returns = "void", examples = {
					@example("") }))
	public static synchronized void setUnityProperty(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String propertyName = (String) scope.getArg("propertyName", IType.STRING);
		String propertyValue = (String) scope.getArg("propertyValue", IType.STRING);

		PropertyTopicMessage topicMessage = new PropertyTopicMessage(scope, sender, receiver, objectName, propertyName, propertyValue);

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to set Topic is : ");
		DEBUG.LOG(stringMessage);

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

	
	@action(name = "callUnityMonoAction", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "actionName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attribute", type = IType.STRING, optional = false, doc = @doc("The attribute list and their values")) }, 
			doc = @doc(value = "Call a unity game object method that has one parameter", returns = "void", examples = {
					@example("") }))
	public static synchronized void callUnityMonoAction(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String actionName = (String) scope.getArg("actionName", IType.STRING);
		String attribute = (String) scope.getArg("attribute", IType.STRING);

		
		

		MonoActionTopicMessage topicMessage = new MonoActionTopicMessage(scope, sender, receiver, objectName, actionName, attribute);

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to MonoActionTopicMessage Topic is : ");
		DEBUG.LOG(stringMessage);

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
	
	
	
	@action(name = "callUnityPluralAction", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "actionName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "attributes", type = IType.MAP, optional = false, doc = @doc("The attribute list and their values")) }, 
			doc = @doc(value = "Call a unity game object method that has several parameters.", returns = "void", examples = {
					@example("") }))
	public static synchronized void callUnityPluralAction(final IScope scope) {

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

		PluralActionTopicMessage topicMessage = new PluralActionTopicMessage(scope, sender, receiver, objectName, actionName, items);

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to PluralActionTopicMessage Topic is : ");
		DEBUG.LOG(stringMessage);

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
	
	
	@action(name = "setUnityColor", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "color", type = IType.STRING, optional = false, doc = @doc("The color name")),
			}, doc = @doc(value = "Set a unity game object color", returns = "void", examples = {
					@example("") }))
	public static synchronized void setUnityColor(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String color = (String) scope.getArg("color", IType.STRING);

		ColorTopicMessage topicMessage = new ColorTopicMessage(scope, sender, receiver, objectName, color);
		
		

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to Get Topic is : ");
		DEBUG.LOG(stringMessage);

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
	
	

	@action(name = "setUnityPosition", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "position", type = IType.MAP, optional = false, doc = @doc("The position values (x,y,z)")),
			}, doc = @doc(value = "Set the position of a unity game object", returns = "void", examples = {
					@example("") }))
	public static synchronized void setUnityPosition(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		Map<String, String> position = (Map<String, String>) scope.getArg("position", IType.MAP);
		
		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : position.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}

		PositionTopicMessage topicMessage = new PositionTopicMessage(scope, sender, receiver, objectName, items);

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);

		DEBUG.LOG(" --->>>>   the message --> " + stringMessage);

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
		DEBUG.LOG("New message sent to Unity. Topic: " + unityTopic.getName() + "   Number: " + stringMessage);
	}

	
	
	
	@action(name = "unityMove", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "position", type = IType.MAP, optional = false, doc = @doc("The position values (x,y,z)")),
			}, doc = @doc(value = "Set the position of a unity game object", returns = "void", examples = {
					@example("") }))
	public static synchronized void unityMove(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		Map<String, String> position = (Map<String, String>) scope.getArg("position", IType.MAP);
		
		ArrayList<ItemAttributes> items = new ArrayList();
		for (Map.Entry<?, ?> entry : position.entrySet()) {
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue());
			items.add(it);
		}

		MoveTopicMessage topicMessage = new MoveTopicMessage(scope, sender, receiver, objectName, items);

		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);

		DEBUG.LOG(" --->>>>   the message --> " + stringMessage);

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
	
	
	
	
	@action(name = "newUnityObject", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "type", type = IType.STRING, optional = false, doc = @doc("The object type")),
			@arg(name = "color", type = IType.STRING, optional = false, doc = @doc("The object color")),
			@arg(name = "position", type = IType.STRING, optional = false, doc = @doc("The object position")),
			}, doc = @doc(value = "Create a new unity game object on the scene and set its initial color and position", returns = "void", examples = {
					@example("") }))
	public static synchronized void newUnityObject(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String type = (String) scope.getArg("type", IType.STRING);
		String color = (String) scope.getArg("color", IType.STRING);
		String position = (String) scope.getArg("position", IType.STRING);

		CreateTopicMessage topicMessage = new CreateTopicMessage(scope, sender, receiver, objectName, type, color, position);
		
		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to create object topic Topic is : ");
		DEBUG.LOG(stringMessage);

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
	
	
	@action(name = "unityNotificationSubscribe", args = {
			@arg(name = "objectName", type = IType.STRING, optional = false, doc = @doc("The game object name")),
			@arg(name = "notificationId", type = IType.STRING, optional = false, doc = @doc("notificationId: the notification ID to communicate when notifying an agent by unity")),
			@arg(name = "fieldType", type = IType.STRING, optional = false, doc = @doc("fieldType: a field or a property in the game object")),
			@arg(name = "fieldName", type = IType.STRING, optional = false, doc = @doc("fieldName: The field name")),
			@arg(name = "fieldValue", type = IType.STRING, optional = false, doc = @doc("fieldValue: The field value")),
			@arg(name = "fieldOperator", type = IType.STRING, optional = false, doc = @doc("fieldOperator: The comparaison operator")),
			}, doc = @doc(value = "Subscribe to the notification mechanism, allowing unity to notify Gama when the condition on the specified field has been met.", returns = "void", examples = {
					@example("") }))
	public static synchronized void unityNotificationSubscribe(final IScope scope) {

		String sender = (String) scope.getAgent().getName();
		String notificationId = (String) scope.getArg("notificationId", IType.STRING);
		String receiver = (String) scope.getArg("objectName", IType.STRING);
		String objectName = (String) scope.getArg("objectName", IType.STRING);
		String fieldType = (String) scope.getArg("fieldType", IType.STRING);
		String fieldName = (String) scope.getArg("fieldName", IType.STRING);
		String fieldValue = (String) scope.getArg("fieldValue", IType.STRING);
		String fieldOperator = (String) scope.getArg("fieldOperator", IType.STRING);

		NotificationTopicMessage topicMessage = new NotificationTopicMessage(scope, sender, receiver, notificationId, objectName, fieldType, fieldName, fieldValue, fieldOperator);
		
		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(topicMessage);
		
		DEBUG.LOG("Message to send to create object topic Topic is : ");
		DEBUG.LOG(stringMessage);

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

	
	
	
	
	
	private void setColor() throws MqttException {
		final MqttTopic colorTopic = client.getTopic(IUnitySkill.TOPIC_COLOR);

		final int temperatureNumber = Utils.createRandomNumberBetween(20, 30);
		final String temperature = temperatureNumber + "°C";

		colorTopic.publish(new MqttMessage(temperature.getBytes()));

		DEBUG.LOG("Published data. Topic: " + colorTopic.getName() + "  Message: " + temperature);
	}

	
	
	@action(name = "disconnectMqttClient", args = {
			@arg(name = "idClient", type = IType.STRING, optional = false, doc = @doc("predicate name")) }, doc = @doc(value = "Disconnect the client from the Mqtt server.", returns = "true if it is in the base.", examples = {
					@example("") }))
	public static String disconnectMqttClient(final IScope scope) {
		String clientId = Utils.getMacAddress() + "-" + scope.getArg("idClient", IType.STRING) + "-pub";
		try {
			if (client.isConnected())
				client.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return clientId;
	}

	@action(name = "subscribe_To_Topic", args = {
			@arg(name = "idClient", type = IType.STRING, optional = false, doc = @doc("Client Id")),
			@arg(name = "topic", type = IType.STRING, optional = false, doc = @doc("Topic Name")) }, doc = @doc(value = "Subscribe a client to a topic", returns = "true if success, false otherwise", examples = {
					@example("") }))
	public String SubscribeToTopic(final IScope scope) {
		String clientId = Utils.getMacAddress() + "-" + scope.getArg("idClient", IType.STRING) + "-pub";
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

		return "Subscribed to the topic: " +topic;
	}

	@action(name = "get_unity_message", doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public String getUnityMessage(final IScope scope) {
		return subscribeCallback.getNextMessage();

	}

	@action(name = "get_unity_replay", doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public String getReplayUnityMessage(final IScope scope) {
		return subscribeCallback.getNextReplayMessage();
	}

	@action(name = "get_unity_notification", doc = @doc(value = "Get the next received mqtt message.", returns = "The message content if there is a received message, null otherwise.", examples = {
			@example("") }))
	public String getUnityNotificationMessage(final IScope scope) {
		return subscribeCallback.getNextNotificationMessage();
	}

}