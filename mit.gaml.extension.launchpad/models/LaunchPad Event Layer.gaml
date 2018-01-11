/**
* Name: Launch Pad Event Feature
* Author: Arnaud Grignard & Huynh Quang Nghi
* Description: Model which shows how to use the event layer to trigger an action with a LaunchPAd Novation
* Tags: tangible interface, gui
 */
model event_layer_model

global skills:[launchpadskill]
{
	map<string,rgb> function_map <-["UP"::#red,"DOWN"::#green,"LEFT"::#blue,"RIGHT"::#yellow,"SESSION"::#cyan,"USER_1"::#magenta,"USER_2"::#black,"MIXER"::#white];
	init{}
	action updateGrid
	{   
		write buttonPressed;
		if(function_map.keys contains buttonPressed and buttonPressed != "MIXER"){
		    ask cell[ int(padPressed.y *8 + padPressed.x)]{color <- function_map[buttonPressed];}
		    do setPadLight color:"yellow";
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



