/**
* Name: HardSync 
* Model displaying the use HardSync as synchronisation in a distributed ABM
* Author: Lucas Grosjean
* Tags: HPC, proxy, distribution, data synchronisation
*/


model HardSync

global skills: [MPI_SKILL, ProxySkill]
{
	int grid_width <- 2;
	int grid_height <- 1;
	int size_OLZ <- 15;
	int end_cycle <- 30;
	
	int initial_prey_energy <- 200;
	int prey_energy <- initial_prey_energy;
	
	init
	{
		if(MPI_RANK = 0)
		{
			create prey
			{
				location <- {45,70};
				energy <- initial_prey_energy;
				syncmode <- "HardSync";
			}
			
			create predator
			{
				location <- {20,70};
				index_ <- 0; 
			}
			
		}else
		{
			create predator
			{
				location <- {80,70};
				index_ <- 0;
			}
		}
	}

	reflex
	{
		write("------------------------"+cycle+"--------------------------");
	}
	
	reflex when: cycle = end_cycle
	{
		do die;
	}
}

species prey
{
	string syncmode <- "";
	int energy <- initial_prey_energy;
	bool eated <- false;
	
	reflex energy_decay
	{
		energy <- energy - 1;
	}
}

species predator
{
	prey target;	
	int index_;
	
	reflex acquire_target
	{
		if(target = nil)
		{
			if(prey != nil and length(prey) > index)
			{		
				target <- prey[index_];
			}
		}
	}
	
	reflex eat
	{
		if(target != nil)
		{
			if(!target.eated)
			{
				int ind <- index_;
				if(target.energy < 25)
				{
					ask target
					{
						self.eated <- true;
						//do die;
						prey_energy <- 0;
					}
				}
			}else
			{
					prey_energy <- 0;
			}
		}
	}
	
	reflex attack_target
	{
		if(target != nil)
		{
			if(!target.eated)
			{
				int ind <- index_;
				ask target
				{
					energy <- energy - 5;
					prey_energy <- energy;
				}
			}	
		}else
		{
			prey_energy <- 0;
		}
	}
}
/* OLZ species */
grid OLZ width: grid_width height: grid_height neighbors: 4 skills: [MPI_SKILL, ProxySkill]
{ 
	int rank <- grid_x + (grid_y * grid_width);
	
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
		
	init
	{
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

	reflex agent_inside_OLZ when: index = MPI_RANK and MPI_RANK = 0
	{
		let agents_in_OLZ <- prey overlapping OLZ_combined;
		let agents_outside_OLZ <- prey where ( not(each in agents_in_OLZ));

		let no_agents_in_OLZ <- prey overlapping OLZ_combined;
		let no_agents_outside_OLZ <- prey where ( not(each in agents_in_OLZ));
		
		write("agents_in_OLZ OLZ : " + agents_in_OLZ);
		write("agents_outside_OLZ OLZ : " + agents_outside_OLZ);
		write("no_agents_in_OLZ OLZ : " + no_agents_in_OLZ);
		write("no_agents_outside_OLZ OLZ : " + no_agents_outside_OLZ);
		
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
	
	reflex end_step_update when : rank = MPI_RANK and MPI_RANK = 0 and cycle >= 5
	{	
		write("end_step_update cycle " + cycle);
		write("agentsToCopy____(" + new_agents_in_my_OLZ+")");
		do agentsToCopy(new_agents_in_my_OLZ); // important function to send a copy of agent to other processors
		
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
		draw OLZ_combined color: rgb(255,125,125,125);
	}
}

experiment HS_distribution type: distribution until: (cycle = end_cycle)
{
	int i <- 0;
	reflex when: (cycle > 1) and MPI_RANK = 0
	{
		ask simulation { // saving the chart to ../output.log/snapshot/...
			save (snapshot("chart")) to: "../output.log/snapshot/" + MPI_RANK+ "/HardSync" + myself.i + ".png" rewrite: true;
		}
		i <- i + 1;
	}
	
	output
	{	display chart
		{
			species prey;
			species predator;
	    	chart "Prey0 energy over time" type: series title_font: font('SanSerif' , 25.0, #italic) label_font: font('SanSerif', 18 #plain) legend_font: font('SanSerif', 18 #bold)
	    	{
				data "Prey0 energy" value: prey_energy color: #green;
	    	}
		}
	}
}