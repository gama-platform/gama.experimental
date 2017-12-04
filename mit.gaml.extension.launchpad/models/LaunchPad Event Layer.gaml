/**
* Name: Event Feature
* Author: Arnaud Grignard & Patrick Taillandier
* Description: Model which shows how to use the event layer to trigger an action according to an event occuring in the display. The experiment 
* has two displays : one for the changing color event, one for the changing shape event.
* Tags: gui
 */
model event_layer_model


global
{

//number of agents to create
	int nbAgent <- 500;
	init
	{
	}

	//Action to change the color of the agents, according to the point to know which agents we're in intersection with the point
	action change_color
	{
		point p <- getPad(0);
		write p;
//		write getButton(0);
		ask cell[ int(p.x *8 + p.y)]
		{
			write self;
			if (color = # green)
			{
				color <- # red;
			} else
			{
				color <- # green;
			}

		}
		//change the color of the agents
		//		list<cell> selected_agents <- cell overlapping (circle(10) at_location #user_location);
		//		ask selected_agents
		//		{
		//			color <- color = °green ? °pink : °green;
		//		}

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

			//event, launches the action change_shape if the event mouse_down (ie. the user clicks on the layer event) is triggered
			// The block is executed in the context of the experiment, so we have to ask the simulation to do it. 
			event "pad_down" type: "launchpad" action: change_color;
		}

	}

}

