/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	


	init {
		create Agent_without_RAG with:(color: #red);
		create Agent_with_RAG with:(color: #green);
		
		ask Agent_without_RAG {
			chat_bot <- create_assistant(llm:llm);
			
		} 
		ask Agent_with_RAG {
			content_retriever cr <- create_rag("../includes/RAG");
			memory mem <- create_chat_memory(llm,"You are an expert assistant who only answers using the provided documents");
			chat_bot <- create_assistant(llm:llm, content_retriever: cr, memory:mem);
		} 
		
		string question <- "Who is Jonh Doe?";
		write " ***** " + question + " ***** " ;
		ask first(Agent_without_RAG) {do answer_question(question);}
		ask first(Agent_with_RAG) {do answer_question(question);}
		
	} 
	 
}

 
species LLM_Agent skills: [llm] {
	rgb color;
	init {
		llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.2");
	}
	
	action answer_question(string question) {
		write "\n ****** " + name + "*****" color: color;
		write send_to_assistant(chat_bot, question) color: color;
	} 
}
species Agent_without_RAG parent: LLM_Agent;

species Agent_with_RAG parent: LLM_Agent ;

experiment main type: gui ;


