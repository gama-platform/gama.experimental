/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Testconnection

global skills:[hecrasSkill]{
	
	init{ 
		do load_hecras();
		write Dfs0File_Read_Data("C:\\git\\HydraulicTools\\RESULT2015.res11", "KIM_SON");
		
	}
	
}
experiment hecras type:gui{
	output{
	}
}
