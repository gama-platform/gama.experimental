/**
* Name: send_recv
* Author: Lucas Grosjean, Nicolas Marilleau
* Description: Test of send and receive with MPI
* Tags: MPI, Network, HPC
*/

model send_recv


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
	
		if (mpi_rank = 0)
		{
			int dst <- 1;
			create emptyAgent;
			emptyAgent[0].data <- 10;
			
			create emptyAgent;
			emptyAgent[1].data <- 20;
			
			create emptyAgent;
			emptyAgent[2].data <- 30;
			
			//emptyAgent msg3 <- emptyAgent[0];
		    
			let msg3 <- 3 among emptyAgent;
			//let msg3 <- [emptyAgent[0],emptyAgent[1],emptyAgent[2]];
		    do MPI_SEND(msg3, dst, 50);
		    write("MPI_SEND 3 done");
		    write("" + MPI_RANK + " lenght emptyAgent" + length(emptyAgent));
			
		    /*
		    list<unknown> msg <- [10];
		    do MPI_SEND(msg, dst, 50);
		    write("MPI_SEND 1 done");

			list<unknown> msg2 <- [false, 10, 1.5, "hello"];
		    do MPI_SEND(msg2, dst, 50);
		    write("MPI_SEND 2 done");*/
		    
		} else {
		    int emet <- 0;
		    
		    /*list<unknown> l <- MPI_RECV(emet, 50);
		    write("MPI_RECV done : " + l);

		    list<unknown> l2 <- MPI_RECV(emet, 50);
		    write("MPI_RECV 2 done : " + l2);*/
		    
		    list<emptyAgent> l3 <- MPI_RECV(emet, 50);
		    write("MPI_RECV 3 done : " + l3);
		    
			write("" + MPI_RANK + " emptyAgents :: " + list(emptyAgent));
			ask emptyAgent
			{
				write("rank " + MPI_RANK + "[" + self.name + "(data)::" + self.data + "]");
			}
		    write("" + MPI_RANK + " lenght emptyAgent" + length(emptyAgent));
		}
	    
		do MPI_FINALIZE();
	    do die;
    }
}

species emptyAgent
{
	int data;
}
experiment send_recv until: (cycle = 1)
{
}
