/**
* Name: Distribution_model
* Distribution model to distribute a thematic model using a grid. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Distribution

import "Continuous_Move_thematic.gaml" as Thematic

global skills: [ProxySkill]
{
	int end_cycle <- 300;
	
	file building_shapefile <- file("includes/building.shp");
	//Shape of the environment
	geometry shape <- envelope(building_shapefile); 
	list<people> init_peoples;
	
	init
 	{	
 		create Thematic.Thematic_experiment;
 	}
 	
 	reflex run_thematic_model // run a cycle of the thematic model
 	{
 		write("distribution step : --------------------------------------" + cycle);
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			do _step_;
 		}
 	}
 	
 	reflex end_distribution_model_end_cycle when: cycle = end_cycle // end the distribution model when we reach the cycle end_cycle
	{
		write("-----------------end_cycle reached-----------------");
		do die;
	}
	
	reflex serialize
	{
		list<people> peoples_;
		ask Thematic.Thematic_experiment[0].simulation
 		{
 			peoples_ <- list(people);
 		}
		do testSerialize(peoples_);
		
		map<int, list<people>> mappy <- [0::peoples_, 1::peoples_, 2::peoples_, 3::peoples_];
		do testSerializeWithAlltoAll(mappy);
	}
}
experiment distribution_experiment type: proxy until: (cycle = end_cycle)
{
	list<people> peoples; 		// trick to print the peoples on the distribution model
	list<building> buildings; 	// trick to print the buildings on the distribution model
	
	reflex update_peoples
	{
		list<people> peoples_;
		list<building> buildings_;
		ask Thematic.Thematic_experiment[0].simulation 
		{
			 peoples_ <- list(people);
			 buildings_ <- list(building);
		}
		peoples <- peoples_;
		buildings <- buildings_;
	}
	reflex snap
	{
		
		ask simulation
		{	
		}
	}
	output
	{
		display agent
		{
			agents people value: peoples;	
			agents building value: buildings;
		}
	}
}
