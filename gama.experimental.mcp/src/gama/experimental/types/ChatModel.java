/*******************************************************************************************************
 *
 * Predicate.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.experimental.types;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel.OllamaChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.experimental.constants.MCPConstants;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Predicate.
 */
@vars ({ @variable(name = MCPConstants.MODEL_TYPE, type = IType.STRING, doc = @doc("LLM type: openai or ollama")),
	@variable(name = MCPConstants.MODEL_NAME, type = IType.STRING, doc = @doc("model to use gpt-4o-mini,llama3.2 ... ")),
	@variable(name = MCPConstants.MEMORY, type = IType.STRING, doc = @doc("model to use gpt-4o-mini,llama3.2 ... ")),
	@variable(name = MCPConstants.OLLAMA_URL, type = IType.STRING, doc = @doc("URL of LLM (for Ollama)")), // "http://localhost:11434"
	@variable(name = MCPConstants.API_KEY, type = IType.STRING, doc = @doc("API Key (for OpenAi)")),
	@variable(name = MCPConstants.RESPONSE_FORMAT, type = IType.STRING, doc = @doc("responseFormat:'json', 'text' by default")),
	@variable(name = MCPConstants.NUM_CTX, type = IType.INT, doc = @doc("numCtx (for Ollama)")),
	@variable(name = MCPConstants.NUM_PREDICT, type = IType.INT, doc = @doc("numPredict (for Ollama)")),
	@variable(name = MCPConstants.REPEAT_PENALTY, type = IType.FLOAT, doc = @doc("repeatPenalty (for Ollama)")),
	@variable(name = MCPConstants.SEED, type = IType.INT, doc = @doc("seed")),
	@variable(name = MCPConstants.TEMPERATURE, type = IType.FLOAT, doc = @doc("temperature")),
	@variable(name = MCPConstants.TOP_K, type = IType.INT, doc = @doc("topK (for Ollama)")),
	@variable(name = MCPConstants.TOP_P, type = IType.FLOAT, doc = @doc("topP")),
	@variable(name = MCPConstants.FREQUENCY_PENALTY, type = IType.FLOAT, doc = @doc("frequencyPenalty (for OpenAI)")),
	@variable(name = MCPConstants.MAX_COMPLETION_TOKENS, type = IType.INT, doc = @doc("maxCompletionTokens (for OpenAI)")),
	@variable(name = MCPConstants.MAX_RETRIES, type = IType.INT, doc = @doc("maxRetries (for OpenAI)")),
	@variable(name = MCPConstants.MAX_TOKENS, type = IType.INT, doc = @doc("maxTokens (for OpenAI)")),
	@variable(name = MCPConstants.PRESENCE_PENALTY, type = IType.FLOAT, doc = @doc("presencePenalty (for OpenAI)")),
	@variable(name = MCPConstants.STORE, type = IType.BOOL, doc = @doc("presencePenalty (for OpenAI)")),
	@variable(name = MCPConstants.TIME_OUT, type = IType.INT, doc = @doc("timeout (for OpenAI)"))})
public class ChatModel implements IValue {

	private String modelType;
	private String modelName;
	private String OllamaUrl;
	private String ApiKey;
	private String responseFormat;
	private Integer numCtx;
	private Integer numPredict;
	private Double repeatPenalty;
	private Integer seed;
	private Double temperature;
	private Integer topK;
	private Double topP;
	private Double frequencyPenalty;
	private Integer maxCompletionTokens;
	private Integer maxRetries;
	private Integer maxTokens;
	private Double presencePenalty;
	private Boolean store;
	private Integer timeOut;
	private dev.langchain4j.model.chat.ChatModel model;
	private Memory memory;
	
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(),"model", model);
	}
	
	public ChatModel(String modelnameToBuild, String urlToBuild, String responseFormat,Integer numCtx, Integer seed, Double temperature, Double topP, Integer numPredict, Double repeatPenalty,  Integer topK) {
		super();
		OllamaChatModelBuilder modelTobuild = OllamaChatModel.builder().baseUrl(urlToBuild)
				.modelName(modelnameToBuild);// "llama3.2"

		if (responseFormat != null) {
			modelTobuild = modelTobuild.responseFormat(responseFormat.toString().toLowerCase().equals("json")
					? dev.langchain4j.model.chat.request.ResponseFormat.JSON
					: dev.langchain4j.model.chat.request.ResponseFormat.TEXT);

			this.responseFormat = responseFormat;
		}
		
		if (numCtx != null) {
			modelTobuild = modelTobuild.numCtx(numCtx);
			this.numCtx = numCtx;
			
		}
		if (numPredict != null) {
			modelTobuild = modelTobuild.numPredict(numPredict);
			this.numPredict = numPredict;
		}
		if (repeatPenalty != null) {
			modelTobuild = modelTobuild.repeatPenalty(repeatPenalty);
			this.repeatPenalty = repeatPenalty;
		}
		
		if (seed != null) {
			modelTobuild = modelTobuild.seed(seed);
			this.seed = seed;
		}
		
		if (temperature != null) {
			modelTobuild = modelTobuild.temperature(temperature);
			this.temperature = temperature;
		}
		if (topP != null) {
			modelTobuild = modelTobuild.topP(topP);
			this.topP = topP;
		}
		if (topK != null) {
			modelTobuild = modelTobuild.topK(topK);
			this.topK = topK;
		}
		model = modelTobuild.logRequests(true).build();
		

	} 
	
	public ChatModel(String keyToBuild, String modelnameToBuild, String responseFormat, Double frequencyPenalty, Integer maxCompletionTokens, Integer maxRetries, Integer maxTokens, Double presencePenalty, Integer seed, Boolean store,  Double temperature, Integer timeout,  Double topP  ) {
		super();

		OpenAiChatModelBuilder modelTobuild = OpenAiChatModel.builder().apiKey(keyToBuild)
				.modelName(modelnameToBuild); // "gpt-4o-mini"

		if (responseFormat != null) {
			modelTobuild = modelTobuild.responseFormat(responseFormat);
			this.responseFormat = responseFormat;
		} 
		if (frequencyPenalty != null) {
			modelTobuild = modelTobuild.frequencyPenalty(frequencyPenalty);
			this.frequencyPenalty = frequencyPenalty;
		}
		if (maxCompletionTokens != null) {
			modelTobuild = modelTobuild.maxCompletionTokens(maxCompletionTokens);
			this.maxCompletionTokens = maxCompletionTokens;
		}
		if (maxRetries != null) {
			modelTobuild = modelTobuild.maxRetries(maxRetries);
			this.maxRetries = maxRetries;
		}
		if (maxTokens !=null) {
			modelTobuild = modelTobuild.maxTokens(maxTokens);
			this.maxTokens = maxTokens;
		}
		if (presencePenalty != null) {
			modelTobuild = modelTobuild.presencePenalty(presencePenalty);
			this.presencePenalty = presencePenalty;
		}
		if (seed != null) {
			modelTobuild = modelTobuild.seed(seed);
			this.seed = seed;
		}
		if (store != null) {
			modelTobuild = modelTobuild.store(store);
			this.store = store;
		}
		if (temperature != null) {
			modelTobuild = modelTobuild.temperature(temperature);
			this.temperature = temperature;
		}
		if (timeout != null) {
			modelTobuild = modelTobuild.timeout(Duration.ofSeconds(timeout));
			this.timeOut = timeout;
		}
		if (topP != null) { 
			modelTobuild = modelTobuild.topP(topP);
			this.topP = topP;
		}

		model = modelTobuild.logRequests(true).build();
	}
	
	
	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	public String askQuestion(String prompt, boolean addPromptToMemory, boolean addAnswerToMemory, boolean UseMemory) {
			
			if (UseMemory && memory != null && addPromptToMemory) 
				memory.addToMemory(prompt);
			List<ChatMessage> fullConversation = (UseMemory && memory != null) ? new ArrayList<>(memory.getMemory().messages()) : new ArrayList<>();
			UserMessage currentMessage = UserMessage.from(prompt);
			fullConversation.add(currentMessage); 
			
			ChatResponse response = model.chat(fullConversation);
			if(UseMemory && memory != null && addAnswerToMemory) {
				memory.getMemory().add(response.aiMessage());
			}
			return response.aiMessage().text();
	
	}

	@getter (MCPConstants.MEMORY)
	public Memory getMemory() {
		return memory;
	}

	
	@getter (MCPConstants.MODEL_TYPE)
	public String getModelType() {
		return modelType;
	}


	@getter (MCPConstants.MODEL_NAME)
	public String getModelName() {
		return modelName;
	}


	@getter (MCPConstants.OLLAMA_URL)
	public String getOllamaUrl() {
		return OllamaUrl;
	}


	@getter (MCPConstants.API_KEY)
	public String getApiKey() {
		return ApiKey;
	}


	@getter (MCPConstants.RESPONSE_FORMAT)
	public String getResponseFormat() {
		return responseFormat;
	}


	@getter (MCPConstants.NUM_CTX)
	public Integer getNumCtx() {
		return numCtx;
	}


	@getter (MCPConstants.NUM_PREDICT)
	public Integer getNiumPredict() {
		return numPredict;
	}


	@getter (MCPConstants.REPEAT_PENALTY)
	public Double getRepeatPenalty() {
		return repeatPenalty;
	}


	@getter (MCPConstants.SEED)
	public Integer getSeed() {
		return seed;
	}


	@getter (MCPConstants.TEMPERATURE)
	public Double getTemperature() {
		return temperature;
	}


	@getter (MCPConstants.TOP_K)
	public Integer getTopK() {
		return topK;
	}


	@getter (MCPConstants.TOP_P)
	public Double getTopP() {
		return topP;
	}


	@getter (MCPConstants.FREQUENCY_PENALTY)
	public Double getFrequencyPenalty() {
		return frequencyPenalty;
	}


	@getter (MCPConstants.MAX_COMPLETION_TOKENS)
	public Integer getMaxCompletionTokens() {
		return maxCompletionTokens;
	}


	@getter (MCPConstants.MAX_RETRIES)
	public Integer getMaxRetries() {
		return maxRetries;
	}


	@getter (MCPConstants.MAX_TOKENS)
	public Integer getMaxTokens() {
		return maxTokens;
	}


	@getter (MCPConstants.PRESENCE_PENALTY)
	public Double getPresencePenalty() {
		return presencePenalty;
	}


	@getter (MCPConstants.STORE)
	public Boolean getStore() {
		return store;
	}


	@getter (MCPConstants.TIME_OUT)
	public Integer getTimeOut() {
		return timeOut;
	}



	
	public dev.langchain4j.model.chat.ChatModel getModel() {
		return model;
	}


	public void setModel(dev.langchain4j.model.chat.ChatModel model) {
		this.model = model;
	}


	
	/**
	 * Instantiates a new predicate.
	 */
	
	public ChatModel() {
		super();
	}
	
	
	public ChatModel(ChatModel p) {
		
	}
	
	
	

	@Override
	public String toString() {
		return "ChatModel(" + model.toString() +")";
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return toString();
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	@Override
	public ChatModel copy(final IScope scope) throws GamaRuntimeException {
		return new ChatModel(this);
	}

	

	@Override
	public int hashCode() {
		return model.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final ChatModel other = (ChatModel) obj;
		return model.equals(other.model);
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */ 
	@Override
	public IType<?> getGamlType() { return Types.get(ChatModelType.id); }

}
