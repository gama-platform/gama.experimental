/**
* Name: firstmpi
* Author: nicolas
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model communication


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
		

		do writeLog("mon rank est " + mpi_rank);	
		do writeLog("la size est " + mpi_size);
	
		if (mpi_rank = 0){
	
		    int dst <- 1;
		    list<int> msg <- [10];
		    do MPI_SEND mesg: msg dest: dst stag: 50;
		    do writeLog("MPI_SEND done");
		    
		} else {
		    int emet <- 0;
		    list l <- self MPI_RECV [source:: emet, rtag:: 50];
		    do writeLog("MPI_RECV done : " + l);
		}
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

experiment com_mpi until: (cycle = 1)
{
}