/**
* Name: mpi controler 3D
* Author: Lucas Grosjean
* Description: Controler of SHP.gaml
*/
model controler

import "SHP.gaml" as pp

global skills: [MPI_Network] 
{
		
    int mpiRank <- 0;
    int netSize <- 0;
    
    int nb_agent_total <- 50;
    int nb_agent_per_proc;
	
    int current_step <- 0;
    int nbLoop <- 100;
	
	// Rank of neighbor
    int otherNeighbor;
    
	int p;
	
    init 
    {
    	do runModel;
        do MPI_INIT;
		
	    mpiRank <- MPI_RANK();
	     
	    netSize <- MPI_SIZE();
	    
    	do init_sub_simulation;
    	
    
    	nb_agent_per_proc <- nb_agent_total / netSize;
    	write("nb_agent_per_proc : "+nb_agent_per_proc+"\n");
    	p <- pp.movingExp[0].create_agents(nb_agent_per_proc);
		
    	otherNeighbor <- mpiRank - 1;
    	if (otherNeighbor = -1)
    	{ 
    		otherNeighbor <- 1;
    	}
    
	    write(""+mpiRank+" mon rank est " + ""+mpiRank+"\n") ;
	    
    	write(""+mpiRank+" netSize = "+netSize+"\n");
    
    	write(""+mpiRank+" otherNeighbor = "+otherNeighbor+"\n");
    	
    	loop times: nbLoop {
		
			write("------------------------------------------------------------------------"+mpiRank+" loop : "+current_step+" \n");
    		if(even(mpiRank))
    		{
    			do runMpi_even;
    		}else
    		{
    			do runMpi_odd;
    		}
    		current_step <- current_step + 1;
		}
		
    	do finalize;
    }
    
    action init_sub_simulation
    {
    	create pp.movingExp;
    }
    
    action runModel
    {
		ask (pp.movingExp collect each.simulation)
	    {
			do _step_;
	    }
    }
    
    action runMpi_even
    { 	
    	write(""+mpiRank+" cleanOuterOLZ ****\n");
    	do cleanOuterOLZ;
    	
    	write(""+mpiRank+" updateOuterOLZ_even ****\n");
    	do updateOuterOLZ_even;
    	
    	write(""+mpiRank+" runModel****\n");
    	do runModel;
    	
    	write(""+mpiRank+" updateInnerOLZ_even ****\n");
    	do updateInnerOLZ_even;
    }
    
    action runMpi_odd
    { 	
    	write(""+mpiRank+" cleanOuterOLZ ****\n");
    	do cleanOuterOLZ;
    	
    	write(""+mpiRank+" updateOuterOLZ ****\n");
    	do updateOuterOLZ_odd;
    	
    	write(""+mpiRank+" runModel****\n");
    	do runModel;
    	
    	write(""+mpiRank+" updateInnerOLZ ****\n");
    	do updateInnerOLZ_odd;
    }
    
    action cleanOuterOLZ
   	{
   		write(""+mpiRank+" cleanOuterOLZ\n");
   		int deleted_agents <- pp.movingExp[0].delete_agent_not_in_band(mpiRank);
   		write(""+mpiRank+" nb deleted "+deleted_agents+"\n");
    }
    
    action updateOuterOLZ_even
    { 
	   	list<agent_common> generic_species_list <- pp.movingExp[0].get_agent_list_in_OLZ_outer(); 
		do MPI_SEND mesg: generic_species_list dest: otherNeighbor stag: 50;      	
		
		unknown generic_species_list <- self MPI_RECV [source:: otherNeighbor, rtag:: 50];		   	
	   	list<agent_common> generic_species_list_created <- pp.movingExp[0].create_agents_from_list(generic_species_list);
    }
    
	action updateInnerOLZ_even
    {
	   	list<agent_common> generic_species_list <- pp.movingExp[0].get_agent_list_in_OLZ_inner(); 
		do MPI_SEND mesg: generic_species_list dest: otherNeighbor stag: 50;      	
		
		unknown generic_species_list <- self MPI_RECV [source:: otherNeighbor, rtag:: 50];		   	
	   	list<agent_common> generic_species_list_created <- pp.movingExp[0].create_agents_from_list(generic_species_list);
    }
    
    action updateOuterOLZ_odd
    {    		
		unknown generic_species_list <- self MPI_RECV [source:: otherNeighbor, rtag:: 50];		   	
	   	list<agent_common> generic_species_list_created <- pp.movingExp[0].create_agents_from_list(generic_species_list); 	
	   	
		list<agent_common> generic_species_list <- pp.movingExp[0].get_agent_list_in_OLZ_outer(); 
		do MPI_SEND mesg: generic_species_list dest: otherNeighbor stag: 50;      	
    }
    
	action updateInnerOLZ_odd
    {
		unknown generic_species_list <- self MPI_RECV [source:: otherNeighbor, rtag:: 50];		   	
	   	list<agent_common> generic_species_list_created <- pp.movingExp[0].create_agents_from_list(generic_species_list);
	   	
		list<agent_common> generic_species_list <- pp.movingExp[0].get_agent_list_in_OLZ_inner(); 
		do MPI_SEND mesg: generic_species_list dest: otherNeighbor stag: 50;      	
    }
    
    action finalize // trouver un moyen d'exec le finalize ou bien de détecter la fin de sous-modèle 
    {
    	do MPI_FINALIZE;
    	list<agent_common> generic_species_list <- pp.movingExp[0].get_agent_list_in_area(mpiRank);
    	
    	write(""+mpiRank+"=================================================================FINAL LIST "+generic_species_list+"\n");
    }
}


/* Attention ici le nom de l'expe doit etre le meme que celui donne dans le xml */
experiment SHPControler type: gui { }
