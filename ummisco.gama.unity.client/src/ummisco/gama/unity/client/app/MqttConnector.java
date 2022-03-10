package ummisco.gama.unity.client.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MqttConnector implements Callable<Void> {

	IMqttClient client;

	public ArrayList<MqttMessage> mailBox = new ArrayList<MqttMessage>();
	public ArrayList<String> filteredTopics = new ArrayList<String>();
	Map<String, Object> receivedData = new HashMap<String, Object>();

	public MqttConnector() throws MqttException {
		this.client = getNewClient("tcp://localhost", 1883);

	}

	public MqttConnector(String url, int port) throws MqttException {
		this.client = getNewClient(url, port);
	}

	class SubscribeCallback implements MqttCallback {

		// @Override
		public void connectionLost(Throwable cause) {
			System.out.println("connection lost");
		}

		public void deliveryComplete(IMqttDeliveryToken arg0) {
			System.out.println("message sent");
		}

		// @Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			storeMessage(topic, message);
			System.out.println("Message arrived and saved. Topic: " + topic + "  Message: " + message.toString());
		}

	}

	IMqttClient getClient() {
		return this.client;
	}

	public IMqttClient getNewClient(String url, int port) throws MqttException {
		String clientId = UUID.randomUUID().toString();
		String urlPort = url + ":" + port;
		IMqttClient client = new MqttClient(urlPort, clientId);
		client.setCallback(new SubscribeCallback());
		return client;
	}

	public boolean connectClient() throws MqttSecurityException, MqttException {
		MqttConnectOptions options = new MqttConnectOptions();
	//	((Object) options).setAutomaticReconnect(true);
		options.setCleanSession(true);
		options.setConnectionTimeout(10);
		client.connect(options);
		if (client.isConnected())
			return true;

		return false;
	}

	public boolean publish(String topic, String content) throws Exception {
		return sendMessage(topic, content);
	}

	public boolean sendMessage(String topic, String content) throws Exception {
		if (!client.isConnected()) {
			return false;
		}
		MqttMessage msg = createMessage(content);
		msg.setQos(0);
		msg.setRetained(true);
		client.publish(topic, msg);
		return true;
	}

	private MqttMessage createMessage(String content) {
		byte[] msg = String.format(content).getBytes();
		return new MqttMessage(msg);
	}

	private void checkReceivedMessage() {

	}

	public void subscribeToTopic(String boxName) throws MqttException {
		client.subscribe(boxName);
	}

	public Void call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private synchronized void storeMessage(String topic, MqttMessage msg) {
		mailBox.add(msg);
	}

	public boolean hasNextMessage() {
		if (mailBox.size() > 0) {
			return true;
		}
		return false;
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

	public void clearAllMessages() {
		mailBox.clear();
	}

	public void clearAllMessages(String topic) {
		if (receivedData.containsKey(topic)) {
			((ArrayList<MqttMessage>) receivedData.get(topic)).clear();
		}
	}

}
