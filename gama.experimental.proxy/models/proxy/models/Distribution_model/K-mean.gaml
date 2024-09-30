/**
* Name: Kmean
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model Kmean

import "../Models_to_distribute/Continuous_Move.gaml" as continuous_move

/* Insert your model definition here */


global
{
	int number_of_centroids <- 5;
	list<point> all_people_in_sub_model;
	
	float sub_model_width;
	float sub_model_height;
	
	init
	{
		write("uber world width " + world.shape.width);
		write("uber world height " + world.shape.height);
		create continuous_move.main;
		ask(continuous_move.main[0])
		{
			sub_model_width <- world.shape.width;
			sub_model_height <- world.shape.height;
		}
		
		write("uber world width " + sub_model_width);
		write("uber world height " + sub_model_width);
		create centroids number: number_of_centroids
		{
			location <- { rnd(world.shape.width), rnd(world.shape.height)};
		}
	}
	
	reflex _step_sub_model
	{
		ask(continuous_move.main collect each.simulation)
		{
			write("stepping");
			do _step_;
		}
	}
	
	reflex
	{
		ask centroids
		{
			mypoints <- list(nil);
		}
		
		ask continuous_move.main[0]
		{
			all_people_in_sub_model <- people collect each.shape.centroid;
		}
		
		loop tmp over: all_people_in_sub_model
		{
			ask (centroids closest_to tmp)
			{
				add tmp to: self.mypoints;
			}
		}
		
		ask centroids
		{
			write("lenght["+self+"] : " + length(mypoints));
			//write("location : " + location);
			location <- mean(mypoints collect each.location); // move centroid in the middle of the convex
			
		}
	}
}

species centroids
{
	rgb color_kmeans <-  rgb(rnd(255),rnd(255),rnd(255));
	list<point> mypoints;
	
	aspect kmeans_aspect2D
	{
		draw cross(3, 0.5) color: color_kmeans border: color_kmeans - 25;
		
		geometry convex <- convex_hull(polygon(mypoints));
		
		draw convex color: rgb(color_kmeans,0.2);
	}
}

experiment main2 type: gui 
{
	output 
	{
		display map type: 2d 
		{
			species centroids aspect: kmeans_aspect2D;
		}
	}
}
