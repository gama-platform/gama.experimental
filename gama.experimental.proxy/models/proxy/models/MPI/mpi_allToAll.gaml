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
	int rank_to_send_data <- 0;

    int int_to_send;
	string file_name;

	init
	{
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;

		map<int, list<int>> msg;
		loop ind from: 0 to: mpi_size
		{
			if(ind != mpi_rank)
			{
				msg[ind] <- list(mpi_rank);
				loop ind2 from: 0 to: ind
				{
					msg[ind] <- msg[ind] + list(mpi_rank);
				}
			}
		}

		write("mpi world size is " + mpi_size);
		write("message " + msg);
		map<int, list<int>> alltoall <- MPI_ALLTOALL(msg);	
		write("result of alltoall : " + alltoall);
		
		map<int, list<emptyAgent>> msg2;
		loop ind from: 0 to: mpi_size
		{
			if(ind != mpi_rank)
			{
				create emptyAgent with: [data::rnd(10)];
				msg2[ind] <- list<emptyAgent>(emptyAgent[0]);
			}
		}
		
		map<int, list<emptyAgent>> alltoall2 <- MPI_ALLTOALL(msg2);	
		write("result of alltoall2 : " + alltoall2);
		
		do die;
	}
}

species emptyAgent
{
	int data;
}
experiment mpi_alltoall type: distribution
{ 
}