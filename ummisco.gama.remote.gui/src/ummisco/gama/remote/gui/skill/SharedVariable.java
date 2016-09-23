package ummisco.gama.remote.gui.skill;

import java.util.Map;
import java.util.Observable;

import org.eclipse.paho.client.mqttv3.MqttException;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import ummisco.gama.remote.gui.connector.MQTTConnector;

public class SharedVariable  {
	public final static int EXPOSED_VARIABLE = 1;
	public final static int LISTENED_VARIABLE = 2;
	
	String attributeName;
	String exposedName;
	
	int connectionType = -1;
	Object value;
	int lastUpdate;
	MQTTConnector connection;
	IAgent agent;
	
	SharedVariable(IAgent agt, String name, String exposedName, MQTTConnector connect, int IO) throws MqttException
	{
		connection = connect; //new MQTTConnector(server,login,pass,exposedName,IO);
		this.agent = agt;
		this.attributeName = name;
		this.exposedName = exposedName;
		this.connectionType = IO;
		if(connectionType==LISTENED_VARIABLE)
			connection.subscribeToGroup(exposedName);
		this.update(agt.getScope());
	}
	
	public void update(IScope scope)
	{
		switch(connectionType)
		{
			case EXPOSED_VARIABLE : {exposeValue(); break;}
			case LISTENED_VARIABLE : {listenValue(); break;}
		}
	}
	
	public void dispose()
	{
		
	}
	
	private void exposeValue() 
	{
		try {
				if(!agent.getAttribute(attributeName).equals(value))
				{
						connection.sendMessage(exposedName, agent.getAttribute(attributeName));
				}
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void listenValue()
	{
		Object data = connection.getLastData(exposedName);
		System.out.println("update "+ data);

		if(data != null)
		{
			this.value = data;
			this.agent.setAttribute(attributeName,data);
		}
	}
	
}
