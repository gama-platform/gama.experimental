/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string msg0 <- "there is a fire, what do we do? structure the anwser to 0 or 1. do not output anything else.";

	init {
		write msg0;
		create A {
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory("You are a computer scientist");
			comingmsg <- msg0;
		}

		create A {
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory("You are a teenager.");
			do add_to_chat_memory message: msg0 memory: chat_memory;
		} } }

species A skills: [mcp_skill] {
	provider chat_model;
	memory chat_memory;
	string mymsg;
	string comingmsg;

	reflex chating when: comingmsg != nil {
		do add_to_chat_memory message: comingmsg memory: chat_memory;
		mymsg <- send_to_llm(   comingmsg, chat_model);
		write self;
//		write fetch_chat_memory(chat_memory);
		write mymsg;
		comingmsg <- nil;
		ask ((A as list) - self) {
			comingmsg <- myself.mymsg ;
		}

		do add_to_chat_memory message: mymsg memory: chat_memory;
	}

	aspect default {
		draw cube(10);
		draw mymsg at: location + {0, 0, 10} font: font("Helvetica", 30, #bold) color: #red;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
			species A;
		}

	}

}
