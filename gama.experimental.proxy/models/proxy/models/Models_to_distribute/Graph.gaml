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
	// the number of classes to create
	int k <- 5;
	
	// the number of points
	int N <- 80;
	
	graph myGraph;
	list<list<node_agent>> clusters;
	list<rgb> colors <- [rgb (255,0,0), rgb(255,255,0), rgb(255,0,255) , rgb(120,120,255), rgb(100,100,100)];
	
	init
	{
		create datapoints number: N
		{
			location <- { rnd(100), rnd(100)};
		}
	}
	
	reflex updateGraph 
	{
		ask edge_agent
		{
			do die;
		}
		//Create a new graph using the distance to compute the edges
		myGraph <- as_distance_graph(node_agent, 20, edge_agent);
		clusters <- girvan_newman_clustering(myGraph, k);
		
		
		write("cluster " + clusters);
		write("cluster leng " + length(clusters));
	}
	
	reflex balance when: (cycle mod 10 = 0) or cycle = 0
	{
		clusters <- girvan_newman_clustering(myGraph, k);
		
		int i <- 0;
		loop c over: clusters 
		{
			rgb col;
			if(i < length(colors))
			{
				col <- colors[i];	
			}else
			{	
				col <- rgb(rnd(255),rnd(255),rnd(255));
			}
			ask list<node_agent>(c) {
				color <- col;
			}
			i <- i + 1;
		}
	}
	
	
	list<list<node_agent>> createKClass(list<list<node_agent>> clusterToReduce, int nbClass)
	{
		if(length(clusterToReduce) <= nbClass)
		{
			return clusterToReduce;
		}
		
		clusterToReduce <- clusterToReduce sort_by (length(each));
		loop cluster over: clusterToReduce 
        {
        	write("size vefore : " + length(cluster));
        }
        
        list<list<node_agent>> bins;
        
        loop i from: 0 to: nbClass-1
        {
        	bins << [];
        }

        // Add sublists to bins
        loop sublist over: clusterToReduce 
        {
            int minIndex <- findMinBinIndex(bins , nbClass);
            bins[minIndex] <-  bins[minIndex] + sublist;
        }
        
        loop bin over: bins 
        {
        	write("size : " + length(bin));
        }

        return bins;
    }
    
    int findMinBinIndex(list<list<node_agent>> bins, int numberOfBin) 
    {
        int minIndex <- 0;
        int minSize <- length(bins[0]);
        
        loop i from: 0 to: numberOfBin-1
        { 
            if (length(bins[i]) < minSize) {
                minIndex <- i;
                minSize <- length(bins[i]);
            }
		}
		
        return minIndex;
    }
}


species edge_agent {
	aspect base {
		draw shape color: #black;
	}
}

species node_agent mirrors: datapoints
{
	//Each location will be the one of the bug at the previous step
	rgb color <- #black;
    point location <- target.location update: target.location;
    
    reflex
    {
    	
    }
    
	aspect base 
	{
		draw sphere(1.1) color: color; 
	}
}


species datapoints skills:[moving] 
{
	rgb color_kmeans <- rgb(0,0,0);
	list<datapoints> relation;
	
	reflex move 
    {
    	do wander amplitude: 90.0;
    }
    
	aspect kmeans_aspect2D
	{
		draw circle(0.5) color: color_kmeans border: color_kmeans - 25;
		loop tmp over: relation
		{	
			draw line(self, tmp) color: #black;
		}
	}
}
experiment clustering2D type: gui
{
	output
	{
		display map_kmeans 
		{
			species edge_agent aspect: base;
			species node_agent aspect: base;
		}
	}
}
