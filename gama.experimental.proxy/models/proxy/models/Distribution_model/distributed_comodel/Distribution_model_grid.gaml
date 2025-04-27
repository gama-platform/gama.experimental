/**
* Name: Distribution_model
* Distribution model to distribute a thematic model using a grid. 
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
	
	int grid_width <- 4;
	int grid_height <- 1;
	int size_OLZ <- 5;
	
	float start_thematic_time;
	float end_thematic_time;
	
	file building_shapefile <- file("includes/building.shp");
	//Shape of the environment
	geometry shape <- envelope(building_shapefile); 
	list<people> init_peoples;
	
	init
 	{	
 		/* INIT TIMER */
 		start_thematic_time <- machine_time;
 		
 		/* INIT THEMATIC MODEL */
 		create Thematic.Thematic_experiment;
 		
 		/* INIT COMMUNICATION AGENT */
 		create Communication_Agent_MPI;
 		
 		/* INIT MPI NETWORK*/
 		MPI_RANK <- Communication_Agent_MPI[0].MPI_RANK;
 		MPI_SIZE <- Communication_Agent_MPI[0].MPI_SIZE;
		
 		/* INIT PARTITIONNING AGENT */
 		create Partitionning_agent;
 		
 		/* INITIAL PEOPLE IN THE THEMATIC SIMULATION */
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			init_peoples <- list(people); // get the initial population of people
 		}
 		
		loop current_people over: init_peoples
		{
 			cell c <- cell(current_people.location);
			if(c.rank != MPI_RANK) // remove people not in my cell
			{
				ask current_people
				{
					do die;	
				}
			}
 		}
 	}
 	
 	reflex run_thematic_model // run a cycle of the thematic model
 	{
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			do _step_;
 		}
 	}
 	
 	reflex end_distribution_model_end_cycle when: cycle = end_cycle // end the distribution model when we reach the cycle end_cycle
	{
		end_thematic_time <- machine_time;
		write("total execution time : " + ((end_thematic_time - start_thematic_time) / 1000) + "second(s)");
		write("-----------------end_cycle reached-----------------");
		do die;
	}
}

species Partitionning_agent
{
	reflex getCell
	{	
		list<people> peoples;
		map<int,list<people>> people_to_send;
		
		ask Thematic.Thematic_experiment[0]
		{
			peoples <- people collect each where not dead(each);
		}
		loop current_people over: peoples
		{			
			if(not dead(current_people))
			{	
				cell c <- cell(current_people.location); // get cell where the agent is currently on
				if(c.rank != MPI_RANK) // cell different than mine
				{
					if( people_to_send[c.rank] = nil)
					{
						people_to_send[c.rank] <- list<people>(current_people); // update people_to_send
					}else
					{
						people_to_send[c.rank] << current_people;
					}
				}
			}	
		}
		do send_people_not_in_my_cell(people_to_send);
	}
	
	action send_people_not_in_my_cell(map<int,list<people>> people_to_send)
	{
		write("sending people : " + people_to_send);
		
		map<int,list<people>> new_people;
		ask Communication_Agent_MPI
		{			
			 new_people <- all_to_all(people_to_send);
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

grid cell width: grid_width height: grid_height neighbors: 4
{ 
	int rank <- grid_x + (grid_y * grid_width);
	
	/* INNER OLZ */
	geometry OLZ_top_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_bottom_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_left_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0};
	geometry OLZ_right_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0};
	
	geometry OLZ_combined; 
	init
	{
		if(grid_y - 1 >= 0)
		{		
			OLZ_combined <- OLZ_combined + OLZ_top_inner;
		}
		if(grid_y + 1 < grid_height)
		{		
			OLZ_combined <- OLZ_combined + OLZ_bottom_inner;
		}
		if(grid_x - 1 >=0)
		{		
			OLZ_combined <- OLZ_combined + OLZ_left_inner;
		}	
		if(grid_x + 1 < grid_width)
		{		
			OLZ_combined <- OLZ_combined + OLZ_right_inner;
		}
	}
	
	aspect default
	{
		draw self.shape color: rgb(#white,125) border:#black;	
		draw "[" + self.grid_x + "," + self.grid_y +"] : " + rank color: #red font:font('Default', 15, #bold) at: {self.location.x, self.location.y};
		draw OLZ_combined color: rgb(#green, 125) border: #black;
	}
}

species Communication_Agent_MPI skills:[MPI_SKILL]
{
 	map<int, unknown> all_to_all(map<int, unknown> data_send) 	// all to all communication
 	{
	    map<int, unknown> data_recv <- MPI_ALLTOALL(data_send); // MPI all to all	
	    return data_recv;
 	}
 	
 	list<unknown> gather(unknown data, int dest)
 	{
	    list<unknown> data_recv <- MPI_GATHER(data, dest);
	    return data_recv;
 	}
 	
 	action send(unknown data, int dst)
 	{
 		do MPI_SEND(data, dst);
 	}
 	
 	unknown receive(int emet)
 	{
	    unknown data <- MPI_RECV(emet);
 	}
}

experiment distribution_experiment type: MPI_EXP
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
		write("SNAPPING___________________________________ " + cycle);
		int mpi_id <- MPI_RANK;
		
		ask simulation
		{	
			save (snapshot("agent")) to: "../../output.log/snapshot/" + mpi_id + "/cycle-"+ cycle + ".png" rewrite: true;	
		}
	}
	output
	{
		display agent
		{
			agents people value: peoples;	
			agents building value: buildings;
			species cell aspect: default;
		}
	}
}
