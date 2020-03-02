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
		create client;
	}
}

species client skills: [fuzzy_logic] {
	
	float quality;
	float service;
	
	init {		
		// Initialize the Fuzzy logic Inference System (FIS) for the fcl file
		do fl_init_fis(tipper_fcl_file);
		
		// The operator fl_set_variable(name_agent_attribute, name_fis_variable) 
		// associates an agent attribute (through its name) and a variable of the FIS
		// TODO fl_set_variable should takes the variable itself instead of the variable name.
		do fl_set_variable("service", VARIABLE_SERVICE);
		do fl_set_variable("quality", VARIABLE_FOOD);
	}
	
	reflex computer_tip {
		// Recompute the quality of food and of service to simulate a new restaurant
		quality <- rnd(10.0);
		service <- rnd(10.0);
		
		// Evaluate the FIS; evaluation takes into account the new values of the agent attribute
		do fl_evaluate();
		
		// Access to an output value given its name.
		write sample(self.fl_get_output(OUTPUT_TIP));
		write sample(quality);
		write sample(service);		
		write "--------------------------------------------------";
	}
}

experiment FuzzyLogicenginfromFICfile type: gui {}
