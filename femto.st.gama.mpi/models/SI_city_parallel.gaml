model SI_city

global skills:[MPI_Network] {

    int dest <- 0;

    int nb_people <- 50;
    float agent_speed <- 5.0 #km/#h;
    float infection_distance <- 2.0 #m;
    float proba_infection <- 0.05;
    int nb_infected_init <- 5;
    float step <- 1 #minutes;
    geometry shape<-rectangle(1020 #m, 500 #m);

    /* For MPI */
    int my_rank <- 0;
    int netSize <- 0;
    
    buffer phantom;
    buffer remote;

    init{
	     /* For MPI */
		do MPI_INIT;
		my_rank <- MPI_RANK();
		write "mon rank est " + my_rank;
	
		if (my_rank = 0){
		  dest <- 1;
		  create buffer number:1 {
		  	phantom <- self;
		  	shape <- rectangle(10,500);
		  	location <- point(495,250);
		  }
		   create buffer number:1 {
		  	remote <- self;
		  	shape <- rectangle(10,500);
		  	location <- point(505,250);
		  }	
	
		} else {
		
		  dest <- 1;
		  
		 create buffer number:1 {
		  	phantom <- self;
		  	shape <- rectangle(10,500);
		  	location <- point(525,250);
		  }
		   create buffer number:1 {
		  	remote <- self;
		  	shape <- rectangle(10,500);
		  	location <- point(515,250);
		  }	
		}
	
		create people number:nb_people {
		       name <- name + "_" + my_rank;
	    }
		
	    ask nb_infected_init among people {
	           is_infected <- true;
	    }
    }

    action reset_remote
    {
		ask people overlapping remote {
			do die;
		}
    }
    reflex myStep {

	    list<people> phantomPeople <- people overlapping phantom;
	
		do reset_remote;
		
	    /* lets do it bully */
	    int len <- length(phantomPeople);
	    list<int> sizeMsg <- [len];
	    do MPI_SEND mesg: sizeMsg dest: dest stag: 50;
	
	    list<int> envoi <- [0];
	
	    ask phantomPeople
		{
		    add int(location.x) to: envoi;
		    add int(location.y) to: envoi;
		    add int(is_infected) to: envoi;
		}
	write "send";
	    do MPI_SEND mesg: envoi dest: dest stag: 50;
	
	    sizeMsg <- self MPI_RECV [rcvsize:: 1, source:: dest, rtag:: 50];
	    len <- sizeMsg at 0;
	
	    list<int> recu <- self MPI_RECV [rcvsize:: (len*3), source:: dest, rtag:: 50];
	write "recv";
	    int i <- 0;
		create people number:len {
		       location <- point(recu at i*3, recu at (i*3)+1);
		       is_infected <- bool(recu at (i*3)+2)	;
		} 	
	} 
}	

species buffer;

species people skills:[moving]{     
    float speed <- agent_speed;
    bool is_infected <- false;

    /* reflex move{
        do wander;
    } */
    reflex infect when: is_infected{
    	
        ask people at_distance infection_distance {
            if flip(proba_infection) {
                is_infected <- true;
            }
        }
    }

    aspect circle{
        draw circle(5) color:is_infected ? #red : #green;
    }
}

experiment main_experiment type:gui{
    parameter "Infection distance" var: infection_distance;
    parameter "Proba infection" var: proba_infection min: 0.0 max: 1.0;
    parameter "Nb people infected at init" var: nb_infected_init ;
    output {
        display map {
            species people aspect:circle;           
        }
    }
}
