package ummisco.gama.remote.gui.connector;

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

import ummisco.gama.remote.gui.skill.SharedVariable;
import ummisco.gama.serializer.factory.StreamConverter;



public final class MQTTConnector {
	public final static String SERVER_URL = "SERVER_URL";
	public final static String SERVER_PORT = "SERVER_PORT";
	public final static String LOCAL_NAME = "LOCAL_NAME";
	public final static String LOGIN = "LOGIN";
	public final static String PASSWORD = "PASSWORD";

	public static String DEFAULT_USER = "admin";
	public static String DEFAULT_LOCAL_NAME = "gama-ui"+Calendar.getInstance().getTimeInMillis()+"@";
	public static String DEFAULT_PASSWORD = "password";
	public static String DEFAULT_HOST =  "localhost";
	public static String DEFAULT_PORT =  "1883";
	
	protected MqttClient sendConnection = null;
	Map<String, Object> receivedData ;
	
	public MQTTConnector(String server,  String userName, String password) throws MqttException
	{	
		this.connectToServer(server, null, userName, password);
		receivedData = new HashMap<String, Object>();
	}
	
	class Callback implements MqttCallback
	{
		@Override
		public void connectionLost(Throwable arg0)  {
			//throw new MqttException(arg0);
			System.out.println("connection lost");
		}
		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			System.out.println("message sended");
		}
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			String body = message.toString();
			storeData(topic,body);
		}
	}
	
	public Object  getLastData(String topic)
	{
		Object  data = storeDataS(topic,null);
		return data;
	}
	
	private synchronized Object storeDataS(String topic, Object dts)
	{
		if(dts == null) {
			Object tmp = this.receivedData.get(topic);
			this.receivedData.remove(topic);
			return tmp;
			
		}
		this.receivedData.remove(topic);
		this.receivedData.put(topic, dts);
		return dts;
	}
	
	private final void storeData(String topic, String message)
	{
		XStream dataStreamer = new XStream(new DomDriver());
		Object data = (Object)dataStreamer.fromXML(message);
		storeDataS(topic, data);
	}

	public final void releaseConnection() throws MqttException{
			sendConnection.disconnect();
			sendConnection = null;
	}

	public final void sendMessage(String dest, Object data ) throws MqttException
	{
		XStream dataStreamer = new XStream(new DomDriver());
		String dataS = dataStreamer.toXML(data);
		this.sendFormatedMessage(dest, dataS);
	}
	
	private final void sendFormatedMessage( String receiver, String content) throws MqttException {
			MqttMessage mm = new MqttMessage(content.getBytes());
			sendConnection.publish(receiver, mm);
	}

	public void subscribeToGroup(String boxName)  throws MqttException {
			sendConnection.subscribe(boxName);
	}
	
	public void unsubscribeGroup(String boxName) throws MqttException   {
			sendConnection.unsubscribe(boxName);
		}

	protected void connectToServer(String server, String port, String userName, String password) throws MqttException {
		if(sendConnection == null) {
			server = (server==null?DEFAULT_HOST:server);
			port = (port==null?DEFAULT_PORT:port);
			userName = (userName==null?DEFAULT_USER:userName);
			password = (password==null?DEFAULT_PASSWORD:userName);
			String localName = DEFAULT_LOCAL_NAME+server;
			sendConnection = new MqttClient("tcp://"+server+":"+port, localName, new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
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
