/**
* Name: MultiLingual Simple LLM Connexion test
* Author: Jean-Daniel ZUCKER and hqnghi
* Description: 
* This model simulates an exchange between two agents: a computer scientist father aware of a fire,
* and his teenage son, using an MCP-type plugin and an LLM (here, llama3.1 via Ollama).
* The "father" agent asks the language model what he should say to convince his son to join him.
* The son, in turn, replies with the help of the LLM, in a simulated dialogue between two AIs.
* The system uses `create_chat_model`, `create_chat_memory` and `send_to_llm` in a GAMA environment
* interfaced with an Ollama server running on localhost.
* 
* The language of exchange can be specified in the variable language line 25
* The discussion with the LLM is in english but then translated.
* 
* Technologies used: GAMA, Ollama (REST server on localhost:11434), LLM (llama3.1), MCP Plugin
* 
* Tags: dialogue, AI, LLM, multi-agent, GAMA, ollama, llama3, mcp
*/
model Testconnection

global {
// Name of the local LLM model used with Ollama
	string llmmodel <- "llama3.2"; // alternative: "llama3.1" ;

	// When Ollama is running in the background (ollama serve), it exposes a REST API at this address
	string ollamaPort <- "http://localhost:11434";
	string language <- "Vietnamese"; // The languauge to display the demo
	init {
	// Create the first agent: the Father
		create AI {
			role <- "<<SYS>> You are a father of one kid, aware of a fire in the building. <</SYS>> ";
			// Create a connection to the local LLM via Ollama
			llm <- create_ollama_chat_model(url: ollamaPort, model_name: llmmodel);
			chat_memory <- create_chat_memory(llm, role); // No previous conversation yet

			// The initial message sent to the LLM to start the conversation
			llm_querry <-
			" There is a fire in my buiding. My son is on another floor. I want him to escape the building. What should I tell him ? just answer the sentence I should tell him. Nothing else";

			// Send the prompt to the LLM and store its answer
			mymsg <- send_to_llm(llm, llm_querry);
			mymsglit <- send_to_llm_without_memory(llm, "respond only the translation in" + language + " of'''" + mymsg + "''. Do not add any comment.");
			
			chat_desire <- true; // Indicates that the agent wants to send a message
			icon <- image_file("../Images/father.png"); // Image to represent the father
			location <- {20, 20}; // Position in the world
		}

		// Create the second agent: the Teenager
		create AI {
			role <- "[INST] <<SYS>> You are a Teenager <</SYS>> ";
			llm <- create_ollama_chat_model(url: ollamaPort, model_name: llmmodel);
			mymsg <- ""; // No message yet
			chat_desire <- false; // Does not initiate conversation
			icon <- image_file("../Images/young.png"); // Image to represent the teenager
			location <- {20, 80}; // Position in the world
		} } }

species AI skills: [llm] {
// LLM interface and memory attributes
	string role <- ""; // Role prompt for the LLM
	string llm_querry <- ""; // Dynamic question sent to the LLM
	bool chat_desire <- false; // True if the agent wants to speak
	string mymsg <- ""; // Message to be sent
	string mymsglit <- ""; // Message in native demonstration language
	string comingmsg <- nil; // Message received from another agent
	image_file icon; // Agent icon
	geometry shape <- square(30); // Shape drawn in the simulation

	// Main behavior: simulate dialogue when a message is received or agent wants to speak
	reflex chating when: (comingmsg != nil or chat_desire) {
		if chat_desire {
		// Log the outgoing message
			write string(self) + ": message I want to send " + mymsg;

			// Send the message to the other agent
			ask ((AI as list) - self) {
				comingmsg <- myself.mymsg;
			}

			// Update the conversation history
			do add_to_memory(chat_memory, " I told him " + mymsg);
			mymsg <- "";
			chat_desire <- false; // Reset desire to talk
		}

		if comingmsg != nil {
		// Log the incoming message
			write string(self) + ": message I got : " + comingmsg;

			// Create a new query to the LLM based on the incoming message
			llm_querry <- "I receive this message " + comingmsg + " What should I reply ? just answer the sentence I should reply. Nothing else";
			mymsg <- send_to_llm(llm, llm_querry + "[/INST]");
			
			mymsglit <- send_to_llm_without_memory(llm, "respond only the translation in" + language + " of'''" + mymsg + ". [/INST]");

			// Update memory and signal desire to respond
			do add_to_memory(chat_memory,  " He then told me "  + comingmsg);
			
			chat_desire <- true;
			comingmsg <- nil;
		}

	}

	aspect default {
	// Draw the main shape of the agent
		draw shape color: color depth: 0.1;

		// Draw the agent's icon just above the shape
		draw icon size: 25 at: (location + {0, 0, 0.2});

		// Visual indicator: green = agent wants to talk, red = idle
		draw circle(3) color: chat_desire ? #green : #red border: #black at: location + {10, -10};
		if (mymsg != "") {
		// ==== Message wrapping and display ====
			list<string> wrapped_lines <- []; // Stores broken-up lines
			int max_line_length <- 50;
			int total_length <- length(mymsglit);
			int start <- 0;
			int end;
			int dy <- 0;

			// Wrap the message string into chunks of max_line_length
			loop while: (start < total_length) {
				end <- min(start + max_line_length, total_length);
				wrapped_lines <- wrapped_lines + [copy_between(mymsglit, start, end)];
				start <- end;
			}

			// Display each wrapped line with a vertical offset
			loop i from: 0 to: length(wrapped_lines) - 1 {
				draw wrapped_lines[i] at: location + {18, -12 + dy, 10} font: font("Helvetica", 20, #bold) color: #red;
				dy <- dy + 6;
			}

		}

	}

}

experiment Dialog type: gui {
	parameter "Langue" var: language among:
	["Français", "Vietnamese", "English", "Español", "Deutsch", "Italiano", "Русский", "العربية", "中文", "日本語", "한국어", "Tiếng Việt", "Português"] init: "Vietnamese";
	parameter "llm model" var: llmmodel among: ["llama3.2", "llama3:8b", "mistral", "gemma:2b", "phi3", "phi2", "codellama:7b", "neural-chat", "orca-mini", "tinyllama", "llava:7b"]
	init: "llama3.2";
	output {
		display Field type: opengl axes: false {
			species AI; // Show all AI agents
		}

	}

}