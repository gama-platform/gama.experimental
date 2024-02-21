/***
* Name: Fuzzy Logic engine from FICfile
* Author: ben
* Description: A first example of agent with a decision-making process using fuzzy logic
* Tags: fuzzy logic, reasoning, fcl
***/

model FuzzyLogicenginfromFICfile

global {
	// The fcl file that contains all the variables, outputs and rules
	fcl_file tipper_fcl_file <- fcl_file("../includes/tipper.fcl");
	
	// VARIABLES AND OUTPUTS FROM THE tipper.fcl file
	string VARIABLE_SERVICE <- "service";
	string VARIABLE_FOOD <- "food";
	string OUTPUT_TIP <- "tip";
	
	init {
		create client ; 
	}
}

species client skills: [fuzzy_logic] {
	
	float quality_food;
	float service;
	
	float tip;
	
	init {		
		// Initialize the Fuzzy logic Inference System (FIS) for the fcl file
		fl_init_fis from_file: tipper_fcl_file;
		
		// The statement fl_set_variable binds agent's attributes with either FIS variables or outputs
		// Bind attributes with FIS variables
		fl_bind quality_food with_fis_variable: VARIABLE_FOOD;
		fl_bind service with_fis_variable: VARIABLE_SERVICE;
		
		// Bind attribute with FIS outputs
		fl_bind tip with_fis_output: OUTPUT_TIP ;
		
	}
	
	reflex computer_tip {
		// Recompute the quality of food and of service to simulate a new restaurant
		quality_food <- rnd(10.0);
		service <- rnd(10.0);
		
		// Evaluate the FIS; evaluation takes into account the new values of the agent attribute
		do fl_evaluate();
		
		// Access to an output value given its name.
		write sample(self.fl_get_output(OUTPUT_TIP));
		write sample(tip);
		write sample(quality_food);
		write sample(service);		
		write "--------------------------------------------------";
	}
}

experiment FuzzyLogicenginfromFICfile type: gui { }
