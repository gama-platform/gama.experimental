/**
* Name: Visualisation
* Visualisation_Agent from distributed visualisation. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Visualisation

global skills: [MPI_SKILL]
{
	init
	{
		write("initializing on processor " + MPI_RANK);
		create visualisation_agent;
	}
}

species visualisation_agent skills: [MPI_SKILL]{
	
	int rank; // MPI rank of the processor
	list<int> values; // values on this processor
	list<float> values_from_other_rank; // only for root
	float average_values; // averagez values on this processor
	float total_average_values; // only for root
	int root <- 0;
	
	init{
		rank <- MPI_RANK;	
		values <- [rnd(10),rnd(20),rnd(40),rnd(42),rnd(22),rnd(22),
			rnd(34),rnd(42),rnd(120)
		]; // random values
		average_values <- mean(values); // average values on this processor
	}
	
	reflex compute_result{
		if(rank = root){		
			values_from_other_rank <- list<int> (MPI_GATHER(average_values, root));
			total_average_values <- mean(values_from_other_rank); // compute total average
		}else
		{
			do MPI_GATHER(average_values, root);
		}
	}
}

experiment distributed_visualisation type: distribution until: (cycle = 5)
{
}
