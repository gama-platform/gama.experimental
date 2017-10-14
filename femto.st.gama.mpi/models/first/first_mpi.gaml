/**
* Name: firstmpi
* Author: nicolas
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model firstmpi


global skills:[MPI_Network]
{
	int mpi_rank <- 0;
	int mpi_size <- 0;
	init
	{
		do MPI_INIT;
		mpi_rank <- MPI_RANK();
		write "mon rank est " + mpi_rank;

		mpi_size <- MPI_SIZE();
		write "la size est " + mpi_size;
	}
}

experiment test_mpi
{
	output
	{
		monitor "rank" value:my_rank;		
	}

}
