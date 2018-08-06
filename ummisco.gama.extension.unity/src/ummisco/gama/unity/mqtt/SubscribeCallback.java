package ummisco.gama.unity.mqtt;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import ummisco.gama.unity.skills.IUnitySkill;

//import org.eclipse.paho.client.mqttv3.*;

/**
 */
public class SubscribeCallback implements MqttCallback {

	public ArrayList<MqttMessage> mailBox = new ArrayList<MqttMessage>();
	public ArrayList<MqttMessage> replayMailBox = new ArrayList<MqttMessage>();
	public ArrayList<MqttMessage> notificationiMailBox = new ArrayList<MqttMessage>();
  //  @Override
    public void connectionLost(Throwable cause) {
        //This is called when the connection is lost. We could reconnect here.
    }

   // @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Message arrived. Topic: " + topic + "  Message: " + message.toString());
        if(topic.equals(IUnitySkill.TOPIC_REPLAY)) {
        	replayMailBox.add(message);
            System.out.println("A replay has been recevied");
        }else 
	        if(topic.equals(IUnitySkill.TOPIC_NOTIFICATION)){
	        	notificationiMailBox.add(message);
	        	 System.out.println("A notification has been recevied");
	        }else {
	        	mailBox.add(message);
	        	System.out.println("Unknown message has been recevied");
	        }
       
    }
    
    public String getNextMessage(){
    	if(mailBox.size() > 0) {
    		String msg = mailBox.get(0).toString();
    		mailBox.remove(0);
    		return msg;
    	}else {
    		return null;
    	}
    }
    
    public String getNextReplayMessage(){
    	if(replayMailBox.size() > 0) {
    		String msg = replayMailBox.get(0).toString();
    		replayMailBox.remove(0);
    		return msg;
    	}else {
    		return null;
    	}
    }
    
    public String getNextNotificationMessage(){
    	if(notificationiMailBox.size() > 0) {
    		String msg = notificationiMailBox.get(0).toString();
    		notificationiMailBox.remove(0);
    		return msg;
    	}else {
    		return null;
    	}
    }

   // @Override


@Override
public void deliveryComplete(IMqttDeliveryToken arg0) {
	// TODO Auto-generated method stub
	
}


}