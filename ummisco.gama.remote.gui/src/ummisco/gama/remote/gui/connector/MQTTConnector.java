package ummisco.gama.remote.gui.connector;

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


public final class MQTTConnector {
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
	Map<String, Object> receivedData;

	public MQTTConnector(final String server, final String userName, final String password) throws MqttException {
		this.connectToServer(server, null, userName, password);
		receivedData = new HashMap<>();
	}

	class Callback implements MqttCallback {
		@Override
		public void connectionLost(final Throwable arg0) {
			// throw new MqttException(arg0);
			System.out.println("connection lost");
		}

		@Override
		public void deliveryComplete(final IMqttDeliveryToken arg0) {
			System.out.println("message sended");
		}

		@Override
		public void messageArrived(final String topic, final MqttMessage message) throws Exception {
			final String body = message.toString();
			storeData(topic, body);
		}
	}

	public Object getLastData(final String topic) {
		final Object data = storeDataS(topic, null);
		return data;
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
	
	class Data{
			public Object name;
			public Object value;
	}
	
	private void storeData(final String topic, final String message) {
		//won't work but will compile
//		final XStream dataStreamer = new XStream(new DomDriver());
//		final Object data = dataStreamer.fromXML(message);
//	
//		System.out.println(" Received message is : "+ message);
//		System.out.println(" Received Data is : "+ data);
//	
//		storeDataS(topic, data);
	}

	public void releaseConnection() throws MqttException {
		sendConnection.disconnect();
		sendConnection = null;
	}

	public void sendMessage(final String dest, final Object data) throws MqttException {
		//won't work but will compile
//		final XStream dataStreamer = new XStream(new DomDriver());
//		final String dataS = dataStreamer.toXML(data);
//		this.sendFormatedMessage(dest, dataS);
	}

	private void sendFormatedMessage(final String receiver, final String content) throws MqttException {
		final MqttMessage mm = new MqttMessage(content.getBytes());
		sendConnection.publish(receiver, mm);
		System.out.println(" ---> Content is : "+content);
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
			sendConnection.setCallback(new Callback());
			connOpts.setCleanSession(true);
			connOpts.setKeepAliveInterval(30);
			connOpts.setUserName(userName);
			connOpts.setPassword(password.toCharArray());
			sendConnection.connect(connOpts);

		}
	}
}
