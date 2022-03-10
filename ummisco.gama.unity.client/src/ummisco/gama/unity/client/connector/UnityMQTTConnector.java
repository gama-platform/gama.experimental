package ummisco.gama.unity.client.connector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.agent.MinimalAgent;
import msi.gama.runtime.IScope;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.network.common.Connector;
import ummisco.gama.network.common.GamaNetworkException;
import ummisco.gama.serializer.factory.StreamConverter;
import ummisco.gama.serializer.gamaType.converters.ConverterScope;
import ummisco.gama.unity.client.data.Student;
import ummisco.gama.unity.client.skills.UnityAgent;
import ummisco.gama.unity.client.skills.UnitySerializer;
import ummisco.gama.unity.client.wox.serial.WoxSerializer;

public final class UnityMQTTConnector extends Connector {
	public final static String SERVER_URL = "SERVER_URL";
	public final static String SERVER_PORT = "SERVER_PORT";
	public final static String LOCAL_NAME = "LOCAL_NAME";
	public final static String LOGIN = "LOGIN";
	public final static String PASSWORD = "PASSWORD";

	public static String DEFAULT_USER = "admin";
	public static String DEFAULT_LOCAL_NAME = "gama-ui" + Calendar.getInstance().getTimeInMillis() + "@";
	public static String DEFAULT_PASSWORD = "password";
	public static String DEFAULT_HOST = "localhost";
	public static String DEFAULT_PORT = "1883";

	protected MqttClient sendConnection = null;
	
	public ArrayList<MqttMessage> mailBox = new ArrayList<MqttMessage>();
	public ArrayList<String> filteredTopics = new ArrayList<String>();
	Map<String, Object> receivedData = new HashMap<>();
	

	public UnityMQTTConnector(final String server, final String port, final String userName, final String password) throws MqttException {
		this.connectToServer(server, port, userName, password);
		receivedData = new HashMap<>();
	}
	
	static {
		DEBUG.ON();
	}

	class SubscribeCallback implements MqttCallback {
		
		// @Override
		public void connectionLost(Throwable cause) {
			DEBUG.OUT("connection lost");
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			DEBUG.OUT("message sent");
		}	
			
		// @Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			storeMessage(topic, message);
			DEBUG.LOG("Message arrived and saved. Topic: " + topic + "  Message: " + message.toString());
		}
			
	}
	

	private synchronized Object storeDataS(final String topic, final Object dts) {
		if (dts == null) {
			final Object tmp = this.receivedData.get(topic);
			this.receivedData.remove(topic);
			return tmp;

		}
		this.receivedData.remove(topic);
		this.receivedData.put(topic, dts);
		return dts;
	}

	private void storeData(final String topic, final String message) {
		DEBUG.OUT(" (Do modification here) New message arived on topic "+topic + " Its content is: "+message);
		
		
		final XStream dataStreamer = new XStream(new DomDriver());
		final Object data = dataStreamer.fromXML(message);
		
		storeDataS(topic, data);
	}

	public void releaseConnection() throws MqttException {
		sendConnection.disconnect();
		sendConnection = null;
	}

	public void sendMessage(final String dest, final Object data) throws MqttException {
		final XStream dataStreamer = new XStream(new DomDriver());
		final String dataS = dataStreamer.toXML(data);
		this.sendFormatedMessage(dest, dataS);
	}
	
	public void sendMessage(final String dest, final String data) throws MqttException {
		this.sendFormatedMessage(dest, data);
	}
	
	public void sendMessage(final IScope scope, final String topic, final String sender, final String objectName, final Object content) throws MqttException {
		final String message = buildMessage(scope, sender, objectName, content);
		System.out.println(" Message to send is : -------> "+message);
		sendMessage( topic, message);
	}
	
	private String buildMessage(final IScope scope, final String sender, final String objectName, final Object content) {
		if(content instanceof MinimalAgent ){
			final MinimalAgent mAgent = (MinimalAgent) content; //scope.getArg("content", IType.NONE);
			System.out.println("The agent is : "+mAgent);
			final UnityAgent UAgent = new UnityAgent();
			UAgent.getUnityAgent(mAgent);
			GamaMessage gameMessage = new GamaMessage(scope, sender, objectName, UAgent);
			
			// ------------------------------
			Student st = Student.getNewStudent();
			
			System.out.println(" /////////////////////////////////////////////// ");
			
			WoxSerializer  wSerializer = new WoxSerializer();
			
		
			String serializedContent = wSerializer.getSerializedToString(st);
					
			// String woxSerializedObject =  wSerializer.getSerializedToString(gameMessage);
			
			System.out.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX \n " + serializedContent 
					+ " \n XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ");
			
			return serializeMessage(scope, gameMessage);
		}else {
			final XStream dataStreamer = new XStream(new DomDriver());
			final String dataS = dataStreamer.toXML(content);
			return dataS;
		}
	}
	
	public String serializeMessage(final IScope scope, final Object message) {
		final UnitySerializer unitySerializer = new UnitySerializer();
		unitySerializer.SetSerializer(getXStream(scope));
		return unitySerializer.agentShapeToXML(message);
	}
	
	public static XStream getXStream(final IScope scope) {
		final ConverterScope cScope = new ConverterScope(scope);
		final XStream xstream = StreamConverter.loadAndBuild(cScope);
		return xstream;
	}

	private void sendFormatedMessage(final String receiver, final String content) throws MqttException {
		final MqttMessage mm = new MqttMessage(content.getBytes());
		DEBUG.LOG("Send the message to topic "+receiver);
		sendConnection.publish(receiver, mm);
		
	}

	public void subscribeToGroup(final String boxName) throws MqttException {
		sendConnection.subscribe(boxName);
	}

	public void unsubscribeGroup(final String boxName) throws MqttException {
		sendConnection.unsubscribe(boxName);
	}

	protected void connectToServer(final String originalServer, final String originalPort,
			final String originalUserName, final String originalPassword) throws MqttException {
		if (sendConnection == null) {
			final String server = originalServer == null ? DEFAULT_HOST : originalServer;
			final String port = originalPort == null ? DEFAULT_PORT : originalPort;
			final String userName = originalUserName == null ? DEFAULT_USER : originalUserName;
			final String password = originalPassword == null ? DEFAULT_PASSWORD : originalPassword;
			final String localName = DEFAULT_LOCAL_NAME + server;
			sendConnection = new MqttClient("tcp://" + server + ":" + port, localName, new MemoryPersistence());
			final MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			
			//sendConnection.setCallback(new Callback());
			sendConnection.setCallback(new SubscribeCallback());
			
			connOpts.setCleanSession(true);
			connOpts.setKeepAliveInterval(30);
			connOpts.setUserName(userName);
			connOpts.setPassword(password.toCharArray());
			sendConnection.connect(connOpts);
		}
		DEBUG.OUT("Connected");
	}

		
	public MqttClient getClient() {
		return sendConnection;
	}
	
	private synchronized void storeMessage(String topic, MqttMessage msg) {
		/*
		if(!filteredTopics.contains(topic)) {
		 if (receivedData.containsKey(topic)) {
			 	ArrayList<MqttMessage> box = (ArrayList<MqttMessage>) receivedData.get(topic);
			 	box.add(msg);
	        } else {
	        	ArrayList<MqttMessage> newBox = new ArrayList<MqttMessage>();
	        	newBox.add(msg);
	        	receivedData.put(topic, newBox);
	        }
		}else {
			mailBox.add(msg);
		}
		*/
		mailBox.add(msg);
	}

	public String getNextMessage() {
		if (mailBox.size() > 0) {
			String msg = mailBox.get(0).toString();
			mailBox.remove(0);
			return msg;
		} else {
			return null;
		}
	}
	
	public String getNextMessage(String topic) {
		 if (receivedData.containsKey(topic)) {
			 ArrayList<MqttMessage> box = (ArrayList<MqttMessage>) receivedData.get(topic);
			if (box.size() > 0) {
				String msg = box.get(box.size()-1).toString();
				box.remove(box.size()-1);
				DEBUG.LOG(topic+" Mail Box size is: " + box.size());
				return msg;
			} 
		}
		 return null;
	}
	
	public boolean hasNextMessage() {
		 if (mailBox.size() > 0) {
			return true;
		}
		 return false;
	}
	
	public boolean hasNextMessage(String topic) {
		 if (receivedData.containsKey(topic)) {
			 ArrayList<MqttMessage> box = (ArrayList<MqttMessage>) receivedData.get(topic);
			if (box.size() > 0) {
				return true;
			} 
		}
		 return false;
	}

	public void clearAllMessages() {
		mailBox.clear();
	}
	
	public void clearAllMessages(String topic) {
		 if (receivedData.containsKey(topic)) {
			 ((ArrayList<MqttMessage>) receivedData.get(topic)).clear();
		 }
	}

	@Override
	protected void connectToServer(IAgent agent) throws GamaNetworkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isAlive(IAgent agent) throws GamaNetworkException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void subscribeToGroup(IAgent agt, String boxName) throws GamaNetworkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void unsubscribeGroup(IAgent agt, String boxName) throws GamaNetworkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void releaseConnection(IScope scope) throws GamaNetworkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sendMessage(IAgent sender, String receiver, String content) throws GamaNetworkException {
		// TODO Auto-generated method stub
		
	}	

}
	


