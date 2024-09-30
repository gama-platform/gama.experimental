/**
* Name: Distribution_model
* Distribution model to distribute a thematic model. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Distribution

import "Continuous_Move_thematic.gaml" as Thematic

global
{
	int end_cycle <- 50000000;
	int MPI_RANK;
	float local_average;
	float global_average <- 0.0;
	
	float start_thematic_time;
	float end_thematic_time;
	float cycle_duration_second;
	
	float total_thematic_execution_time <- 0.0;
	
	init
 	{
 		create Thematic.Thematic_experiment;
 		ask Thematic.Thematic_experiment[0]
 		{
 			self.shape <- myself.shape;
 		}
 		
 		create DataAccess_Agent;
 		create partitionning;
		create centroids number: 10;
 		
 		//create Visualisation_Agent;
 		//create Communication_Agent_MPI;
 		//create Coherence_Agent;
 		//create LoadBalancing_Agent;
 		
 		//MPI_RANK <- Coherence_Agent[0].MPI_RANK;
 		//write("MY MPIRANK IS " + MPI_RANK);
 	}
 	
 	reflex when: cycle = end_cycle
	{
		do die;
	}
 	
 	reflex run_thematic
 	{
 		//write("distribution step : --------------------------------------" + cycle);
 		start_thematic_time <- machine_time;
 		//write("start_thematic_time " + start_thematic_time);
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			do _step_;
 		}
 		end_thematic_time <- machine_time;
 		//write("end_thematic_time " + end_thematic_time);
 		cycle_duration_second <- (end_thematic_time - start_thematic_time)*1000;
 		total_thematic_execution_time <- total_thematic_execution_time + cycle_duration_second;
 	}
}

species DataAccess_Agent
{
 	list<people> get_agent
 	{
 		list<people> agts;
 		ask Thematic.Thematic_experiment[0]
 		{
 			agts <- list<people>(people);
 		}
 		return agts;
 	}
}

species Communication_Agent_MPI skills:[MPI_SKILL]
{
 	action send(unknown data, int dst)
 	{
	    write("MPI_SEND " + data + " to " + dst);
 		do MPI_SEND(data, dst, 50);
 	}
 	
 	unknown receive(int emet)
 	{
	    unknown data <- MPI_RECV(emet, 50);
	    write("MPI_RECV " + data + " from " + emet);
 	}
 	
 	action scatter(unknown data)
 	{
 		// todo
 	}
 	
 	list<unknown> gather(unknown data, int dest)
 	{
	    list<unknown> data_recv <- MPI_GATHER(data, dest);
	    return data_recv;
 	}
 	
 	map<int, unknown> all_to_all(map<int, unknown> data_send)
 	{
	    map<int, unknown> data_recv <- MPI_ALLTOALL(data_send);
	    return data_recv;
 	}
}

species Visualisation_Agent
{
	action snap
	{
 		ask Thematic.Thematic_experiment[0].simulation 
		{	
			save (snapshot("chart")) to: "output.log/snapshot/" + cycle + ".png" rewrite: true;
		}
	}
}

species Coherence_Agent skills: [MPI_SKILL]
{	
	action synchronize
	{
		do MPI_BARRIER;
	}
}

species partitionning
{
	reflex compute_convex
	{
		list<centroids> centers <- list<centroids>(centroids);
		list<people> peoples;
		ask Thematic.Thematic_experiment[0]
		{
			peoples <- people collect each where not dead(each);
		}
		write("centers " + centers);
		loop tmp over: peoples
		{			
			if(not dead(tmp))
			{		
				centroids closest <- centers closest_to tmp;
				closest.mypoints << tmp; 
			}
		}
		
		ask centers
		{
			write("points " + mypoints);
		}
	}
}
species centroids
{
	rgb color_kmeans <- rgb(rnd(255),rnd(255),rnd(255));
	list<people> mypoints <- list<people> ([]);
	
	init
	{
		location <- { rnd(world.shape.width), rnd(world.shape.height)};
	}
	
	reflex update_location when: length(mypoints) > 0
	{
		list toRemove;
		loop tmp over: mypoints
		{
			if(dead(tmp))
			{
				toRemove << tmp;
			}
		}
		mypoints <- mypoints - toRemove;
		location <- mean(mypoints collect each.location); // move centroid in the middle of the convex
		
		mypoints <- list<people> ([]);
	}
	
	aspect default
	{
		draw cross(3, 0.5) color: color_kmeans border: color_kmeans - 25;	
		geometry convex <- convex_hull(polygon(mypoints));
		draw convex color: rgb(color_kmeans,0.2);
	}
}

experiment distribution_experiment /*type: MPI_EXP*/  until: (cycle = end_cycle)
{
	/*reflex when: cycle = end_cycle - 1
	{
		ask simulation 
		{	
			save (snapshot("data")) to: "output.log/snapshot/" + MPI_RANK + "/cycle"+ cycle + ".png" rewrite: true;
			save (snapshot("time")) to: "output.log/snapshot/" + MPI_RANK + "/thematic_cycle_"+ cycle + "_duration.png" rewrite: true;
			
		}
	}
	
	reflex when: cycle = end_cycle - 1
	{
		ask simulation 
		{
			save (snapshot("duration")) to: "output.log/snapshot/" + MPI_RANK + "/Total_duration.png" rewrite: true;	
		}
	}*/
	output
	{
		display map
		{
			species centroids;	
		}
		display map2 type: 2d {
			species people;	
			species building;	
		}
		/*display data
		{
			chart "average data over time" type: series title_font: font('SanSerif' , 25, #italic) label_font: font('SanSerif', 18 #plain) legend_font: font('SanSerif', 18 #bold)
			{
				if(MPI_RANK = 0)
				{				
					data "global_average " value: global_average color: #green;
				}
				data "local_average " value: local_average color: #green;
			}
		}
		display time
		{
			chart "cycle_time" type: series  title_font: font('SanSerif' , 25, #italic) label_font: font('SanSerif', 18 #plain) legend_font: font('SanSerif', 18 #bold)
			{
				data "cycle time " value: cycle_duration_second color: #red;
			}
		}
		display duration 
		{
			chart "duration" type: series  title_font: font('SanSerif' , 25, #italic) label_font: font('SanSerif', 18 #plain) legend_font: font('SanSerif', 18 #bold)
			{
				data "total cycle time " value: total_thematic_execution_time color: #purple;
			}
		}*/
	}
}
