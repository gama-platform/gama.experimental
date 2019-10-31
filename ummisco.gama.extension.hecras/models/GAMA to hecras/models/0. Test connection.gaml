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
		write Compute_CurrentPlan();
		write QuitRas();
		
	}
	
}
experiment hecras type:gui{
	output{
	}
}
