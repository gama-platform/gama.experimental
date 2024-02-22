/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Testconnection

global skills:[MikeSkill]{
	
	init{ 
		do load_Mike();
		write Dfs0File_Read_Data("C:\\git\\HydraulicTools\\RESULT2015.res11", "KIM_SON");
		
	}
	
}
experiment mike type:gui{
	output{
	}
}
