package ummisco.gama.remote.gui.connector;


import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttClient;

import ummisco.gama.mqtt.common.IGamaConnector;
import ummisco.gama.network.mqqt.MQTTConnector;


public class GamaSensorConnector  implements IGamaConnector {
    String boxName =  null;
    MqttClient connection = null;
   
	@Override
	public void connect(String boxName, String server) throws Exception {
		//this.connection = MQTTConnector.connectSender(server);
		this.boxName = boxName;
	}
	
/*	private UTF8Buffer registerAsBuffer(String name)
	{
		UTF8Buffer tmp = this.buffers.get(name);
		if(tmp == null )
		{
			tmp = new UTF8Buffer(destination+name);
			this.buffers.put(name, tmp);
		}
		return tmp;
	}
	*/
	public void sendData(Map<String, String >  data)
	{
	/*	if(topic == null)
			topic = new UTF8Buffer(boxName);
		//System.out.println("topic :"+topic+": ");
		 connection.publish(topic, new AsciiBuffer(SimpleMapSerializer.map2String(data)), QoS.AT_LEAST_ONCE, false);*/
	}
	
	public static void main(String [] arg) throws Exception
	{
		GamaSensorConnector tmp = new GamaSensorConnector(); 
		tmp.connect("test","localhost");
	}

	@Override
	public void connect(String sensorName) throws Exception {
		// TODO Auto-generated method stub
		
	}


}
