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


import java.nio.file.Files;
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
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.util.FileUtils;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
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
import gama.experimental.types.ToolProvider;
import gama.experimental.types.ToolProviderType;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;

/**
 * The Class NetworkSkill.
 */

@skill(name = MCPConstants.LLM_SKILL, concept = { MCPConstants.LLM_MODEL}, doc = @doc("The " + MCPConstants.LLM_SKILL + " skill provides new features that allow agents to ask questions to a chatbot (LLM)"))
@vars({
 @variable(name = MCPConstants.LLM_MODEL, type = ChatModelType.id, init = "nil",doc = @doc("A chat model (to be built) that can answer questions and be used as a key element of the chat bot")),
	@variable(name = MCPConstants.CHAT_BOT, type = AssistantType.id, init = "nil",doc = @doc("A chat bot (to be built) that can answer questions taking into account external data (RAG) and trigger actions")),
	@variable(name = MCPConstants.CHAT_MEMORY, type = MemoryType.id, init = "nil",doc = @doc("A chat memory (to be built) that can be used to store data for the chat model"))
}) 
public class LLMSkill extends Skill {

	static {
		DEBUG.ON();
	}
	
	@action(name = "create_ollama_chat_model", args = {
			@arg(name = "model_name", type = IType.STRING, doc = @doc(" model_name specifies the exact name or identifier of the language model to be used for generating responses (e.g. 'llama3.2') ")),
			@arg(name = "url", type = IType.STRING, doc = @doc("url specifies the endpoint URL of the local or remote Ollama server that the model communicates with (for Ollama)")), // "http://localhost:11434"
			@arg(name = "response_format", type = IType.STRING, doc = @doc("response_format specifies the format in which the model should return its output, such as plain text or structured JSON. 2 possible values: 'json' or 'text' (by default)")),
			@arg(name = "num_ctx", type = IType.INT, doc = @doc("num_ctx specifies the maximum number of context tokens the model can use to process a prompt, including instructions, documents, and conversation history (for Ollama)")),
			@arg(name = "num_predict", type = IType.INT, doc = @doc("num_predict specifies the maximum number of tokens the model is allowed to generate in its response (for Ollama)")),
			@arg(name = "repeat_penalty", type = IType.FLOAT, doc = @doc("repeat_penalty controls how strongly the model is discouraged from repeating the same tokens or phrases in its response (for Ollama)")),
			@arg(name = "seed", type = IType.INT, doc = @doc("seed sets the random number generator seed to make the model’s output deterministic and reproducible")),
			@arg(name = "temperature", type = IType.FLOAT, doc = @doc("temperature controls the randomness of the model’s output, with higher values producing more creative and varied responses")),
			@arg(name = "top_k", type = IType.INT, doc = @doc("top_k limits the model’s token selection to the top K most probable tokens, influencing the diversity and focus of the generated output (for Ollama)")),
			@arg(name = "top_p", type = IType.FLOAT, doc = @doc("top_p (nucleus sampling) sets the probability threshold for choosing the next token, allowing the model to sample from the most likely tokens whose cumulative probability exceeds this value"))
	}, doc = @doc(value = "Action that builds a chat model based on a connection with Ollama", returns = "The chat_model built"))
	public ChatModel create_ollama_chat_model(final IScope scope) {
		final String modelnameToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String urlToBuild = (String) scope.getArg("url", IType.STRING); 
		final String responseFormat = scope.hasArg("response_format") ? (String) scope.getArg("response_format", IType.STRING) : null;
		final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
		final Integer numCtx = scope.hasArg("num_ctx") ? (Integer) scope.getArg("num_ctx", IType.INT) : null;
		final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
		final Double topP = scope.hasArg("top_p") ? (Double) scope.getArg("top_p", IType.FLOAT) : null;
		final Integer numPredict = scope.hasArg("num_predict") ? (Integer) scope.getArg("num_predict", IType.INT) : null;
		final Double repeatPenalty = scope.hasArg("repeat_penalty") ? (Double) scope.getArg("repeat_penalty", IType.FLOAT) : null;
		final Integer topK = scope.hasArg("top_k") ? (Integer) scope.getArg("top_k", IType.INT) : null;
		return new ChatModel( modelnameToBuild, urlToBuild, responseFormat, numCtx, seed, temperature, topP, numPredict, repeatPenalty, topK);
	}
	
	@action(name = "create_openai_chat_model", args = {
			@arg(name = "model_name", type = IType.STRING, doc = @doc(" model_name specifies the exact name or identifier of the language model to be used for generating responses (e.g. 'gpt-4o-mini') ")),
			@arg(name = "key", type = IType.STRING, doc = @doc("key refers to the API key used to authenticate requests to the OpenAI service (for OpenAi)")),
			@arg(name = "response_format", type = IType.STRING, doc = @doc("response_format specifies the format in which the model should return its output, such as plain text or structured JSON. 2 possible values: 'json' or 'text' (by default)")),
			@arg(name = "seed", type = IType.INT, doc = @doc("seed sets the random number generator seed to make the model’s output deterministic and reproducible")),
			@arg(name = "temperature", type = IType.FLOAT, doc = @doc("temperature controls the randomness of the model’s output, with higher values producing more creative and varied responses")),
			@arg(name = "top_p", type = IType.FLOAT, doc = @doc("top_p (nucleus sampling) sets the probability threshold for choosing the next token, allowing the model to sample from the most likely tokens whose cumulative probability exceeds this value.")),
			@arg(name = "frequency_penalty", type = IType.FLOAT, doc = @doc("frequency_penalty reduces the likelihood of the model repeating tokens by penalizing tokens based on their frequency in the generated text (for OpenAI)")),
			@arg(name = "max_completion_tokens", type = IType.INT, doc = @doc("max_completion_tokens sets the maximum number of tokens the model can generate in its completion or response (for OpenAI)")),
			@arg(name = "max_retries", type = IType.INT, doc = @doc("max_retries specifies the maximum number of times the system will retry a failed request to the model (for OpenAI)")),
			@arg(name = "max_tokens", type = IType.INT, doc = @doc("max_tokens defines the total maximum number of tokens allowed for both the input (prompt) and the output (completion) combined (for OpenAI)")),
			@arg(name = "presence_penalty", type = IType.FLOAT, doc = @doc("presence_penalty reduces the likelihood of the model mentioning new topics or tokens that have already appeared, encouraging more diverse and novel content (for OpenAI)")),
			@arg(name = "store", type = IType.BOOL, doc = @doc("store is a boolean that indicates whether the generated data (such as embeddings or chat history) should be saved or not (for OpenAI)")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("timeout specifies the maximum amount of time the system will wait for a response from the model before aborting the request (for OpenAI)"))

	}, doc = @doc(value = "Action that builds a chat model based on a connection with OpenAI", returns = "The chat_model built"))
	public ChatModel create_openai_chat_model(final IScope scope) {
		final String modelnameToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String keyToBuild = (String) scope.getArg("key", IType.STRING);
		final String responseFormat = scope.hasArg("response_format") ? (String) scope.getArg("response_format", IType.STRING) : null;
		final Double frequencyPenalty = scope.hasArg("frequency_penalty") ? (Double) scope.getArg("frequency_penalty", IType.FLOAT) : null;
		final Integer maxCompletionTokens = scope.hasArg("max_completion_tokens") ? (Integer) scope.getArg("max_completion_tokens", IType.INT) : null;
		final Integer maxRetries = scope.hasArg("max_retries") ? (Integer) scope.getArg("max_retries", IType.INT) : null;
		final Integer maxTokens = scope.hasArg("max_tokens") ? (Integer) scope.getArg("max_tokens", IType.INT) : null;
		final Double presencePenalty = scope.hasArg("presence_penalty") ? (Double) scope.getArg("presence_penalty", IType.FLOAT) : null;
		final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
		final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
		final Double topP = scope.hasArg("top_p") ? (Double) scope.getArg("top_p", IType.FLOAT) : null;
		final Integer timeout = scope.hasArg("timeout") ? (Integer) scope.getArg("timeout", IType.INT) : null;
		final Boolean store = scope.hasArg("store") ? (Boolean) scope.getArg("store", IType.BOOL) : null;
		return new ChatModel(keyToBuild, modelnameToBuild, responseFormat, frequencyPenalty, maxCompletionTokens, maxRetries, maxTokens, presencePenalty, seed, store, temperature, timeout, topP);
		
	}
	
	@action(name = "create_chat_model", args = {
			@arg(name = "model_type", type = IType.STRING, doc = @doc("model_type specifies the chat model type: 'openai' or 'ollama'")),
			@arg(name = "model_name", type = IType.STRING, doc = @doc("model_name specifies the exact name or identifier of the language model to be used for generating responses (e.g. 'gpt-4o-mini')")),
			@arg(name = "key", type = IType.STRING, doc = @doc("key refers to the API key used to authenticate requests to the OpenAI service (for OpenAi)")),
			@arg(name = "response_format", type = IType.STRING, doc = @doc("response_format specifies the format in which the model should return its output, such as plain text or structured JSON. 2 possible values: 'json' or 'text' (by default)")),
			@arg(name = "seed", type = IType.INT, doc = @doc("seed sets the random number generator seed to make the model’s output deterministic and reproducible")),
			@arg(name = "temperature", type = IType.FLOAT, doc = @doc("temperature controls the randomness of the model’s output, with higher values producing more creative and varied responses")),
			@arg(name = "top_p", type = IType.FLOAT, doc = @doc("top_p (nucleus sampling) sets the probability threshold for choosing the next token, allowing the model to sample from the most likely tokens whose cumulative probability exceeds this value.")),
			@arg(name = "frequency_penalty", type = IType.FLOAT, doc = @doc("frequency_penalty reduces the likelihood of the model repeating tokens by penalizing tokens based on their frequency in the generated text (for OpenAI)")),
			@arg(name = "max_completion_tokens", type = IType.INT, doc = @doc("max_completion_tokens sets the maximum number of tokens the model can generate in its completion or response (for OpenAI)")),
			@arg(name = "max_retries", type = IType.INT, doc = @doc("max_retries specifies the maximum number of times the system will retry a failed request to the model (for OpenAI)")),
			@arg(name = "max_tokens", type = IType.INT, doc = @doc("max_tokens defines the total maximum number of tokens allowed for both the input (prompt) and the output (completion) combined (for OpenAI)")),
			@arg(name = "presence_penalty", type = IType.FLOAT, doc = @doc("presence_penalty reduces the likelihood of the model mentioning new topics or tokens that have already appeared, encouraging more diverse and novel content (for OpenAI)")),
			@arg(name = "store", type = IType.BOOL, doc = @doc("tore is a boolean that indicates whether the generated data (such as embeddings or chat history) should be saved or not (for OpenAI)")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("timeout specifies the maximum amount of time the system will wait for a response from the model before aborting the request (for OpenAI)")),
			@arg(name = "url", type = IType.STRING, doc = @doc("url specifies the endpoint URL of the local or remote Ollama server that the model communicates with (for Ollama)")), // "http://localhost:11434"
			@arg(name = "num_ctx", type = IType.INT, doc = @doc("num_ctx specifies the maximum number of context tokens the model can use to process a prompt, including instructions, documents, and conversation history (for Ollama)")),
			@arg(name = "num_predict", type = IType.INT, doc = @doc("num_predict specifies the maximum number of tokens the model is allowed to generate in its response (for Ollama)")),
			@arg(name = "repeat_penalty", type = IType.FLOAT, doc = @doc("repeat_penalty controls how strongly the model is discouraged from repeating the same tokens or phrases in its response. (for Ollama)")),
			@arg(name = "top_k", type = IType.INT, doc = @doc(" top_k limits the model’s token selection to the top K most probable tokens, influencing the diversity and focus of the generated output (for Ollama)"))
			
	}, doc = @doc(value = "Action that builds a chat model", returns = "The chat_model built"))
	public ChatModel create_chat_model(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String modelType = (String) scope.getArg("model_type", IType.STRING);
		final String modelnameToBuild = (String) scope.getArg("model_name", IType.STRING);
		final String urlToBuild = (String) scope.getArg("url", IType.STRING); 
		final String keyToBuild = (String) scope.getArg("key", IType.STRING);

		if ("openai".equals(modelType)) {
			final String responseFormat = scope.hasArg("response_format") ? (String) scope.getArg("response_format", IType.STRING) : null;
			final Double frequencyPenalty = scope.hasArg("frequency_penalty") ? (Double) scope.getArg("frequency_penalty", IType.FLOAT) : null;
			final Integer maxCompletionTokens = scope.hasArg("max_completion_tokens") ? (Integer) scope.getArg("max_completion_tokens", IType.INT) : null;
			final Integer maxRetries = scope.hasArg("max_retries") ? (Integer) scope.getArg("max_retries", IType.INT) : null;
			final Integer maxTokens = scope.hasArg("max_tokens") ? (Integer) scope.getArg("max_tokens", IType.INT) : null;
			final Double presencePenalty = scope.hasArg("presence_penalty") ? (Double) scope.getArg("presence_penalty", IType.FLOAT) : null;
			final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
			final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
			final Double topP = scope.hasArg("top_p") ? (Double) scope.getArg("top_p", IType.FLOAT) : null;
			final Integer timeout = scope.hasArg("timeout") ? (Integer) scope.getArg("timeout", IType.INT) : null;
			final Boolean store = scope.hasArg("store") ? (Boolean) scope.getArg("store", IType.BOOL) : null;
			return new ChatModel(keyToBuild, modelnameToBuild, responseFormat, frequencyPenalty, maxCompletionTokens, maxRetries, maxTokens, presencePenalty, seed, store, temperature, timeout, topP);
			
		} else {
			final String responseFormat = scope.hasArg("response_format") ? (String) scope.getArg("response_format", IType.STRING) : null;
			final Integer seed = scope.hasArg("seed") ? (Integer) scope.getArg("seed", IType.INT) : null;
			final Integer numCtx = scope.hasArg("num_ctx") ? (Integer) scope.getArg("num_ctx", IType.INT) : null;
			final Double temperature = scope.hasArg("temperature") ? (Double) scope.getArg("temperature", IType.FLOAT) : null;
			final Double topP = scope.hasArg("top_p") ? (Double) scope.getArg("top_p", IType.FLOAT) : null;
			final Integer numPredict = scope.hasArg("num_predict") ? (Integer) scope.getArg("num_predict", IType.INT) : null;
			final Double repeatPenalty = scope.hasArg("repeat_penalty") ? (Double) scope.getArg("repeat_penalty", IType.FLOAT) : null;
			final Integer topK = scope.hasArg("top_k") ? (Integer) scope.getArg("top_k", IType.INT) : null;
			return new ChatModel(modelnameToBuild, urlToBuild, responseFormat, numCtx, seed, temperature, topP, numPredict, repeatPenalty, topK);
		} 

	}

	@action(name = "create_chat_memory", args = {@arg(name = "llm", type = ChatModelType.id, doc = @doc("llm specifies the chat model to which the memory will be linked")),
			@arg(name = "init_memory", type = IType.STRING, doc = @doc("init_memory Specifies the initial content of the memory (by default, blank)"), optional = true),
			@arg(name = "max_tokens", type = IType.INT, doc = @doc("max_tokens defines the maximum number of tokens that can be stored or retained in memory to maintain the conversation context (by default: 1000)"), optional = true)
			 }, doc = @doc(value = "Action that builds a memory linked to a chat model — the memory will be used to construct the message sent to the chat model", returns = "The memory built"))
	public Memory create_chat_memory(final IScope scope) {
		final ChatModel chatModel = (ChatModel) scope.getArg("llm", ChatModelType.id);
		final String msgToAdd = (String) scope.getArg("init_memory", IType.STRING);
		final Integer maxT = (Integer) scope.getIntArg("max_tokens");
		Memory chatMemory = new Memory(chatModel, maxT);
		chatMemory.addToMemory(msgToAdd);
		return chatMemory;

	}

	@action(name = "create_tool_executor", args = {
			@arg(name = "tool_name", type = IType.STRING, doc = @doc("name defines the unique identifier used to reference the tool when it is called by the assistant")),
			@arg(name = "description", type = IType.STRING, doc = @doc(", description provides a brief explanation of the tool’s purpose to help the assistant understand when and how to use it")),
			@arg(name = "execute", type = IType.ACTION, doc = @doc("execute specifies the GAMA action that must be triggered by the assistant")), 
	}, doc = @doc(value = "Action that builds a tool_provider in charge of executing a GAMA action when it is invoked by the assistant during a conversation", returns = "The tool_provider built"))
	public ToolProvider create_tool_executor(final IScope scope) {
		final ToolProvider provider = new ToolProvider();
		final String name = scope.getStringArg("tool_name");
		final String description = scope.getStringArg("description");
		final ActionDescription executor = (ActionDescription) scope.getArg("execute", IType.ACTION);
		provider.addToolExecutor(scope,name, description, executor);
		return provider; 

	}

	@action(name = "create_tool_executor_from_json", args = {
			@arg(name = "json", type = IType.STRING, doc = @doc("name defines the unique identifier used to reference the tool when it is called by the assistant")),
			@arg(name = "execute", type = IType.ACTION, doc = @doc("execute specifies the GAMA action that must be triggered by the assistant")), 
	}, doc = @doc(value = "Action that builds a tool_provider in charge of executing a GAMA action when it is invoked by the assistant during a conversation", returns = "The tool_provider built"))
	public ToolProvider create_tool_executor_from_json(final IScope scope) {
		final ToolProvider provider = new ToolProvider();
		final String json = scope.getStringArg("json");  
		final ActionDescription executor = (ActionDescription) scope.getArg("execute", IType.ACTION);
		provider.addToolExecutor(scope,json, executor);
		return provider; 

	}
	
	@action(name = "add_tool_executor", args = {
			@arg(name = "provider", type = ToolProviderType.id, doc = @doc("provider specifies the tool provider to which the tool executor should be added")),
			@arg(name = "tool_name", type = IType.STRING, doc = @doc("name defines the unique identifier used to reference the tool when it is called by the assistant")),
			@arg(name = "description", type = IType.STRING, doc = @doc(", description provides a brief explanation of the tool’s purpose to help the assistant understand when and how to use it")),
			@arg(name = "execute", type = IType.ACTION, doc = @doc("execute specifies the GAMA action that must be triggered by the assistant"))
			
	}, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public ToolProvider add_tool_executor(final IScope scope) {
		final ToolProvider provider = (ToolProvider) scope.getArg("provider", ToolProviderType.id);
		final String name = scope.getStringArg("tool_name");
		final String description = scope.getStringArg("description");
		final ActionDescription executor = (ActionDescription) scope.getArg("execute", IType.ACTION);
		provider.addToolExecutor(scope,name, description, executor);
		return provider; 

	}

	
	@action(name = "add_tool_executor_from_json", args = {
			@arg(name = "provider", type = ToolProviderType.id, doc = @doc("provider specifies the tool provider to which the tool executor should be added")),
			@arg(name = "json", type = IType.STRING, doc = @doc("name defines the unique identifier used to reference the tool when it is called by the assistant")),
			@arg(name = "execute", type = IType.ACTION, doc = @doc("execute specifies the GAMA action that must be triggered by the assistant"))
			
	}, doc = @doc(value = "Action that executes a command in the OS, as if it is executed from a terminal.", returns = "The error message if any"))
	public ToolProvider add_tool_executor_by_json(final IScope scope) {
		final ToolProvider provider = (ToolProvider) scope.getArg("provider", ToolProviderType.id);
		final String json = scope.getStringArg("json"); 
		final ActionDescription executor = (ActionDescription) scope.getArg("execute", IType.ACTION);
		provider.addToolExecutor(scope,json, executor);
		return provider; 

	}
	
	
	@action(name = "create_client_executor", args = {
			@arg(name = "client", type = MCPClientType.id, doc = @doc("client specifies the mcp_client used to connect the assistant to the external tool")) 
	}, doc = @doc(value = "Action that builds a tool_provider in charge of executing an external tool  when it is invoked by the assistant during a conversation", returns = "The tool_provider built"))
	public ToolProvider create_tool_executor_from_client(final IScope scope) {
		final MCPClient client = (MCPClient) scope.getArg("client", MCPClientType.id);
		return client == null ? new ToolProvider() : new ToolProvider(client) ;

	}
	


	@action(name = "fetch_memory", args = {
			@arg(name = "memory", type = MemoryType.id, doc = @doc("memory specifies the memory to fetch")) }, 
			doc = @doc(value = "Action that returns the contents of the memory as a list of strings", returns = "A list of strings representing the contents of the memory"))
	public IList<String> fetch_chat_memory(final IScope scope) {
		final Memory chatMemory = (Memory) scope.getArg("memory", MemoryType.id);
		IList<String> msgs = GamaListFactory.create();
		chatMemory.getMemory().messages().stream().forEach((c) -> msgs.add(c.toString()));

		return msgs;

	}

	@action(name = "add_to_memory", args = {
			@arg(name = "memory", type = MemoryType.id, doc = @doc("memory specifies the memory to which the message will be added")) ,
			@arg(name = "message", type = IType.STRING, doc = @doc("message that will added to the memory"))
			}, doc = @doc(value = "Action that adds a message to a memory", returns = "The message added"))
	public String add_to_chat_memory(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final Memory chatMemory = (Memory) scope.getArg("memory", MemoryType.id);
		if (chatMemory != null) {
			chatMemory.addToMemory(msgToAdd);
		}

		return msgToAdd;

	}
	
	@action(name = "send_to_llm_without_memory", args = { @arg(name = "llm", type = ChatModelType.id, doc = @doc("llm specifies the chat model that will respond to the given message"), optional = false),
			@arg(name = "message", type = IType.STRING, doc = @doc("message speficies the prompt sent to the chat model"), optional = false) }, 
			doc = @doc(value = "Action that sends a message (prompt) to a chat_model without taking into account the memory of the chat model", returns = "The message returned by the chat model"))
	public String send_to_llm_without_memory(final IScope scope) {
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final ChatModel model = (ChatModel) scope.getArg("llm", ChatModelType.id);
		if (model != null) {
			return model.askQuestion(msgToAdd, false, false, false);
		}

		return "";

	}

	@action(name = "send_to_llm", args = { @arg(name = "llm", type = ChatModelType.id, doc = @doc("llm specifies the chat model that will respond to the given message"), optional = false),
			@arg(name = "message", type = IType.STRING, doc = @doc("message speficies the prompt sent to the chat model"), optional = false),
			@arg(name = "add_message_to_memory", type = IType.BOOL, doc = @doc("add_message_to_memory specifies if the message sent has to be added to the memory or not"),  optional = true) ,
					@arg(name = "add_answer_to_memory", type = IType.BOOL, doc = @doc("add_answer_to_memory specifies if the message answered by the chat model has to be added to the memory or not"),  optional = true) }, 
			doc = @doc(value = "Action that sends a message (prompt) to a chat_model with taking into account the memory of the chat model", returns = "The message returned by the chat model"))
	public String send_to_llm(final IScope scope) {
		boolean addPromptToMemory = scope.hasArg("add_message_to_memory") ? scope.getBoolArg("add_message_to_memory") :false;
		boolean addAnswerToMemory = scope.hasArg("add_answer_to_memory") ? scope.getBoolArg("add_answer_to_memory") :false;
		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final ChatModel model = (ChatModel) scope.getArg("llm", ChatModelType.id);
		if (model != null) {
			return model.askQuestion(msgToAdd, true, addPromptToMemory, addAnswerToMemory);

//			return model.askQuestion(msgToAdd, true, addPromptToMemory, addAnswerToMemory).replaceAll("(?s)<think>.*?</think>", "");
		}

		return "";

	}

	@action(name = "send_to_assistant", args = {
			@arg(name = "assistant", type = AssistantType.id, doc = @doc("assistant specifies the assistant that will respond to the given message")),
			@arg(name = "message", type = IType.STRING, doc = @doc("message speficies the prompt sent to the assistant")) }, 
			doc = @doc(value = "Action that sends a message (prompt) to an assistant", returns = "The message returned by the assistant"))
	public String send_to_assistant(final IScope scope) {

		final String msgToAdd = (String) scope.getArg("message", IType.STRING);
		final Assistant assistant = (Assistant) scope.getArg("assistant",  AssistantType.id);
		return assistant.askQuestion(msgToAdd);

	}
	


	@action(name = "create_mcp_transport", args = {
			@arg(name = "url", type = IType.STRING, doc = @doc("url specifies the endpoint URL used to receive server-sent events (SSE) for real-time communication with the assistant")),
			@arg(name = "timeout", type = IType.INT, doc = @doc("timeout defines the maximum duration the client will wait for a response before the request is aborted")) }, 
			doc = @doc(value = "Action that builds a mcp_transport, that defines how messages are exchanged between an agent and an assistant, allowing customizable communication mechanisms", returns = "The mcp_transport built"))
	public MCPTransport create_mcp_transport(final IScope scope) {

		final String urlToAdd = (String) scope.getArg("url", IType.STRING);
		final Integer timeout = (Integer) scope.getArg("timeout", IType.INT);

		return new MCPTransport(urlToAdd, timeout);

	}

	@action(name = "create_mcp_client", args = {
			@arg(name = "transport", type =  MCPTransportType.id, doc = @doc("transport specifies the mcp_transport used for message exchanged")) }, 
			doc = @doc(value = "Action that builds a mcp_client, that manages interactions with an assistant by sending messages through the specified mcp_transport and handling the responses", returns = "The mcp_client built"))
	public Object create_mcp_client(final IScope scope) {
		final MCPTransport transport = (MCPTransport) scope.getArg("transport", MCPTransportType.id);
		McpClient mcpClient = new DefaultMcpClient.Builder().transport(transport.getTransport()).build();
		return mcpClient;
	}
 
	
	@action(name = "create_assistant", args = { @arg(name = "llm", type = ChatModelType.id, doc = @doc("llm specifies the chat model used by the assistant"), optional = false),
			@arg(name = "memory", type = MemoryType.id, doc = @doc("memory specifies the chat memory used by the assistant (optional)"), optional = true),
			@arg(name = "tool_provider", type = ToolProviderType.id, doc = @doc("tool_provider specifies the tools used by the assistant (optional)"), optional = true), 
			@arg(name = "content_retriever", type = ContentRetrieverType.id, doc = @doc("content_retriever specifies the content retriever(RAG) used by the assistant (optional)"), optional = true), }, 
			doc = @doc(value = "Action that builds an LLM assistant, enabling structured interactions with chat models by incorporating memory, RAG, and executor tools", returns = "The assistant built"))
	public Assistant create_assistant(final IScope scope) {
		// final IAgent agent = scope.getAgent();
		final ChatModel chatModel = (ChatModel) scope.getArg("llm", ChatModelType.id);
		final ToolProvider toolProvider = scope.hasArg("tool_provider") ? (ToolProvider) scope.getArg("tool_provider", ToolProviderType.id) : null; 
		final ContentRetriever contentRetriever =  scope.hasArg("content_retriever") ? (ContentRetriever) scope.getArg("content_retriever", ContentRetrieverType.id) : null; 
		  
		final Memory memory = scope.hasArg("memory") ?(Memory) scope.getArg("memory", MemoryType.id) : null;   
		return new Assistant(chatModel, toolProvider, contentRetriever, memory);  
	}


	@action(name = "create_rag", args = { @arg(name = "directoty_path", type = IType.STRING, doc = @doc("path of the directory containing the data"))
			 }, doc = @doc(value = "Action that builds a content_retriever contaning the data loaded from the directory path", returns = "A content_retriever (RAG) with the specified data; Return nil if the folder does not exist"))
	public ContentRetriever create_rag(final IScope scope) {
		final String pathToAdd = (String) scope.getArg("directoty_path", IType.STRING);
		String pathToAddAP = FileUtils.constructAbsoluteFilePath(GAMA.getRuntimeScope(), pathToAdd,true);
		Path p = Paths.get(pathToAddAP);
		if (Files.exists(p) && Files.isDirectory(p)) 
			return new ContentRetriever(p);
		GamaRuntimeException.error(pathToAddAP + " does not exist or is not a directory", scope);
		return null;

	}


}
