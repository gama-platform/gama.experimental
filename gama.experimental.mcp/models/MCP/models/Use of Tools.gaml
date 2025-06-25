/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string
	roleMsg <- "You are a socialist";
	string msg1 <- "How do I optimize database queries for a large-scale e-commerce platform? Answer short in three to five lines maximum.";
	string msg2 <- "Give a concrete example implementation of the first point? Be short, 10 lines of code maximum.";
	list<string> msgto <- [msg1, msg2];

	action toto {
		create cricket;
	}

	init {
		create A {
			llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(llm,roleMsg);
			tool <- create_tool_executor(tool_name: "create a cricket",description: "it will increase but never decrease the population", execute: world.toto );
			my_assistant <- create_assistant(llm: llm, memory: chat_memory, tool_provider: tool);
			mymsg<- send_to_assistant(assistant: my_assistant, message: "i want to decrease the green house gas");
			write mymsg;
		} 
	} 
}

species A skills: [llm] {
	chat_model llm;
	memory chat_memory;
	mcp_transport transport;
	mcp_client client;
	tool_provider tool;
	assistant my_assistant;
	string mymsg;

	reflex chating {
		do add_to_memory message: mymsg memory: chat_memory;
		mymsg <- send_to_assistant(assistant: my_assistant, message: mymsg);
		write mymsg;
		do add_to_memory message: mymsg memory: chat_memory;
	} 
}

species cricket {

	aspect default {
		draw cube(10) color: #red;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
			species cricket;
		}

	}

}
