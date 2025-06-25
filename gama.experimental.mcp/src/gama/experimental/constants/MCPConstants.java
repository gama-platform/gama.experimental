/*******************************************************************************************************
 *
 * INetworkSkill.java, in gama.network, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.experimental.constants;

/**
 * The Interface INetworkSkill.
 */
public interface MCPConstants {
	
	
	public static final String MEMORY = "memory";
	
	public static final String LLM_MODEL = "llm";
	
	public static final String CHAT_BOT = "chat_bot";
	
	/** The connect topic. */
	public static final String CONNECT_TOPIC = "connect";

	/** The server url. */
	public static final String SERVER_URL = "to";
	

	/** The server url. */
	public static final String CHAT_MEMORY = "chat_memory";
	
	public static final String SYSTEM_MESSAGE="system_message";
	
	/** The base url. */
	public static final String BASE_URL = "baseUrl";

	
	/** The llm skill. */
	public static final String LLM_SKILL = "llm";
	
	/** The fetch message. */
	public static final String FETCH_MESSAGE = "fetch_message";
	
	/** The has more message in box. */
	public static final String HAS_MORE_MESSAGE_IN_BOX = "has_more_message";

	/** The simulate step. */
	// SKILL TEST
	 public static final String FETCH_MESSAGE_FROM_NETWORK = "fetch_message_from_network";
	
	
	
    public static final String MODEL_TYPE = "model_type";
    public static final String MODEL_NAME = "model_name";
    public static final String OLLAMA_URL = "ollama_url";
    public static final String API_KEY = "key";
    public static final String RESPONSE_FORMAT = "response_format";
    public static final String NUM_CTX = "num_ctx";
    public static final String NUM_PREDICT = "num_predict";
    public static final String REPEAT_PENALTY = "repeat_penalty";
    
    public static final String SEED = "seed";
    public static final String TEMPERATURE = "temperature";
    public static final String TOP_K = "top_p";
    public static final String TOP_P = "top_k";
    public static final String FREQUENCY_PENALTY = "frequency_penalty";
    public static final String MAX_COMPLETION_TOKENS = "max_completion_tokens";
    public static final String MAX_RETRIES = "max_retries";
    
    public static final String MAX_TOKENS = "max_tokens";
    public static final String PRESENCE_PENALTY = "presencePenalty";
    public static final String STORE = "store";
    public static final String TIME_OUT = "timeout";
}
