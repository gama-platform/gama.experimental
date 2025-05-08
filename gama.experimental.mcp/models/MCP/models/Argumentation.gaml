/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string
	context <- "We are playing a role-playing game. Here is the general context: a new type of meter has been introduced—smart water meters. Farmers must decide whether or not they want to adopt them.";

	init {
		create Farmer {
			role <- "You are playing the role of a young farmer who enjoys trying out new technologies.";
			adoption <- true;
			confidence_level <- 3;
			economic_level <- 2;
			color <- #blue;
			role <- role + read_attribute();
			icon <- image_file("Images/young.png");
			location <- {20, 20};
			name <- "Fred";
			proba_chatting <- 1.0;
		}

		create Farmer {
			role <- "You are playing the role of a farmer and father. You are interested in environmental issues and may be sensitive to questions about water resources.";
			adoption <- false;
			confidence_level <- 1;
			economic_level <- 3;
			color <- #orange;
			role <- role + read_attribute();
			icon <- image_file("Images/rich.png");
			location <- {20, 80};
			name <- "Edmond";
			
			proba_chatting <- 0.2;
		}

		create Farmer {
			role <- "You are playing the role of a 38-year-old organic farmer. You are very influenced by " + Farmer[0].name + "’s opinion. ";
			adoption <- true;
			confidence_level <- 2;
			economic_level <- 1;
			color <- #magenta;
			role <- role + read_attribute();
			icon <- image_file("Images/bio.png");
			location <- {80, 20};
			name <- "Joséphine";
			
			proba_chatting <- 0.5;
		}

		create Farmer {
			role <-
			"You are playing the role of an old farmer who is reluctant to try new technologies. You have an old mechanical meter and don’t see why you should switch to a smart water meter. To you, smart meters are expensive and complicated. You are not interested in environmental issues.";
			adoption <- false;
			confidence_level <- 3;
			economic_level <- 2;
			color <- #brown;
			role <- role + read_attribute();
			icon <- image_file("Images/old.png");
			location <- {80, 80};
			name <- "Robert";
			
			proba_chatting <- 0.1;
		}
	}
	
	reflex end_sim when: empty(Farmer where each.wish_to_talk){
		do pause;
	}

}

species Farmer skills: [mcp_skill] {
	unknown chat_model;
	string role <- "";
	bool wish_to_talk <- true;
	string already_given <- "";
	string already_received <- "";
	image_file icon;
	geometry shape <- square(30);
	int confidence_level <- 2 min: 1 max: 3;
	int economic_level <- 2 min: 1 max: 3;
	rgb color <- rnd_color(20, 150);
	bool adoption <- false;
	Farmer speak_with;
	string last_word <- "";
	float proba_chatting <- 0.5;
	
	
	string read_attribute {
		string mess <- "";
		if confidence_level = 1 {
			mess <- " You are very unsure of yourself and can easily change your mind about using smart water meters.";
		} else if confidence_level = 2 {
			mess <- " You have an opinion about the use of smart water meters, but you might eventually change your mind on the subject.";
		} else {
			mess <- " You are very confident in your opinion and do not want to change your mind about using smart water meters.";
		}

		if economic_level = 1 {
			mess <- mess + " You have a low economic status: you are poor and have very little financial leeway.";
		} else if economic_level = 2 {
			mess <- mess + " You have an average financial status, which allows you to invest in some new technologies.";
		} else {
			mess <- mess + " You are very wealthy and can easily invest in new technologies.";
		}

		mess <- mess + (adoption ? " You are a user of smart water meters" : " You are not a user of smart water meters");
		return mess;
	}

	init {
		chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.1");
	}

	reflex chating when: wish_to_talk and flip(proba_chatting) {
		speak_with <- nil;
		Farmer to_who <- one_of(Farmer - self);
		string firstmsg <- context + ". " + role + ". Give a single argument to " + to_who.name + " to explain why " + (adoption ?
		"smart water meters should be adopted " : "smart water meters should not be adopted ") + "as a farmer, avoiding repeating arguments that have already been mentioned." + (empty(already_given)
		? "" : (" Already mentioned arguments: " + already_given)) + ". Return only the argument.";
		string msg <- send_to_llm(llm: chat_model, message: firstmsg);
		last_word <- msg;
		speak_with <- to_who;
		ask experiment {
			do update_outputs;
		}

		write ("\n" + name + " to " + to_who.name + " -> " + (msg)) color: color;
		string msg_to_send <- "l'agriculteur " + name + " souhaite vous donner son avis sur les compteurs d'eau communicant";
		msg_to_send <- msg_to_send + " il vous dit pour vous convaincre: " + msg;
		already_given <- already_given + msg;
		ask to_who {
			already_received <- already_received + msg_to_send;
			string comingmsg <- context + "\n" + role + "\nHere are the arguments received. " + already_received + ". " + (empty(already_given) ?
			"" : (" Here are the ones you gave: " + already_given));
			string
			msg_ <- comingmsg + "\nDo you want to use smart water meters (or continue using them)? Answer with ‘YES, I plan to use smart water meters’ or ‘NO, I do not want to use smart water meters’ and provide arguments for your choice";
			string adotion_str <- send_to_llm(llm: chat_model, message: msg_);
			last_word <- adotion_str;
			write ("\n" + name + " - Adoption -> " + adotion_str) color: color;
			if "yes" in lower_case(adotion_str) {
				adoption <- true;
			} else if "no" in lower_case(adotion_str) {
				adoption <- false;
			} 

			ask experiment {
				do update_outputs;
			} }

		string
		msg_c <- context + "\n" + role + "\nDo you still have new things to say knowing that you have already said this? " + already_given + ". Answer either YES or NO regarding whether you have new arguments to provide";
		string continue_str <- send_to_llm(llm: chat_model, message: msg_c);
		wish_to_talk <- "oui" in lower_case(continue_str);
		write ("\n" + name + " - Continue talking -> " + (continue_str)) color: color;
		if "yes" in lower_case(continue_str) {
			wish_to_talk <- true;
		} else if "no" in lower_case(continue_str) {
			wish_to_talk <- false;
		} else {
			wish_to_talk <- true;
		}

		last_word <- last_word + "\n" + continue_str;
		ask experiment {
			do update_outputs;
		} }

	aspect link {
		if (speak_with != nil) {
			geometry l <- line(self, speak_with) - self - speak_with;
			draw l width: 10 end_arrow: 3 color: color;
		}

	}

	aspect default {
		if (not wish_to_talk) {
			draw shape + 2 color: #gray;
		}

		draw shape color: color depth: 0.1;
		draw icon size: 25 at: (location + {0, 0, 0.2});
		draw circle(3) color: adoption ? #green : #red border: #black at: location + {10, -10} depth: 1;
		draw name font: font(15, #bold) color: #white at: location + {0, 13.5, 0.2} anchor: #center;
	} }

experiment main type: gui {
	output {
		layout 0 tabs: true editors: false;
		display Field type: opengl axes: false {
			species Farmer;
			species Farmer aspect: link transparency: 0.2;
		}

	}

}