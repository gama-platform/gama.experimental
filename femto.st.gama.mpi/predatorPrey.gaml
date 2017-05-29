/**
* Name: Predator agents (parent species)
* Author:
* Description: 5th part of the tutorial : Predator Prey
* Tags: inheritance
*/

model prey_predator

global skills:[MPI_Network] {
	int nb_preys_init <- 200;
	int nb_predators_init <- 20;
	float prey_max_energy <- 1.0;
	float prey_max_transfert <- 0.1 ;
	float prey_energy_consum <- 0.05;
	float predator_max_energy <- 1.0;
	float predator_energy_transfert <- 0.5;
	float predator_energy_consum <- 0.02;
	int nb_preys -> {length (prey)};
	int nb_predators -> {length (predator)};

	int my_rank <- 0;
	int my_size <- 0;
	
	init {
		do MPI_INIT; 

		create prey number: nb_preys_init ; 
		create predator number: nb_predators_init ;
		
		write "Init";
		write "nb_preys_init: " + nb_preys_init;
		write "nb_predators_init: " + nb_predators_init;
		write "seed : " + seed;

		my_rank <- MPI_RANK();
		write "mon rank est " + my_rank;

		my_size <- MPI_SIZE ();
		write "ma size est " + my_size;	

 	}
	
	
	reflex aff {
		write "Message at cycle " + cycle ;
	}
	
	reflex stop when: cycle>100 {
		do halt();
	}
}

species generic_species skills: [MPI_Network] {
	float size <- 1.0;
	rgb color  ;
	float max_energy;
	float max_transfert;
	float energy_consum;
	vegetation_cell myCell <- one_of (vegetation_cell) ;
	float energy <- (rnd(1000) / 1000) * max_energy  update: energy - energy_consum max: max_energy ;
	int ur_rank <- 3;
	
	init {
		// do MPI_INIT;
		location <- myCell.location;
		ur_rank <- MPI_RANK();
		write "Rank = " + ur_rank;
	}
		
	reflex basic_move {
		myCell <- one_of (myCell.neighbours) ;
		location <- myCell.location ;
	}
		
	reflex die when: energy <= 0 {
		do die ;
	}
	
	aspect base {
		draw circle(size) color: color ;
	}
}

species prey parent: generic_species {
	rgb color <- #blue;
	float max_energy <- prey_max_energy ;
	float max_transfert <- prey_max_transfert ;
	float energy_consum <- prey_energy_consum ;
		
	reflex eat when: myCell.food > 0 {
		float energy_transfert <- min([max_transfert, myCell.food]) ;
		myCell.food <- myCell.food - energy_transfert ;
		energy <- energy + energy_transfert ;
		write "Eat Rank = " + MPI_RANK();
	}

	reflex send when: ur_rank = 0 {

	       do MPI_SEND dest: (ur_rank+1) mod 2 stag: 50;
	       write("envoi");	 
	}
	
	reflex recv when: ur_rank = 1 {

	       int recu <- self MPI_RECV [source:: (ur_rank+1) mod 2, rtag:: 50];
	       write("recu = " + recu);	 
	}
}
	
species predator parent: generic_species {
	rgb color <- #red ;
	float max_energy <- predator_max_energy ;
	float energy_transfert <- predator_energy_transfert ;
	float energy_consum <- predator_energy_consum ;
	list<prey> reachable_preys update: prey inside (myCell);
		
	reflex eat when: ! empty(reachable_preys) {
		ask one_of (reachable_preys) {
			do die ;
		}
		energy <- energy + energy_transfert ;
	}
}
	
grid vegetation_cell width: 50 height: 50 neighbors: 4 {
	float maxFood <- 1.0 ;
	float foodProd <- (rnd(1000) / 1000) * 0.01 ;
	float food <- (rnd(1000) / 1000) max: maxFood update: food + foodProd ;
	rgb color <- rgb(int(255 * (1 - food)), 255, int(255 * (1 - food))) update: rgb(int(255 * (1 - food)), 255, int(255 *(1 - food))) ;
	list<vegetation_cell> neighbours  <- (self neighbors_at 2); 
}

experiment prey_predatorExp type: gui {
	parameter "Nb Preys: " var: nb_preys_init  min: 0 max: 1000 category: "Prey" ;
	parameter "Prey max energy: " var: prey_max_energy category: "Prey" ;
	parameter "Prey max transfert: " var: prey_max_transfert  category: "Prey" ;
	parameter "Prey energy consumption: " var: prey_energy_consum  category: "Prey" ;
	parameter "Nb predators: " var: nb_predators_init  min: 0 max: 200 category: "Predator" ;
	parameter "Predator max energy: " var: predator_max_energy category: "Predator" ;
	parameter "Predator energy transfert: " var: predator_energy_transfert  category: "Predator" ;
	parameter "Predator energy consumption: " var: predator_energy_consum  category: "Predator" ;
	
	output {
		display main_display {
			grid vegetation_cell lines: #black ;
			species prey aspect: base ;
			species predator aspect: base ;
		}
		monitor "Number of preys" value: nb_preys;
		monitor "Number of predators" value: nb_predators;
	}
}
 

