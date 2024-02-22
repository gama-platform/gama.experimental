/**
* Name: Migrationreference
* Based on the internal empty template. 
* Author: lucas
* Tags: HPC, proxy, distribution
*/


model Migrationreference

import "../Models_to_distribute/MovingAgent.gaml"

global skills:[ProxySkill, MPI_SKILL, network]
{
	int grid_width <- 2;
	int grid_height <- 1;
	int size_OLZ <- 20;
	int simulation_id <- 0;
	string file_name;
	
	bool debug <- true;
	
	init
	{
		simulation_id <- MPI_RANK;
		
		file_name <- "log"+simulation_id+".txt";

		do clearLogFile();
		do writeLog("My rank " + MPI_RANK);
		
		if(simulation_id = 0) 
		{
			create movingAgent
			{
				location <- {25,25};
				target <- {80,25};
			}
			create followingAgent
			{
				targetAgent <- one_of(movingAgent);
			}
		}
	}
	
	action writeLog(string log)
	{
		//save log format: "text" to: file_name rewrite:false;
	}
	
	action clearLogFile
	{
		//save "" to: file_name rewrite:true;
	}
	
	reflex
	{
		if(length(movingAgent) > 0)
		{		
			do printSyncMode(one_of(movingAgent));
		}
	}
	
	aspect default
	{
		draw shape;
	}
}

grid OLZ width: grid_width height: grid_height neighbors: 8 skills: [ProxySkill, MPI_SKILL]
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
		
		file_name_sub <- "log"+MPI_RANK+".txt";
		write("setting MPI_RANK : " + MPI_RANK);
		write("setting file_name_sub : " + file_name_sub);
	}
	
	// key : rank of the neighbour cell, value : list of agent
	map<int, list<movingAgent>> new_agents_in_my_OLZ <- map<int, list<movingAgent>>([]); 			// agents entering OLZ
	map<int, list<movingAgent>> agents_in_my_OLZ <- map<int, list<movingAgent>>([]);				// agents currently in OLZ
	
	map<int, list<movingAgent>> agents_in_OLZ_previous_step <- map<int, list<movingAgent>>([]); 	// agent that was in the OLZ last step
	
	map<int, list<movingAgent>> agent_leaving_OLZ_to_neighbor <- map<int, list<movingAgent>>([]); 	// agent leaving the OLZ to the neighbor managed area
	map<int, list<movingAgent>> agent_leaving_OLZ_to_me <- map<int, list<movingAgent>>([]); 		// agent leaving the OLZ to my managed area
	
	map<int, list<movingAgent>> agent_to_update <- map<int, list<movingAgent>>([]); 				// agent to be updated in neighbor
	map<int, list<movingAgent>> agent_to_migrate <- map<int, list<movingAgent>>([]); 				// agent to be migrated to neighbor
	
	
	reflex agent_inside_OLZ when: index = simulation_id and simulation_id = 0
	{
		let agents_in_OLZ <- movingAgent overlapping OLZ_combined;
		let agents_outside_OLZ <- movingAgent where ( not(each in agents_in_OLZ));
		
		write("movingAgent: "+movingAgent);
		write("movingAgent le : " + length(movingAgent));
		
		ask movingAgent
		{
			write("I am " + self);
			write("location " + self.location);
			write("ask movingAgent self getClass : ");
			do getClass(self);
		}
		
		write("agents_in_OLZ OLZ : "+agents_in_OLZ);
		write("agents_outside_OLZ OLZ : "+agents_outside_OLZ);
		
		ask agents_in_OLZ
		{
			write("agents_in_OLZ OzzzzzzLZ : "+self);
			write("agents_in_OLZ self getClass : ");
			do getClass(self);
			
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
			write("outside OzzzzLZ : "+self);
			write("myself.OLZ_list : " + myself.OLZ_list);
			
			write("agents_outside_OLZ self getClass : ");
			do getClass(self);
			
			loop OLZ_shape over: myself.OLZ_list
			{
				int indexShape <- myself.neighborhood_shape[OLZ_shape];
				write("indexShape : " + indexShape);
				write("self : " + self);
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
		
		do writeLog2("-----------------" + cycle + "-----------------------");
		do writeLog2("agents_in_my_OLZ " + agents_in_my_OLZ);
		do writeLog2("agents_in_OLZ_previous_step " + agents_in_OLZ_previous_step);
		do writeLog2("new agents : " + new_agents_in_my_OLZ + " cycle " + cycle);


		write("-------------------------------"+cycle + "(simulation_id ::"+simulation_id+")"+ "(rank ::"+rank+")-----------------------------------");
		if(new_agents_in_my_OLZ != nil)
		{
			write("new_agents_in_my_OLZ " + new_agents_in_my_OLZ);
		}
		if(agents_in_my_OLZ != nil)
		{
			write("agents_in_my_OLZ " + agents_in_my_OLZ);
		}
		if(agent_leaving_OLZ_to_me != nil)
		{
			write("agent_leaving_OLZ_to_me " + agent_leaving_OLZ_to_me);
		}
		if(agent_leaving_OLZ_to_neighbor != nil)
		{
			write("agent_leaving_OLZ_to_neighbor " + agent_leaving_OLZ_to_neighbor);
		}
		
	}
	
	reflex end_step_update when : rank = simulation_id and simulation_id = 0
	{
		write("agentsToCopy " + new_agents_in_my_OLZ);
		do agentsToCopy(new_agents_in_my_OLZ);
		
		write("agentsToMigrateXXXXXXXXXXXXX " + agent_leaving_OLZ_to_neighbor);
		do agentsToMigrate(agent_leaving_OLZ_to_neighbor);
		
		agents_in_OLZ_previous_step <- agents_in_my_OLZ;
		new_agents_in_my_OLZ <- nil;
		agents_in_my_OLZ <- nil;
		agent_leaving_OLZ_to_me <- nil;
		agent_leaving_OLZ_to_neighbor <- nil;
	}
	
	action writeLog2(string log)
	{
		save log format: "text" to: file_name_sub rewrite:false;
	}
	
	aspect default
	{
		draw self.shape color: rgb(#white,125) border:#black;	
		draw "[" + self.grid_x + "," + self.grid_y +"] : " + rank color: rgb(#red,125);
		
		if(OLZ[simulation_id] = self)
		{
			draw OLZ_combined color: rgb(200,200,100,125);
		}
		
	}
}

experiment distribution type: distribution until: (cycle = 100)
{
}