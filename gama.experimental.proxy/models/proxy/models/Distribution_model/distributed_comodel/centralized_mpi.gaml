/**
* Name: centralized_mpi
* Distribution model to distribute a thematic model using a grid. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Distribution

import "Continuous_Move_thematic.gaml" as Thematic

global
{
	int end_cycle <- 100;
	
	file building_shapefile <- file("includes/building.shp");
	//Shape of the environment
	geometry shape <- envelope(building_shapefile); 
	list<people> init_peoples;
	
	init
 	{	
 		create Thematic.Thematic_experiment;
 		create Communication_Agent_MPI;
 	}
 	
 	reflex run_thematic_model // run a cycle of the thematic model
 	{
 		write("distribution step : --------------------------------------" + cycle);
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			//do _step_;
 		}
 	}
 	
 	
 	
 	reflex end_distribution_model_end_cycle when: cycle = end_cycle // end the distribution model when we reach the cycle end_cycle
	{
		write("-----------------end_cycle reached-----------------");
		
		ask Communication_Agent_MPI
		{
			do MPI_FINALIZE;
		}
		do die;
	}
 	
 	reflex test_all_to_all
 	{
 		ask Communication_Agent_MPI
 		{
 			do MPI_BARRIER();
 			write("going");
	 		
	 		//do testAlltoAll(2000000);
	 		
	 		//do testAlltoAllv(3000000); 		// OK
	 		//do testAlltoAllv(2500000); 	// NOK
	 		//do testAlltoAllv(2000000); 	// NOK
	 		//do testAlltoAllv(1750000);		// OK
	 		//do testAlltoAllv(1500000);		// OK
	 		//do testAlltoAllv(1000000);		// OK
	 		//do testAlltoAllv(500000); 		// OK
	 		//do testAlltoAllv(400000); 		// OK
	 		//do testAlltoAllv(350000); 	// NOK
	 		//do testAlltoAllv(300000); 	// NOK
	 		//do testAlltoAllv(250000); 	// NOK
	 		//do testAlltoAllv(200000); 	// NOK
	 		//do testAlltoAllv(100000); 	// NOK
	 		//do testAlltoAllv(50000); 		// NOK
	 		//do testAlltoAllv(25000); 		// NOK
	 		//do testAlltoAllv(20000); 		// OK
	 		//do testAlltoAllv(12500); 		// OK
	 		//do testAlltoAllv(10000); 		// OK
	 		
	 		do testAlltoAllv(9000); 		// OK
	 		do testAlltoAllv(7500); 		// OK
	 		do testAlltoAllv(5000); 		// OK
	 		do testAlltoAllv(3500); 		// OK
	 		do testAlltoAllv(2000); 		// OK
	 		do testAlltoAllv(1000); 		// OK
	 		do testAlltoAllv(700);	 		// OK
	 		do testAlltoAllv(500);	 		// OK
	 		do testAlltoAllv(200);	 		// OK
	 		
	 		//do testAlltoAllv(100); 			// OK
	 		//do testAlltoAllv(10); 			// OK
	 		//do testAlltoAllv(1); 			// OK
 			write("gone");
 		}
 	}
 	
 	reflex comm
 	{
 		/*ask Communication_Agent_MPI
 		{
 			do MPI_BARRIER();
 		}
 		
 		list<people> peoples_;
		ask Thematic.Thematic_experiment[0].simulation
 		{
 			peoples_ <- list(people);
 		}
 		
 		ask Communication_Agent_MPI
 		{
			map<int,list<people>> m;
 			if(MPI_RANK = 0)
			{
				loop MPI_INDEX from: 0 to: MPI_SIZE-1 
				{
					if(MPI_INDEX != MPI_RANK)
					{
						m[MPI_INDEX] <- peoples_;				
					}else
					{
						m[MPI_INDEX] <- peoples_;
					}
				}
				do MPI_ALLTOALL(m);
			}else
			{	
				do MPI_ALLTOALL(m);
			}	
		}*/
 		
 		/*ask Communication_Agent_MPI
 		{
 			if(MPI_RANK = 0)
			{
		    	do MPI_SEND(peoples_, 1, 50);
			}else
			{	
		    	list<unknown> l2 <- MPI_RECV(0, 50);
		    	write("received : " + l2);
				ask Thematic.Thematic_experiment[0].simulation
		 		{
		 			ask people
		 			{
		 				do die;
		 			}
		 		}
			}
 		}*/
 		
 		
 		/*ask Communication_Agent_MPI
		{
			map<int,list<people>> m;
			if(MPI_RANK = 0)
			{
				loop MPI_INDEX from: 0 to: MPI_SIZE-1 
				{
					if(MPI_INDEX != MPI_RANK)
					{
						m[MPI_INDEX] <- peoples_;				
					}
				}
			}
			let maps <- all_to_all(m); // sending/receiving data
			
			//write("maps " + maps);
			
			write("" + MPI_RANK + " OK");
			if(MPI_RANK != 0)
			{
				ask Thematic.Thematic_experiment[0].simulation
		 		{
		 			ask people
		 			{
		 				//do die;
		 			}
		 		}
			}
		}*/
 	}
}

species Communication_Agent_MPI skills:[MPI_SKILL]
{	
	
	init
	{
		if(MPI_RANK != 0)	
		{	
	 		ask Thematic.Thematic_experiment[0].simulation
	 		{
	 			ask people
	 			{
	 				do die;
	 			}
	 		}
		}
	}
 	map<int, unknown> all_to_all(map<int, unknown> data_send) // all to all communication
 	{
	    map<int, unknown> data_recv <- MPI_ALLTOALL(data_send); // MPI all to all
	    return data_recv;
 	}
}

experiment distribution_experiment type: MPI_EXP until: (cycle = end_cycle)
{
}
