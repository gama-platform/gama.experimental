/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global { 
	string msg0 <- "there is a fire, what do we do? give one guide as short answer in one phrase";

	init {
		write msg0;
		create A {
			do build_model;
			do create_chat_memory role: "You are a computer scientist";
			comingmsg <- msg0;
		}

		create A {
			do build_model;
			do create_chat_memory role: "You are a teenager";
			do add_msg_memory msg: msg0;
		}

	}

}

species A skills: [mcp_skill] {
	string mymsg;
	string comingmsg;

	reflex chating when: comingmsg != nil {
		do add_msg_memory msg: comingmsg;
		mymsg <- chat(comingmsg);
		write self;
		write mymsg;
		comingmsg <- nil;
		ask ((A as list) - self) {
			comingmsg <- myself.mymsg+". give one guide as short answer in one phrase";
		}

		do add_msg_memory msg: mymsg;
	}

	aspect default {
		draw cube(10);
		draw mymsg at:location + {0, 0, 10} font: font("Helvetica", 30, #bold) color: #red;
	}

}

experiment main type: gui {
	output {
		display Field type: opengl {
			species A;
		}

	}

}
