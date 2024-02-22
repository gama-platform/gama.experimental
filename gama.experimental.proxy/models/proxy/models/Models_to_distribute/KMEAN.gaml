/**
* Name: Agent Based Clustering
* Author: Jean-Danie Zucker with Patrick Taillandier's and Arnaud Grignard's Help
* Description: This model displays the step to step algorithm of k-means
* See for  https://en.wikipedia.org/wiki/K-means_clustering ...
* Clustering
* The k-medoid could be added
* To be added stop the simulation when convergence is reached
* To be added an overlay
* To be added position the points at the begining usug user interaction model...
*/


model MASKMEANS


global
{
	// the number of classes to create (kmeans)
	int k <- 2;
	
	// the number of points
	int N <- 500;
	
	init
	{
		create centroids number: k
		{
			location <- { rnd(100), rnd(100) };
		}
		create datapoints number: N
		{
			location <- { rnd(100), rnd(100)};
			mycenter <- centroids closest_to self;
		}
		
		loop tmp over: centroids
		{
			tmp.color_kmeans  <- rgb(rnd(255),rnd(255),rnd(255));
		}
	}
}

species datapoints skills:[moving] 
{
	rgb color_kmeans <- rgb(0,0,0) 	;
	centroids mycenter;
	
	reflex move 
    {
    	do wander amplitude: 90.0;
    }
    
    reflex when: mycenter != nil
    {		
		mycenter.mypoints <- mycenter.mypoints - self;
		mycenter <- centroids closest_to self;
		
		color_kmeans <- mycenter.color_kmeans;
		add self to: mycenter.mypoints;
    }
	aspect kmeans_aspect2D
	{
		draw circle(0.5) color: color_kmeans border: color_kmeans - 25;
	}
}

species centroids
{
	rgb color_kmeans <-  rgb(225,225,225);
	list<datapoints> mypoints;
	
	reflex update_location when: length(mypoints) > 0
	{
		location <- mean(mypoints collect each.location); // move centroid in the middle of the convex
		write("lenght : " + length(mypoints));
	}
	
	aspect kmeans_aspect2D
	{
		draw cross(3, 0.5) color: color_kmeans border: color_kmeans - 25;
		
		list<geometry> li <- mypoints collect each.shape;
		geometry convex <- convex_hull(polygon(li));
		
		draw convex color: rgb(color_kmeans, 0.5);
	}
}

experiment clustering2D type: gui
{
	output
	{
		display map_kmeans 
		{
			species datapoints aspect: kmeans_aspect2D transparency:0.4;
			species centroids aspect: kmeans_aspect2D;
		}

	}
}
