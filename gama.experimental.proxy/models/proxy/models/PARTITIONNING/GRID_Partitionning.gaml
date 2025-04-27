/**
* Name: GRIDPARTITIONNING
* Grid partitionning with various algorithm. 
* Author: Lucas Grosjean
* Tags: Partitionning, Grid
*/

model GRIDPARTITIONNING

global
{	
	int grid_width <- 30;
	int grid_height <- 30;
	int neighbors <- 4;
	int cluster_number <- 64;
	
	list<list<int>> voronoi_clusters;
	list<list<int>> kmeans_clusters;
	list<list<int>> grid_clusters;
	list<list<int>> horizontal_clusters;
	list<list<int>> vertical_clusters;
	list<list<int>> bsp_clusters; 
	list<list<int>> circular_clusters;
	list<list<int>> spiral_clusters;
	list<list<int>> diagonal_clusters;
	list<list<int>> checkerboard_clusters; 
	list<list<int>> wave_clusters;
	list<list<int>> honeycomb_clusters;
	list<list<int>> fractal_clusters;
	
	bool coloring_done <- false;
	
	init
 	{
		seed <- 10.0;
 		
		// K-means partitioning
		kmeans_clusters <- grid_KMEAN_partitionning(grid_width,grid_height,cluster_number,4);
		
		 
		// Voronoi partitioning
		voronoi_clusters <- grid_voronoi_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Grid partitioning
		grid_clusters <- grid_grid_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Horizontal strips partitioning	     
		horizontal_clusters <- grid_horizontal_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Vertical strips partitioning
		vertical_clusters <- grid_vertical_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// BSP partitioning
		bsp_clusters <- grid_bsp_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Circular partitioning
		circular_clusters <- grid_circular_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Spiral partitioning
		spiral_clusters <- grid_spiral_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Diagonal partitioning
		diagonal_clusters <- grid_diagonal_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Checkerboard partitioning
		checkerboard_clusters <- grid_checkerboard_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Fractal partitioning
		fractal_clusters <- grid_fractal_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Honeycomb partitioning
		honeycomb_clusters <- grid_honeycomb_partitioning(grid_width,grid_height,cluster_number,4);
		 
		// Wave partitioning
		wave_clusters <- grid_wave_partitioning(grid_width,grid_height,cluster_number,4);
 	}
 	
	
}
	
grid grid_to_cluster width: grid_width height: grid_height neighbors: 4
{
	action coloring(list<list<int>> clusters)
	{
		list colors <- [#yellow, #red, #green, #pink, #purple, #aliceblue, #antiquewhite, #aqua, #aquamarine, #azure, #beige, #bisque, #black, #blanchedalmond, #blue, #blueviolet, #brown, #burlywood, #cadetblue, #chartreuse, #chocolate, #coral, #cornflowerblue, #cornsilk, #crimson, #cyan, #darkblue, #darkcyan, #darkgoldenrod, #darkgray, #darkgreen, #darkkhaki, #darkmagenta, #darkolivegreen, #darkorange, #darkorchid, #darkred, #darksalmon, #darkseagreen, #darkslateblue, #darkslategray, #darkturquoise, #darkviolet, #deeppink, #deepskyblue, #dimgray, #dodgerblue, #firebrick, #floralwhite, #forestgreen, #fuchsia, #gainsboro, #ghostwhite, #gold, #goldenrod, #gray, #green, #greenyellow, #honeydew, #hotpink, #indianred, #indigo, #ivory, #khaki, #lavender, #lavenderblush, #lawngreen, #lemonchiffon, #lightblue, #lightcoral, #lightcyan, #lightgoldenrodyellow, #lightgray, #lightgreen, #lightpink, #lightsalmon, #lightseagreen, #lightskyblue, #lightslategray, #lightsteelblue, #lightyellow, #lime, #limegreen, #linen, #magenta, #maroon, #mediumaquamarine, #mediumblue, #mediumorchid, #mediumpurple, #mediumseagreen, #mediumslateblue, #mediumspringgreen, #mediumturquoise, #mediumvioletred, #midnightblue, #mintcream, #mistyrose, #moccasin, #navajowhite, #navy, #oldlace, #olive, #olivedrab, #orange, #orangered, #orchid, #palegoldenrod, #palegreen, #paleturquoise, #palevioletred, #papayawhip, #peachpuff, #peru, #pink, #plum, #powderblue, #purple, #red, #rosybrown, #royalblue, #saddlebrown, #salmon, #sandybrown, #seagreen, #seashell, #sienna, #silver, #skyblue, #slateblue, #slategray, #snow, #springgreen, #steelblue, #tan, #teal, #thistle, #tomato, #turquoise, #violet, #wheat, #white, #whitesmoke, #yellow, #yellowgreen];
		int index_color <- 0;
		loop cluster over: clusters
		{
			loop cell over: cluster
			{
				grid_to_cluster[cell].color <- colors[index_color];
			}
			index_color <- index_color + 1;
		}
	}
	
	aspect voronoi
	{
		if(self.index = 0)
		{		
			do coloring(voronoi_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect kmeans
	{
		if(self.index = 0)
		{		
			do coloring(kmeans_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect grid_aspect
	{
		if(self.index = 0)
		{		
			do coloring(grid_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect horizontal
	{
		if(self.index = 0)
		{		
			do coloring(horizontal_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect vertical
	{
		if(self.index = 0)
		{		
			do coloring(vertical_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect bsp
	{
		if(self.index = 0)
		{		
			do coloring(bsp_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect circular
	{
		if(self.index = 0)
		{		
			do coloring(circular_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect spiral
	{
		if(self.index = 0)
		{		
			do coloring(spiral_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect diagonal
	{
		if(self.index = 0)
		{		
			do coloring(diagonal_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect checkerboard
	{
		if(self.index = 0)
		{		
			do coloring(checkerboard_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect wave
	{
		if(self.index = 0)
		{		
			do coloring(wave_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect honeycomb
	{
		if(self.index = 0)
		{		
			do coloring(honeycomb_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
	
	aspect fractal
	{
		if(self.index = 0)
		{		
			do coloring(fractal_clusters);
			coloring_done <- true;
		}
		draw self color: color;
	}
}


experiment partition 
{
	output
	{
		display voronoi_display
		{
			species grid_to_cluster aspect: voronoi; 
		}
		display KMEAN_display
		{
			species grid_to_cluster aspect: kmeans; 
		}
		display grid_display
		{
			species grid_to_cluster aspect: grid_aspect; 
		}
		display horizontal_display
		{
			species grid_to_cluster aspect: horizontal; 
		}
		display vertical_display
		{
			species grid_to_cluster aspect: vertical; 
		}
		display bsp_display
		{
			species grid_to_cluster aspect: bsp; 
		}
		display circular_display
		{
			species grid_to_cluster aspect: circular; 
		}
		display spiral_display
		{
			species grid_to_cluster aspect: spiral; 
		}
		display diagonal_display
		{
			species grid_to_cluster aspect: diagonal; 
		}
		display checkerboard_display
		{
			species grid_to_cluster aspect: checkerboard; 
		}
		display wave_display
		{
			species grid_to_cluster aspect: wave; 
		}
		display honeycomb_display
		{
			species grid_to_cluster aspect: honeycomb; 
		}
		display fractal_display
		{
			species grid_to_cluster aspect: fractal; 
		}
	}
}