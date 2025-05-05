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
package gama.experimental.mcpskill;

/**
 * The Interface INetworkSkill.
 */
public interface IMCPSkill {
	
	/** The connect topic. */
	String CONNECT_TOPIC = "connect";

	/** The server url. */
	String SERVER_URL = "to";
	

	/** The server url. */
	String LLM_MODEL = "llm";
	/** The server url. */
	String CHAT_MEMORY = "chat_memory";
	
	String SYSTEM_MESSAGE="systemMessage";
	
	/** The base url. */
	String BASE_URL = "baseUrl";

	/** The model name. */
	String MODEL_NAME = "modelName";
	
	/** The login. */
	String LOGIN = "login";
	
	/** The password. */
	String PASSWORD = "password";
	
	/** The withname. */
	String WITHNAME = "with_name";
	
	/** The protocol. */
	String PROTOCOL = "protocol";
	
	/** The port. */
	String PORT = "port";

	/** The net agent name. */
	// Agent Data
	String NET_AGENT_NAME = "network_name";
	 
	/** For HTTP requests. */
	String HTTP_REQUEST = "http";

	/** The network skill. */
	///// SKILL NETWORK
	String MCP_SKILL = "mcp_skill";
	
	/** The fetch message. */
	String FETCH_MESSAGE = "fetch_message";
	
	/** The has more message in box. */
	String HAS_MORE_MESSAGE_IN_BOX = "has_more_message";

	/** The simulate step. */
	// SKILL TEST
	String FETCH_MESSAGE_FROM_NETWORK = "fetch_message_from_network";
}
