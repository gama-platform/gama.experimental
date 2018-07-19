package ummisco.gama.unity.mqtt;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

//import org.eclipse.paho.client.mqttv3.*;

/**
 */
public class SubscribeCallback implements MqttCallback {

	public ArrayList<MqttMessage> mailBox = new ArrayList<MqttMessage>();
  //  @Override
    public void connectionLost(Throwable cause) {
        //This is called when the connection is lost. We could reconnect here.
    }

   // @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Message arrived. Topic: " + topic + "  Message: " + message.toString());
        mailBox.add(message);
        if ("home/LWT".equals(topic)) {
            System.err.println("Sensor gone!");
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

   // @Override


@Override
public void deliveryComplete(IMqttDeliveryToken arg0) {
	// TODO Auto-generated method stub
	
}


}