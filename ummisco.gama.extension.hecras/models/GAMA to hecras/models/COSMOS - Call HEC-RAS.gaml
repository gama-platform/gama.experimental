/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Testconnection

global skills:[hecrasSkill]{
	
	init{ 
		write load_hecras();

//		write Generate_RasMap("C:/Users/benoit/Documents/Hello World Coupling/HelloWorldCoupling.rasmap","Plan 03", "Hello DEM 200x100","25JUL2019", 0, 24, 0, 59);		
		write Generate_RasMap("C:/Users/benoit/Documents/Phuc Xa Simulation/PhucXaSimulation.rasmap","Plan 01", "Phuc Xa DEM + 2 Bathymetries","15AUG2019", 0, 0, 0, 4);
		
//		file f <- file("C:/Users/benoit/Documents/Hello World Coupling/HelloWorldCoupling.prj");
		file f <- file("C:/Users/benoit/Documents/Phuc Xa Simulation/PhucXaSimulation.prj");
		
		do Project_Open(f);	
		do Compute_CurrentPlan();
		do QuitRas();	
	}
	
}

experiment hecras type:gui{
	output{
	}
}
