package ummisco.gama.unity.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.unity.skills.IUnitySkill;

//import org.eclipse.paho.client.mqttv3.*;

/**
 */

public class SubscribeCallback implements MqttCallback {

	public ArrayList<MqttMessage> mailBox = new ArrayList<MqttMessage>();
	public ArrayList<String> filteredTopics = new ArrayList<String>();
	Map<String, Object> receivedData = new HashMap<>();
		
	static {
		DEBUG.ON();
	}
	
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
	
	
	private void storeMessage(String topic, MqttMessage msg) {
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

	public void clearAllMessages() {
		mailBox.clear();
	}
	
	public void clearAllMessages(String topic) {
		 if (receivedData.containsKey(topic)) {
			 ((ArrayList<MqttMessage>) receivedData.get(topic)).clear();
		 }
	}


	

}