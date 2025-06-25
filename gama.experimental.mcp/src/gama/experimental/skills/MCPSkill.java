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
package gama.experimental.skills;


import java.nio.file.Path;
import java.nio.file.Paths;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutor;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.experimental.constants.MCPConstants;
import gama.experimental.types.Assistant;
import gama.experimental.types.AssistantType;
import gama.experimental.types.ChatModel;
import gama.experimental.types.ChatModelType;
import gama.experimental.types.ContentRetriever;
import gama.experimental.types.ContentRetrieverType;
import gama.experimental.types.MCPClient;
import gama.experimental.types.MCPClientType;
import gama.experimental.types.MCPTransport;
import gama.experimental.types.MCPTransportType;
import gama.experimental.types.Memory;
import gama.experimental.types.MemoryType;
import gama.experimental.types.Provider;
import gama.experimental.types.ProviderType;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;

/**
 * The Class NetworkSkill.
 */

@skill(name = MCPConstants.MCP_SKILL, concept = { MCPConstants.LLM_MODEL}, doc = @doc("The " + MCPConstants.MCP_SKILL + " skill provides new features that allow agents to ask questions to a chatbot (LLM)"))
@vars({
	// @variable(name = MCPConstants.LLM_MODEL, type = ChatModelType.id, init = "nil",doc = @doc("A chat model (to be built) that can answer questions and be used as a key element of the chat bot")),
//	@variable(name = MCPConstants.CHAT_BOT, type = AssistantType.id, init = "nil",doc = @doc("A chat bot (to be built) that can answer questions taking into account external data (RAG) and trigger actions")),
//	@variable(name = MCPConstants.CHAT_MEMORY, type = MemoryType.id, init = "nil",doc = @doc("A chat memory (to be built) that can be used to store data for the chat model"))
})
public class MCPSkill extends Skill {

	static {
		DEBUG.ON();
	}
	
	@action(name = "create_ollama_chat_model", args = {
			@arg(name = "model_name", type = IType.STRING, doc = @doc(" model_name specifies the exact name or identifier of the language model to be used for generating responses (e.g. 'llama3.2') ")),
			@arg(name = "url", type = IType.STRING, doc = @doc("url specifies the endpoint URL of the local or remote Ollama server that the model communicates with (for Ollama)")), // "http://localhost:11434"
			@arg(name = "responseFormat", type = IType.STRING, doc = @doc("responseFormat specifies the format in which the model should return its output, such as plain text or structured JSON. 2 possible values: 'json' or 'text' (by default)")),
			@arg(name = "numCtx", type = IType.INT, doc = @doc("numCtx specifies the maximum number of context tokens the model can use to process a prompt, including instructions, documents, and conversation history (for Ollama)")),
			@arg(name = "numPredict", type = IType.INT, doc = @doc("numPredict specifies the maximum number of tokens the model is allowed to generate in its response (for Ollama)")),
			@arg(name = "repeatPenalty", type = IType.FLOAT, doc = @doc("repeatPenalty controls how strongly the model is discouraged from repeating the same tokens or phrases in its response. (for Ollama)")),
			@arg(name = "seed", type = IType.INT, doc = @doc("seed sets the random number generator seed to make the model’s output deterministic and reproducible")),
			@arg(name = "temperature", type = IType.FLOAT, doc = @doc("temperature controls the randomness of the model’s output, with higher values producing more creative and varied responses")),
			@arg(name = "topK", type = IType.INT, doc = @doc(" topK limits the model’s token selection to the top K most probable tokens, influencing the diversity and focus of the generated output (for Ollama)")),
			@arg(name = "topP", type = IType.FLOAT, doc = @doc("topP (nucleus sampling) sets the probability threshold for choosing the next token, allowing the model to sample from the most likely tokens whose cumulative probability exceeds this value."))
	}, doc = @doc(value = "Action that builds a chat model based on a connection with Ollama", returns = "The chat_model built"))
	public ChatModel create_ollama_chat_model(final IScope scope) {
		final String modelnameToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String urlToBuild = (String) scope.getArg("url", IType.STRING); 
		final String responseFormat = scope.hasArg("responseFormat") ? (String) scope.getArg("responseFormat", IType.STRING) : null;
		final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
		final Integer numCtx = scope.hasArg("numCtx") ? (Integer) scope.getArg("numCtx", IType.INT) : null;
		final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
		final Double topP = scope.hasArg("topP") ? (Double) scope.getArg("topP", IType.FLOAT) : null;
		final Integer numPredict = scope.hasArg("numPredict") ? (Integer) scope.getArg("numPredict", IType.INT) : null;
		final Double repeatPenalty = scope.hasArg("repeatPenalty") ? (Double) scope.getArg("repeatPenalty", IType.FLOAT) : null;
		final Integer topK = scope.hasArg("topK") ? (Integer) scope.getArg("topK", IType.INT) : null;
		return new ChatModel( modelnameToBuild, urlToBuild, responseFormat, numCtx, seed, temperature, topP, numPredict, repeatPenalty, topK);
	}
	
	@action(name = "create_openai_chat_model", args = {
			@arg(name = "model_name", type = IType.STRING, doc = @doc(" model_name specifies the exact name or identifier of the language model to be used for generating responses (e.g. 'gpt-4o-mini') ")),
			@arg(name = "key", type = IType.STRING, doc = @doc("key refers to the API key used to authenticate requests to the OpenAI service (for OpenAi)")),
			@arg(name = "responseFormat", type = IType.STRING, doc = @doc("responseFormat specifies the format in which the model should return its output, such as plain text or structured JSON. 2 possible values: 'json' or 'text' (by default)")),
			@arg(name = "seed", type = IType.INT, doc = @doc("seed sets the random number generator seed to make the model’s output deterministic and reproducible")),
			@arg(name = "temperature", type = IType.FLOAT, doc = @doc("temperature controls the randomness of the model’s output, with higher values producing more creative and varied responses")),
			@arg(name = "topP", type = IType.FLOAT, doc = @doc("topP (nucleus sampling) sets the probability threshold for choosing the next token, allowing the model to sample from the most likely tokens whose cumulative probability exceeds this value.")),
			@arg(name = "frequencyPenalty", type = IType.FLOAT, doc = @doc("frequencyPenalty reduces the likelihood of the model repeating tokens by penalizing tokens based on their frequency in the generated text (for OpenAI)")),
			@arg(name = "maxCompletionTokens", type = IType.INT, doc = @doc("maxCompletionTokens sets the maximum number of tokens the model can generate in its completion or response (for OpenAI)")),
			@arg(name = "maxRetries", type = IType.INT, doc = @doc("maxRetries specifies the maximum number of times the system will retry a failed request to the model (for OpenAI)")),
			@arg(name = "maxTokens", type = IType.INT, doc = @doc("maxTokens defines the total maximum number of tokens allowed for both the input (prompt) and the output (completion) combined (for OpenAI)")),
			@arg(name = "presencePenalty", type = IType.FLOAT, doc = @doc("presencePenalty reduces the likelihood of the model mentioning new topics or tokens that have already appeared, encouraging more diverse and novel content (for OpenAI)")),
			@arg(name = "store", type = IType.BOOL, doc = @doc("tore is a boolean that indicates whether the generated data (such as embeddings or chat history) should be saved or not (for OpenAI)")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("timeout specifies the maximum amount of time the system will wait for a response from the model before aborting the request (for OpenAI)"))

	}, doc = @doc(value = "Action that builds a chat model based on a connection with OpenAI", returns = "The chat_model built"))
	public ChatModel create_openai_chat_model(final IScope scope) {
		final String modelnameToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String keyToBuild = (String) scope.getArg("key", IType.STRING);
		final String responseFormat = scope.hasArg("responseFormat") ? (String) scope.getArg("responseFormat", IType.STRING) : null;
		final Double frequencyPenalty = scope.hasArg("frequencyPenalty") ? (Double) scope.getArg("frequencyPenalty", IType.FLOAT) : null;
		final Integer maxCompletionTokens = scope.hasArg("maxCompletionTokens") ? (Integer) scope.getArg("maxCompletionTokens", IType.INT) : null;
		final Integer maxRetries = scope.hasArg("maxRetries") ? (Integer) scope.getArg("maxRetries", IType.INT) : null;
		final Integer maxTokens = scope.hasArg("maxTokens") ? (Integer) scope.getArg("maxTokens", IType.INT) : null;
		final Double presencePenalty = scope.hasArg("presencePenalty") ? (Double) scope.getArg("presencePenalty", IType.FLOAT) : null;
		final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
		final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
		final Double topP = scope.hasArg("topP") ? (Double) scope.getArg("topP", IType.FLOAT) : null;
		final Integer timeout = scope.hasArg("timeout") ? (Integer) scope.getArg("timeout", IType.INT) : null;
		final Boolean store = scope.hasArg("store") ? (Boolean) scope.getArg("store", IType.BOOL) : null;
		return new ChatModel(keyToBuild, modelnameToBuild, responseFormat, frequencyPenalty, maxCompletionTokens, maxRetries, maxTokens, presencePenalty, seed, store, temperature, timeout, topP);
		
	}
	
	@action(name = "create_chat_model", args = {
			@arg(name = "model_type", type = IType.STRING, doc = @doc("model_type specifies the chat model type: 'openai' or 'ollama'")),
			@arg(name = "model_name", type = IType.STRING, doc = @doc(" model_name specifies the exact name or identifier of the language model to be used for generating responses (e.g. 'gpt-4o-mini') ")),
			@arg(name = "key", type = IType.STRING, doc = @doc("key refers to the API key used to authenticate requests to the OpenAI service (for OpenAi)")),
			@arg(name = "responseFormat", type = IType.STRING, doc = @doc("responseFormat specifies the format in which the model should return its output, such as plain text or structured JSON. 2 possible values: 'json' or 'text' (by default)")),
			@arg(name = "seed", type = IType.INT, doc = @doc("seed sets the random number generator seed to make the model’s output deterministic and reproducible")),
			@arg(name = "temperature", type = IType.FLOAT, doc = @doc("temperature controls the randomness of the model’s output, with higher values producing more creative and varied responses")),
			@arg(name = "topP", type = IType.FLOAT, doc = @doc("topP (nucleus sampling) sets the probability threshold for choosing the next token, allowing the model to sample from the most likely tokens whose cumulative probability exceeds this value.")),
			@arg(name = "frequencyPenalty", type = IType.FLOAT, doc = @doc("frequencyPenalty reduces the likelihood of the model repeating tokens by penalizing tokens based on their frequency in the generated text (for OpenAI)")),
			@arg(name = "maxCompletionTokens", type = IType.INT, doc = @doc("maxCompletionTokens sets the maximum number of tokens the model can generate in its completion or response (for OpenAI)")),
			@arg(name = "maxRetries", type = IType.INT, doc = @doc("maxRetries specifies the maximum number of times the system will retry a failed request to the model (for OpenAI)")),
			@arg(name = "maxTokens", type = IType.INT, doc = @doc("maxTokens defines the total maximum number of tokens allowed for both the input (prompt) and the output (completion) combined (for OpenAI)")),
			@arg(name = "presencePenalty", type = IType.FLOAT, doc = @doc("presencePenalty reduces the likelihood of the model mentioning new topics or tokens that have already appeared, encouraging more diverse and novel content (for OpenAI)")),
			@arg(name = "store", type = IType.BOOL, doc = @doc("tore is a boolean that indicates whether the generated data (such as embeddings or chat history) should be saved or not (for OpenAI)")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("timeout specifies the maximum amount of time the system will wait for a response from the model before aborting the request (for OpenAI)")),
			@arg(name = "url", type = IType.STRING, doc = @doc("url specifies the endpoint URL of the local or remote Ollama server that the model communicates with (for Ollama)")), // "http://localhost:11434"
			@arg(name = "numCtx", type = IType.INT, doc = @doc("numCtx specifies the maximum number of context tokens the model can use to process a prompt, including instructions, documents, and conversation history (for Ollama)")),
			@arg(name = "numPredict", type = IType.INT, doc = @doc("numPredict specifies the maximum number of tokens the model is allowed to generate in its response (for Ollama)")),
			@arg(name = "repeatPenalty", type = IType.FLOAT, doc = @doc("repeatPenalty controls how strongly the model is discouraged from repeating the same tokens or phrases in its response. (for Ollama)")),
			@arg(name = "topK", type = IType.INT, doc = @doc(" topK limits the model’s token selection to the top K most probable tokens, influencing the diversity and focus of the generated output (for Ollama)"))
			
	}, doc = @doc(value = "Action that builds a chat model", returns = "The chat_model built"))
	public ChatModel create_chat_model(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String modelType = (String) scope.getArg("model_type", IType.STRING);
		final String modelnameToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String urlToBuild = (String) scope.getArg("url", IType.STRING); 
		final String keyToBuild = (String) scope.getArg("key", IType.STRING);

		if ("openai".equals(modelType)) {
			final String responseFormat = scope.hasArg("responseFormat") ? (String) scope.getArg("responseFormat", IType.STRING) : null;
			final Double frequencyPenalty = scope.hasArg("frequencyPenalty") ? (Double) scope.getArg("frequencyPenalty", IType.FLOAT) : null;
			final Integer maxCompletionTokens = scope.hasArg("maxCompletionTokens") ? (Integer) scope.getArg("maxCompletionTokens", IType.INT) : null;
			final Integer maxRetries = scope.hasArg("maxRetries") ? (Integer) scope.getArg("maxRetries", IType.INT) : null;
			final Integer maxTokens = scope.hasArg("maxTokens") ? (Integer) scope.getArg("maxTokens", IType.INT) : null;
			final Double presencePenalty = scope.hasArg("presencePenalty") ? (Double) scope.getArg("presencePenalty", IType.FLOAT) : null;
			final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
			final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
			final Double topP = scope.hasArg("topP") ? (Double) scope.getArg("topP", IType.FLOAT) : null;
			final Integer timeout = scope.hasArg("timeout") ? (Integer) scope.getArg("timeout", IType.INT) : null;
			final Boolean store = scope.hasArg("store") ? (Boolean) scope.getArg("store", IType.BOOL) : null;
			return new ChatModel(keyToBuild, modelnameToBuild, responseFormat, frequencyPenalty, maxCompletionTokens, maxRetries, maxTokens, presencePenalty, seed, store, temperature, timeout, topP);
			
		} else {
			final String responseFormat = scope.hasArg("responseFormat") ? (String) scope.getArg("responseFormat", IType.STRING) : null;
			final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
			final Integer numCtx = scope.hasArg("numCtx") ? (Integer) scope.getArg("numCtx", IType.INT) : null;
			final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
			final Double topP = scope.hasArg("topP") ? (Double) scope.getArg("topP", IType.FLOAT) : null;
			final Integer numPredict = scope.hasArg("numPredict") ? (Integer) scope.getArg("numPredict", IType.INT) : null;
			final Double repeatPenalty = scope.hasArg("repeatPenalty") ? (Double) scope.getArg("repeatPenalty", IType.FLOAT) : null;
			final Integer topK = scope.hasArg("topK") ? (Integer) scope.getArg("topK", IType.INT) : null;
			return new ChatModel(modelnameToBuild, urlToBuild, responseFormat, numCtx, seed, temperature, topP, numPredict, repeatPenalty, topK);
		} 

	}

	@action(name = "create_chat_memory", args = {@arg(name = "llm", type = ChatModelType.id, doc = @doc("llm ai")),
			@arg(name = "init_memory", type = IType.STRING, doc = @doc("init_memory Specifies the initial content of the memory (optional)"), optional = true),
			@arg(name = "max_tokens", type = IType.INT, doc = @doc("max_tokens defines the maximum number of tokens that can be stored or retained in memory to maintain the conversation context (by default: 1000)"), optional = true)
			 }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Memory create_chat_memory(final IScope scope) {
		final ChatModel chatModel = (ChatModel) scope.getArg("llm", ChatModelType.id);
		final String msgToAdd = (String) scope.getArg("init_memory", IType.STRING);
		final Integer maxT = (Integer) scope.getIntArg("max_tokens");
		Memory chatMemory = new Memory(chatModel, maxT);
		chatMemory.addToMemory(msgToAdd);
		return chatMemory;

	}
	



	
	private void addToolExecutor(IScope scope, Provider provider,String name, String description, ActionDescription executor) {
		ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
			String aname = executor.getName();
			if (scope.getModel() != null && scope.getModel().getAction(aname) != null) {
				return scope.getModel().getAction(aname).executeOn(scope).toString();
			}
			return toolExecutionRequest.arguments();
		};
		ToolSpecification toolSpecification = ToolSpecification.builder().name(name).description(description).build();
		provider.addTool(toolSpecification, toolExecutor);
		
	}
	
	@action(name = "create_tool_executor", args = {
			@arg(name = "tool_name", type = IType.STRING, doc = @doc("significant name")),
			@arg(name = "description", type = IType.STRING, doc = @doc("clear detailed description for the tool")),
			@arg(name = "execute", type = IType.ACTION, doc = @doc("action to execute")), 
			
	}, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Provider create_tool_executor(final IScope scope) {
		final Provider provider = new Provider();
		final String name = scope.getStringArg("tool_name");
		final String description = scope.getStringArg("description");
		final ActionDescription executor = (ActionDescription) scope.getArg("execute", IType.ACTION);
		addToolExecutor(scope, provider,name, description, executor);
		return provider; 

	}
	
	@action(name = "create_client_executor", args = {
			@arg(name = "client", type = MCPClientType.id, doc = @doc("significant name")) 
	}, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Provider create_tool_executor_from_client(final IScope scope) {
		final MCPClient client = (MCPClient) scope.getArg("client", MCPClientType.id);
		return client == null ? new Provider() : new Provider(client) ;

	}
	
	@action(name = "add_tool_executor", args = {
			@arg(name = "provider", type = ProviderType.id, doc = @doc("the provider to add the tool executor to")),
			@arg(name = "tool_name", type = IType.STRING, doc = @doc("significant name")),
			@arg(name = "description", type = IType.STRING, doc = @doc("clear detailed description for the tool")),
			@arg(name = "execute", type = IType.ACTION, doc = @doc("action to execute")), 
			
	}, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Provider add_tool_executor(final IScope scope) {
		final Provider provider = (Provider) scope.getArg("provider", ProviderType.id);
		final String name = scope.getStringArg("tool_name");
		final String description = scope.getStringArg("description");
		final ActionDescription executor = (ActionDescription) scope.getArg("execute", IType.ACTION);

		addToolExecutor(scope, provider,name, description, executor);
		return provider; 

	}
	

	@action(name = "fetch_chat_memory", args = {
			@arg(name = "memory", type = MemoryType.id, doc = @doc("memory to fetch")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public IList<String> fetch_chat_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final Memory chatMemory = (Memory) scope.getArg("memory", MemoryType.id);
		IList<String> msgs = GamaListFactory.create();
		chatMemory.getMemory().messages().stream().forEach((c) -> msgs.add(c.toString()));

		return msgs;

	}

	@action(name = "add_to_chat_memory", args = {
			@arg(name = "memory", type = MemoryType.id, doc = @doc("command to execute")) ,
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute"))
			}, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String add_to_chat_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final Memory chatMemory = (Memory) scope.getArg("memory", MemoryType.id);
		if (chatMemory != null) {
			chatMemory.addToMemory(msgToAdd);
		}

		return msgToAdd;

	}
	
	@action(name = "send_to_llm_without_memory", args = { @arg(name = "llm", type = ChatModelType.id, doc = @doc("command to execute"), optional = false),
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute"), optional = false) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String send_to_llm_without_memory(final IScope scope) {
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final ChatModel model = (ChatModel) scope.getArg("llm", ChatModelType.id);
		if (model != null) {
			return model.askQuestion(msgToAdd, false, false, false);
		}

		return "";

	}

	@action(name = "send_to_llm", args = { @arg(name = "llm", type = ChatModelType.id, doc = @doc("command to execute"), optional = false),
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute"), optional = false),
			@arg(name = "add_message_to_memory", type = IType.BOOL, doc = @doc("command to execute"),  optional = true) ,
					@arg(name = "add_answer_to_memory", type = IType.BOOL, doc = @doc("command to execute"),  optional = true) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String send_to_llm(final IScope scope) {
		boolean addPromptToMemory = scope.hasArg("add_message_to_memory") ? scope.getBoolArg("add_message_to_memory") :false;
		boolean addAnswerToMemory = scope.hasArg("add_answer_to_memory") ? scope.getBoolArg("add_answer_to_memory") :false;
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final ChatModel model = (ChatModel) scope.getArg("llm", ChatModelType.id);
		if (model != null) {
			return model.askQuestion(msgToAdd, true, addPromptToMemory, addAnswerToMemory);
		}

		return "";

	}

	@action(name = "send_to_assistant", args = {
			@arg(name = "assistant", type = AssistantType.id, doc = @doc("command to execute")),
			@arg(name = "message", type = IType.STRING, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public String send_to_assistant(final IScope scope) {

		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final Assistant assistant = (Assistant) scope.getArg("assistant",  AssistantType.id);
		return assistant.askQuestion(msgToAdd);

	}
	


	@action(name = "create_mcp_transport", args = {
			@arg(name = "url", type = IType.STRING, doc = @doc("command to execute")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public MCPTransport create_mcp_transport(final IScope scope) {

		final String urlToAdd = (String) scope.getArg("url", IType.STRING);
		final Integer timeout = (Integer) scope.getArg("timeout", IType.INT);

		return new MCPTransport(urlToAdd, timeout);

	}

	@action(name = "create_mcp_client", args = {
			@arg(name = "transport", type =  MCPTransportType.id, doc = @doc("command to execute")) }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Object create_mcp_client(final IScope scope) {

		final MCPTransport transport = (MCPTransport) scope.getArg("transport", MCPTransportType.id);

		McpClient mcpClient = new DefaultMcpClient.Builder().transport(transport.getTransport()).build();

		return mcpClient;

	}


	
	
	@action(name = "create_assistant", args = { @arg(name = "llm", type = ChatModelType.id, doc = @doc("llm ai"), optional = false),
			@arg(name = "memory", type = MemoryType.id, doc = @doc("memory"), optional = true),
			@arg(name = "tools", type = ProviderType.id, doc = @doc("toolprovider"), optional = true), 
			@arg(name = "content_retriever", type = ContentRetrieverType.id, doc = @doc("content_retriever"), optional = true), }, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public Assistant create_assistant(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final ChatModel chatModel = (ChatModel) scope.getArg("llm", ChatModelType.id);
		final Provider toolProvider = scope.hasArg("tools") ? (Provider) scope.getArg("tools", ProviderType.id) : null; 
		final ContentRetriever contentRetriever =  scope.hasArg("content_retriever") ? (ContentRetriever) scope.getArg("content_retriever", ContentRetrieverType.id) : null; 
		  
		final Memory memory = scope.hasArg("memory") ?(Memory) scope.getArg("memory", MemoryType.id) : null;   
		return new Assistant(chatModel, toolProvider, contentRetriever, memory);  
	}


	@action(name = "create_rag", args = { @arg(name = "path", type = IType.STRING, doc = @doc("path to rag"))
			 }, doc = @doc(value = "path to rag learn docs.", returns = "The error message if any"))
	public ContentRetriever create_rag(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String pathToAdd = (String) scope.getArg("path", IType.STRING);
		return new ContentRetriever(pathToAdd);

	}

	public static Path toPath(String relativePath) {
	//	URL fileUrl = MCPSkill.class.getClassLoader().getResource(relativePath);
		return Paths.get(relativePath);
	}


}
