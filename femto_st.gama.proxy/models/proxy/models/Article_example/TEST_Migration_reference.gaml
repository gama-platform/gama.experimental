/**
* Name: Migrationreference
* Based on the internal empty template. 
* Author: lucas
* Tags: HPC, proxy, distribution
*/


model Migrationreference

import "../Models_to_distribute/MovingAgent.gaml"

global
{
	int grid_width <- 2;
	int grid_height <- 1;
	int size_OLZ <- 20;
	int simulation_id <- 0;
	bool debug <- true;
	
	init
	{
		if(simulation_id = 0)
		{
			create movingAgent
			{
				location <- {25,25};
				target <- {80,25};
			}
			/*create followingAgent
			{
				location <- {12,25};
				targetAgent <- one_of(movingAgent);
			}*/
		}
	}
	
	reflex when: cycle = 40
	{
		//do die;
	}
	
	aspect default
	{
		draw shape;
	}
}

grid OLZ width: grid_width height: grid_height neighbors: 8
{ 
	
	int rank <- grid_x + (grid_y * grid_width);
	
	string file_name_sub;
	
	list<geometry> OLZ_list;
	geometry OLZ_combined;
	map<geometry, int> neighborhood_shape;
	
	// INNER OLZ 
	geometry OLZ_top_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_bottom_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_left_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0};
	geometry OLZ_right_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0};
	
	// CORNER
	geometry OLZ_bottom_left_inner <- OLZ_left_inner inter OLZ_bottom_inner;
	geometry OLZ_bottom_right_inner <- OLZ_right_inner inter OLZ_bottom_inner;
	geometry OLZ_top_left_inner <- OLZ_left_inner inter OLZ_top_inner;
	geometry OLZ_top_right_inner <- OLZ_right_inner inter OLZ_top_inner;
	
	// OUTER OLZ
	geometry OLZ_top_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0}) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_bottom_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height ) translated_by {0,-(size_OLZ / 2),0}) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_left_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0}) translated_by {-(size_OLZ / 2),0,0};
	geometry OLZ_right_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0}) translated_by {(size_OLZ / 2),0,0};
	
	// ALL INNER OLZ
	geometry inner_OLZ <- OLZ_top_inner + OLZ_bottom_inner + OLZ_left_inner + OLZ_right_inner;
	
	// ALL OUTER OLZ
	geometry outer_OLZ <- OLZ_top_outer + OLZ_bottom_outer + OLZ_left_outer + OLZ_right_outer;
	
	string file_name;
		
	init
	{
		write("init my rank : " + rank);
		
		// INNER OLZ
		if(grid_y - 1 >= 0)
		{		
			write(""+grid_x + "," + (grid_y-1));
			neighborhood_shape << OLZ_top_inner :: (grid_x + ((grid_y - 1) * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_top_inner;
			OLZ_list << OLZ_top_inner;
		}
		if(grid_y + 1 < grid_height)
		{		
			neighborhood_shape << OLZ_bottom_inner :: (grid_x + ((grid_y + 1) * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_bottom_inner;
			OLZ_list << OLZ_bottom_inner;
		}
		if(grid_x - 1 >=0)
		{		
			neighborhood_shape << OLZ_left_inner :: ((grid_x - 1)  + (grid_y * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_left_inner;
			OLZ_list << OLZ_left_inner;
		}	
		if(grid_x + 1 < grid_width)
		{		
			neighborhood_shape << OLZ_right_inner :: ((grid_x + 1)  + (grid_y * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_right_inner;
			OLZ_list << OLZ_right_inner;
		}
		
		// CORNER
		if(grid_x + 1 < grid_width and grid_y - 1 >= 0)
		{		
			neighborhood_shape << OLZ_top_right_inner :: ((grid_x + 1)  + ((grid_y - 1)  * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_top_right_inner;
			OLZ_list << OLZ_top_right_inner;
		} 
		if(grid_x - 1 >= 0 and grid_y + 1 < grid_height)
		{		
			neighborhood_shape << OLZ_bottom_left_inner :: ((grid_x - 1)  + ((grid_y + 1)  * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_bottom_left_inner;
			OLZ_list << OLZ_bottom_left_inner;
		}
		if(grid_x + 1 < grid_width and grid_y + 1 < grid_height)
		{		
			neighborhood_shape << OLZ_bottom_right_inner :: ((grid_x + 1)  + ((grid_y + 1)  * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_bottom_right_inner;
			OLZ_list << OLZ_bottom_right_inner;
		}
		if(grid_x - 1 >= 0 and grid_y - 1 >= 0)
		{		
			neighborhood_shape << OLZ_top_left_inner :: ((grid_x - 1)  + ((grid_y - 1)  * grid_width));
			OLZ_combined <- OLZ_combined + OLZ_top_left_inner;
			OLZ_list << OLZ_top_left_inner;
		}
	}
	
	// key : rank of the neighbour cell, value : list of agent
	map<int, list<agent>> new_agents_in_my_OLZ <- map<int, list<agent>>([]); 			// agents entering OLZ
	map<int, list<agent>> agents_in_my_OLZ <- map<int, list<agent>>([]);				// agents currently in OLZ
	
	map<int, list<agent>> agents_in_OLZ_previous_step <- map<int, list<agent>>([]); 	// agent that was in the OLZ last step
	
	map<int, list<agent>> agent_leaving_OLZ_to_neighbor <- map<int, list<agent>>([]); 	// agent leaving the OLZ to the neighbor managed area
	map<int, list<agent>> agent_leaving_OLZ_to_me <- map<int, list<agent>>([]); 		// agent leaving the OLZ to my managed area
	
	map<int, list<agent>> agent_to_update <- map<int, list<agent>>([]); 				// agent to be updated in neighbor
	map<int, list<agent>> agent_to_migrate <- map<int, list<agent>>([]); 				// agent to be migrated to neighbor
	
	
	reflex agent_inside_OLZ when: index = simulation_id and simulation_id = 0
	{
		let agents_in_OLZ <- movingAgent overlapping OLZ_combined;
		let agents_outside_OLZ <- movingAgent where ( not(each in agents_in_OLZ));
		
		write("agents_in_OLZ OLZ : " + agents_in_OLZ);
		write("agents_outside_OLZ OLZ : " + agents_outside_OLZ);
		
		ask agents_in_OLZ
		{
			loop OLZ_shape over: myself.OLZ_list
			{
				int indexShape <- myself.neighborhood_shape[OLZ_shape];
				if(self overlaps(OLZ_shape))
				{
					if(myself.agents_in_my_OLZ[indexShape] != nil)
					{
						myself.agents_in_my_OLZ[indexShape] <- myself.agents_in_my_OLZ[indexShape] + self;
					}else
					{
						myself.agents_in_my_OLZ[indexShape] <- [self];
					}
					
					if(myself.agents_in_OLZ_previous_step[indexShape] = nil)
					{
						if(myself.new_agents_in_my_OLZ[indexShape] != nil)
						{						
							myself.new_agents_in_my_OLZ[indexShape] <- myself.new_agents_in_my_OLZ[indexShape] + self;
						}else
						{
							myself.new_agents_in_my_OLZ[indexShape] <- [self];
						}
						
					}else if(not(myself.agents_in_OLZ_previous_step[indexShape] contains(self)))
					{
						if(myself.new_agents_in_my_OLZ[indexShape] != nil)
						{	
							myself.new_agents_in_my_OLZ[indexShape] <- myself.new_agents_in_my_OLZ[indexShape] + self;
						}else
						{
							myself.new_agents_in_my_OLZ[indexShape] <- [self];
						}
					}
				}
			}
		}
		
		ask agents_outside_OLZ
		{
			loop OLZ_shape over: myself.OLZ_list
			{
				int indexShape <- myself.neighborhood_shape[OLZ_shape];
				write("agents_in_OLZ_previous_step : " + myself.agents_in_OLZ_previous_step[indexShape]);
				
				if(myself.agents_in_OLZ_previous_step[indexShape] != nil and myself.agents_in_OLZ_previous_step[indexShape] contains self)
				{			
					write("this agent was in OLZ last step but isnt now : " + self);
					
					if(self overlaps myself.shape)
					{
						write("agent_leaving_OLZ_to_me " + self);
						if(myself.agent_leaving_OLZ_to_me[indexShape] != nil)
						{						
							myself.agent_leaving_OLZ_to_me[indexShape] <- myself.agent_leaving_OLZ_to_me[indexShape] + self;
						}else
						{
							myself.agent_leaving_OLZ_to_me[indexShape] <- [self];
						}
					}else
					{
						write("agent_leaving_OLZ_to_neighbor " + self);
						if(myself.agent_leaving_OLZ_to_neighbor[indexShape] != nil)
						{						
							myself.agent_leaving_OLZ_to_neighbor[indexShape] <- myself.agent_leaving_OLZ_to_me[indexShape] + self;
						}else
						{
							myself.agent_leaving_OLZ_to_neighbor[indexShape] <- [self];
						}
					}
				}	
			}
		}
	}
	
	reflex debug_print when: debug and rank = simulation_id
	{
		if(new_agents_in_my_OLZ != nil)
		{
			//write("new_agents_in_my_OLZ " + new_agents_in_my_OLZ);
		}
		if(agents_in_my_OLZ != nil)
		{
			//write("agents_in_my_OLZ " + agents_in_my_OLZ);
		}
		if(agent_leaving_OLZ_to_me != nil)
		{
			//write("agent_leaving_OLZ_to_me " + agent_leaving_OLZ_to_me);
		}
		if(agent_leaving_OLZ_to_neighbor != nil)
		{
			//write("agent_leaving_OLZ_to_neighbor " + agent_leaving_OLZ_to_neighbor);
		}
		
	}
	
	reflex end_step_update when : rank = simulation_id and simulation_id = 0
	{	
		agents_in_OLZ_previous_step <- agents_in_my_OLZ;
		new_agents_in_my_OLZ <- nil;
		agents_in_my_OLZ <- nil;
		agent_leaving_OLZ_to_me <- nil;
		agent_leaving_OLZ_to_neighbor <- nil;
	}
	
	aspect default
	{
		draw self.shape color: rgb(#white,125) border:#black;	
		draw "[" + self.grid_x + "," + self.grid_y +"] : " + rank color: rgb(#red,125);
		
		//if(OLZ[simulation_id] = self)
		//{
			draw OLZ_combined color: rgb(200,200,100,125);
		//}
		
	}
}

experiment TEST_distribution type: proxy
{
	init
	{
		//create simulation with: [simulation_id::1];
	}
	output{
		display OLZ_proxy_grid type: 2d
		{
			species OLZ;
			species movingAgent aspect: classic;
			species followingAgent aspect: classic;
		}
	}
}