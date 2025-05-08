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
		create people;
	}

	init {
		create A {
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(role: roleMsg);
			unknown toolSpecification <- specify_tool(tool: "add People", description: "to increase the population, it will create an agent of species people");
			unknown toolExecutor <- create_tool_executor(execute: world.toto);
			unknown toolProvider <- create_tool_provider([toolSpecification::toolExecutor]);
			//			write fetch_chat_memory(chat_memory);
			my_assistant <- create_assistant(llm: chat_model, memory: chat_memory, tools: toolProvider);
			write send_to_assistant(assistant: my_assistant, message: "i want to decrease the population ");
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
		do add_to_chat_memory message: msgto[cycle] memory: chat_memory;
		//		mymsg <- send_to_llm(llm: chat_model, message: msgto[cycle], with_memory: chat_memory);
		mymsg <- send_to_assistant(assistant: my_assistant, message: msgto[cycle]);
		write mymsg;
		do add_to_chat_memory message: mymsg memory: chat_memory;
	} }

species people {

	aspect default {
		draw cube(10) color: #red;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
			species people;
		}

	}

}
