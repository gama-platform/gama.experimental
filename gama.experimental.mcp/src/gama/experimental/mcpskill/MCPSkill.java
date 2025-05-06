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
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
	@action(name = "build_model", args = { @arg(name = "llm", type = IType.STRING, doc = @doc("LLM name: openai or ollama")),
			@arg(name = "model", type = IType.STRING, doc = @doc("model to use gpt-4o-mini,llama3.2 ... ")),
			@arg(name = "url", type = IType.STRING, doc = @doc("URL of LLM (for Ollama)")), // "http://localhost:11434"
			@arg(name = "key", type = IType.STRING, doc = @doc("API Key (for OpenAi)")), }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String build_model(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String llmToBuild = (String) scope.getArg("llm", IType.STRING);
		final String modelToBuild = (String) scope.getArg("model", IType.STRING);
		final String urlToBuild = (String) scope.getArg("url", IType.STRING);
		final String keyToBuild = (String) scope.getArg("key", IType.STRING);
		if ("openai".equals(llmToBuild)) {
			ChatModel model = (OpenAiChatModel) scope.getAgent().getAttribute(IMCPSkill.LLM_MODEL);
			if (model == null) {
				model = OpenAiChatModel.builder().apiKey(keyToBuild).modelName(modelToBuild) // "gpt-4o-mini"
						.logRequests(true).build();
				scope.getAgent().setAttribute(IMCPSkill.LLM_MODEL, model);

			}
		} else {
			ChatModel model = (OllamaChatModel) scope.getAgent().getAttribute(IMCPSkill.LLM_MODEL);
			if (model == null) {
				model = OllamaChatModel.builder().baseUrl(urlToBuild).modelName(modelToBuild)// "llama3.2"
						.logRequests(true).build();
				scope.getAgent().setAttribute(IMCPSkill.LLM_MODEL, model);

			}

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

		ChatModel model = (ChatModel) scope.getAgent().getAttribute(IMCPSkill.LLM_MODEL);
		if (model != null) {
			String ans = model.chat(msgToAdd);

			return ans;
		}

		return "";

	}
}
