/**
* Name: Thematic_model
* Thematic model to be distributed by a distribution model. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Thematic

global 
{
	int world_lenght <- 50;
	int world_height <- 50;
	
	init
	{
		create agent_with_data number: 200;
	}
}

species agent_with_data skills:[moving]
{
	int data <- rnd(50);
	rgb col <- #red;
	point target <- any_location_in(world);
	
	reflex
	{
		data <- rnd(50);
	}	
	
	aspect default
	{		
		draw line(location, target) color: col;
		draw circle(1) color: col;
		draw name color: #black;
	}
	
	reflex move when: target != location
	{
		do goto speed: speed target:target;
	}
	
	reflex target when: target = location
	{
		target <- any_location_in(world.shape);
	}
	
	list<agent> insert_self_into_list(list<agent> li)
	{
		if(li != nil)
		{
			add self to: li;
		}else
		{
			li <- [self];
		}
		return li;
	}
}

experiment Thematic_experiment2
{
	
}
experiment Thematic_experiment
{
	/*reflex
	{
		write("thematic step : " + cycle);
		ask agent_with_data
		{
			write(self.data);
		}
		write("each " + agent_with_data collect each.data);
		
	}
	reflex
	{
		ask simulation 
		{	
			save (snapshot("chart")) to: "output.log/snapshot/" + cycle + ".png" rewrite: true;
		}
	}*/
	
	output
	{	display chart
		{
			species agent_with_data;	
		}
	}
}
