/**
* Name: lisbonne
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model Lisbonne

global{
	
    float distance_to_intercept <- 10.0;

    int number_of_red_species <- 500;
    int number_of_green_species <- 500;
    
    init 
    {
    	world.shape <- square(500);
    	
    	create Green number: number_of_green_species;
    	create Red number: number_of_red_species;
    	create hull;
    }
    
}

species hull
{
	geometry init_convex
	{
		list<geometry> li <- Green collect each.shape;
		li <- li + Red collect each.shape;
				
		return convex_hull(polygon(li));
	}
	
	action divide_convex_X(geometry convex_to_divide, int number_of_sub_convex)
	{
		point center_of_convex <- centroid (convex_to_divide);
		list<geometry> right_list <- Green where (each.location.y > center_of_convex.y);
		right_list <- right_list + Red where (each.location.y > center_of_convex.y);
		
		
		list<geometry> left_list <- Green where (each.location.y <= center_of_convex.y);
		left_list <- left_list + Red where (each.location.y<=  center_of_convex.y);
		
		
		geometry convex_right <- convex_hull(polygon(right_list));
		geometry convex_left <- convex_hull(polygon(left_list));
		
		number_of_sub_convex <- number_of_sub_convex - 1;
		
		draw square(10) at: center_of_convex color: rgb(#purple);
		write("number_of_sub_convex X " + number_of_sub_convex);
		
		if(number_of_sub_convex <= 0)
		{	
			draw convex_left color: rgb(rnd(255), rnd(255), rnd(255), 0.3);
			draw convex_right color: rgb(rnd(255), rnd(255), rnd(255), 0.3);
		}else
		{
			do divide_convex_Y(convex_right, number_of_sub_convex);
			do divide_convex_Y(convex_left, number_of_sub_convex);
		}
	}
	
	action divide_convex_Y(geometry convex_to_divide, int number_of_sub_convex)
	{
		point center_of_convex <- centroid (convex_to_divide);
		list<geometry> right_list <- Green where (each.location.y > center_of_convex.y);
		right_list <- right_list + Red where (each.location.y > center_of_convex.y);
		
		list<geometry> left_list <- Green where (each.location.y <= center_of_convex.y);
		left_list <- left_list + Red where (each.location.y<=  center_of_convex.y);
		
		geometry convex_right <- convex_hull(polygon(right_list));
		geometry convex_left <- convex_hull(polygon(left_list));
		
		number_of_sub_convex <- number_of_sub_convex - 1;
		write("number_of_sub_convex Y " + number_of_sub_convex);
		
		draw square(10) at: center_of_convex color: rgb(#purple);
		
		if(number_of_sub_convex <= 0)
		{	
			draw convex_left color: rgb(rnd(255), rnd(255), rnd(255), 0.3);
			draw convex_right color: rgb(rnd(255), rnd(255), rnd(255), 0.3);
		}else
		{
			do divide_convex_X(convex_right, number_of_sub_convex);
			do divide_convex_X(convex_left, number_of_sub_convex);
		}
	}
	
	aspect default
	{
		do divide_convex_Y(init_convex(), 0);
	}
}
species Green skills:[moving] 
{
	
    init 
    {
    	speed <- 1.0;
    }
    
    reflex move 
    {
    	do wander amplitude: 90.0;
    }
    
    aspect default 
    {
    	draw circle(1) color:#green border: #black;
    }
}

species Red skills:[moving] {
	
    Green target;
    
    init 
    {
	    speed <- 0.0;
	    heading <- 90.0;
    }
    
    reflex search_target when: target = nil
     {
	    ask Green at_distance(distance_to_intercept) 
	    {
	        myself.target <- self;
	    }
    }
    
    reflex follow when: target != nil 
    {
	    speed <- 0.8;
	    do goto target: target;
    }
    
    aspect default 
    {
	    draw circle(1) color:#red border: #black;
	    if (target!=nil) 
	    {
	        draw polyline([self.location,target.location]) color:#black;
	    }
    }
}


experiment Lisbonne
{
	
	output{
		display movingDisplay type: 2d
		{
        	species Green aspect: default;
        	species Red aspect: default;
        	species hull aspect: default;
		}
	}
}

