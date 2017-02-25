package ummisco.gama.remote.gui.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;

import msi.gama.extensions.messaging.GamaMailbox;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.outputs.IOutput;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.IList;
import msi.gaml.skills.Skill;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.IType;
import ummisco.gama.network.skills.INetworkSkill;
import ummisco.gama.remote.gui.connector.MQTTConnector;



@vars({ @var(name = IRemoteGUISkill.NET_AGENT_NAME, type = IType.STRING, doc = @doc("Net ID of the agent")),
	@var(name = IRemoteGUISkill.SERVER_URL, type = IType.STRING, doc = @doc("Net ID of the agent")),	
	@var(name = IRemoteGUISkill.LOGIN, type = IType.STRING, doc = @doc("Net ID of the agent")),	
	@var(name = IRemoteGUISkill.PASSWORD, type = IType.STRING, doc = @doc("Net ID of the agent")),	
	@var(name = IRemoteGUISkill.EXPOSED_VAR_LIST, type = IType.LIST, doc = @doc("Net ID of the agent"))})
	@skill(name = IRemoteGUISkill.SKILL_NAME, concept = { IConcept.GUI, IConcept.COMMUNICATION, IConcept.SKILL })
public class RemoteGUISkill extends Skill implements IRemoteGUISkill{
	final static String SHARED_VARIABLE_LIST = "SHARED_VARIABLE_LIST";
	ArrayList<SharedVariable> vars = new ArrayList<SharedVariable>();
	MQTTConnector connection = null;
	
	@action(name = IRemoteGUISkill.CONFIGURE_TOPIC, args = {
			@arg(name = IRemoteGUISkill.LOGIN, type = IType.STRING, optional = true, doc = @doc("server nameL")),
			@arg(name = IRemoteGUISkill.PASSWORD, type = IType.STRING, optional = true, doc = @doc("server nameL")),
			@arg(name = IRemoteGUISkill.SERVER_URL, type = IType.STRING, optional = false, doc = @doc("server URL")) }, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public void connectToServer(IScope scope)
	{
		if(!scope.getSimulation().getAttributes().keySet().contains(SHARED_VARIABLE_LIST))
			this.startSkill(scope);
		
		IAgent agt = scope.getAgent();
		
		String serverURL = (String) scope.getArg(IRemoteGUISkill.SERVER_URL, IType.STRING);
		String login = (String) scope.getArg(INetworkSkill.LOGIN, IType.STRING);
		String password = (String) scope.getArg(INetworkSkill.PASSWORD, IType.STRING);
		
		agt.setAttribute(IRemoteGUISkill.SERVER_URL, serverURL);
		agt.setAttribute(IRemoteGUISkill.LOGIN, login);
		agt.setAttribute(IRemoteGUISkill.PASSWORD, password);
		try {
			connection = new MQTTConnector(serverURL, login, password);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@action(name = IRemoteGUISkill.EXPOSE_VAR, args = {
			@arg(name = IRemoteGUISkill.VAR_NAME, type = IType.LIST, optional = false, doc = @doc("server nameL")),
			@arg(name = IRemoteGUISkill.EXPOSED_NAME, type = IType.STRING, optional = false, doc = @doc("server nameL"))
		 }, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public void exposeToRemoteGui(IScope scope)
	{
		IAgent agt = scope.getAgent();
		String url = (String)agt.getAttribute(IRemoteGUISkill.SERVER_URL);
		String login = (String)agt.getAttribute(IRemoteGUISkill.LOGIN);
		String pass = (String) agt.getAttribute(IRemoteGUISkill.PASSWORD);
		@SuppressWarnings("unchecked")
		ArrayList<String> varName =  (ArrayList<String>)scope.getListArg(IRemoteGUISkill.VAR_NAME); //scope.getArg(IRemoteGUISkill.VAR_NAME,IType.MAP);
		String exposedName = (String )scope.getArg(IRemoteGUISkill.EXPOSED_NAME,IType.STRING);
		
	try {
			SharedVariable varS = new SharedVariable(agt, (ArrayList<String>) varName, exposedName, connection,SharedVariable.EXPOSED_VARIABLE);
			this.getShareVariables(scope).add(varS);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@action(name = IRemoteGUISkill.LISTEN_VAR, args = {
			@arg(name = IRemoteGUISkill.STORE_NAME, type = IType.STRING, optional = false, doc = @doc("server nameL")),
			@arg(name = IRemoteGUISkill.EXPOSED_NAME, type = IType.STRING, optional = false, doc = @doc("server nameL"))
		 }, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public void listenFromRemoteGui(IScope scope)
	{
		IAgent agt = scope.getAgent();
		
		String url = (String)agt.getAttribute(IRemoteGUISkill.SERVER_URL);
		String login = (String)agt.getAttribute(IRemoteGUISkill.LOGIN);
		String pass = (String) agt.getAttribute(IRemoteGUISkill.PASSWORD);
		String varName = (String )scope.getArg(IRemoteGUISkill.STORE_NAME,IType.STRING);
		String exposedName = (String )scope.getArg(IRemoteGUISkill.EXPOSED_NAME,IType.STRING);
		
		try {
			SharedVariable varS = new SharedVariable(agt, varName, exposedName, connection, SharedVariable.LISTENED_VARIABLE);
			this.getShareVariables(scope).add(varS);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	@SuppressWarnings("unchecked")
	private ArrayList<SharedVariable> initialize(IScope scope)
	{
		scope.getSimulation().setAttribute(SHARED_VARIABLE_LIST, new ArrayList<SharedVariable>());
		return (ArrayList<SharedVariable>)scope.getSimulation().getAttribute(SHARED_VARIABLE_LIST); 
	}
	@SuppressWarnings("unchecked")
	private ArrayList<SharedVariable> getShareVariables(IScope scope)
	{
		ArrayList<SharedVariable> res = (ArrayList<SharedVariable>)scope.getSimulation().getAttribute(SHARED_VARIABLE_LIST);
		if(res==null)
			res = initialize(scope);
		return res;
	}
	
	private void startSkill(IScope scope)
	{
		initialize(scope);
		registerSimulationEvent(scope);
	}

	
	
	private void updateVariables(IScope scope)
	{
		ArrayList<SharedVariable> agts = getShareVariables(scope);
		for(SharedVariable agt:agts)
		{
			agt.update(scope);
		}
	}

	
	private void registerSimulationEvent(IScope scope)
	{
		scope.getSimulation().postEndAction(new IExecutable() {
			@Override
			public Object executeOn(IScope scope) throws GamaRuntimeException {
				updateVariables(scope);
				return null;
			}
		});
	
		scope.getSimulation().postDisposeAction(new IExecutable() {
			@Override
			public Object executeOn(IScope scope) throws GamaRuntimeException {
				closeAllSharedVariable(scope);
				return null;
			}
		});
	}
	
	private void closeAllSharedVariable(IScope scope)
	{
		ArrayList<SharedVariable> agts = getShareVariables(scope);
		for(SharedVariable agt:agts)
		{
			agt.dispose();
		}
		try {
			connection.releaseConnection();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.initialize(scope);
	}

	
}
