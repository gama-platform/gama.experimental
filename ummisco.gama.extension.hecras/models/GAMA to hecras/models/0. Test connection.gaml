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
//		write Generate_RasMap("C:\\git\\gama.experimental\\ummisco.gama.extension.hecras\\models\\GAMA to hecras\\HWC\\HWC2.rasmap","Plan 04", "25JUL2019", 0, 2, 0, 60);
//		file f <- file("../HWC/HWC2.prj");
		//  write Generate_RasMap("D:/Downloads/Taha - Transfert/Phuc Xa Simulation/PhucXaSimulation.rasmap","Plan 01", "Phuc Xa DEM + 2 Bathymetries","15AUG2019", 0, 2, 0, 60);
		write Generate_RasMap("C:/Users/benoit/Documents/Phuc Xa Simulation/PhucXaSimulation.rasmap","Plan 01", "Phuc Xa DEM + 2 Bathymetries","15AUG2019", 0, 0, 0, 4);
//		write Generate_RasMap("C:/Users/benoit/Documents/Hello World Coupling/HelloWorldCoupling.rasmap","Plan 03", "Hello DEM 200x100","25JUL2019", 0, 24, 0, 59);

		file f <- file("C:/Users/benoit/Documents/Phuc Xa Simulation/PhucXaSimulation.prj");
		
//		file f <- file("C:/Users/benoit/Documents/Hello World Coupling/HelloWorldCoupling.prj");
		
		write Project_Open(f);
//		write Compute_HideComputationWindow();
	
		write Compute_CurrentPlan();
		write QuitRas();
		
	}
	
}
experiment hecras type:gui{
	output{
	}
}
