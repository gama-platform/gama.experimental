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
	int end_cycle <- 500;
	int MPI_RANK;
	int MPI_SIZE;
	
	float start_thematic_time;
	float end_thematic_time;
	init
 	{	
 		start_thematic_time <- machine_time;
 		write("start_thematic_time " + start_thematic_time);
 		
 		create Coherence_Agent;
 		
 		MPI_RANK <- Coherence_Agent[0].MPI_RANK;
 		MPI_SIZE <- Coherence_Agent[0].MPI_SIZE;
 		write("MY MPIRANK IS " + MPI_RANK);
 		write("MPI_SIZE IS " + MPI_SIZE);
 		
 		create Thematic.Thematic_experiment;
 		ask Thematic.Thematic_experiment[0]
 		{
 			self.shape <- myself.shape;
 			
 			int total_people <- length(people);
 			write("total_people : " + total_people);
 			int split_people <- (total_people / MPI_SIZE);
 			write("split_people : " + split_people);
 			
			int start_index <- (split_people * MPI_RANK);
			int end_index <- start_index + split_people - 1;
 			write("[" + start_index + "-" + end_index + "]");
 			
 			list toRemove;
 			loop peep from: 0 to: total_people-1
 			{
 				if(peep >= start_index and peep <= end_index)
 				{
 					write("keeping agent " + peep +"alive");
 				}else
 				{
 					write("killing people " + peep);
 					ask people[peep]
 					{
 						toRemove << self;
 					}
 				}
 			}
 			ask toRemove
 			{
 				do die;
 			}
 			
 			write("maximum agent at init : " + split_people);
 			write("amount we got : " + length(people));
 			
 			ask people
 			{
 				write("hello i'm alive " + name);
 			}
 			
 		}
		create centroids number: 1;
 		
 		create Partitionning_Agent;
 		create Communication_Agent_MPI;
 	}
 	
 	reflex run_thematic_model
 	{
 		write("distribution step : --------------------------------------" + cycle);
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			do _step_;
 		}
 	}
 	
 	reflex end_distribution_model_no_more_agent
 	{
 		ask Thematic.Thematic_experiment[0]
 		{
 			if(length(people) = 0)
 			{
 				end_thematic_time <- machine_time;
 				write("total execution time : " + ((end_thematic_time - start_thematic_time) / 1000) + "second(s)");
 				write("-----------------no more agent to execute-----------------");
 				ask myself
 				{
 					do die;
 				}
 			}
 		}
 	}
 	reflex end_distribution_model_end_cycle when: cycle = end_cycle
	{
		end_thematic_time <- machine_time;
		write("total execution time : " + ((end_thematic_time - start_thematic_time) / 1000) + "second(s)");
		write("-----------------end_cycle reached-----------------");
		do die;
	}
}

species Partitionning_Agent
{
	reflex distribution
	{
		do compute_convex();
 		do all_to_all_centroids();
	}
	
	action compute_convex
	{
		list<people> peoples;
		list<people> dead_peoples;
		map<int,list<people>> people_to_send;
		
		ask Thematic.Thematic_experiment[0]
		{
			peoples <- people collect each where not dead(each);
			dead_peoples <- people collect each where dead(each);
		}
		
		loop current_people over: peoples
		{			
			if(not dead(current_people))
			{		
				centroids closest <- centroids closest_to current_people;
				write(" " + current_people + " is closer from centroids " + closest);
				closest.mypoints << current_people; 
			}
		}
		write("dead poeple : " + dead_peoples);
		ask centroids
		{
			write("my(" + self  + ") points are : " + mypoints + " location " + location + " UUID : " + getUUID());
			if(mpi_rank != MPI_RANK)
			{
				write("self " + self + "  :: " + mpi_rank);
				people_to_send[mpi_rank] <- mypoints;
			}
			
		}
		
		write("people_to_send " + people_to_send);
		do send_people_far_from_my_centroids(people_to_send);
		
		ask centroids
		{
			do update_location;	// update location
			do clean_mylist; // empty my_list
		}
	}
	
	action all_to_all_centroids
	{
		map<int, list<centroids>> centroids_to_send;
		loop MPI_INDEX from: 0 to: MPI_SIZE 
		{ 
			if(MPI_INDEX != MPI_RANK)
			{				
				centroids_to_send[MPI_INDEX] <- list<centroids>(centroids[0]);
			}
		}
		
		ask Communication_Agent_MPI
		{
			do all_to_all(centroids_to_send); // sending/receiving centroids[0]
		}
	}
	
	action send_people_far_from_my_centroids(map<int,list<people>> people_to_send)
	{
		write("sending people : " + people_to_send);
		
		map<int,list<people>> new_people;
		ask Communication_Agent_MPI
		{			
			 new_people <- all_to_all(people_to_send);
		}
		loop peoples over: people_to_send
		{
			ask peoples
			{
				write("killing people " + name);
				do die;
			}
		}
		write("new_people inside " + new_people);
	}
}
species centroids
{
	rgb color_kmeans <- rgb(rnd(255),rnd(255),rnd(255));
	list<people> mypoints <- list<people> ([]);
	int mpi_rank;
	
	init
	{
		location <- { rnd(world.shape.width), rnd(world.shape.height)};
		mpi_rank <- MPI_RANK;
		write("my(" + name + ") UUID " + getUUID());
	}
	
	action update_location
	{
		list toRemove;
		loop tmp over: mypoints
		{
			if(dead(tmp))
			{
				toRemove << tmp;
			}
		}
		mypoints <- mypoints - toRemove;
		location <- mean(mypoints collect each.location); // move centroid in the middle of the convex
	}
	
	action clean_mylist
	{
		mypoints <- list<people> ([]);
	}
	
	aspect default
	{
		draw cross(3, 0.5) color: color_kmeans border: color_kmeans - 25;	
		geometry convex <- convex_hull(polygon(mypoints));
		draw convex color: rgb(color_kmeans,0.2);
	}
}

species Communication_Agent_MPI skills:[MPI_SKILL]
{
 	action send(unknown data, int dst)
 	{
	    write("MPI_SEND " + data + " to " + dst);
 		do MPI_SEND(data, dst, 50);
 	}
 	
 	unknown receive(int emet)
 	{
	    unknown data <- MPI_RECV(emet, 50);
	    write("MPI_RECV " + data + " from " + emet);
 	}
 	
 	action scatter(unknown data)
 	{
 		// todo
 	}
 	
 	list<unknown> gather(unknown data, int dest)
 	{
	    list<unknown> data_recv <- MPI_GATHER(data, dest);
	    return data_recv;
 	}
 	
 	map<int, unknown> all_to_all(map<int, unknown> data_send)
 	{
 		write("all_to_all from (" + MPI_RANK + ") : " + data_send);
	    map<int, unknown> data_recv <- MPI_ALLTOALL(data_send);
	    write("DATA RECEIVED : " + data_recv);
	    return data_recv;
 	}
}

species Visualisation_Agent
{
	action snap
	{
 		ask Thematic.Thematic_experiment[0].simulation 
		{	
			save (snapshot("chart")) to: "output.log/snapshot/" + cycle + ".png" rewrite: true;
		}
	}
}

species Coherence_Agent skills: [MPI_SKILL]
{	
	action synchronize
	{
		do MPI_BARRIER;
	}
}

experiment distribution_experiment type: MPI_EXP  until: (cycle = end_cycle)
{
	reflex
	{
		ask simulation 
		{	
			//save (snapshot("agent")) to: "../../output.log/snapshot/" + MPI_RANK + "/cycle"+ cycle + ".png" rewrite: true;	
		}
	}
	output
	{
		display agent
		{
			species centroids;
			species people;
		}
	}
}
