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
			chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.2");
			chat_memory <- create_chat_memory(role: roleMsg);
			mcp_transport<-create_mcp_transport(url:"http://localhost:3001/sse",timeout:60);
			mcp_client<-create_mcp_client(transport: mcp_transport);
			mcp_tool<-create_mcp_tool(client: mcp_client);
			my_bot<-create_mcp_ai_service(llm: chat_model, tool: mcp_tool);
			string res<- send_to_ai_service(bot:my_bot, message: "What is 5+12? Use the provided tool to answer " +
                    "and always assume that the tool is correct.");
            write res;
            mymsg<-msg1;
      
		} 
	}
}

species A skills: [mcp_skill] {
	unknown chat_model;
	unknown chat_memory;
	unknown mcp_transport;
	unknown mcp_client;
	unknown mcp_tool;
	unknown my_bot;

	string mymsg;
	reflex chating { 
		mymsg<- send_to_llm(llm:chat_model, message: "explain more about your intelligent");
		write mymsg;
		do add_to_chat_memory message: mymsg memory: chat_memory;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
		}

	}

}
