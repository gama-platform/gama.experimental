/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string roleMsg <- "You are a socialist";
	string msg1 <- "How do I optimize database queries for a large-scale e-commerce platform? Answer short in three to five lines maximum.";
	string msg2 <- "Give a concrete example implementation of the first point? Be short, 10 lines of code maximum.";
	list<string> msgto <- [msg1, msg2];

	action toto {
		create cricket;
	}

	init {
		create A {
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(role: roleMsg);

			unknown cr <- create_rag(path: "/Users/hqn/git/gama.experimental/gama.experimental.mcp/models/MCP/includes/documents/",filter:"*.txt");
			my_assistant <- create_assistant(llm: chat_model, memory: chat_memory,  contentRetriever: cr);
			mymsg <- send_to_assistant(assistant: my_assistant, message: "Can I cancel my reservation?");
			write mymsg;
			mymsg <- send_to_assistant(assistant: my_assistant, message: "I had an accident, should I pay extra?");
			write mymsg;
			mymsg <- send_to_assistant(assistant: my_assistant, message: "tell me resume bio of John Doe?");
			write mymsg;
			//			write "\n\n\n";
			//			write fetch_chat_memory(chat_memory);
		} } }

species A skills: [mcp_skill] {
	unknown chat_model;
	unknown chat_memory;
	unknown mcp_transport;
	unknown mcp_client;
	unknown mcp_tool;
	unknown my_bot;
	unknown my_assistant;
	string mymsg;

	reflex chating {
		do add_to_chat_memory message: mymsg memory: chat_memory;
		//		mymsg <- send_to_llm(llm: chat_model, message: msgto[cycle], with_memory: chat_memory);
		mymsg <- send_to_assistant(assistant: my_assistant, message: mymsg);
		write mymsg;
		do add_to_chat_memory message: mymsg memory: chat_memory;
	} }

species cricket {

	aspect default {
		draw cube(10) color: #red;
	}

}

experiment main type: gui { 
}
