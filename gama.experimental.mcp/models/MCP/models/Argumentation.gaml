/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	string context <- "Nous jouons un jeu de rôle. Voici le contexte général: il existe un nouveau type de compteur : les compteurs d'eau communicants. Les agriculteurs doivent définir s'ils veulent l'adopter ou non";

	init {
		
		create Farmer {
			role <- "tu joues le role d'un jeune agriculteur, qui aime tester de nouvelles technologies.";
			adoption <- true;
			confidence_level <- 3;
			economic_level <- 2;
			color <- #blue;
			role <- role + read_attribute();
			icon <- image_file("Images/young.png");

			location <- {20,20};
			name <- "Fred";
			
		
		
		create Farmer {
			role <- "tu joues le role d'un agriculteur père de famille. Tu ne sais rien sur les compteurs d'eau communicants. Tu es intéressé par les questions environnementales et tu peux être sensible aux questions des resources en eaux";
			
			adoption <- false;
			confidence_level <- 1;
			economic_level <- 3;
			color <- #orange;
			role <- role + read_attribute();
			icon <- image_file("Images/rich.png");
			location <- {20,80};
			name <- "Edmond";
		
		}
		
			create Farmer {
				role <- "tu joues le role d'une agricultrice bio de 38 ans. Tu es très sensible à l'avis de " + Farmer[0];
				adoption <- true;
				confidence_level <- 2;
				economic_level <- 1;
				color <- #magenta;
				role <- role + read_attribute();
				icon <- image_file("Images/bio.png");
				location <- {80,20};
				name <- "Joséphine";
			}
		}
		
		create Farmer {
			role <- "tu joues le role d'un vieil agriculteur, qui est réticent à tester de nouvelles technologies. Tu es équipé d'un 
				vieux compteur mécanique et tu ne vois pas pourquoi tu devrais changer de compteur d'eau. Pour toi, les compteurs communicants, cela vaut cher et c'est complexe.
 				Tu n'es pas intéressé par les questions environnementales";
			
			adoption <- false;
			confidence_level <- 3;
			economic_level <- 2;
			color <- #brown;
			role <- role + read_attribute();
			icon <- image_file("Images/old.png");
			location <- {80,80};
			name <- "Robert";
		
		}
		
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
	int economic_level <- 2  min: 1 max: 3;
	
	rgb color <- rnd_color(20, 150);
	
	bool adoption <- false;
	
	Farmer speak_with;
	
	string last_word <- "";

	string read_attribute {
		//string mess <- " J'ai un niveau d'assurance de " + confidence_level + " sur 5. 1 signifie que je ne suis pas sûr du tout de moi et que je peux très facilement changer d'avis sur l'utilisation des compteurs d'eau communicants. Un niveau de 5 signifie que je suis très sûr de moi et que je ne souhaite pas changer d'avis."; 
		string mess <- "";//" J'ai un niveau d'assurance de " + confidence_level + " sur 5. 1 signifie que je ne suis pas sûr du tout de moi et que je peux très facilement changer d'avis sur l'utilisation des compteurs d'eau communicants. Un niveau de 5 signifie que je suis très sûr de moi et que je ne souhaite pas changer d'avis."; 
		if confidence_level = 1 {
			mess <-  " Tu n'es pas sûr du tout de toi et tu peux très facilement changer d'avis sur l'utilisation des compteurs d'eau communicants.";
		} else if confidence_level = 2{
			mess <-  " Tu as un avis sur l'utilisation des compteurs d'eau communicants, mais tu peux éventuellement changer d'avis sur ce sujet.";
		} else {
			mess <-  " Tu es très sûr de toi et tu ne veux pas changer d'avis sur l'utilisation des compteurs d'eau communicants.";
		}
		if economic_level = 1 {
			mess <-  mess + " Tu as un niveau économique faible : tu es pauvre et tu n'as pas beaucoup de marges financières.";
		} else if economic_level = 2{
			mess <-  mess + " Tu as un niveau financier moyen, qui te permet d'investir dans quelques nouvelles technologies.";
		} else {
			mess <-  mess + " Tu es très riche et tu peux facilement investir dans les nouvelles technologies.";
		}
		mess <- mess + (adoption ?  " Je suis un utilisateur des compteurs d'eau communicant" : " Je n'ai jamais utilisé avant les compteurs communicants");
		return mess;
	}
	init {
		chat_model <- create_chat_model(llm: "ollama", url: "http://localhost:11434", model_name: "llama3.1");
		
	}
	
	reflex chating when: wish_to_talk  {
		speak_with <- nil;
		Farmer to_who <- one_of(Farmer - self);
		string firstmsg <- context+". " + role +" .Donne un unique argument à " + to_who.name + " pour expliquer pourquoi il faut " + (adoption ? "adopter " : "ne pas adopter") + " les compteurs d'eau communicant en tant qu'agirculteur et des choses déjà dites et éviter de redonner des arguments déjà évoqué."  + (empty(already_given) ? "": ("Choses déjà dites: " + already_given)) + ". Ne renvoie que l'argument"  ;
		string msg <- send_to_llm(llm:chat_model, message:firstmsg );
		last_word <- msg;
		//write ( "\n\nfirstmsg : " + name + " -> "+ firstmsg) color: color;
		write ( "\n"+ name + " to " + to_who.name + " -> " + (msg)) color: color;
		string msg_to_send <- "l'agriculteur " + name + " souhaite vous donner son avis sur les compteurs d'eau communicant";
		msg_to_send  <- msg_to_send + " il vous dit pour vous convaincre: " + msg ;
		//write name + "-> " + sample(msg);
		already_given <- already_given +  msg;
		ask  to_who{ 
			myself.speak_with <- self;
			already_received <- already_received + msg_to_send;
		
			string comingmsg <- context + "\n" + role + "\nVoici les arguments reçus. " + already_received + ". " + (empty(already_given) ? "": ("Voici ceux que tu as données: " + already_given));
			string msg_ <- comingmsg + "\nEst-ce que tu veux utiliser les compteurs d'eau communicants (ou continuer à les utiliser) ? réponse par 'OUI, je compte utiliser les compteurs d'eau communicants' ou 'NON, je ne veux pas utiliser les compteurs d'eau communicants' en donnant des arguments pour ton choix" ;
			//write ("\nmsg_: " + name + " -> "+ msg) color: color;
		
		
			string adotion_str <- send_to_llm(llm:chat_model, message: msg_);
			last_word <- adotion_str;
		
			write ( "\n" + name + " - Adoption -> " + adotion_str) color: color;
			if "oui" in lower_case(adotion_str) {
				adoption <- true;
			} else if "non" in lower_case(adotion_str) {
				adoption <- false;
			} else {
				write "NOT SURE" color: color;
			}
			ask experiment {
				do update_outputs;
			}
			//write name + " Adoption -> " + sample(adotion_str);
			
			
		
		}
		string msg_c <- context + "\n" + role +"\nEst-ce que tu as encore de nouvelles choses à dire sachant que tu as déjà dit ça + " + already_given + " ? réponse soit OUI ou NON sur le fait d'avoir encore des nouveaux arguments à donner";
		//write ( "\nmsg2_: " + name + "-> " + msg_c) color: color;
		string continue_str <- send_to_llm(llm:chat_model, message:msg_c);
		wish_to_talk<- "oui" in lower_case(continue_str);
		
		
		write ( "\n"+ name + " - Continue talking -> "+ (continue_str)) color: color;
		if "oui" in lower_case(continue_str) {
			wish_to_talk <- true;
		} else if "non" in lower_case(continue_str) {
			wish_to_talk <- false;
		} else {
			write "NOT SURE" color: color;
		}
		last_word <- last_word + "\n" + continue_str;
			
		ask experiment {
			do update_outputs;
		}
	}
	
	aspect link {
		if (speak_with != nil) {
			geometry l <- line(self ,speak_with )  - self - speak_with;
			draw l width: 10 end_arrow: 3  color: color ;
		}
		/*if (last_word != nil) {
			draw last_word font: font(15, #bold) color: #black at: location + {0,15,0.2} anchor: #center;
		
		}*/
		
		
	}

	aspect default {
		//draw circle(7)  color: color ;
		if (not wish_to_talk) {
			draw shape + 2 color: #gray ;
		
		}
		draw shape  color: color ;
		draw  icon size: 25 ;
		draw circle(3) color: adoption ? #green : #red border: #black at: location + {10,-10};
		draw name font: font(15, #bold) color: #white at: location + {0,13.5,0.2} anchor: #center;
		
		//draw mymsg at: location + {0, 0, 10} font: font("Helvetica", 30, #bold) color: #red;
	}

}
experiment exploration type: batch until: cycle > 10 repeat: 10{
	
	reflex end {
		loop s over: simulations {
			write "num adopters: " + Farmer count each.adoption;
		}
	}
}
experiment main type: gui {
	output {
		 layout 0 tabs:true editors: false;
		display Field type: opengl axes: false {
			species Farmer;
			species Farmer aspect: link transparency: 0.2 ;
			
		}

	}

}
