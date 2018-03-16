/**
* Name: mpi controler
* Author:
* Description: 
* Tags: inheritance
*/

model controler

global skills:[MPI_Network] {
		
    int netSize <- 0;
    int nbLoop <- 5;
    agent exp;


    int timeStep(int node, int start, int end, int overlap )
    {
    	
	   	/* Find neighbors  */
    	int leftNeighbor <- node-1;
    	if (leftNeighbor = -1){ leftNeighbor <- netSize -1;	}
    	int rightNeighbor <- node+ 1;
    	if (rightNeighbor = netSize) { rightNeighbor <- 0; }

		/*  First: kill agents that or not on our environment part : left and right */
    	int p <- evaluate_sub_model(exp,"ask prey where (each.location.x <= start {remove self from: scheduled_preys; do die;}");	
   		p <- evaluate_sub_model(exp,"ask predator where (each.location.x <= start {remove self from: scheduled_predators; do die;}");
    	p <- evaluate_sub_model(exp,"ask prey where (each.location.x > end {remove self from: scheduled_preys; do die;}");	
   		p <- evaluate_sub_model(exp,"ask predator where (each.location.x > end {remove self from: scheduled_predators; do die;}");
	
		/* Get agents in the overlap zone  and send them to neighbors */
		int leftOverlap <- (start + overlap);
		int rightOverlap <- (end - overlap);
		
		if ( node != 0 ) {
			list<point> leftPreyList <- evaluate_sub_model(exp,"(prey where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
			list<point> leftPredatorList <- evaluate_sub_model(exp,"(predator where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //([each.location,each.max_energy,each.max_transfert,each.energy_consum])");
			do MPI_SEND mesg: leftPreyList dest: leftNeighbor stag: 50;
			do MPI_SEND mesg: leftPredatorList dest: leftNeighbor stag: 50;
			write("*** " + node + " sends left overlap");
		}
		
		if ( node != netSize + 1){
			list<point> rightPreyList <- evaluate_sub_model(exp,"(prey where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
			list<point> rightPredatorList <- evaluate_sub_model(exp,"(predator where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //([each.location,each.max_energy,each.max_transfert,each.energy_consum])");
		    do MPI_SEND mesg: rightPreyList dest: rightNeighbor stag: 50;
		    do MPI_SEND mesg: rightPredatorList dest: rightNeighbor stag: 50;
			write("*** " + node + " sends right overlap");		
		}
		
		/* Receive overlap zones from neighbors  */
		if ( node != 0 ){
			list<point> leftOverlapPreys <- self MPI_RECV [rcvsize:: 2, source:: leftNeighbor, rtag:: 50];
	        list<point> leftOverlapPreds <- self MPI_RECV [rcvsize:: 2, source:: leftNeighbor, rtag:: 50];
	        write("*** 0 receive overlap. received = " + leftOverlapPreys + " loop " + leftOverlapPreds);
	        
            int nbCreatePrey <- evaluate_sub_model(exp,"create_preys(" + leftOverlapPreys + ", false)");
        	int nbCreatePred <- evaluate_sub_model(exp,"create_predators(" + leftOverlapPreds + ", false)");
        }
        
        if ( node != (netSize-1) ) {
			list<point> rightOverlapPreys <- self MPI_RECV [rcvsize:: 2, source:: rightNeighbor, rtag:: 50];
	        list<point> rightOverlapPreds <- self MPI_RECV [rcvsize:: 2, source:: rightNeighbor, rtag:: 50];
	        write("*** 0 receive overlap. received = " + rightOverlapPreys + " loop " + rightOverlapPreds);
	        
            int nbCreatePrey <- evaluate_sub_model(exp,"create_preys(" + rightOverlapPreys + ", false)");
       		int nbCreatePred <- evaluate_sub_model(exp,"create_predators(" + rightOverlapPreds + ", false)");
        }
        		        
		/* run the model */
		int st <- step_sub_model(exp);
		p <- evaluate_sub_model(exp,"length(prey)"); 
		write "*** 0 model run, step" + st + " prey " + p;
		
		/* Gather agents that have moved in the other part  and send then on the neighbors */
		if ( node != 0 ) {
			list<point> leftOutcomePreyList <- evaluate_sub_model(exp,"(scheduled_preys where (each.location.x < start)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
			list<point> leftOutcomePredatorList <- evaluate_sub_model(exp,"(scheduled_predators where (each.location.x < start)) collect (each.location)"); //each.max_energy,each.max_transfert,each.energy_consum])");
	    	do MPI_SEND mesg: leftOutcomePreyList dest: leftNeighbor stag: 50;
	    	do MPI_SEND mesg: leftOutcomePreyList dest: leftNeighbor stag: 50;
    	}
    	
    	if ( node != (netSize-1) ) {
			list<point> rightOutcomePreyList <- evaluate_sub_model(exp,"(scheduled_preys where (each.location.x > end)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
			list<point> rightOutcomePredatorList <- evaluate_sub_model(exp,"(scheduled_predators where (each.location.x > end)) collect (each.location)"); //each.max_energy,each.max_transfert,each.energy_consum])");
	    	do MPI_SEND mesg: rightOutcomePreyList dest: rightNeighbor stag: 50;
	    	do MPI_SEND mesg: rightOutcomePreyList dest: rightNeighbor stag: 50;
    	}
		write ("*** " + node + " after send outcomes" );
		
    	/*  Receive incominig agents and create them in my part */
    	if (node != 0 ) {
			list<point> leftIncomePreyList <- self MPI_RECV [rcvsize:: 2, source:: leftNeighbor, rtag:: 50];
	        list<point> leftIncomePredatorList <- self MPI_RECV [rcvsize:: 2, source:: leftNeighbor, rtag:: 50];
	        int nbCreatePrey <- evaluate_sub_model(exp,"create_preys("+leftIncomePreyList+", true)");
	        int nbCreatePred <- evaluate_sub_model(exp,"create_predators("+leftIncomePredatorList+", true)");
			write ("*** " + node + " after incomes" );	
		}	
    	if ( node != (netSize-1) ) {
			list<point> rightIncomePreyList <- self MPI_RECV [rcvsize:: 2, source:: rightNeighbor, rtag:: 50];
	        list<point> rightIncomePredatorList <- self MPI_RECV [rcvsize:: 2, source:: rightNeighbor, rtag:: 50];
	        int nbCreatePrey <- evaluate_sub_model(exp,"create_preys("+rightIncomePreyList+", true)");
	        int nbCreatePred <- evaluate_sub_model(exp,"create_predators("+rightIncomePredatorList+", true)");
			write ("*** " + node + " after incomes" );
		}		
    	return step ;
    }
    		
    init {
		
        do MPI_INIT;
		
		int my_rank <- MPI_RANK();
		write "mon rank est " + my_rank;
		
		/* Attention ici le nom de l'expe doit etre le meme que celui donne dans le gaml */	
		agent exp <- load_sub_model("prey_predatorExp","/home/philippe/recherche/git/gama.experimental/femto.st.gama.mpi/models/parallelPredatorPrey/predatorPrey.gaml"); 
		
		netSize <- MPI_SIZE ();
		int size <- 200 / netSize;
    	int start <- my_rank*size ;		
		int l <-  0;
				
		if (my_rank = 0){	
			
		    loop while: l < nbLoop {
		    	

				int step <- timeStep( my_rank, start, start + size, 10 );	
		    		
				l <- l + 1;
			}
			
		} else {
			    
			loop while: l < nbLoop {
				
				int step <- timeStep( my_rank, start, start + size, 10 );				
				l <- l + 1;
			}
			    
		}
		write("End");
    }
}

species generic_species {
	float size <- 1.0;
	rgb color  ;
	float max_energy;
	float max_transfert;
	float energy_consum;
	float energy <- nil ;
}

species prey parent: generic_species schedules:[]{
	rgb color <- #blue;
	float max_energy <- nil ;
	float max_transfert <- nil ;
	float energy_consum <- nil ;
}
	
species predator parent: generic_species schedules:[]{
	rgb color <- #red ;
	float max_energy <- nil ;
	float max_transfert <- nil ;
	float energy_consum <- nil ;
	list<prey> reachable_preys <- nil ;	
}


/* Attention ici le nom de l'expe doit etre le meme que celui donne dans le xml */
experiment ParallelControler type: gui { }
