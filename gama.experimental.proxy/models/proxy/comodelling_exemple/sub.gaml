

model continuous_move 

global
{
	int end_cycle <- 10;
	
	init
	{
		create subSpecies number: 4;
	}
}

species subSpecies
{
	int k <- 4;
}

experiment Thematic_experiment type: gui until: (cycle = end_cycle){
	
}
