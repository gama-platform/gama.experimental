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
			do build_model;
			do create_chat_memory role: roleMsg;
		}

	}

}

species A skills: [mcp_skill] {

	reflex chating {
		do add_msg_memory msg: msgto[cycle];
		string tmp <- chat(msgto[cycle]);
		write tmp;
		do add_msg_memory msg: tmp;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
		}

	}

}
