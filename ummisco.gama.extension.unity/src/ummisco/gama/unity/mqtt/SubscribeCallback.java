package ummisco.gama.unity.mqtt;

import java.util.ArrayList;

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
	public ArrayList<MqttMessage> replayMailBox = new ArrayList<MqttMessage>();
	public ArrayList<MqttMessage> notificationMailBox = new ArrayList<MqttMessage>();
	public ArrayList<MqttMessage> createdMailBox = new ArrayList<MqttMessage>();

	// @Override
	public void connectionLost(Throwable cause) {
		// This is called when the connection is lost. We could reconnect here.
	}

	static {
		DEBUG.OFF();
	}

	// @Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		DEBUG.LOG("Message arrived. Topic: " + topic + "  Message: " + message.toString());
		if (topic.equals(IUnitySkill.TOPIC_REPLAY)) {
			replayMailBox.add(message);
			DEBUG.LOG("A replay has been recevied");
		} else if (topic.equals(IUnitySkill.TOPIC_NOTIFICATION_RECEIVED)) {
			notificationMailBox.add(message);
			DEBUG.LOG("A notification has been recevied");
		} else if (topic.equals(IUnitySkill.TOPIC_CREATED_AGENT)) {
			createdMailBox.add(message);
			DEBUG.LOG("A created agent message has been recevied");
		}else {
			mailBox.add(message);
			DEBUG.LOG("Unknown message has been recevied");
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

	public String getNextReplayMessage() {
		if (replayMailBox.size() > 0) {
			String msg = replayMailBox.get(0).toString();
			replayMailBox.remove(0);
			DEBUG.LOG("replayMailBox size is: " + replayMailBox.size());
			DEBUG.LOG("And message is : " + msg);
			return msg;
		} else {
			return null;
		}
	}

	public String getNextNotificationMessage() {
		if (notificationMailBox.size() > 0) {
			String msg = notificationMailBox.get(0).toString();
			notificationMailBox.remove(0);
			return msg;
		} else {
			return null;
		}
	}

	// @Override

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

}