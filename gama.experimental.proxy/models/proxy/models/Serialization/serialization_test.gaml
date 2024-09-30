/**
* Name: send_recv
* Author: Lucas Grosjean, Nicolas Marilleau
* Description: Test of send and receive with MPI
* Tags: MPI, Network, HPC
*/

model serialization_test

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
		
		write("my mpi rank _________________________ " + mpi_rank);	
		write("mpi world size is____________________ " + mpi_size);
		
		create emptyAgent;
		emptyAgent[0].data <- mpi_rank;
	
		if (mpi_rank = 0)
		{
			ask emptyAgent
		    {
				write("name111 : " + name + " data " + data +" uuid " + self.getUUID());    	
		    }
		    
			int dst <- 1;
			list<emptyAgent> msg <- list(emptyAgent[0]);
		    do MPI_SEND(msg, dst, 50);
		    write("MPI_SEND done");
		    
		    list<emptyAgent> l3 <- MPI_RECV(dst, 50); // should be not be created but updated
		    
		    ask emptyAgent
		    {
				write("name : " + name + " data " + data +" uuid " + self.getUUID());    	
		    }
		    
		    // at this point, only emptyAgent[0] exist, with data = 100
		} else 
		{
		    int emet <- 0;
		    list<emptyAgent> l3 <- MPI_RECV(emet, 50); // receiving a new agent
		    write("MPI_RECV 3 done : " + l3[0].data);
		    write("MPI_RECV 3 done2 : " + l3[0].getUUID());
		    
		    emptyAgent[1].data <- 100; // updating data
		    
			list<emptyAgent> msg <- list(emptyAgent[1]); // sending received agent back
		    do MPI_SEND(msg, emet, 50);
		    
		    ask emptyAgent
		    {
				write("name : " + name + " data " + data);    	
		    }
		    // at this point both emptyAgent[0] and [1] exist
	    }
    
		do MPI_FINALIZE();
	    do die;
    }
}

species emptyAgent
{
	int data;
}

experiment serialization_test type: MPI_EXP
{
}
