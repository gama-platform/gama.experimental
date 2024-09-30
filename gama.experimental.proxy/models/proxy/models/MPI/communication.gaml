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
    
    init
    {
		mpi_rank <- MPI_RANK;
		mpi_size <- MPI_SIZE;
		
		write("mon rank est " + mpi_rank);	
		write("la size est " + mpi_size);
	
		if (mpi_rank = 0){
	
		    int dst <- 1;
		    list<int> msg <- [10];
		    do MPI_SEND(msg ,dst, 50);
		    write("MPI_SEND done ");
		    
		} else {
		    int emet <- 0;
		    list l <- MPI_RECV(emet, 50);
		    write("MPI_RECV done : " + l);
		}
	    do die;
    }
}

experiment com_mpi
{
}