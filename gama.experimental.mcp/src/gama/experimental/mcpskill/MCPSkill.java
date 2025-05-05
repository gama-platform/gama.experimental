/*******************************************************************************************************
 *
 * NetworkSkill.java, in gama.network, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.experimental.mcpskill;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.messaging.GamaMessage;
import gama.core.messaging.MessagingSkill;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.gaml.operators.Cast;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;

/**
 * The Class NetworkSkill.
 */
@vars({ @variable(name = IMCPSkill.NET_AGENT_NAME, type = IType.STRING, doc = @doc("Net ID of the agent")) })
@skill(name = IMCPSkill.MCP_SKILL, concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
@doc("The " + IMCPSkill.MCP_SKILL + " skill provides new features to let agents exchange message through network. "
		+ "Sending and receiving data is done with the " + MessagingSkill.SKILL_NAME + " skill's actions.")
public class MCPSkill extends Skill {

	static {
		DEBUG.OFF();
	}

	/** The Constant REGISTERED_AGENTS. */
	final static String REGISTERED_AGENTS = "registered_agents";

	/** The Constant REGISTRED_SERVER. */
	final static String REGISTERED_SERVER = "registered_servers";

	/**
	 * System exec.
	 *
	 * @param scope the scope
	 * @return the string
	 */
	@action(name = "execute", args = {
			@arg(name = "command", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String systemExec(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String commandToExecute = (String) scope.getArg("command", IType.STRING);

		// String res = "";

		Process p;
		try {
			p = Runtime.getRuntime().exec(commandToExecute);

			final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			return stdError.readLine();
		} catch (final IOException e) {

			e.printStackTrace();
		}
		return "";

	}

	/**
	 * System exec.
	 *
	 * @param scope the scope
	 * @return the string
	 */
	@action(name = "build_model", args = {
			@arg(name = "llm", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String build_model(final IScope scope) {
		// final IAgent agent = scope.getAgent();
//		final String commandToExecute = (String) scope.getArg("command", IType.STRING);
		OllamaChatModel model = (OllamaChatModel) scope.getAgent().getAttribute(IMCPSkill.LLM_MODEL);
		if (model == null) {
			model = OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName("llama3.1").logRequests(true)
					.build();
			scope.getAgent().setAttribute(IMCPSkill.LLM_MODEL, model);

		}

		return "";

	}

	/**
	 * System exec.
	 *
	 * @param scope the scope
	 * @return the string
	 */
	@action(name = "create_chat_memory", args = {
			@arg(name = "role", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String create_chat_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String msgToAdd = (String) scope.getArg("role", IType.STRING);
		ChatMemory chatMemory = (ChatMemory) scope.getAgent().getAttribute(IMCPSkill.CHAT_MEMORY);
		if (chatMemory == null) {
			chatMemory = TokenWindowChatMemory.withMaxTokens(1000, new OpenAiTokenCountEstimator(GPT_4_O_MINI));
			SystemMessage systemMessage = SystemMessage.from(msgToAdd);
			chatMemory.add(systemMessage);

			scope.getAgent().setAttribute(IMCPSkill.CHAT_MEMORY, chatMemory);

		}

		return "";

	}

	/**
	 * System exec.
	 *
	 * @param scope the scope
	 * @return the string
	 */
	@action(name = "add_msg_memory", args = {
			@arg(name = "msg", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String add_msg_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String msgToAdd = (String) scope.getArg("msg", IType.STRING);
		ChatMemory chatMemory = (ChatMemory) scope.getAgent().getAttribute(IMCPSkill.CHAT_MEMORY);
		if (chatMemory != null) {
			UserMessage userMessage1 = userMessage(msgToAdd);
			chatMemory.add(userMessage1);
			scope.getAgent().setAttribute(IMCPSkill.CHAT_MEMORY, chatMemory);

		}

		return "";

	}

	/**
	 * System exec.
	 *
	 * @param scope the scope
	 * @return the string
	 */
	@action(name = "chat", args = {
			@arg(name = "msg", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String chat(final IScope scope) {

		final String msgToAdd = (String) scope.getArg("msg", IType.STRING);

		OllamaChatModel model = (OllamaChatModel) scope.getAgent().getAttribute(IMCPSkill.LLM_MODEL);
		if (model != null) {
			String ans = model.chat(msgToAdd);
			
			return ans;
		}

		return "";

	}

	/**
	 * Connect to server.
	 *
	 * @param scope the scope
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@SuppressWarnings("unchecked")
	@action(name = IMCPSkill.CONNECT_TOPIC, args = {
			@arg(name = IMCPSkill.PROTOCOL, type = IType.STRING, doc = @doc("protocol type (MQTT (by default), TCP, UDP, websocket, arduino): the possible value ares '"
					+ "', otherwise the MQTT protocol is used.")),
			@arg(name = IMCPSkill.PORT, type = IType.INT, doc = @doc("Port number")),
			@arg(name = "raw", type = IType.BOOL, doc = @doc("message type raw or rich")),
			@arg(name = IMCPSkill.WITHNAME, type = IType.STRING, optional = true, doc = @doc("ID of the agent (its name) for the simulation")),
			@arg(name = IMCPSkill.LOGIN, type = IType.STRING, optional = true, doc = @doc("login for the connection to the server")),
			@arg(name = IMCPSkill.PASSWORD, type = IType.STRING, optional = true, doc = @doc("password associated to the login")),
			@arg(name = IMCPSkill.SERVER_URL, type = IType.STRING, optional = true, doc = @doc("server URL (localhost or a server URL)")) }, doc = @doc(value = "Action used by a networking agent to connect to a server or to create a server.", examples = {
					@example(" do connect with_name:\"any_name\";"), @example(" do connect protocol: \"arduino\";"), }))
	public boolean connectToServer(final IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
		final String serverURL = (String) scope.getArg(IMCPSkill.SERVER_URL, IType.STRING);
		final String login = (String) scope.getArg(IMCPSkill.LOGIN, IType.STRING);
		final String password = (String) scope.getArg(IMCPSkill.PASSWORD, IType.STRING);
		final String networkName = (String) scope.getArg(IMCPSkill.WITHNAME, IType.STRING);
		final String protocol = (String) scope.getArg(IMCPSkill.PROTOCOL, IType.STRING);
		final Boolean raw_package = (Boolean) scope.getArg("raw", IType.BOOL);
		final Integer port = (Integer) scope.getArg(IMCPSkill.PORT, IType.INT);

		return true;
	}

	/**
	 * Fetch message.
	 *
	 * @param scope the scope
	 * @return the gama message
	 */
	@action(name = IMCPSkill.FETCH_MESSAGE, doc = @doc(value = "Fetch the first message from the mailbox (and remove it from the mailing box). If the mailbox is empty, it returns a nil message.", examples = {
			@example("message mess <- fetch_message();"), @example("loop while:has_more_message(){ \n"
					+ "	message mess <- fetch_message();\n" + "	write message.contents;\n" + "}") }))
	public GamaMessage fetchMessage(final IScope scope) {
		final IAgent agent = scope.getAgent();
		GamaMessage msg = null;
		return msg;
	}

	/**
	 * Checks for more message.
	 *
	 * @param scope the scope
	 * @return true, if successful
	 */
	@action(name = IMCPSkill.HAS_MORE_MESSAGE_IN_BOX, doc = @doc(value = "Check whether the mailbox contains any message.", examples = {
			@example("bool mailbox_contain_messages <- has_more_message();"),
			@example("loop while:has_more_message(){ \n" + "	message mess <- fetch_message();\n"
					+ "	write message.contents;\n" + "}") }))
	public boolean hasMoreMessage(final IScope scope) {
		final IAgent agent = scope.getAgent();
		return false;
	}

	/**
	 * Fetch messages of agents.
	 *
	 * @param scope the scope
	 */
	@action(name = IMCPSkill.FETCH_MESSAGE_FROM_NETWORK, doc = @doc(value = "Fetch all messages from network to mailbox. Use this in specific case only, this action is done at the end of each step. ", examples = {
			@example("do fetch_message_from_network;//forces gama to get all the new messages since the begining of the cycle\n"
					+ "loop while: has_more_message(){ \n" + "	message mess <- fetch_message();\n"
					+ "	write message.contents;\n" + "}") }))
	public boolean fetchMessagesOfAgents(final IScope scope) {

		return true;
	}

}
