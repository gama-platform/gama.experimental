/**
* Name: Shapefile MPI
* Author:  Lucas Grosjean
* Description: Shapefile in GAMA with agent MPI through MPI.
*/
model SHP

global 
{
	file shape_file_buildings <- shape_file("/includes/buildings_simple.shp");
	geometry shape <- envelope(shape_file_buildings);
	
	init 
	{
		create building from: shape_file_buildings;
		//create agent_common number: 10;
	}
}

species agent_common skills:[moving]
{
	int index;
	building workplace <- one_of(building);
	building home <- one_of(building);
    point the_target <- nil ;
	
	init 
	{
		self.index <- rnd(100);
		self.location <- any_point_in(home);
	}
	
	reflex im_home when: not (self overlaps home) and not (self overlaps workplace)
	{
		write("im moving!!!!!!!!!!!!!!!!!!!!!! : "+self.name+"\n");
	}
	
	reflex im_home when: (self overlaps home)
	{
		write("im home : "+self.name+"\n");
	}
	reflex im_working when: (self overlaps workplace)
	{
		write("im working : "+self.name+"\n");
	}
	
	reflex go_home when: ( (the_target = nil) and (self overlaps workplace) )
	{
		the_target <- any_point_in(home);
	}
	
	reflex go_work when: ( (the_target = nil) and (self overlaps home) )
	{
		the_target <- any_point_in(workplace);
	}
	
	reflex move when: the_target != nil {
	    do goto target: the_target;
	    if the_target = location {
	        the_target <- nil ;
	    }
    }
    
    aspect default {
		draw circle(2.0) color: #red;
	}
}

species building 
{
	
	geometry OLZ_outer <- 10 around self.shape;
	geometry OLZ_inner <- 5 around self.shape;
	
	aspect default {
		draw self color: #yellow;
	}
	
}

experiment movingExp type: gui 
{
	output {
		display city_display type: opengl {
			species building;
			species agent_common;
		}
	}
	
	action create_agents(int nb)
	{
		create agent_common number:nb;
	}
	
	list<agent_common> get_agent_list_in_area(int rank)
	{
		list<agent_common> agent_list_inside <- agent_common overlapping one_of(building);
		list<agent_common> agent_list_outside <- agent_common where (not(agent_list_inside contains each));
		
		if(even(rank))
		{
			write("agent_list_inside = "+agent_list_inside+"\n");
			return agent_list_inside;
		
		}else
		{
			write("agent_list_outside = "+agent_list_outside+"\n");
			return agent_list_outside;
		}
	}
	
	list<agent_common> get_agent_list_in_OLZ_inner
	{
		list<agent_common> agent_list_inside_OLZ <- agent_common overlapping one_of(5 around building);
		return agent_list_inside_OLZ;
	}
	
	list<agent_common> get_agent_list_in_OLZ_outer
	{
		list<agent_common> agent_list_inside_OLZ_inner <- agent_common overlapping one_of (5 around building);
		list<agent_common> agent_list_inside_OLZ <- agent_common overlapping one_of(10 around building);
		
		agent_list_inside_OLZ <- agent_list_inside_OLZ - agent_list_inside_OLZ_inner;
		return agent_list_inside_OLZ;
	}
	
	int delete_agent_not_in_band(int rank)
	{
		
		list<agent_common> agent_list_inside <- agent_common overlapping one_of(building);
		list<agent_common> agent_list_olz_inner <- agent_common overlapping one_of(5 around building);
		agent_list_inside <- agent_list_inside + agent_list_olz_inner;
		
		list<agent_common> agent_list_outside <- agent_common where (not(agent_list_inside contains each));
		
		int dead_agent <- 0;
		if(even(rank))
		{
			write("agent_list_outside die = "+agent_list_outside+"\n");
			ask agent_list_outside
			{
				dead_agent <- dead_agent + 1;
				do die;
			}
		
		}else
		{
			write("agent_list_inside die = "+agent_list_inside+"\n");
			ask agent_list_inside
			{
				dead_agent <- dead_agent + 1;
				do die;
			}
		}
		
		return dead_agent;
	}
	
	list<agent_common> create_agents_from_list(unknown specie_attributes_list)
	{
		list<agent_common> list_generic;
		loop tmp over: specie_attributes_list
		{
			map<string,unknown> map_unk <- map(tmp);
			add create_agent(map_unk) to: list_generic;
		}
		
		return list_generic;
	}
	
	agent_common create_agent(map<string,unknown> specie_attributes)
	{
		create agent_common returns:created
		{
			self.location <- specie_attributes at "location";
			self.index <- specie_attributes at "index";
		}
		
		return created[0];
	}
}

