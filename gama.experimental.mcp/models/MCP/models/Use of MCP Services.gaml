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
			llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(llm,roleMsg);
			transport<-create_mcp_transport(url:"https://router.mcp.so/sse",timeout:260);
			client<-create_mcp_client(transport: transport);
			mcp_tool<-create_client_executor(client); 
			my_bot<-create_assistant(llm: llm, tool_provider: mcp_tool);
      
		} 
	}
}

species A skills: [llm] {
	mcp_transport transport;
	mcp_client client;
	tool_provider mcp_tool;
	assistant my_bot;

	string mymsg;
	reflex chating { 		
		mymsg<- send_to_llm(llm, msgto[cycle], true, true);
		write mymsg;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
		}

	}

}
