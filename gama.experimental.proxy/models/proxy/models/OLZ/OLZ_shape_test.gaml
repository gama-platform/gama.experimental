/**
* Name: OLZ_shape_test
* 
* Model to test out the shape of the OLZ depending of the grid width and height.
* 
* 
* Author: Lucas Grosjean
* Tags: Proxy, HPC, multi simulation, distribution
*/

model OLZ_proxy_grid

import "../Models_to_distribute/MovingAgent.gaml"

global 
{		
	int grid_width <- 4;
	int grid_height <- 4;
	int size_OLZ <- 2;
}

grid cell width: grid_width height: grid_height neighbors: 4
{ 
	
	int rank <- grid_x + (grid_y * grid_width);
	
	list<geometry> OLZ_list;
	map<geometry, int> neighborhood_shape;
	
	geometry OLZ_combined;
	
	/* INNER OLZ */
	geometry OLZ_top_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_bottom_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_left_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0};
	geometry OLZ_right_inner <- shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0};
	
	/* CORNER */
	geometry OLZ_bottom_left_inner <- OLZ_left_inner inter OLZ_bottom_inner;
	geometry OLZ_bottom_right_inner <- OLZ_right_inner inter OLZ_bottom_inner;
	geometry OLZ_top_left_inner <- OLZ_left_inner inter OLZ_top_inner;
	geometry OLZ_top_right_inner <- OLZ_right_inner inter OLZ_top_inner;
	
	/* OUTER OLZ */
	geometry OLZ_top_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,(size_OLZ / 2),0}) translated_by {0,-(size_OLZ / 2),0};
	geometry OLZ_bottom_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {0,-(size_OLZ / 2),0}) translated_by {0,(size_OLZ / 2),0};
	geometry OLZ_left_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {size_OLZ / 2,0,0}) translated_by {-(size_OLZ / 2),0,0};
	geometry OLZ_right_outer <- (shape - rectangle(world.shape.width / grid_width, world.shape.height / grid_height) translated_by {-(size_OLZ / 2),0,0}) translated_by {(size_OLZ / 2),0,0};
	
	/* ALL INNER OLZ */
	geometry inner_OLZ <- OLZ_top_inner + OLZ_bottom_inner + OLZ_left_inner + OLZ_right_inner;
	
	/* ALL OUTER OLZ */
	geometry outer_OLZ <- OLZ_top_outer + OLZ_bottom_outer + OLZ_left_outer + OLZ_right_outer;
	
	init
	{
		write("rank : " + rank);
		
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
		draw "[" + self.grid_x + "," + self.grid_y +"] : " + rank color: #red font:font('Default', 20, #bold) at: {self.location.x - 5, self.location.y};
		
		if(cell[0] = self)
		{	
			
			loop shape_to_display over: neighborhood_shape.keys
			{
				draw shape_to_display color: rgb(#green, 125) border: #black;
			}
			draw outer_OLZ color: rgb(#red, 125) border: #black;
		}
	}
}

experiment OLZ_proxy_grid_centralized 
{
	output{
		display OLZ_proxy_grid type: 2d
		{
			species cell;
		}
	}
}
