package ummisco.gama.unity.skills;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import ummisco.gama.unity.messages.GamaUnityMessage;
import ummisco.gama.unity.messages.ItemAttributes;
import ummisco.gama.unity.mqtt.SubscribeCallback;
import ummisco.gama.unity.mqtt.Utils;


/**
* @author 
@symbol(name = "StartUnity", kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false, concept = {
		IConcept.SYSTEM })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc(value = "The statement allow to move the ball.", usages = {
		@usage(value = "Move a Ball in unity scene", examples = { @example("StartUnity;") }) })
*/
@skill(name = UnitySkill.SKILL_NAME, concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
public class UnitySkill extends Skill {

  public static final String SKILL_NAME = "unity";
  public static final String BROKER_URL = "tcp://localhost:1883";
  
  public static final MqttConnectOptions options = new MqttConnectOptions();
  
  public static final String TOPIC_COLOR = "changeColor";
  public static final String TOPIC_POSITION = "changePosition";
  public static final String TOPIC_UNITY = "Unity";
  public static final String TOPIC_GAMA = "Gama";
  public static final String TOPIC_SET = "Set";
  public static final String TOPIC_GET = "Get";
  
  public static int nbr = 1;
  public static MqttClient client = null;
  public static SubscribeCallback subscribeCallback = new SubscribeCallback();
  public ArrayList<String> mailBox = new ArrayList<String>();
  
  
  
  //@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		nbr++;
		return " ";
	}
  
  
@action(name = "connectMqttClient",
			args = { @arg ( name = "idClient", type = IType.STRING, optional = false, doc = @doc ("predicate name"))},
			doc = @doc ( value = "Generates a client ID and connects it to the Mqtt server.", returns = "true if it is in the base.", examples = { @example ("") }))
  public static String connectMqttClient(final IScope scope) {
  	String clientId = Utils.getMacAddress() +"-"+ scope.getArg("idClient", IType.STRING)+"-pub";
      try {
      		client = new MqttClient(BROKER_URL, clientId);
      		options.setCleanSession(false);
              options.setWill(client.getTopic("home/LWT"), "I'm gone :(".getBytes(), 0, false);
              client.connect(options);
              System.out.println("Client : "+scope.getArg("idClient", IType.STRING)+" connected with success!");
	        } catch (MqttException e) {
	            e.printStackTrace();
	            System.exit(1);
	        }
      
      scope.getSimulation().postDisposeAction(scope1 -> {
      	try {
      		if(client.isConnected())
      			client.disconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		});
      
      
      return clientId;
  }



@action(name = "sendUnityMessage",
	args = { @arg ( name = "senderU", type = IType.STRING, optional = false, doc = @doc ("The client ID")),
			 @arg ( name = "actionU", type = IType.STRING, optional = false, doc = @doc ("The client ID")),
	         @arg ( name = "objectU", type = IType.STRING, optional = false, doc = @doc ("The client ID")),
	         @arg ( name = "attributeU", type = IType.MAP, optional = false, doc = @doc ("The client ID")),
	         @arg ( name = "valueU", type = IType.STRING, optional = false, doc = @doc ("The client ID"))
		},
	doc = @doc ( value = "Send a message to unity.", returns = "true if it is in the base.", examples = { @example ("") }))
	public static String sendMqttMessage(final IScope scope) {
		
		String sender = (String) scope.getArg("senderU", IType.STRING);
		String action = (String) scope.getArg("actionU", IType.STRING);
		String object = (String) scope.getArg("objectU", IType.STRING);
		Map<String, String> attribute = (Map<String, String>) scope.getArg("attributeU", IType.MAP);
		String value = (String) scope.getArg("valueU", IType.STRING);
		
		
		
		ArrayList<ItemAttributes>  item = new ArrayList();
		for (Map.Entry<?, ?> entry : attribute.entrySet())
		{
			ItemAttributes it = new ItemAttributes(entry.getKey(), entry.getValue()); 
		    item.add(it);
		}
		
		//GamaUnityMessage messageUnity = new GamaUnityMessage(scope, sender, "receiver", action, object, attribute, value, "content");
		GamaUnityMessage messageUnity = new GamaUnityMessage(scope, sender, "receiver", action, object, item, value, "content");
		
		
		
		XStream xstream = new XStream();
		final String stringMessage = xstream.toXML(messageUnity);
		
		System.out.println(" --->>>>   the message --> "+stringMessage);
		
	      final MqttTopic unityTopic = client.getTopic(TOPIC_UNITY);
	     // final String stringMessage = "{sender:"+sender+", action:"+action+", object:"+object+", attribute:"+attribute+", value:"+value+"}";
	      try {
	    	 // unityTopic.publish(new MqttMessage(stringMessage.getBytes()));
				
			//--------
				 MqttMessage message = new MqttMessage();
				 message.setPayload(stringMessage.getBytes());
				 unityTopic.publish(message);
			// --------
				 
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
	      System.out.println("New message sent to Unity. Topic: " + unityTopic.getName() + "   Number: " + stringMessage);
	    return "Message sent!";
	}


  @operator(value = "setUnityPosition", doc = { @doc("Sends a message to unity") }, category = { IOperatorCategory.STRING }) 
  public static String changePosition(final IScope scope, Double position) {
  	
      final MqttTopic positionTopic = client.getTopic(TOPIC_UNITY);
      final String StPosition = position + "";
      try {
			positionTopic.publish(new MqttMessage(StPosition.getBytes()));
			
		//--------
			MqttMessage message = new MqttMessage();
			message.setPayload("{foo: bar, lat: 0.23443, long: 12.3453245}".getBytes());
			positionTopic.publish(message);
		// --------
			 
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
      System.out.println("Published new position. Topic: " + positionTopic.getName() + "   Number: " + StPosition);
      return "Position sent";
  }

  private void setColor() throws MqttException {
      final MqttTopic colorTopic = client.getTopic(TOPIC_COLOR);

      final int temperatureNumber = Utils.createRandomNumberBetween(20, 30);
      final String temperature = temperatureNumber + "°C";

      colorTopic.publish(new MqttMessage(temperature.getBytes()));

      System.out.println("Published data. Topic: " + colorTopic.getName() + "  Message: " + temperature);
  }

  
  
  @action(name = "disconnectMqttClient", args = { @arg ( name = "idClient", type = IType.STRING, optional = false, doc = @doc ("predicate name"))},
			doc = @doc (value = "Disconnect the client from the Mqtt server.", returns = "true if it is in the base.", examples = { @example ("") }))
  public static String disconnectMqttClient(final IScope scope) {
  	String clientId = Utils.getMacAddress() +"-"+ scope.getArg("idClient", IType.STRING)+"-pub";
      	try {
      		if(client.isConnected())
      			client.disconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return clientId;
  }
  


 @action(name = "subscribeToTopic", args = { 
		 @arg (name = "idClient", type = IType.STRING, optional = false, doc = @doc ("Client Id")),
		 @arg (name = "topic", type = IType.STRING, optional = false, doc = @doc ("Topic Name")) },
			doc = @doc (
					value = "Subscribe a client to a topic",
					returns = "true if success, false otherwise",
					examples = { @example ("") }))
   public String SubscribeToTopic(final IScope scope) {
	 String clientId = Utils.getMacAddress() +"-"+ scope.getArg("idClient", IType.STRING)+"-pub";
	 final String topic = (String) scope.getArg("topic", IType.STRING);
	 
	 
        try {
        	client.setCallback(subscribeCallback);
        	//client.connect();
             //Subscribe to all subtopics of home
            client.subscribe(topic);
            System.out.println("Subscriber is now listening to "+topic);
          } catch (MqttException e) {
              e.printStackTrace();
              System.exit(1);
          }
        
        return "Subscribed to topic!";
  }
 
 
 @action(name = "getMqttMessage", args = { @arg ( name = "idClient", type = IType.STRING, optional = false, doc = @doc ("predicate name"))},
			doc = @doc ( value = "Get the next received mqtt message.", returns = "The message content if there is received message, null otherwise.", examples = { @example ("") }))
   public String getMqttMessage(final IScope scope) {
	 String clientId = Utils.getMacAddress() +"-"+ scope.getArg("idClient", IType.STRING)+"-pub";
	 
	 return subscribeCallback.getNextMessage();
	
  }
 
 
  
  

  
  
  

}