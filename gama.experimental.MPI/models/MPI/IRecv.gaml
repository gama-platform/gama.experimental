/**
* Name: IRecv
* Author: Lucas Grosjean
* Description: Test of irecv in GAMA
* Tags: MPI, Network, HPC
*/

model IRecv


global skills:[MPI_SKILL]
{
    int mpi_rank <- 0;
    int mpi_size <- 0;
	
	int iterations <- 5000;
    
    init
    {
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;
		
		write("my mpi rank _________________________ " + mpi_rank);	
		write("mpi world size is____________________ " + mpi_size);
    	
    	
    	if (mpi_rank = 0){
    		do sleepy(1000);
    		do MPI_SEND("hello", 1);
    		do die;
		}else
		{
			unknown l <- MPI_IRECV();
			loop while: l = nil // while not received, we loop
			{
				write("??? " + l);
		    	l <- MPI_IRECV();
			}
    		write('message received ' + l);
    		do die;
		}
    }
}

experiment IRecv type: MPI_EXP
{
}
