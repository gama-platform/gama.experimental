/**
* Name: Visualisation
* Visualisation_Agent for distributed  data visualisation. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model, MPI
*/

model Visualisation

global skills: [MPI_SKILL]
{
	init
	{
		write("initializing on processor " + MPI_RANK);
		create visualisation_agent;
		
		do die;
	}
}

species visualisation_agent skills: [MPI_SKILL]{
	
	int rank; 									// MPI rank of the processor
	list<int> values; 							// values on this processor
	float average_values; 						// local average values
	int root <- 0;
	
	init{
		rank <- MPI_RANK;	
		values <- [rnd(10),rnd(20),rnd(40),rnd(42),rnd(22),rnd(22),
			rnd(34),rnd(42),rnd(120)
		]; // random values
		average_values <- mean(values); // average values on this processor
		
		if(rank != root)
		{
			do send_data;
		}else // root
		{		
			do compute_result;
		}	
	}
	
	action send_data
	{
		do MPI_GATHER(average_values, root);  // sending average value to root
	}
	
	action compute_result
	{	
		float total_average_values;
		list<float> values_from_other_rank; 
		values_from_other_rank <- list<int> (MPI_GATHER(average_values, root)); // receive all the local average
		total_average_values <- mean(values_from_other_rank); 					// compute global average
	}
}

experiment distributed_visualisation type: distribution until: (cycle = 5)
{
}
