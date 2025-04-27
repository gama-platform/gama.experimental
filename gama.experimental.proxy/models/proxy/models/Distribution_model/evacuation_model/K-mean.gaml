/**
* Name: Kmean
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/

model Kmean

import "../../Models_to_distribute/Continuous_Move.gaml" as continuous_move

global
{
	file building_shapefile <- file("includes/building.shp");
	geometry shape <- envelope(building_shapefile);
	
	int number_of_centroids <- 16;
	list<people> all_people_in_sub_model;
	int nb_people <- 32;						// number of people in the Model
	
	init
	{
		create continuous_move.main with:[seed::seed, nb_people::nb_people];
		ask(continuous_move.main[0])
		{
			all_people_in_sub_model <- list(people);
		}
		
		create centroid_agent number: number_of_centroids
		{
			location <- one_of(all_people_in_sub_model).location;
		}
		
		loop agt over: all_people_in_sub_model
		{
			centroid_agent one_centroids <- shuffle(centroid_agent)[0];
			ask one_centroids
			{
				// randomly assign data_point to a centroid
				add agt to: my_points;
			}
		}
	}
	
	bool check_end_simulation
 	{
 		bool end_simu <- false;
 		ask continuous_move.main[0].simulation
 		{
	 		if(simulationOver) // check if sub model is done simulating
	 		{	
	 			end_simu <- true;
	 			write(" ");
	 			write("SIMULATION IS OVER");
	 		}	
 		}
 		return end_simu;
 	}
 	
 	action end_simulations
	{
 		if(check_end_simulation())
 		{	
 			write("DISTRIBUTION MODEL IS OVER");
 			write("total_duration " + float(total_duration)/1000 + "s");
 			
 			ask continuous_move.main[0].simulation
 			{
 				do die;
 			}
	 		do die;
	 		
 		}
	}
 	
	action _step_sub_model
	{	
 		do end_simulations();			// check end of the model
		ask continuous_move.main[0].simulation
		{
			write("stepping");
			do _step_;
			
			all_people_in_sub_model <- list(people);
		}
	}
	
	reflex
	{
		do _step_sub_model;
	}
}

species centroid_agent
{
	rgb color_kmean;
	list<people> my_points;
	list<people> tmp_my_points;
	
	init
	{
		color_kmean <- rgb(rnd(255),rnd(255),rnd(255));
	}
	reflex
	{	
		map<int, list<people>> migrating_agents;
		loop tmp over: my_points
		{
			centroid_agent closest <- centroid_agent closest_to tmp;
			if(closest = nil)
			{
				ask simulation
				{
					do pause;
				}
			}
			if(closest != self)
			{
				//write("tmp leaving " + tmp);
				//add tmp to: self.mypoints;
				if(migrating_agents[closest.index] = nil)
				{
					migrating_agents[closest.index] <- list<people>(tmp);
				}else
				{
					migrating_agents[closest.index] << tmp;
				}
			}
		}
		
		loop migrating_agent over: migrating_agents.pairs
		{
			//write("migrating_agent " + migrating_agent);
			//write("migrating_agent.keyu " + migrating_agent.key);
			//write("migrating_agent.value " + migrating_agent.value);
			
			centroid_agent migrate_to <- centroid_agent[migrating_agent.key];
			ask migrate_to
			{
				tmp_my_points <- tmp_my_points + list<people>(migrating_agent.value);
				//write("hello " + self);
				//write("hello " + self.tmp_my_points);
			}
			
			//write("my_points before " + my_points);
			loop migrating_agent_to_remove over: migrating_agent.value
			{
				remove migrating_agent_to_remove from: my_points;
			}
			//write("my_points afetr " + my_points);
		}
		
		write("tmp_my_points " + tmp_my_points);
		my_points <- my_points + tmp_my_points;
		tmp_my_points <- [];
		
		//write("migrating_agent " + migrating_agents);
		//write("lenght["+self+"] : " + length(mypoints));
		my_points <- my_points where !dead(each);
		location <- mean(my_points collect each.location); // move centroid in the middle of the convex
	}
	
	aspect default
	{
		draw cross(2, 0.5) color: color_kmean;
		geometry convex <- convex_hull(polygon(my_points));
		draw convex color: rgb(color_kmean,0.2) border: #black;
	}
}

experiment distributed
{
	output 
	{
		display "hello"
		{
			species people;
			species centroid_agent;
		}
		display people
		{
			graphics "exit" {
				ask continuous_move.main[0].simulation
				{
					loop tmp over: people
					{				
						draw circle(2) at: tmp.location color: tmp.color;	
					}
					loop tmp over: building
					{
						draw tmp color: #gray depth: tmp.height border: #black;
					}
				}
			}
		}
		
	}
}
