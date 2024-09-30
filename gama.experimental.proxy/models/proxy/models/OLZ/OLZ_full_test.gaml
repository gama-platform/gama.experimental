/**
* Name: OLZ_shape_test
* 
* Model to test out the shape of the OLZ depending of the grid width and height.
* 
* 
* Author: Lucas Grosjean
* Tags: Proxy, HPC, multi simulation, distribution
*/

model OLZ_full_test

global skills:[MPI_SKILL, ProxySkill]
{		
	//geometry shape <- rectangle(rnd(100) + 50, rnd(100) + 50);
	int grid_width <- 2;//rnd(10) + 1;
	int grid_height <- 1;//rnd(10) + 1;
	int size_OLZ <- 5;
	
	int simulation_id <- 0;
	
	init
	{	
		write("shape" + shape);
		
		if(MPI_RANK = 0)
		{
			create moving
			{
				location <- {30,30};
				//targets <- [{30,30}, {70,30}, {70,70}, {30,70}];
				targets <- [{25,50}, {75,50}];
				target <- targets[current_target_index];
			}	
		}
	}
}

species moving skills:[moving, ProxySkill]
{	
	rgb col <- #red;
	list<point> targets;
	point target;
	int current_target_index <- 1;
	
	aspect local
	{	
		draw circle(2) at: location color:#purple;
		/*draw name color: #black;
		draw line(location, target) color: col;
		loop i from: 0 to: length(targets) 
		{
			draw square(2) at: targets[i].location color: #blue;
		}*/ 
	}
	
	reflex move when: target != location
	{
		do goto speed: speed*2 target: target;
		write("I am at : " + location);
	}
	
	reflex target when: target = location
	{
		current_target_index <- current_target_index + 1;
		if(current_target_index >= (length(targets)))
		{
			current_target_index <- 0;
		}
		target <- targets[current_target_index];
	}
	
	list<agent> insert_self_into_list(list<agent> li)
	{
		if(li != nil)
		{
			add self to: li;
		}else
		{
			li <- [self];
		}
		return li;
	}
}

grid OLZ width: grid_width height: grid_height neighbors: 4 skills: [MPI_SKILL, ProxySkill]
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

	reflex agent_inside_OLZ when: index = MPI_RANK
	{
		//write("zoubi");
		let agents_in_OLZ <- moving overlapping OLZ_combined;
		let agents_outside_OLZ <- moving where ( not(each in agents_in_OLZ));

		let no_agents_in_OLZ <- moving overlapping OLZ_combined;
		let no_agents_outside_OLZ <- moving where ( not(each in agents_in_OLZ));
		
		//write("agents_in_OLZ OLZ : " + agents_in_OLZ);
		//write("agents_outside_OLZ OLZ : " + agents_outside_OLZ);
		//write("no_agents_in_OLZ OLZ : " + no_agents_in_OLZ);
		//write("no_agents_outside_OLZ OLZ : " + no_agents_outside_OLZ);
		
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
				//write("agents_in_OLZ_previous_step : " + myself.agents_in_OLZ_previous_step[indexShape]);
				
				if(myself.agents_in_OLZ_previous_step[indexShape] != nil and myself.agents_in_OLZ_previous_step[indexShape] contains self)
				{			
					//write("this agent was in OLZ last step but isnt now : " + self);
					
					if(self overlaps myself.shape)
					{
						//write("agent_leaving_OLZ_to_me " + self);
						if(myself.agent_leaving_OLZ_to_me[indexShape] != nil)
						{						
							myself.agent_leaving_OLZ_to_me[indexShape] <- myself.agent_leaving_OLZ_to_me[indexShape] + self;
						}else
						{
							myself.agent_leaving_OLZ_to_me[indexShape] <- [self];
						}
					}else
					{
						//write("agent_leaving_OLZ_to_neighbor " + self);
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
	
	
	aspect default
	{
		draw self.shape color: rgb(#white,125) border:#black;	
		draw "[" + self.grid_x + "," + self.grid_y +"] : RANK " + rank color: rgb(#red,125) font: font('Default', 10, #bold);
		draw OLZ_combined color: rgb(255,125,125,125);
	}
}

experiment OLZ_proxy_grid_distribution type: distribution until: cycle = 100
{
	int i <- 0;
	reflex when: (cycle > 1)
	{
		ask simulation 
		{
			// We choose a neutral background
			save (snapshot("OLZ_proxy_grid")) to: "../output.log/snapshot/" + MPI_RANK+ "/OLZ" + myself.i + ".png" rewrite: true;
		}
		i <- i + 1;
	}
	output{
		display OLZ_proxy_grid type: 2d
		{
			species OLZ;
			species moving aspect: local;
		}
	}
}