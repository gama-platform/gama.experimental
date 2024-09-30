/**
* Name: Migrationreference
* Based on the internal empty template. 
* Author: lucas
* Tags: HPC, proxy, distribution
*/


model FAKE_USE_CASE

global
{
	int grid_width <- 2;
	int grid_height <- 1;
	int size_OLZ <- 15;
	int simulation_id <- 0;
	bool debug <- true;
	int font_size <- 25;
	
	init
	{
		if(simulation_id = 0)
		{
			create prey
			{
				location <- {45,30};
				energy <- 30;
				syncmode <- "HardSync";
				_index <- 0;
				name <- "prey0";
			}
			create prey
			{
				location <- {55,30};
				energy <- 30;
				fake <- true;
				_index <- 1;
				name <- "prey0_COPY";
			}
			
			create prey
			{
				location <- {45,70};
				energy <- 80;
				syncmode <- "HardSync";
				_index <- 2;
				name <- "prey1";
			}
			create prey
			{
				location <- {55,70};
				energy <- 80;
				fake <- true;
				_index <- 3;
				name <- "prey1_COPY";
			}
			
			/*create predator
			{
				location <- {20,30};
				index_ <- 0; 
				col <- #blue;
				target <- prey[index_];
			}*/
			create predator
			{
				location <- {80,30};
				index_ <- 1;
				col <- #blue;
				target <- prey[index_];
			}
			/*create predator
			{
				location <- {20,70};
				index_ <- 2; 
				col <- #blue;
				target <- prey[index_];
			}*/
			create predator
			{
				location <- {80,70};
				index_ <- 3;
				col <- #blue;
				target <- prey[index_];
			}
		}else
		{
			
		}
	}
}

species prey
{
	rgb col;
	bool fake <- false;
	string syncmode <- "";
	int energy <- 100;
	int _index;
	
	init
	{
		col <- #red;
	}
	
	aspect default
	{		
		if(!fake)
		{
			draw circle(3) color: col;
			draw name at: {location.x - 5, location.y - 5} color: #black font: font('Default', font_size, #bold);
			//draw string(energy) at: {location.x - 1, location.y+1} color: #black font: font('Default', font_size, #bold);
		}else
		{
			draw circle(1.25) color: rgb(col, 0.5);
			draw name at: {location.x - 5, location.y - 3} color: #black font: font('Default', font_size, #bold);
			//draw string(energy) at: {location.x - 1, location.y+1} color: #black font: font('Default', font_size, #bold);
		}
	}
}

species predator
{
	rgb col;
	prey target;	
	int index_;
	
	reflex acquire_target
	{
		if(target = nil)
		{
			target <- one_of(prey);
			write("new target " + target);
		}else {		
			write("I SEE THIS prey AGENT : " + target);
		}
	}
	
	aspect default
	{		
		draw circle(3) color: col;
		draw name at: {location.x - 5.5 ,location.y - 3.2} color: #black font: font('Default', font_size, #bold);
		if(target != nil)
		{		
			draw line(location, target) color: col;
		}
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
	
	
	aspect default
	{
		draw self.shape color: rgb(#white,125) border:#black;	
		draw "[" + self.grid_x + "," + self.grid_y +"] : RANK " + rank color: rgb(#red,125) font: font('Default', font_size, #bold) at: {location.x - 10 ,location.y - 3.2};
		draw OLZ_combined color: rgb(255,125,125,125);
		
	}
}

experiment TEST_distribution
{
	output{
		display OLZ_proxy_grid type: 2d
		{
			species OLZ;
			species prey;
			species predator;
		}
	}
}