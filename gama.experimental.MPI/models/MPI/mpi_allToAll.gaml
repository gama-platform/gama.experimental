/**
* Name: mpialltoall
* MPI_ALLTOALL with GAMA
* Author: Lucas Grosjean
* Tags: HPC, MPI, Network, Communication
*/


model mpialltoall

global skills: [MPI_SKILL]
{
	int mpi_rank <- 0;
    int mpi_size <- 0;

	init
	{
		write("HELLO WOLRD MPI0");
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;

		do all_to_all_data(1000);
		
		
		//do all_to_all_agent;
		
		do die;
	}
	
	action all_to_all_data(int size)
	{
		map<int, list<int>> msg;
		loop ind from: 0 to: mpi_size - 1
		{
			if(ind != mpi_rank)
			{
				msg[ind] <- [];
				loop indSize from: 0 to: size
				{
					msg[ind] << mpi_rank; // sending my rank
				}
			}
		}
		
		write("sending message " + msg);
		map<int, list<int>> alltoall <- MPI_ALLTOALL(msg);	
		write("result of alltoall : " + alltoall);
		
		loop ind from: 0 to: mpi_size - 1 
		{
			if(ind != mpi_rank)
			{
				write("" + ind + " :: " + alltoall[ind]);
			}
		}
	}
	
	action all_to_all_agent
	{
		map<int, list<emptyAgent>> msg2;
		loop ind from: 0 to: mpi_size
		{
			if(ind != mpi_rank)
			{
				create emptyAgent with: [data::rnd(10)];
				msg2[ind] <- list<emptyAgent>(emptyAgent[0]);
			}
		}
		
		write("sending message " + msg2);
		map<int, list<emptyAgent>> alltoall2 <- MPI_ALLTOALL(msg2);	
		write("result of alltoall2 : " + alltoall2);
	}
}

species emptyAgent
{
	int data;
}
experiment mpi_alltoall type: MPI_EXP
{ 
}