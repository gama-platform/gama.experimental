/**
* Name: Distribution_model
* Distribution model to distribute a thematic model. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Distribution

import "Continuous_Move_thematic.gaml" as Thematic

global
{
	int end_cycle <- 200;
	int MPI_RANK;
	int MPI_SIZE;
	
	file building_shapefile <- file("includes/building.shp");
	geometry shape <- envelope(building_shapefile); // Shape used for the thematic model environment
	
	init
 	{	
 		
 		create Communication_Agent_MPI; 					// init of the communication agent
 		MPI_RANK <- Communication_Agent_MPI[0].MPI_RANK;	// MPI RANK of the distribution model instance
 		MPI_SIZE <- Communication_Agent_MPI[0].MPI_SIZE;	// Number of MPI rank on the network
 		
 		create Thematic.Thematic_experiment;
 		ask Thematic.Thematic_experiment[0] // out of total_people people, we keep total_people/MPI_SIZE number of people on each processor at init
 		{
 			int total_people <- length(people);
 			int split_people <- (total_people / MPI_SIZE);
 			
			int start_index <- (split_people * MPI_RANK);
			int end_index <- start_index + split_people - 1;
 			
 			list toRemove;
 			loop peep from: 0 to: total_people-1
 			{
 				if( !(peep >= start_index and peep <= end_index))
 				{
 					ask people[peep]
 					{
 						toRemove << self;
 					}
 				}
 			}
 			ask toRemove
 			{
 				do die; // remove the people we don't compute with this distribution model instance
 			}
 		}
		
		create Partitioning_Agent; 				// create Partitioning_Agent
 		create Load_Balancing_Agent;		 	// create Load_Balancing_Agent
 	}
 	
 	reflex run_thematic_model // run a step of the thematic model
 	{
 		write("distribution step : --------------------------------------" + cycle);
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			do _step_;
 		}
 	}
 	
 	reflex end_distribution_model_end_cycle when: cycle = end_cycle // end the model when the cycle end_cycle is reach
	{
		write("-----------------end_cycle reached-----------------");
		do die;
	}
}

species Load_Balancing_Agent
{
	init send_initial_centroids_position
	{
 		do all_to_all_centroids();
	}
	
	reflex compute_partitioning // trigger the partitioning and send migrate the agent
	{
		do compute_convex_partitioning();
 		do all_to_all_centroids();
	}
	
	action compute_convex_partitioning // compute the partitioning
	{
		list<people> peoples;
		map<int,list<people>> people_to_send;
		
		ask Thematic.Thematic_experiment[0] // collect the people from the thematic model
		{
			peoples <- people collect each where not dead(each);
		}
		
		loop current_people over: peoples
		{			
			if(not dead(current_people))
			{
				Partitioning_Agent closest <- Partitioning_Agent closest_to current_people; // get centroids closer to current_people
				closest.my_peoples << current_people; 										// add current_people to the my_peoples list of the closest centroid
			}
		}
		
		ask Partitioning_Agent // ask all the partitioning agent that are not our initial agent to fill people_to_send with their people
		{
			convex <- convex_hull(polygon(my_peoples)); // update convex shape for snapshot
			if(mpi_rank != MPI_RANK) // all but our Partitioning Agent
			{
				people_to_send[mpi_rank] <- my_peoples; // filling people_to_send with agent to migrate
			}	
		}
		do send_people_far_from_my_centroids(people_to_send); // migrate people no longer closer to our partitioning agent
		
		ask Partitioning_Agent
		{
			do update_location;	// update location
			do clean_mylist; // empty my_list
		}
	}
	
	action all_to_all_centroids // send our centroid
	{
		map<int, list<Partitioning_Agent>> centroids_to_send;
		loop MPI_INDEX from: 0 to: MPI_SIZE-1 
		{ 
			if(MPI_INDEX != MPI_RANK)
			{				
				centroids_to_send[MPI_INDEX] <- list<Partitioning_Agent>(Partitioning_Agent[0]); // fill centroids_to_send with our Partitioning Agent
			}
		}
		ask Communication_Agent_MPI
		{
			let maps <- all_to_all(centroids_to_send); // sending/receiving centroids (Partitionning agent)
		}
	}
	
	action send_people_far_from_my_centroids(map<int,list<people>> people_to_send) // migrate agent that are closer to other Partitioning_Agent
	{
		write("sending people : " + people_to_send);
		
		map<int,list<people>> new_people;
		ask Communication_Agent_MPI
		{			
			 new_people <- all_to_all(people_to_send); // all to all communication to migrate agent
			 write("agent received : " + new_people);
		}
		loop peoples over: people_to_send
		{
			ask peoples
			{
				do die; // remove the migrated agent from our distribution model
			}
		}
	}
}
species Partitioning_Agent
{
	rgb color_kmeans <- rgb(rnd(255),rnd(255),rnd(255)); 	// random color
	list<people> my_peoples <- list<people> ([]); 			// list of people inside the partition
	geometry convex <- convex_hull(polygon(my_peoples)); 	// shape of the polygon
	int mpi_rank;
	
	init
	{
		mpi_rank <- MPI_RANK; // MPI_RANK
		location <- any_location_in(world.shape); // random position in the world
	}
	
	action update_location // update the location of the centroids
	{
		list toRemove;
		loop tmp over: my_peoples // remove the dead agent from my_peoples
		{
			if(dead(tmp))
			{
				toRemove << tmp;
			}
		}
		my_peoples <- my_peoples - toRemove;
		location <- mean(my_peoples collect each.location); // move centroid at the average location of all peoples in my_peoples
	}
	
	action clean_mylist // clean the list for the next execution
	{
		my_peoples <- list<people> ([]);
	}
	
	aspect default
	{
		draw convex color: rgb(color_kmeans,0.2);
		draw cross(3, 0.5) color: color_kmeans border: color_kmeans - 25;	
	}
}

species Communication_Agent_MPI skills:[MPI_SKILL]
{	
 	map<int, unknown> all_to_all(map<int, unknown> data_send) // all to all communication
 	{
	    map<int, unknown> data_recv <- MPI_ALLTOALL(data_send); // MPI all to all
	    return data_recv;
 	}
}

experiment distribution_experiment type: MPI_EXP
{
	/*list<people> peoples; 		// trick to print the peoples on the distribution model
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
	
	reflex snapshot // take a snapshot of the current distribution model instance
	{
		write("SNAPPING___________________________________ " + cycle);
		int mpi_id <- MPI_RANK;
		
		ask simulation
		{	
			save (snapshot("agent")) to: "../../output.log/snapshot/" + mpi_id + "/cycle-"+ cycle + ".png" rewrite: true;	
		}
	}
	
	output
	{
		display agent // display to take snapshot of
		{
			agents people value: peoples;	
			agents building value: buildings;
			species Partitioning_Agent;
		}
	}*/
}
