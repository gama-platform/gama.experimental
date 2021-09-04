package ummisco.gama.remote.gui.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import org.eclipse.paho.client.mqttv3.MqttException;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import ummisco.gama.remote.gui.connector.MQTTConnector;
import ummisco.gama.remote.reducer.DataReducer;

public class SharedVariable {
//	public final static int EXPOSED_VARIABLE = 1;
//	public final static int LISTENED_VARIABLE = 2;
//
//	ArrayList<String> attributeName;
//	String exposedName;
//
//	int connectionType = -1;
//	Object value;
//	int lastUpdate;
//	MQTTConnector connection;
//	IAgent agent;
//
//	SharedVariable(final IAgent agt, final String names, final String exposedName, final MQTTConnector connect,
//			final int IO) throws MqttException {
//		connection = connect; // new MQTTConnector(server,login,pass,exposedName,IO);
//		this.agent = agt;
//		this.attributeName = new ArrayList<>();
//		this.attributeName.add(names);
//		this.exposedName = exposedName;
//		this.connectionType = IO;
//		if (connectionType == LISTENED_VARIABLE) {
//			connection.subscribeToGroup(exposedName);
//		}
//		this.update(agt.getScope());
//	}
//
//	SharedVariable(final IAgent agt, final ArrayList<String> names, final String exposedName,
//			final MQTTConnector connect, final int IO) throws MqttException {
//		connection = connect; // new MQTTConnector(server,login,pass,exposedName,IO);
//		this.agent = agt;
//		this.attributeName = names;
//		this.exposedName = exposedName;
//		this.connectionType = IO;
//		if (connectionType == LISTENED_VARIABLE) {
//			connection.subscribeToGroup(exposedName);
//		}
//		this.update(agt.getScope());
//	}
//
//	public void update(final IScope scope) {
//		switch (connectionType) {
//			case EXPOSED_VARIABLE: {
//				exposeValue();
//				break;
//			}
//			case LISTENED_VARIABLE: {
//				listenValue();
//				break;
//			}
//		}
//	}
//
//	public void dispose() {
//
//	}
//
//	private boolean attributeChanged() {
//		for (final String cn : this.attributeName) {
//			System.out.println(
//					"test equ " + agent.getAttribute(cn) + "  " + value + "  " + agent.getAttribute(cn).equals(value));
//			if (!agent.getAttribute(cn).equals(value)) { return true; }
//		}
//		return false;
//	}
//
//	private void exposeValue() {
//		try {
//			final Map<String, Object> mmap = new HashMap<>();
//			for (final String cn : this.attributeName) {
//				Object data = agent.getAttribute(cn);
//				if (data instanceof IList) {
//					data = DataReducer.castToList((IList<?>) data);
//				}
//				if (data instanceof IMap) {
//					data = DataReducer.castToMap((IMap<?, ?>) data);
//				}
//				mmap.put(cn, data);
//				this.value = data;
//			}
//			connection.sendMessage(exposedName, mmap);
//		} catch (final MqttException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private void listenValue() {
//		final Object data = connection.getLastData(exposedName);
//		if (data != null) {
//			this.value = data;
//			//this.agent.setAttribute(attributeName.get(0), data);
//			this.agent.setAttribute(attributeName.get(0),  ((Map<String, Object>) data).get(attributeName.get(0)));
//		}
//	}

}
