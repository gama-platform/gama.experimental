/**
* Name: mpigather
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model mpigather

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
		int_to_send <- MPI_RANK;
		
		file_name <- "log"+mpi_rank+".txt";
		do clearLogFile();
		

		if (mpi_rank = 0)
		{
			do writeLog("mpi world size is " + mpi_size);
			list<unknown> gather <- MPI_GATHER(list(mpi_rank), rank_to_send_data);	
			do writeLog("result of gather : " + gather);
		}else
		{
			do MPI_GATHER(list(mpi_rank), rank_to_send_data);	
		    do writeLog("" + mpi_rank + "sent my int");
		}
		
		do die;
	}
    
    action writeLog(string log)
	{
		save log format: "text" to: file_name rewrite:false;
	}
	
	action clearLogFile
	{
		save "" format: "text" to: file_name rewrite:true;
	}
}


experiment mpi_gather
{ 
}