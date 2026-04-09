/**
* Name: mpi_gather
* Author: Lucas Grosjean
* Description: Test of mpi_gather with MPI
* Tags: MPI, Network, HPC
*/


model mpigather


global skills: [MPI_SKILL]
{
	int mpi_rank <- 0;
    int mpi_size <- 0;
	int rank_to_send_data <- 0;			// this processor will receive the data

    int int_to_send;

	init
	{
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;
		int_to_send <- MPI_RANK;
		
		do gather_same_number_of_elements;
		do gather_different_number_of_elements;
		
		do die;
	}
	
	action gather_same_number_of_elements
	{
		if (mpi_rank = 0)
		{
			list<unknown> gather <- MPI_GATHER(nil, rank_to_send_data);		// no need to send anything
			write("result of gather : " + gather);
		}else
		{			
			do MPI_GATHER(list(mpi_rank), rank_to_send_data);	// 2 elements
		   	write("" + mpi_rank + "sent my data");
		}
	}
	
	action gather_different_number_of_elements
	{
		if (mpi_rank = 0)
		{
			list<unknown> gather <- MPI_GATHER(nil, rank_to_send_data);		// no need to send anything
			write("result of gather : " + gather);
		}else
		{
			if(mpi_rank = 1)
			{			
				do MPI_GATHER(list(mpi_rank,mpi_rank), rank_to_send_data);	// 2 elements
			}else
			{
				do MPI_GATHER(list(mpi_rank), rank_to_send_data);			// 1 element
			}
		   	write("" + mpi_rank + "sent my data");
		}
	}
}


experiment mpi_gather type: MPI_EXP
{ 
}