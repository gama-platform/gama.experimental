/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string
	roleMsg <- "You are a non-computer scientist explaining to another junior developer, the project you are working on is an e-commerce platform with Java back-end, " + "Oracle database, and Spring Data JPA";
	string msg1 <- "How do I optimize database queries for a large-scale e-commerce platform? Answer short in three to five lines maximum.";
	string msg2 <- "Give a concrete example implementation of the first point? Be short, 10 lines of code maximum.";
	list<string> msgto <- [msg1, msg2];

	init {
		create A {
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(role: roleMsg);
			has_memory <- false;
		}

		create A {
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(role: roleMsg);
			has_memory <- true;
		} } }

species A skills: [mcp_skill] {
	unknown chat_model;
	unknown chat_memory;
	unknown mcp_transport;
	unknown mcp_client;
	unknown mcp_tool;
	unknown my_bot;
	bool has_memory;
	string mymsg;

	reflex chating {
		write self;
		if (has_memory) {
			do add_to_chat_memory message: msgto[cycle] memory: chat_memory;
			mymsg <- send_to_llm(llm: chat_model, message: msgto[cycle], with_memory: chat_memory);
			do add_to_chat_memory message: mymsg memory: chat_memory;
		} else {
			mymsg <- send_to_llm(llm: chat_model, message: msgto[cycle]);
		}

		write mymsg;
	} 
	}

experiment main type: gui {
	output {
		display Field type: opengl {
		}

	}

}
