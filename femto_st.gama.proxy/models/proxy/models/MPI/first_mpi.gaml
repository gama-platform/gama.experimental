/**
* Name: firstmpi
* Author: nicolas
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model firstmpi


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
		
	    do writeLog("mon rank est : " + mpi_rank);
		do writeLog("la size est : " + mpi_size);

		do MPI_FINALIZE();

		do pause;
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

experiment test_mpi until: (cycle = 1)
{
}