/**
* Name: rank_size
* Author: Lucas Grosjean, Nicolas Marilleau
* Description: test of MPI_RANK and MPI_SIZE
* Tags: MPI, Network, HPC
*/

model rank_size


global skills:[MPI_SKILL]
{
	int mpi_rank <- 0;
	int mpi_size <- 0;
	string file_name;
	
	init
	{
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;
		
		write("my mpi rank is " + mpi_rank);	
		write("mpi world size is " + mpi_size);
		
		do die;
	}
}

experiment rank_size
{
}
