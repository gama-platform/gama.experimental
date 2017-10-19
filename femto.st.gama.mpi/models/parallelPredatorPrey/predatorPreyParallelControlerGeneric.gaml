/**
* Name: mpi controler
* Author:
* Description: 
* Tags: inheritance
*/

model controler

global skills:[MPI_Network] {
		
    int my_rank <- 0;
    int netSize <- 0;
	
    int nbLoop <- 5;
    
    agent exp;
    
    int timeStep (int proc, int start, int end, int overlap )
    {
    	
	   	/* Find neighbors  */
    	int leftNeighbor <- proc -1;
    	if (leftNeighbor = -1){ leftNeighbor <- netSize -1;	}
    	int rightNeighbor <- proc + 1;
    	if (rightNeighbor = netSize) { rightNeighbor <- 0; }

		/*  First: kill agents that or not on our environment part : left and right */
    	int p <- evaluate_sub_model(exp,"ask prey where (each.location.x <= start {remove self from: scheduled_preys; do die;}");	
   		p <- evaluate_sub_model(exp,"ask predator where (each.location.x <= start {remove self from: scheduled_predators; do die;}");
    	p <- evaluate_sub_model(exp,"ask prey where (each.location.x > end {remove self from: scheduled_preys; do die;}");	
   		p <- evaluate_sub_model(exp,"ask predator where (each.location.x > end {remove self from: scheduled_predators; do die;}");
	
		/* Get agents in the overlap zone  and send them to neighbors */
		int leftOverlap <- (start + overlap);
		int rightOverlap <- (end - overlap);
		
		list<point> leftPreyList <- evaluate_sub_model(exp,"(prey where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
		list<point> leftPredatorList <- evaluate_sub_model(exp,"(predator where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //([each.location,each.max_energy,each.max_transfert,each.energy_consum])");
	    do MPI_SEND mesg: leftPreyList dest: leftNeighbor stag: 50;
	    do MPI_SEND mesg: leftPredatorList dest: leftNeighbor stag: 50;
		write("*** " + proc + " sends left overlap");
		
		list<point> rightPreyList <- evaluate_sub_model(exp,"(prey where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
		list<point> rightPredatorList <- evaluate_sub_model(exp,"(predator where (each.location.x > start and each.location.x < leftOverlap)) collect (each.location)"); //([each.location,each.max_energy,each.max_transfert,each.energy_consum])");
	    do MPI_SEND mesg: rightPreyList dest: rightNeighbor stag: 50;
	    do MPI_SEND mesg: rightPredatorList dest: rightNeighbor stag: 50;
		write("*** " + proc + " sends right overlap");		
		
		/* Receive overlap zones from neighbors  */
		if ( proc != 0 ){
			list<point> leftOverlapPreys <- self MPI_RECV [rcvsize:: 2, source:: leftNeighbor, rtag:: 50];
	        list<point> leftOverlapPreds <- self MPI_RECV [rcvsize:: 2, source:: leftNeighbor, rtag:: 50];
	        write("*** 0 receive overlap. received = " + leftOverlapPreys + " loop " + leftOverlapPreds);
	        
            int nbCreatePrey <- evaluate_sub_model(exp,"create_preys(" + leftOverlapPreys + ", false)");
        	int nbCreatePred <- evaluate_sub_model(exp,"create_predators(" + leftOverlapPreds + ", false)");
        }
        
        if ( proc != (netSize-1) ) {
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
		list<point> leftOutcomePreyList <- evaluate_sub_model(exp,"(scheduled_preys where (each.location.x > 100)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
		list<point> leftOutcomePredatorList <- evaluate_sub_model(exp,"(scheduled_predators where (each.location.x > 100)) collect (each.location)"); //each.max_energy,each.max_transfert,each.energy_consum])");
    	do MPI_SEND mesg: leftOutcomePreyList dest: leftNeighbor stag: 50;
    	do MPI_SEND mesg: leftOutcomePreyList dest: leftNeighbor stag: 50;
		write ("*** " + proc + " after send outcomes" );
		
    	/*  Receive incominig agents and create them in my part */
		list<point> incomePreyList <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
        list<point> incomePredatorList <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
        int nbCreatePrey <- evaluate_sub_model(exp,"create_preys("+incomePreyList+", true)");
        int nbCreatePred <- evaluate_sub_model(exp,"create_predators("+incomePredatorList+", true)");
		write ("*** 0, after incomes, step " + l );	
		
    	return step ;
    }
    		
    init {
		
        do MPI_INIT;
		
		my_rank <- MPI_RANK();
		write "mon rank est " + my_rank;
		
		/* Attention ici le nom de l'expe doit etre le meme que celui donne dans le gaml */	
		agent exp <- load_sub_model("prey_predatorExp","/home/philippe/recherche/git/gama.experimental/femto.st.gama.mpi/models/parallelPredatorPrey/predatorPrey.gaml"); 
		
		netSize <- MPI_SIZE ();
		
		if (my_rank = 0){	
			
			int l <-  0;
			int destinataire <- 1;	
			int emet <- 1;
	   		
		    loop while: l < nbLoop {
		    	
		    	int start <- my_rank*size ;
				int step <- timeStep( my_rank, start, start + size, 10 );	
		    		
				l <- l + 1;
			}
			
		} else {
			
			int l <-  0;
			int destinataire <- 0;
			int emet <- 0;
			    
			loop while: l < nbLoop {
				
				/* First: kill agents that or not on our environment part */
				int p <- evaluate_sub_model(exp,"ask prey where(each.location.x <= 100){remove self from: scheduled_preys; do die;}");
			    p <- evaluate_sub_model(exp,"ask predator where (each.location.x <= 100){remove self from: scheduled_predators; do die;}");

				/* Get agents in the overlap zone  and send them to neighbor */
				list<point> preyList <- evaluate_sub_model(exp,"(prey where (each.location.x > 100 and each.location.x < 110)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
				list<point> predatorList <- evaluate_sub_model(exp,"(predator where (each.location.x > 100 and each.location.x < 110)) collect (each.location)"); //each.max_energy,each.max_transfert,each.energy_consum])");
		    	do MPI_SEND mesg: preyList dest: destinataire stag: 50;
		    	do MPI_SEND mesg: predatorList dest: destinataire stag: 50;
				write("*** 1 sends overlap, loop " + l);
		    		
		        /* Receive overlap zone from neighbor  */
		        list<point> lpreys <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
		        list<point> lpreds <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
		        write("*** 1 receive overlap, received = " + lpreys + " loop " + lpreds);
		        
		        int nbCreatePrey <- evaluate_sub_model(exp,"create_preys("+lpreys+", false)");
		        int nbCreatePred <- evaluate_sub_model(exp,"create_predators("+lpreds+", false)");

				/* run the model */
				int st <- step_sub_model(exp);
				int p <- evaluate_sub_model(exp,"length(prey)"); 
				write "sim 1 step" + st + " prey " + p;
				
				/* Gather agents that have moved in the other zone and send then on the other side */
				list<point> outcomePreyList <- evaluate_sub_model(exp,"(scheduled_preys where (each.location.x <= 100)) collect (each.location)"); //,each.max_energy,each.max_transfert,each.energy_consum])");
				list<point> outcomePredatorList <- evaluate_sub_model(exp,"(scheduled_predators where (each.location.x <= 100)) collect (each.location)"); //each.max_energy,each.max_transfert,each.energy_consum])");
		    	do MPI_SEND mesg: outcomePreyList dest: destinataire stag: 50;
		    	do MPI_SEND mesg: outcomePredatorList dest: destinataire stag: 50;
				write ("*** 1, after send outcomes, step " + l );
				
				/*  Receive incominig agents and create them in my part */
				list<point> incomePreyList <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
		        list<point> incomePredatorList <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
		        int nbCreatePrey <- evaluate_sub_model(exp,"create_preys("+incomePreyList+", true)");
		        int nbCreatePred <- evaluate_sub_model(exp,"create_predators("+incomePredatorList+", true)");
				
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
