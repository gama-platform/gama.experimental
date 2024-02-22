/**
* Name: mpigather
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model mpialltoall

/* Insert your model definition here */

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
		map<int, list<emptyAgent>> msg2;
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
		let alltoall <- MPI_ALLTOALL(msg);	
		write("result of alltoall : " + alltoall);
		
		do die;
	}
}
experiment mpi_alltoall type: distribution until: (cycle = 1)
{ 
}