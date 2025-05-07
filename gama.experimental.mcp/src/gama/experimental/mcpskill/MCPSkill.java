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

import java.time.Duration;
import java.util.List;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.messaging.MessagingSkill;
import gama.core.runtime.IScope;
import gama.dev.DEBUG;
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
		DEBUG.ON();
	}

	@action(name = "create_chat_model", args = {
			@arg(name = "llm", type = IType.STRING, doc = @doc("LLM name: openai or ollama")),
			@arg(name = "model_name", type = IType.STRING, doc = @doc("model to use gpt-4o-mini,llama3.2 ... ")),
			@arg(name = "url", type = IType.STRING, doc = @doc("URL of LLM (for Ollama)")), // "http://localhost:11434"
			@arg(name = "key", type = IType.STRING, doc = @doc("API Key (for OpenAi)")), }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_chat_model(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String llmToBuild = (String) scope.getArg("llm", IType.STRING);
		final String modelToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String urlToBuild = (String) scope.getArg("url", IType.STRING);
		final String keyToBuild = (String) scope.getArg("key", IType.STRING);
		if ("openai".equals(llmToBuild)) {
			ChatModel model = OpenAiChatModel.builder().apiKey(keyToBuild).modelName(modelToBuild) // "gpt-4o-mini"
					.logRequests(true).build();
			return model;
		} else {
			ChatModel model = OllamaChatModel.builder().baseUrl(urlToBuild).modelName(modelToBuild)// "llama3.2"
					.logRequests(true).build();
			return model;

		}

	}

	@action(name = "create_chat_memory", args = {
			@arg(name = "role", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_chat_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String msgToAdd = (String) scope.getArg("role", IType.STRING);
		ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000, new OpenAiTokenCountEstimator(GPT_4_O_MINI));
		SystemMessage systemMessage = SystemMessage.from(msgToAdd);
		chatMemory.add(systemMessage);

		return chatMemory;

	}

	@action(name = "add_to_chat_memory", args = {
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute")),
			@arg(name = "memory", type = IType.NONE, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String add_to_chat_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final ChatMemory chatMemory = (ChatMemory) scope.getArg("memory", IType.NONE);
		if (chatMemory != null) {
			UserMessage userMessage1 = userMessage(msgToAdd);
			chatMemory.add(userMessage1);

		}

		return "";

	}

	@action(name = "send_to_llm", args = { @arg(name = "llm", type = IType.NONE, doc = @doc("command to execute")),
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String send_to_llm(final IScope scope) {

		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final ChatModel model = (ChatModel) scope.getArg("llm", IType.NONE);

		if (model != null) {
			String ans = model.chat(msgToAdd);

			return ans;
		}

		return "";

	}

	@action(name = "create_mcp_transport", args = {
			@arg(name = "url", type = IType.STRING, doc = @doc("command to execute")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_mcp_transport(final IScope scope) {

		final String urlToAdd = (String) scope.getArg("url", IType.STRING);
		final Integer timeout = (Integer) scope.getArg("timeout", IType.INT);

		McpTransport transport = new HttpMcpTransport.Builder().sseUrl(urlToAdd).timeout(Duration.ofSeconds(timeout))
				.logRequests(true).logResponses(true).build();

		return transport;

	}

	@action(name = "create_mcp_client", args = {
			@arg(name = "transport", type = IType.NONE, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_mcp_client(final IScope scope) {

		final McpTransport transport = (McpTransport) scope.getArg("transport", IType.NONE);

		McpClient mcpClient = new DefaultMcpClient.Builder().transport(transport).build();

		return mcpClient;

	}

	@action(name = "create_mcp_tool", args = {
			@arg(name = "client", type = IType.NONE, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_mcp_tool(final IScope scope) {

		final McpClient mcpClient = (McpClient) scope.getArg("client", IType.NONE);

		ToolProvider toolProvider = McpToolProvider.builder().mcpClients(List.of(mcpClient)).build();

		return toolProvider;

	}

	@action(name = "create_mcp_ai_service", args = {
			@arg(name = "llm", type = IType.NONE, doc = @doc("command to execute")),
			@arg(name = "tool", type = IType.NONE, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_mcp_ai_service(final IScope scope) {

		final ChatModel model = (ChatModel) scope.getArg("llm", IType.NONE);
		final ToolProvider toolProvider = (ToolProvider) scope.getArg("tool", IType.NONE);

		Bot bot = AiServices.builder(Bot.class).chatModel(model).toolProvider(toolProvider).build();

		return bot;

	}

	@action(name = "send_to_ai_service", args = {
			@arg(name = "bot", type = IType.NONE, doc = @doc("command to execute")),
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String send_to_ai_service(final IScope scope) {
		final Bot bot = (Bot) scope.getArg("bot", IType.NONE);

		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		String response = bot.chat(msgToAdd);

		return response;

	}

	public interface Bot {

		String chat(String prompt);
	}

}
