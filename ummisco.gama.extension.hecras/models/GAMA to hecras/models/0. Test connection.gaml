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
		file f <- file("../HWC/HWC2.prj");
		write Project_Open(f);
//		write Compute_HideComputationWindow();
		write Generate_RasMap("C:\\git\\gama.experimental\\ummisco.gama.extension.hecras\\models\\GAMA to hecras\\HWC\\HWC2.rasmap","Plan 04", "25JUL2019", 0, 2, 0, 60);
	
		write Compute_CurrentPlan();
		write QuitRas();
		
	}
	
}
experiment hecras type:gui{
	output{
	}
}
