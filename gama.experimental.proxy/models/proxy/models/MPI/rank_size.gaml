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

		file_name <- "log"+mpi_rank+".txt";
		do clearLogFile();
		
		do writeLog("my mpi rank is " + mpi_rank);	
		do writeLog("mpi world size is " + mpi_size);
		
		do die;
	}
    
    action writeLog(string log)
	{
		save log to: file_name rewrite:false;
	}
	
	action clearLogFile
	{
		save "" to: file_name rewrite:true;
	}
}

experiment rank_size type: distribution
{
}
