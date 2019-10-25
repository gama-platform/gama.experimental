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
		write Project_Open("E:\\Downloads\\HWC\\HelloWorldCoupling.prj");
//		write Compute_HideComputationWindow();
		write Compute_CurrentPlan();
		write QuitRas();
		
	}
	
}
experiment hecras type:gui{
	output{
	}
}
