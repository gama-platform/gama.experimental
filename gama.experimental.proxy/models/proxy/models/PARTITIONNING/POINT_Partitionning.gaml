/**
* Name: POINT_Partitionning
* POINT partitionning using a KMEAN algorithm where centroids are agents
* Author: Lucas Grosjean
* Tags: Partitionning, Grid
*/

model GRIDPARTITIONNING

global
{	
	int cluster_number <- 8;
	int nb_data_point <- 1000;
	int n <- 500;
	int m <- 500;
	geometry shape <- rectangle(n, m);
	
	init
 	{
		create data_point number: nb_data_point {
			//People agents are placed randomly among the free space
			location <- any_location_in(world.shape);
		} 
		
		create centroid_agent number: cluster_number
		{
			//centroid_agent agents are placed randomly on a data point
			location <- one_of(data_point).location;
		}
		
		write("data_point " + list(data_point));
		
		ask data_point
		{
			centroid_agent one_centroids <- shuffle(centroid_agent)[0];
			ask one_centroids
			{
				// randomly assign data_point to a centroid
				add myself to: my_points;
				myself.color <- self.color_kmean;
			}
		}
 	}
}

species data_point skills:[moving]
{
	rgb color;
	
	reflex
	{
		do wander;
	}
	aspect default
	{
		draw circle(5) color: color;
	}
}

species centroid_agent
{
	rgb color_kmean;
	list<data_point> my_points;
	list<data_point> tmp_my_points;
	
	init
	{
		color_kmean <- rgb(rnd(255),rnd(255),rnd(255));
	}
	reflex
	{	
		map<int, list<data_point>> migrating_agents;
		loop tmp over: my_points
		{
			centroid_agent closest <- centroid_agent closest_to tmp;
			if(closest = nil)
			{
				ask simulation
				{
					do pause;
				}
			}
			if(closest != self)
			{
				write("tmp leaving " + tmp);
				//add tmp to: self.mypoints;
				if(migrating_agents[closest.index] = nil)
				{
					migrating_agents[closest.index] <- list<data_point>(tmp);
				}else
				{
					migrating_agents[closest.index] << tmp;
				}
			}
		}
		
		loop migrating_agent over: migrating_agents.pairs
		{
			write("migrating_agent " + migrating_agent);
			write("migrating_agent.keyu " + migrating_agent.key);
			write("migrating_agent.value " + migrating_agent.value);
			
			centroid_agent migrate_to <- centroid_agent[migrating_agent.key];
			ask migrate_to
			{
				tmp_my_points <- tmp_my_points + list<data_point>(migrating_agent.value);
				//write("hello " + self);
				//write("hello " + self.tmp_my_points);
			}
			
			write("my_points before " + my_points);
			loop migrating_agent_to_remove over: migrating_agent.value
			{
				remove migrating_agent_to_remove from: my_points;
			}
			write("my_points afetr " + my_points);
		}
		
		write("tmp_my_points " + tmp_my_points);
		my_points <- my_points + tmp_my_points;
		tmp_my_points <- [];
		
		//write("migrating_agent " + migrating_agents);
		//write("lenght["+self+"] : " + length(mypoints));
		location <- mean(my_points collect each.location); // move centroid in the middle of the convex
			
		ask my_points
		{
			color <- myself.color_kmean;
		}
	}
	
	aspect default
	{
		draw cross(2, 0.5) color: color_kmean;
		geometry convex <- convex_hull(polygon(my_points));
		draw convex color: rgb(color_kmean,0.2) border: #black;
	}
}

experiment partition 
{
	output
	{	display "data_point" type: 2d 
		{
			species data_point;	
			species centroid_agent;	
		}
	}
}