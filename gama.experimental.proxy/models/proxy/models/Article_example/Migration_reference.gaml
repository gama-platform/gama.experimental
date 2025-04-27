/**
* Name: Migration_reference
* Model displaying the use of different DSP in a distributed ABM
* Author: Lucas Grosjean
* Tags: HPC, proxy, distribution, data synchronisation
*/

model Migration

global skills: [MPI_SKILL]
{
	int grid_width <- 2;
	int grid_height <- 1;
	int size_OLZ <- 15;
	int end_cycle <- 100;
	int nb_neighbor <- 8;
	
	list<agent> agent_to_remove;
	
	init
	{
		if(MPI_RANK = 0)
		{
			create movingAgent
			{
				syncmode <- "GhostMode";
			}
		}
	}
	
	reflex
	{
		ask agent_to_remove
		{
			write("killing " + self);
			do die;
		}
		agent_to_remove <- nil;
	}

	reflex cycle_print
	{
		write("------------------------"+cycle+"--------------------------");
		
		loop tmp over: movingAgent
		{
			write("MOVINGAGENT in my simualtion : " + tmp.name);
		}
	}
	
	reflex when: cycle = end_cycle
	{
		do die;
	}
}

species movingAgent skills:[moving]
{	
	rgb col <- #red;
	point target <- any_location_in(world);
	bool display_true <- false;
	int rank_current_cell <- 0;
	
	//string syncmode;
	
	point pA <- {20,20};
	point pB <- {70,20};
	
	string syncmode;
	
	init
	{
		location <- {20,20};
		target <- pB;
	}
	
	aspect classic
	{		
		draw line(location, target) color: col;
		if(display_true)
		{
			draw circle(1) color: #blue;
		}else
		{
			draw circle(1) color: col;
		}
		
		draw name color: #black;
	}
	
	reflex move when: target != location
	{
		do goto speed: speed target:target;
	}
	
	reflex target when: target = location
	{
		if(target = pB)
		{
			target <- pA;
		}else
		{
			target <- pB;
		}
	}
}

/* OLZ species */
grid OLZ width: grid_width height: grid_height neighbors: nb_neighbor skills: [MPI_SKILL, ProxySkill]
{ 
	int rank <- grid_x + (grid_y * grid_width);
	
	list<geometry> OLZ_list;
	geometry OLZ_combined_inner;
	geometry OLZ_combined_outer;
	map<geometry, int> neighborhood_shape;
	
	// INNER OLZ 
	geometry OLZ_top_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_bottom_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_left_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0};
	geometry OLZ_right_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0};
	
	// INNER CORNER
	geometry OLZ_bottom_left_inner <- OLZ_left_inner inter OLZ_bottom_inner;
	geometry OLZ_bottom_right_inner <- OLZ_right_inner inter OLZ_bottom_inner;
	geometry OLZ_top_left_inner <- OLZ_left_inner inter OLZ_top_inner;
	geometry OLZ_top_right_inner <- OLZ_right_inner inter OLZ_top_inner;
	
	// OUTER OLZ
	geometry OLZ_top_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0}) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_bottom_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height ) translated_by {0,-(size_OLZ / 2),0}) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_left_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0}) translated_by {-(size_OLZ / 2),0,0};
	geometry OLZ_right_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0}) translated_by {(size_OLZ / 2),0,0};
	
	// OUTER CORNER
	geometry OLZ_bottom_left_outer <- OLZ_left_outer inter OLZ_bottom_outer;
	geometry OLZ_bottom_right_outer<- OLZ_right_outer inter OLZ_bottom_outer;
	geometry OLZ_top_left_outer <- OLZ_left_outer inter OLZ_top_outer;
	geometry OLZ_top_right_outer<- OLZ_right_outer inter OLZ_top_outer;
	
	// ALL INNER OLZ
	geometry inner_OLZ <- OLZ_top_inner + OLZ_bottom_inner + OLZ_left_inner + OLZ_right_inner;
	
	// ALL OUTER OLZ
	geometry outer_OLZ <- OLZ_top_outer + OLZ_bottom_outer + OLZ_left_outer + OLZ_right_outer;
		
	init
	{
		// INNER OLZ
		if(grid_y - 1 >= 0)
		{		
			write(""+grid_x + "," + (grid_y-1));
			neighborhood_shape << OLZ_top_inner :: (grid_x + ((grid_y - 1) * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_top_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_top_outer;
			OLZ_list << OLZ_top_inner;
		}
		if(grid_y + 1 < grid_height)
		{		
			neighborhood_shape << OLZ_bottom_inner :: (grid_x + ((grid_y + 1) * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_bottom_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_bottom_outer;
			OLZ_list << OLZ_bottom_inner;
		}
		if(grid_x - 1 >=0)
		{		
			neighborhood_shape << OLZ_left_inner :: ((grid_x - 1)  + (grid_y * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_left_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_left_outer;
			OLZ_list << OLZ_left_inner;
		}	
		if(grid_x + 1 < grid_width)
		{		
			neighborhood_shape << OLZ_right_inner :: ((grid_x + 1)  + (grid_y * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_right_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_right_outer;
			OLZ_list << OLZ_right_inner;
		}
		
		// CORNER
		if(grid_x + 1 < grid_width and grid_y - 1 >= 0)
		{		
			neighborhood_shape << OLZ_top_right_inner :: ((grid_x + 1)  + ((grid_y - 1)  * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_top_right_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_top_right_outer;
			OLZ_list << OLZ_top_right_inner;
		} 
		if(grid_x - 1 >= 0 and grid_y + 1 < grid_height)
		{		
			neighborhood_shape << OLZ_bottom_left_inner :: ((grid_x - 1)  + ((grid_y + 1)  * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_bottom_left_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_bottom_left_outer;
			OLZ_list << OLZ_bottom_left_inner;
		}
		if(grid_x + 1 < grid_width and grid_y + 1 < grid_height)
		{		
			neighborhood_shape << OLZ_bottom_right_inner :: ((grid_x + 1)  + ((grid_y + 1)  * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_bottom_right_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_bottom_right_outer;
			OLZ_list << OLZ_bottom_right_inner;
		}
		if(grid_x - 1 >= 0 and grid_y - 1 >= 0)
		{		
			neighborhood_shape << OLZ_top_left_inner :: ((grid_x - 1)  + ((grid_y - 1)  * grid_width));
			OLZ_combined_inner <- OLZ_combined_inner + OLZ_top_left_inner;
			OLZ_combined_outer <- OLZ_combined_outer + OLZ_top_left_outer;
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

	reflex agent_inside_OLZ when: index = MPI_RANK
	{
		let agents_in_cell_and_outer <- movingAgent overlapping (self.shape+OLZ_combined_inner+OLZ_combined_outer);
		let agents_in_inner_OLZ <- movingAgent overlapping OLZ_combined_inner;
		
		let agents_outside_OLZ <- movingAgent where ( not(each in agents_in_inner_OLZ));
		let agents_outside_me_and_OLZ <- movingAgent where ( not(each in agents_in_cell_and_outer));
		
		write("agents_in_OLZ OLZ : " + agents_in_inner_OLZ);
		write("agents_outside_OLZ OLZ : " + agents_outside_OLZ);
		write("agents_in_cell_and_outer OLZ : " + agents_in_cell_and_outer);
		write("agents_outside_me_and_OLZ OLZ : " + agents_outside_me_and_OLZ);
		
		agent_to_remove <- agents_outside_me_and_OLZ;
		
		loop tmp over: agents_in_inner_OLZ
		{
			write("agent unique if " + tmp.getUUID());
		}
		
		ask agents_in_inner_OLZ
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
						write("agent_leaving_OLZ_to_neighbor " + self); // TODO FIX 
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
	
	reflex end_step_update when : rank = MPI_RANK
	{	
		if(length(new_agents_in_my_OLZ) > 0)
		{			
			write("new_agents_in_my_OLZ before send : " + new_agents_in_my_OLZ);
		}
		do agentsToCopy(new_agents_in_my_OLZ); // important function to send a copy of agent to other processors
		
		if(length(agent_leaving_OLZ_to_neighbor) > 0)
		{			
			write("agent_leaving_OLZ_to_neighbor_neig before send : " + agent_leaving_OLZ_to_neighbor);
		}
		do agentsToMigrate(agent_leaving_OLZ_to_neighbor); // important function to migrate of agent to other processors
		
		agents_in_OLZ_previous_step <- agents_in_my_OLZ;
		new_agents_in_my_OLZ <- nil;
		agents_in_my_OLZ <- nil;
		agent_leaving_OLZ_to_me <- nil;
		agent_leaving_OLZ_to_neighbor <- nil;
	}
	
	
	aspect default
	{
		draw self.shape color: rgb(#white,125) border:#black;	
		draw "[" + self.grid_x + "," + self.grid_y +"] : RANK " + rank color: rgb(#red,125) font: font('Default', 10, #bold);
		
		if(rank = MPI_RANK)
		{		
			draw OLZ_combined_inner color: rgb(#red,0.4);
			draw OLZ_combined_outer color: rgb(#blue,0.4);
		}
		
	}
}

experiment Migration_reference type: distribution
{
	int i <- 0;
	reflex
	{
		ask simulation 
		{	
				// We choose a neutral background
				save (snapshot("chart")) to: "../output.log/snapshot/" + MPI_RANK+ "/MIGRATION_" + myself.i + ".png" rewrite: true;
		}
		i <- i + 1;
	}
	
	output
	{	display chart
		{
			species movingAgent aspect: classic;
			species OLZ;
		}
	}
}