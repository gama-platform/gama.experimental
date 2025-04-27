/**
* Name: send_recv
* Author: Lucas Grosjean
* Description: Test of isend with MPI
* Tags: MPI, Network, HPC
*/

model ISend


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
    }
    
    reflex
    {
    	if (mpi_rank = 0){
			int dst <- 1;
		    map<int, float> msg;
		    msg[0] <- rnd (2.0, float(cycle));
		    msg[45] <- rnd (2.0, float(cycle));
		    msg[57] <- rnd (2.0, float(cycle));
		    
			do MPI_ISEND(msg, dst, cycle);
			
	    	do sleepy(20);
	 		write("total_duration " + float(total_duration)/1000 + "s");
		} else {
		    int emet <- 0;
		    int j <- 0;
	 		write("total_duration " + float(total_duration)/1000 + "s");
	    	unknown l <- MPI_RECV(emet,cycle);
	    	
	    	do sleepy(50);
	    	
	    	write("MPI_RECV done : " + l);
	    	if(l = nil)
	    	{
	    		write("do pause " + l);
	    		float v <- float(total_duration);
	    		write("total_duration " + float(v - (20*cycle))/1000 + "s");
	    		
	    	 	do die;
	    	}
	 		write("total_duration " + float(total_duration)/1000 + "s");
		}
		write("cycle " + cycle);
		
		
    	if(cycle > iterations){
	 		write("total_duration " + float(total_duration)/1000 + "s");
	 		write("duration " + float(duration)/1000 + "s");
	 		
	 		do MPI_BARRIER();
    		do die;
    	}
    }
}

experiment send_recv type: MPI_EXP
{
}
