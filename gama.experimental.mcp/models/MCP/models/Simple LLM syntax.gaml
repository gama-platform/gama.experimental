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
			llm <- create_ollama_chat_model(url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(llm,roleMsg);
			has_memory <- false;
		}

		create A {
			llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(llm,roleMsg);
			has_memory <- true;
		} } }

species A skills: [llm] { 
	 
	bool has_memory;
	string mymsg;

	reflex chating when: cycle < length(msgto)  {
		write self;
		if (has_memory) {
			//the first boolean is used to define if the prompt has to be added to the memory, the second one if the answer has to be added to the memory
			mymsg <- send_to_llm(llm, msgto[cycle], true, true);
		} else {
			mymsg <- send_to_llm_without_memory(llm, msgto[cycle]);
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
