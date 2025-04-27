/**
* Name: mpi_scatter
* Author: Lucas Grosjean
* Description: Test of mpi_scatter with MPI
* Tags: MPI, Network, HPC
*/


model mpiscatter


global skills: [MPI_SKILL]
{
	int mpi_rank <- 0;
    int mpi_size <- 0;
	int rank_sending_data <- 0;			// this processor will send the data

	init
	{
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;
		
		do scatter_same_number_of_elements;
		do scatter_different_number_of_elements;
		
		do die;
	}
	
	action scatter_same_number_of_elements
	{
		map<int, list<int>> msg;
		if (mpi_rank = rank_sending_data)
		{
			loop ind from: 0 to: mpi_size
			{
				msg[ind] <- list(mpi_rank); // sending my rank
			}
		}
		list<unknown> scatter <- MPI_SCATTER(msg, rank_sending_data);
		write("scatter result " + scatter);
	}
	
	action scatter_different_number_of_elements
	{
		map<int, list<int>> msg;
		if (mpi_rank = rank_sending_data)
		{
			loop ind from: 0 to: mpi_size
			{
				if(ind = 1)
				{				
					msg[ind] <- list(mpi_rank,mpi_rank); // sending my rank
				}else
				{
					msg[ind] <- list(mpi_rank); // sending my rank
				}
			}
		}
		list<unknown> scatter <- MPI_SCATTER(msg, rank_sending_data);
		write("scatter result " + scatter);
	}
}


experiment mpi_scatter type: MPI_EXP
{ 
}