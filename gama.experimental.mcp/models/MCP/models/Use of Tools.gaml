/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string roleMsg <- "You are an Autonomous Problem-Solving Architect. 
Your most important capability is recognizing when your current tools are insufficient. 
If you cannot complete a step because you are missing a tool, you must not fail or give up.
Instead, you must propose a new tool for the developer to create.
By using the tool `create_a_new_tool`, you must give this tool the json string that you propose to build a new tool needed. The syntax is as same as in this example:
" + '{
							  "name": "tool name",
							  "description": "describe the tool functional",
							  "parameters": {
							    "type": "object",
							    "properties": {
							      "a parameter, the name must be iteratively of a1 a2 a3...": {
							        "type": "type of parameter",
							        "description": "describe the parameter"
							      }
							    },
							    "required": [
							      "a parameter, the name must be iteratively of a1 a2 a3..."
							    ]
							  }
							}';
	string msg1 <- "How do I optimize database queries for a large-scale e-commerce platform? Answer short in three to five lines maximum.";
	string msg2 <- "Give a concrete example implementation of the first point? Be short, 10 lines of code maximum.";
	list<string> msgto <- [msg1, msg2];

	action toto {
		create cricket;
	}

	string tata(unknown a1) {
		write "TOOL HAS BEEN CREATED: "+a1;
		return "10000";
	}

	string weather (string loc) {
		write loc;
		return "1000 Celcius";
	}

	string create_tool (string tooljson ) {
		write "******TOOL******";
		write tooljson;
		write "******TOOL******";
		//		ask A {
		//			tool <- add_tool_executor_by_json(provider: A[0].tool, json: tooljson, execute: world.tata);
		//		}
		string ret;
		create A {
			llm <- create_ollama_chat_model(url: "http://localhost:11434", model_name: llm_name);
			chat_memory <- create_chat_memory(llm, "you are assitant that use tool");
			tool <- create_tool_executor_from_json(json: tooljson, execute: world.tata);
			chat_bot <- create_assistant(llm: llm, memory: chat_memory, tool_provider: tool);
			mymsg <- send_to_assistant(assistant: chat_bot, message: request_msg);
			write "ANS from new tool>>>>>> ";
			write mymsg;
			ret<-mymsg;
			write "<<<<<<<<ANS from new tool ";
		}

		return ret;
	}

	string llm_name <- "qwen3:30b";
//	string request_msg <- "what is the weather now in new york?";
	string request_msg <- "what is the square root of 2?";

	init {
		create A {
			llm <- create_ollama_chat_model(url: "http://localhost:11434", model_name: llm_name);
			chat_memory <- create_chat_memory(llm, roleMsg);
			tool <- create_tool_executor_from_json(json: '{
				  "name": "create_a_new_tool",
				  "description": "create a tool to resolve a problem, this tool is described by a json string.",
				  "parameters": {
				    "type": "object",
				    "properties": {
				      "tooljson": {
				        "type": "string",
				        "description": "The json string that describe the tool"
				      } 
				    },
				    "required": [
				      "tooljson"
				    ]
				  }
				}', execute: world.create_tool);
			chat_bot <- create_assistant(llm: llm, memory: chat_memory, tool_provider: tool);
			mymsg <- send_to_assistant(assistant: chat_bot, message: request_msg);
			write mymsg;
		} } }

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
	} }

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
