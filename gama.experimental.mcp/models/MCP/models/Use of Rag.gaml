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
			bot <- create_assistant(llm:llm);
		} 
		ask Agent_with_RAG {
			content_retriever cr <- create_rag("../includes/RAG");
			bot <- create_assistant(llm:llm, content_retriever: cr);
		} 
		
		string question <- "Who is John Doe?";
		write " ***** " + question + " ***** " ;
		ask first(Agent_without_RAG) {do answer_question(question);}
		ask first(Agent_with_RAG) {do answer_question(question);}
		
	} 
	
}

 
species LLM_Agent skills: [mcp_skill] {
	chat_model llm;
	assistant bot;
	rgb color;
	init {
		llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.2");
	}
	
	action answer_question(string question) {
		write "\n ****** " + name + "*****" color: color;
		write send_to_assistant(bot, question) color: color;
	} 
}
species Agent_without_RAG parent: LLM_Agent;

species Agent_with_RAG parent: LLM_Agent ;

experiment main type: gui ;


