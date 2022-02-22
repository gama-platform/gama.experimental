package ummisco.gama.remote.gui.skill;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.MqttException;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.network.skills.INetworkSkill;
import ummisco.gama.remote.gui.connector.MQTTConnector;

@vars ({ @variable (
		name = IRemoteGUISkill.NET_AGENT_NAME,
		type = IType.STRING,
		doc = @doc ("Net ID of the agent")),
		@variable (
				name = IRemoteGUISkill.SERVER_URL,
				type = IType.STRING,
				doc = @doc ("Net ID of the agent")),
		@variable (
				name = IRemoteGUISkill.LOGIN,
				type = IType.STRING,
				doc = @doc ("Net ID of the agent")),
		@variable (
				name = IRemoteGUISkill.PASSWORD,
				type = IType.STRING,
				doc = @doc ("Net ID of the agent")),
		@variable (
				name = IRemoteGUISkill.EXPOSED_VAR_LIST,
				type = IType.LIST,
				doc = @doc ("Net ID of the agent")) })
@skill (
		name = IRemoteGUISkill.SKILL_NAME,
		concept = { IConcept.GUI, IConcept.COMMUNICATION, IConcept.SKILL })
public class RemoteGUISkill extends Skill implements IRemoteGUISkill {
	final static String SHARED_VARIABLE_LIST = "SHARED_VARIABLE_LIST";
	ArrayList<SharedVariable> vars = new ArrayList<SharedVariable>();
	MQTTConnector connection = null;

	static {
		DEBUG.OFF();
	}	
	
	@action (
			name = IRemoteGUISkill.CONFIGURE_TOPIC,
			args = { @arg (
					name = IRemoteGUISkill.LOGIN,
					type = IType.STRING,
					optional = true,
					doc = @doc ("server nameL")),
					@arg (
							name = IRemoteGUISkill.PASSWORD,
							type = IType.STRING,
							optional = true,
							doc = @doc ("server nameL")),
					@arg (
							name = IRemoteGUISkill.SERVER_URL,
							type = IType.STRING,
							optional = false,
							doc = @doc ("server URL")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void connectToServer(final IScope scope) {
//		if (!scope.getSimulation().getAttributes().keySet().contains(SHARED_VARIABLE_LIST))
		if (!scope.getSimulation().hasAttribute(SHARED_VARIABLE_LIST))			
			this.startSkill(scope);

		final IAgent agt = scope.getAgent();

		final String serverURL = (String) scope.getArg(IRemoteGUISkill.SERVER_URL, IType.STRING);
		final String login = (String) scope.getArg(INetworkSkill.LOGIN, IType.STRING);
		final String password = (String) scope.getArg(INetworkSkill.PASSWORD, IType.STRING);

		agt.setAttribute(IRemoteGUISkill.SERVER_URL, serverURL);
		agt.setAttribute(IRemoteGUISkill.LOGIN, login);
		agt.setAttribute(IRemoteGUISkill.PASSWORD, password);
		try {
			connection = new MQTTConnector(serverURL, login, password);
		} catch (final MqttException e) {
			e.printStackTrace();
		}
	}

	@action (
			name = IRemoteGUISkill.EXPOSE_VAR,
			args = { @arg (
					name = IRemoteGUISkill.VAR_NAME,
					type = IType.LIST,
					optional = false,
					doc = @doc ("server nameL")),
					@arg (
							name = IRemoteGUISkill.EXPOSED_NAME,
							type = IType.STRING,
							optional = false,
							doc = @doc ("server nameL")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void exposeToRemoteGui(final IScope scope) {
		System.out.println("register variable");

		final IAgent agt = scope.getAgent();
		final String url = (String) agt.getAttribute(IRemoteGUISkill.SERVER_URL);
		final String login = (String) agt.getAttribute(IRemoteGUISkill.LOGIN);
		final String pass = (String) agt.getAttribute(IRemoteGUISkill.PASSWORD);
		@SuppressWarnings ("unchecked") final ArrayList<String> varName =
			//	(ArrayList<String>) scope.getListArg(IRemoteGUISkill.VAR_NAME); // scope.getArg(IRemoteGUISkill.VAR_NAME,IType.MAP);
				new ArrayList<String> (scope.getListArg(IRemoteGUISkill.VAR_NAME));
		final String exposedName = (String) scope.getArg(IRemoteGUISkill.EXPOSED_NAME, IType.STRING);

		try {
			final SharedVariable varS =
					new SharedVariable(agt, varName, exposedName, connection, SharedVariable.EXPOSED_VARIABLE);
			this.getShareVariables(scope).add(varS);
		} catch (final MqttException e) {
			e.printStackTrace();
		}
	}

	@action (
			name = IRemoteGUISkill.LISTEN_VAR,
			args = { @arg (
					name = IRemoteGUISkill.STORE_NAME,
					type = IType.STRING,
					optional = false,
					doc = @doc ("server nameL")),
					@arg (
							name = IRemoteGUISkill.EXPOSED_NAME,
							type = IType.STRING,
							optional = false,
							doc = @doc ("server nameL")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void listenFromRemoteGui(final IScope scope) {
		final IAgent agt = scope.getAgent();

		final String url = (String) agt.getAttribute(IRemoteGUISkill.SERVER_URL);
		final String login = (String) agt.getAttribute(IRemoteGUISkill.LOGIN);
		final String pass = (String) agt.getAttribute(IRemoteGUISkill.PASSWORD);
		final String varName = (String) scope.getArg(IRemoteGUISkill.STORE_NAME, IType.STRING);
		final String exposedName = (String) scope.getArg(IRemoteGUISkill.EXPOSED_NAME, IType.STRING);
		DEBUG.OUT("register");
		try {
			final SharedVariable varS =
					new SharedVariable(agt, varName, exposedName, connection, SharedVariable.LISTENED_VARIABLE);
			this.getShareVariables(scope).add(varS);
		} catch (final MqttException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings ("unchecked")
	private ArrayList<SharedVariable> initialize(final IScope scope) {
		scope.getSimulation().setAttribute(SHARED_VARIABLE_LIST, new ArrayList<SharedVariable>());
		return (ArrayList<SharedVariable>) scope.getSimulation().getAttribute(SHARED_VARIABLE_LIST);
	}

	@SuppressWarnings ("unchecked")
	private ArrayList<SharedVariable> getShareVariables(final IScope scope) {
		ArrayList<SharedVariable> res =
				(ArrayList<SharedVariable>) scope.getSimulation().getAttribute(SHARED_VARIABLE_LIST);
		if (res == null)
			res = initialize(scope);
		return res;
	}

	private void startSkill(final IScope scope) {
		initialize(scope);
		registerSimulationEvent(scope);
	}

	private void updateVariables(final IScope scope) {
		final ArrayList<SharedVariable> agts = getShareVariables(scope);
		for (final SharedVariable agt : agts) {
			DEBUG.OUT("update d'un variable  " + agt.exposedName);

			agt.update(scope);
		}
	}

	private void registerSimulationEvent(final IScope scope) {
		scope.getSimulation().postEndAction(scope1 -> {
			DEBUG.OUT("Register Simulation");
			updateVariables(scope1);
			return null;
		});

		scope.getSimulation().postDisposeAction(scope1 -> {
			closeAllSharedVariable(scope1);
			return null;
		});
	}

	private void closeAllSharedVariable(final IScope scope) {
		final ArrayList<SharedVariable> agts = getShareVariables(scope);
		for (final SharedVariable agt : agts) {
			agt.dispose();
		}
		try {
			connection.releaseConnection();
		} catch (final MqttException e) {
			e.printStackTrace();
		}
		this.initialize(scope);
	}

}
