/**
* Name: GUIExample
* Based on the internal empty template. 
* Author: sklab
* Tags: 
*/


model GUIExample

/* Insert your model definition here */

/**
* Name: GUIElementsModeldel1
* Based on the internal empty template. 
* Author: sklab
* Tags: 
*/

/* Insert your model definition here */

global skills: [unity]
{	
	init{
		create CheckingAgent number: 1;
	  }
}

species CheckingAgent skills: [unity]{
	//rgb color;	
	string UIButtonRaised <- "Button";
	string UICheckbox <- "Checkbox";
	string UIDialogBox <- "DialogBox";
	string UIDivider <- "Divider";
	string UIRoundButton <- "RoundButton";
	string UISelectionBox <- "SelectionBox";
	string UISlider <- "Slider";
	string UISwitch <- "Switch";
	string UIText <- "Text";
	string UITextInput <- "TextInput";
	string UICloseButton <- "CloseButton";
	string UICloseXButton <- "CloseXButton";
	string UILogo <- "Logo";
		
	string  text_title_1 <- "UIText_title_1";
	string selectionBox_color_1 <- "UISelectionBox_color_1";
	string button_stop_move_1 <- "ButtonStopMove_1";
	string checkbox_1 <- "UICheckbox_1";
	string dialogBox_stop_simulation_1 <- "UIDialogBox_StopSimulation_1";
	string roundButton_increase_speed_1 <- "UIRoundButton_increase_speed_1"; 
	string roundButton_increase_speed_2 <- "UIRoundButton_increase_speed_2";
	string roundButton_pause_1 <- "UIRoundButton_pause_1";
	string roundButton_play_1 <- "UIRoundButton_play_1";
	string slider_speed_1 <-"UISlider_Speed_1";
	string random_move_switch_1 <- "UIRandomMoveSwitch_1";
	string text_input_display_1 <- "UITextInput_Display_1";	
	
	string closeButton_1 <- "CloseButton_1";
	string closeXButton_1 <- "CloseXButton_1";
	string logoIRD <- "LogoIRD";
	string logoUMMISCO <- "LogoUMMISCO";
	

	string playerName <- "Object1";
	bool isStopSimulation <- false;
	bool isRandomMoving <- false;
	bool isLinearMoving <- true;
	bool isDialogOn <- false;
	bool isChangeColor <- true;
	bool canMove <- true;
	int startCycle <- 0;
	float agentSpeed <- 0.1;
	
	int cyclesForDialog <- 100;
		
	init{
		location <-{0,0,0};	
		color <- #blue;
		do connect_unity  to:"localhost"  login:"admin" password:"admin" port: 1883;
		do subscribe_to_topic topic:"UITopic";	 
		map<string,string>  option_action ;
		   
		// UIText
		 do unityRemoteUI topic: "UITopic" uiType: UIText parent:"Panel" uiId:text_title_1 location: {675,-100,0} label:"Example of a GAMA-Unity control interface" color: #red height: 30 width:300 size:2 state:1;		
	 	 			 		 	
		// UIDivider
		 do unityRemoteUI topic: "UITopic" uiType: UIDivider parent:"Panel" uiId:"UIDivider_top_1" location: {640,-37,0} height: 80 width:1280 color: rgb(8,127,140) size:1 state:1;				
	     do unityRemoteUI topic: "UITopic" uiType: UIDivider parent:"Panel" uiId:"UIDivider_title_1" location: {640,-132,0} height: 10 width:1280 color: rgb(9,82,86) content_text:"content_textGama" size:1 state:1;		
		 do unityRemoteUI topic: "UITopic" uiType: UIDivider parent:"Panel" uiId:"UIDivider_bottom_1" location: {640,-668,0} height: 100 width:1280 color: rgb(90,170,147) content_text:"content_textGama" size:1 state:1;		
	
	
		 // UILogo
	 	  do unityRemoteUI topic: "UITopic" uiType: UILogo parent:"Panel" uiId:logoIRD location: {-612,-22,0}  height: 66 width:135  content_text:"logo/logo IRD"  size:1 state:1;			
	
		// UISelectionBox
		 option_action <- ["0"::"Red", "1"::"Blue", "2"::"Green", "3"::"Black", "4"::"Yellow"];
		 do unityRemoteUI topic: "UITopic" uiType: UISelectionBox parent:"Panel" uiId:selectionBox_color_1 location: {315,-245,0} label:"Select Color" color: rgb(33,146,241)  height: 98 width:200 option_action: option_action size:1 state:1;		
		 do unityRemoteUI topic: "UITopic" uiType: UIText parent:"Panel" uiId:"UIText_color_select_1" location: {160,-245,0} label:"Please select a color : " height: 30 width:160 size:1 state:1;		
		
		 // UIButtonRaised
		 do unityRemoteUI topic: "UITopic" uiType: UIButtonRaised parent:"Panel" uiId:button_stop_move_1 location: {175,-343,0} label:"Stop moving for 10 cycles" color: rgb(33,146,241)  height: 66 width:135   size:1 state:1;		
		
		// UICheckbox
	 	 option_action <- ["0"::"Color Change Off", "1"::"Color Change On"];
		 do unityRemoteUI topic: "UITopic" uiType: UICheckbox parent:"Panel" uiId:checkbox_1 location: {125,-425,0} height: 40 width:101 option_action: option_action size:1 state:1;		
		 		
		// UIRoundButton
		 do unityRemoteUI topic: "UITopic" uiType: UIRoundButton parent:"Panel" uiId:roundButton_increase_speed_1 location: {525,-500,0} height: 64 width:64 content_text:"ihm/I_urbanise_adapte"  size:1 state:1;		
		 do unityRemoteUI topic: "UITopic" uiType: UIRoundButton parent:"Panel" uiId:roundButton_increase_speed_2 location: {650,-500,0} height: 64 width:64 content_text:"icons/3"  size:1 state:1;		
		do unityRemoteUI topic: "UITopic" uiType: UIRoundButton parent:"Panel" uiId:roundButton_pause_1 location: {1030,-235,0} height: 64 width:64 content_text:"icons/pause"  size:1 state:1;		
		do unityRemoteUI topic: "UITopic" uiType: UIRoundButton parent:"Panel" uiId:roundButton_play_1 location: {1030,-317,0} height: 64 width:64 content_text:"icons/play"  size:1 state:1;		
		
		//  UISlider
		do unityRemoteUI topic: "UITopic" uiType: UISlider parent:"Panel" uiId:slider_speed_1 location: {1000,-500,0} label:"Moving speed " height: 30 width:250 size:1 state:1;		
		 
		// UISwitch
		 option_action <- ["0"::"Random move Off", "1"::"Random move On"];
		 do unityRemoteUI topic: "UITopic" uiType: UISwitch parent:"Panel" uiId:random_move_switch_1 location: {125,-500,0} height: 140 width:140 option_action: option_action size:1 state:1;		
	      
		//  UITextInput
		 do unityRemoteUI topic: "UITopic" uiType: UITextInput parent:"Panel" uiId:text_input_display_1 location: {229,-580,0} label:"The number of cycles to display the dialog box" height: 54 width:320  size:1 state:1;		
			
		  // UICloseButton
	 	  do unityRemoteUI topic: "UITopic" uiType: UICloseButton parent:"Panel" uiId:closeButton_1 location: {1214,-680,0} label:"Close App" color: rgb(128,0,0)  height: 66 width:135   size:1 state:1;
	 	  
	 	  // UICloseXButton
	 	  do unityRemoteUI topic: "UITopic" uiType: UICloseButton parent:"Panel" uiId:closeXButton_1 location: {1245,-22,0} label:"X" color: rgb(128,0,0)  height: 66 width:135   size:1 state:1;			
	}
	
reflex stop_simulation when:isStopSimulation {
  ask world {do pause;}
}

reflex isCanMove when: not canMove and not isDialogOn{
	if(cycle = startCycle + 10){
		canMove <- true;
	}
}

reflex ask_stop_simulation when: not isDialogOn and (cycle mod cyclesForDialog = 0){
	    // UIDialogBox
	    isDialogOn <- true;
		 map<string,string>  option_action <- ["0"::"Continue", "1"::"Stop Simulation"];
		 string dialogContent <- "Please note that your confirmation is required. \n Do you want to stop the simulation? " ;
		 string dialogTitle <- "Stop simulation";
		 do unityRemoteUI topic: "UITopic" uiType: UIDialogBox parent:"Panel" uiId:dialogBox_stop_simulation_1 location: {855, 1282,0} label:dialogTitle height: 285 width:362 content_text: dialogContent option_action: option_action  size:1 state:1;		
}

reflex random_move when: isRandomMoving and canMove and not isDialogOn{
	location <- any_location_in(world);
}

reflex lineair_move when: isLinearMoving and canMove and not isDialogOn{
	location <-{location.x+agentSpeed, location.y+agentSpeed};
	if(location.x>100){
		location <-{0,0,0};
	}
}	
	 	 
reflex check_new_message when: has_next_message(){
		write " Good, a new message";
		
		map<string, string>  m  <-  map<string, string>(get_unity_message());
		
		switch m["elementId"] {
			match selectionBox_color_1 {
				if(isChangeColor){
					do changeColor(int(m["actionCode"]));
				}
			} 
			match button_stop_move_1 {
						canMove <- false;
						startCycle <- cycle;
			}
			match checkbox_1 {
					if(int(m["actionCode"])=1){
						  isChangeColor <- true;
					}else{
						 isChangeColor <- false;
					}
			}			
			match dialogBox_stop_simulation_1 {
				isDialogOn <- false;
					if(int(m["actionCode"])=1){
						  ask world {do pause;}
					}
			}
			match roundButton_increase_speed_1{
				agentSpeed <- agentSpeed * 2;
			}
			match roundButton_increase_speed_2{
				agentSpeed <- agentSpeed * 4;
			}
			match roundButton_pause_1 {
				isStopSimulation <- true;	
			}			
			match roundButton_play_1{
				//write "roundButton_play_1";
			}
			match slider_speed_1 {
				agentSpeed <-  float(m["actionCode"]);
			}
			match random_move_switch_1{
				if(int(m["actionCode"])=0){
					isRandomMoving <- false;
					isLinearMoving <- true;
				}else{
					isRandomMoving <- true;
					isLinearMoving <- false;
				}
			}
			match text_input_display_1{
				cyclesForDialog <-  int(m["content"]);
			}
	}	

		write " 	topic : " + m["topic"];	
		write " 	messageTime : " + int(m["messageTime"]);	
		write " 	messageNumber : " + int(m["messageNumber"]);	
		write " 	elementId : " + m["elementId"];	
		write " 	actionCode : " + m["actionCode"];	
		write " 	content : " + m["content"];	
	} 
	
	action changeColor(int codeColor){
			switch codeColor {
					match 0 {
							color <- #red;
					} 
					match 1 {
						color <- #blue;
					}
					match 2 {
						color <- #green;
					}
					match 3 {
						color <- #black;
					}
					match 4 {
						color <- #yellow;
					}
				}
	}
	
	reflex check_no_new_message when: !has_next_message() and not isDialogOn{
			//write " No new message at " + cycle;
	}
	
	
	
aspect base {
		draw circle(2) color:#red;
	}
	
}

experiment CheckAll type:gui {
/** Insert here the definition of the input and output of the model */
	output {
		display Dp1 type:opengl{
			species CheckingAgent aspect: base;
		}		
	}
}