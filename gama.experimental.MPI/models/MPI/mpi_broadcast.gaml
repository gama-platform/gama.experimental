/**
* Name: mpi_broadcast
* Author: Lucas Grosjean
* Description: Test of mpi_broadcast with MPI
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
		
		do broadcast_data;
		do broadcast_agent;
		
		do die;
	}
	
	action broadcast_data
	{
		if(mpi_rank = rank_sending_data)
		{
			list<int> msg <- list(mpi_rank);
			list<unknown> broadcast <- MPI_BROADCAST(msg, rank_sending_data);
			write("broadcast result " + broadcast);
		}else
		{
			list<unknown> broadcast <- MPI_BROADCAST(nil, rank_sending_data);
			write("broadcast result " + broadcast);
		}
	}
	
	action broadcast_agent
	{
		if(mpi_rank = rank_sending_data)
		{
			create emptyAgent;
			emptyAgent[0].data <- 10;
			list<emptyAgent> msg <- list(emptyAgent[0]);
			list<emptyAgent> broadcast <- MPI_BROADCAST(msg, rank_sending_data);
			write("broadcast result " + broadcast);
		}else
		{
			list<emptyAgent> broadcast <- MPI_BROADCAST(nil, rank_sending_data);
			write("broadcast result " + broadcast);
			
			loop emptyAgt over: broadcast
			{
				write("received emptyAgt : " + emptyAgt.data);
			}
		}
	}
}

species emptyAgent
{
	int data;
}

experiment mpi_broadcast type: MPI_EXP
{ 
}