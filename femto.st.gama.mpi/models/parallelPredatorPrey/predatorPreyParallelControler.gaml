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
    int destinataire <- 0;
    		
    init {
		
        do MPI_INIT;
		
	my_rank <- MPI_RANK();
	write "mon rank est " + my_rank;
	
	/* Attention ici le nom de l'expe doit etre le meme que celui donne dans le gaml */	
	agent exp <- load_sub_model("prey_predatorExp","/home/philippe/devel/gama/mpiModels/controler/predatorPrey.gaml"); 
	
	int l <-  0;
	list<string> myMsg <- {0,0};
	loop while: l < nbLoop {
		
	    if (my_rank = 0){
			
		netSize <- MPI_SIZE ();

	        // Init: kill agents that or not on our part
	    	int p <- evaluate_sub_model(exp,"ask prey where (each.location.x > 100){do die;}");	
	    	p <- evaluate_sub_model(exp,"ask predator where (each.location.x > 100){do die;}");

		list<prey> preyList <- evaluate_sub_model(exp,"prey where (each.location.x > 90 and each.location.x < 100)");
		list<predator> predatorList <- evaluate_sub_model(exp,"predator where (each.location.x > 90 and each.location.x < 100)");
		
		write("*** 0  = " +  preyList + " loop " + l);
		/* parameter values  */
		myMsg <- [l,l+1];
		
		/* Send data to APSF */
		destinataire <- 1;
		loop while: destinataire < netSize {
				
	    	    do MPI_SEND mesg: myMsg dest: destinataire stag: 50;
		    destinataire <- destinataire + 1;	
		}
		write("master envoie, loop " + l);
				
		/* run the model */
		int st <- step_sub_model(exp);
		int p <- evaluate_sub_model(exp,"length(prey)"); 
		write "sim 1 step" + st + " prey " + p;
				
		/* receive results */
		int emet <- 1;
		loop while: emet < netSize {
				
		    list<int> recu <- self MPI_RECV [rcvsize:: 2, source:: emet, rtag:: 50];
		    write("master recu = " + recu + " loop " + l);
		    emet <- emet + 1;	
		}
		
	    } else {
	    		
	        /* Receive the parameters */
	        list<int> recu <- self MPI_RECV [rcvsize:: 2, source:: 0, rtag:: 50];
	        write("slave recu = " + recu + " loop " + l);
	    		
	        /* run the model  */
	        int st <- step_sub_model(exp);
	        int p <- evaluate_sub_model(exp,"length(prey)");
		write "sim 2 step" + st + " prey " + p;		
				
		myMsg <- [recu at 1, recu at 0];
		/* send  the results */
				
		do MPI_SEND mesg: myMsg dest: 0 stag: 50;
		write("slave envoye, loop " + l);
	    }
	    l <- l + 1;
	    	
	}
    }
}

/* Attention ici le nom de l'expe doit etre le meme que celui donne dans le xml */
experiment Controler type: gui {
}
