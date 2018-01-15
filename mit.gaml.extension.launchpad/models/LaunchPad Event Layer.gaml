/**
* Name: Launch Pad Event Feature
* Author: Arnaud Grignard & Huynh Quang Nghi
* Description: Model which shows how to use the event layer to trigger an action with a LaunchPAd Novation
* Tags: tangible interface, gui
 */
model event_layer_model

global skills:[launchpadskill]
{
	list<string> buttonColors <-["green","red","orange","yellow","brown","lightyellow","darkgreen","white"];
	map<string,string> function_map <-["UP"::"green","DOWN"::"red","LEFT"::"orange","RIGHT"::"yellow","SESSION"::"brown","USER_1"::"lightyellow","USER_2"::"darkgreen","MIXER"::"white"];
	init{}
	action updateGrid
	{   
		if(function_map.keys contains buttonPressed and buttonPressed != "MIXER"){
		    ask cell[ int(padPressed.y *8 + padPressed.x)]{color <- rgb(function_map[buttonPressed]);}
		    do setPadLight color:function_map[buttonPressed];
		}
		if(buttonPressed = "MIXER"){
			ask cell[ int(padPressed.y *8 + padPressed.x)]{color <- function_map[buttonPressed];}
		}		
		
		if(buttonPressed="ARM"){
			do resetPad;
			ask cell{
				color<-#white;
			}
		}
		do updateDisplay;
	}
	
	init{
	  do resetPad;
	  do setButtonLight colors:buttonColors;	
	}
}

grid cell width: 8 height: 8
{
	rgb color <- # white;
}

experiment Displays type: gui
{
	output
	{
		display View_change_color type: opengl
		{
			grid cell lines: # black;
			event "pad_down2" type: "launchpad" action: updateGrid;
		}
	}
}



