package ummisco.gama.remote.gui.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.eclipse.paho.client.mqttv3.MqttException;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import ummisco.gama.remote.gui.connector.MQTTConnector;

public class SharedVariable  {
	public final static int EXPOSED_VARIABLE = 1;
	public final static int LISTENED_VARIABLE = 2;
	
	ArrayList<String> attributeName;
	String exposedName;
	
	int connectionType = -1;
	Object value;
	int lastUpdate;
	MQTTConnector connection;
	IAgent agent;
	
	
	SharedVariable(IAgent agt, String names, String exposedName, MQTTConnector connect, int IO) throws MqttException
	{
		connection = connect; //new MQTTConnector(server,login,pass,exposedName,IO);
		this.agent = agt;
		this.attributeName = new ArrayList<String>();
		this.attributeName.add(names);
		this.exposedName = exposedName;
		this.connectionType = IO;
		if(connectionType==LISTENED_VARIABLE)
			connection.subscribeToGroup(exposedName);
		this.update(agt.getScope());
	}
	
	
	SharedVariable(IAgent agt, ArrayList<String> names, String exposedName, MQTTConnector connect, int IO) throws MqttException
	{
		connection = connect; //new MQTTConnector(server,login,pass,exposedName,IO);
		this.agent = agt;
		this.attributeName = names;
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
	
	private boolean attributeChanged()
	{
		for(String cn:this.attributeName)
		{
			if(!agent.getAttribute(cn).equals(value))
				return true;
		}
		return false;
	}
	
	private void exposeValue() 
	{
		try {
				if(attributeChanged())
				{
					Map<String,Object> mmap = new HashMap<String,Object>();
					for(String cn:this.attributeName)
					{
						mmap.put(cn, agent.getAttribute(cn));
						System.out.println(" "+cn+"->"+agent.getAttribute(cn));
					}
					connection.sendMessage(exposedName, mmap);
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
			this.agent.setAttribute(attributeName.get(0),data);
		}
	}
	
}
