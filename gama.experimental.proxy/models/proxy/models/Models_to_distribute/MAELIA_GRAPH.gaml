/**
* Name: MAELIAGRAPH
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model MAELIAGRAPH

global
{
	aspatial_graph my_graph;
	graph gama_graph <- graph([]);
	
	init
	{
		create aspatial_graph; 			// print graph
		my_graph <- aspatial_graph[0]; 	// gama graph
		list<pair<int,int>> edges_to_add <- list<pair<int,int>>([
				pair(45::24), 39::24, 44::45, 46::45, 50::39, 38::39, 57::50, 
				51::50, 55::38, 40::38, 47::55, 56::55, 52::40, 42::40, 
				49::47, 48::47, 53::52, 54::52, 63::42, 62::42, 41::42, 
				58::41, 59::58, 61::58, 37::41, 64::37, 60::64, 65::64, 
				66::36, 35::36, 68::35, 34::35, 69::68, 17::34, 32::34, 
				22::32, 9::32, 31::32, 23::22, 21::22, 11::9, 15::9, 13::11,
				 10::11, 14::13, 12::13, 36::37, 18::31, 30::31, 19::18, 
				 20::18, 16::30, 29::30, 2::29, 4::29, 1::2, 3::2, 28::4, 
				 5::28, 26::28, 7::5, 6::5, 8::26, 25::26
		]);
		ask my_graph
		{
			do add_edges(edges_to_add);
		}
		ask my_graph
		{
			write("node lenght " + length(nodes));
		}
		
		loop tmp over: edges_to_add
		{
			gama_graph <- gama_graph add_edge tmp;
		}
		
		int number_of_cluster <- 4;
		list<list<int>> clusters <- gama_graph cluster_level number_of_cluster;
		
		write("Number of clusters generated " + length(clusters));
		
		if(number_of_cluster != length(clusters))
		{
			write("could not generate " + number_of_cluster + " clusters, could only generate " + length(clusters));
		}
		
		loop node_list over: clusters
		{
			write("node_list " + node_list  + " size " + length(node_list));
			ask aspatial_graph
			{
				rgb col <- rgb(rnd(255),rnd(255),rnd(255));
				loop node over: node_list
				{
					node_custom custom <- has_node(node);
					custom.color <- col;
				}
			}
		}
	}
}

species aspatial_graph
{
	list<node_custom> nodes;
	list<pair<int,int>> edges;
	
	action add_edge(pair<int,int> edge)
	{	
		node_custom source <- has_node(edge.key);
		node_custom target <- has_node(edge.value);
		
		if(source = nil)
		{
			create node_custom with: [value::edge.key] returns: node;
			source <- node[0];
			nodes << source;
		}
		if(target = nil)
		{
			create node_custom with: [value::edge.value] returns: node;
			target <- node[0];
			nodes << target;
		}
		
		edges << edge;
	}
	
	action add_edges(list<pair<int,int>> edges_to_add)
	{
		loop edge over: edges_to_add
		{
			do add_edge(edge);
		}
	}
	
	node_custom has_node(int value_node)
	{
		loop tmp over: nodes
		{
			if(tmp.value = value_node)
			{
				return tmp;
			}
		} 
		return nil;
	}
	
    
    aspect base 
    {
    	loop tmp over: nodes
		{	
			draw circle(2) at: tmp.location color: tmp.color border: #black;
			draw "" + tmp.value at: tmp.location color: #black ; 
		}
		loop tmp over: edges
		{	
			draw line(node_custom(tmp.key), node_custom(tmp.value)) color: #black;
		}
    }
}

species node_custom
{
	int value;
	list<node_custom> children;
	rgb color;
}

experiment exp type: gui
{	
	output {
		
		display d
		{
			species aspatial_graph aspect: base;
		}
	}
}