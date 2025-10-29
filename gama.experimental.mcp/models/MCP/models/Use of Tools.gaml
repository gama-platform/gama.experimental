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
	string weather(string loc) {
		write loc;
		return "1000 Celcius";
	}

	init {
		create A {
			llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(llm,roleMsg);
			tool <- create_tool_executor(tool_name: "create a cricket",description: "it will increase but never decrease the population", execute: world.toto );
			
			tool<-add_tool_executor_by_json(provider:tool,
				json: '{
				  "name": "get_weather",
				  "description": "Returns the current weather for a specified location.",
				  "parameters": {
				    "type": "object",
				    "properties": {
				      "loc": {
				        "type": "string",
				        "description": "The city and state, e.g. San Francisco, CA"
				      }
				    },
				    "required": [
				      "loc"
				    ]
				  }
				}',
				execute: world.weather
			);
			chat_bot <- create_assistant(llm: llm, memory: chat_memory, tool_provider: tool);
			mymsg<- send_to_assistant(assistant: chat_bot, message: "what is the weather now in new york?");
			write mymsg;
		} 
	} 
}

species A skills: [llm] {
	mcp_transport transport;
	mcp_client client;
	tool_provider tool;
	string mymsg;

	reflex chating {
		do add_to_memory message: mymsg memory: chat_memory;
		mymsg <- send_to_assistant(assistant: chat_bot, message: mymsg);
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
