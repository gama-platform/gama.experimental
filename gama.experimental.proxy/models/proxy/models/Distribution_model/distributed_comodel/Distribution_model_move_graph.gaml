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
	list<people> peoples;
	
	float start_thematic_time;
	float end_thematic_time;
	init
 	{	
 		start_thematic_time <- machine_time;
 		write("start_thematic_time " + start_thematic_time);
 		
 		create Thematic.Thematic_experiment;
 		create Communication_Agent_MPI;
 		
 		MPI_RANK <- Communication_Agent_MPI[0].MPI_RANK;
 		MPI_SIZE <- Communication_Agent_MPI[0].MPI_SIZE;
 		
 		write("MPI_RANK " + MPI_RANK);
 		
 		
		write("world.shape.height before " + world.shape.height);
		write("world.shape.width before " + world.shape.width);
 		
 		ask Thematic.Thematic_experiment[0]
 		{
 			peoples <- list(people);
		}
		
		write("world.shape.height last" + world.shape.height);
		write("world.shape.width last " + world.shape.width);
		
 		create Communication_Agent_MPI;
 		create Partitionning_agent;
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

species Partitionning_agent
{
	
}

species Communication_Agent_MPI skills:[MPI_SKILL]
{	
 	map<int, unknown> all_to_all(map<int, unknown> data_send)
 	{
 		write("all_to_all from (" + MPI_RANK + ") : " + data_send);
	    map<int, unknown> data_recv <- MPI_ALLTOALL(data_send);
	    write("DATA RECEIVED : " + data_recv);
	    return data_recv;
 	}
}

experiment distribution_experiment type: MPI_EXP  until: (cycle = end_cycle)
{
	reflex snap
	{
		write("SNAPPING___________________________________ cycle " + cycle);
		int mpi_id <- MPI_RANK;
		//save distribution simulation snapshot
		
		ask simulation
		{	
			save (snapshot("agent")) to: "../../output.log/snapshot/" + mpi_id + "/cycle"+ cycle + ".png" rewrite: true;	
		}
		//save sub simulation snapshot
		ask Thematic.Thematic_experiment[0].simulation 
		{	
			save (snapshot("map")) to: "../../output.log/snapshot/" + mpi_id + "/thematic_cycle"+ cycle + ".png" rewrite: true;
		}
	}
	output
	{
		display agent
		{
			species people;
		}
	}
}
