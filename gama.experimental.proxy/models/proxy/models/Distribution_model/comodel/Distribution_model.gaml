/**
* Name: Distribution_model
* Distribution model to distribute a thematic model. 
* Author: Lucas Grosjean
* Tags: Visualisation, HPC, distributed ABM, distribution model
*/

model Distribution

import "Thematic_model.gaml" as Thematic

global
{
	Thematic thematic;
	int end_cycle <- 50;
	int MPI_RANK;
	float local_average;
	float global_average <- 0.0;
	
	float start_thematic_time;
	float end_thematic_time;
	float cycle_duration_second;
	
	float total_thematic_execution_time <- 0.0;
	
	int thematic_world_lenght;
	int thematic_world_height;
	
	init
 	{
 		create Thematic.Thematic_experiment;
 		create DataAccess_Agent;
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
 		write("distribution step : --------------------------------------" + cycle);
 		start_thematic_time <- machine_time;
 		write("start_thematic_time " + start_thematic_time);
 		ask Thematic.Thematic_experiment[0].simulation
 		{
 			do _step_;
 		}
 		end_thematic_time <- machine_time;
 		write("end_thematic_time " + end_thematic_time);
 		cycle_duration_second <- (end_thematic_time - start_thematic_time)*1000;
 		total_thematic_execution_time <- total_thematic_execution_time + cycle_duration_second;
 	}
 	
 	reflex update_local_average
 	{
 		ask Visualisation_Agent
 		{
 			local_average <- compute_local_average();
 			write("local_average : " + local_average);
 		}
 	}
 	
 	reflex update_global_average
 	{
 		ask Visualisation_Agent
 		{
 			global_average <- compute_global_average();
 		}
 	}
}

species DataAccess_Agent
{
 	list<int> get_data
 	{
 		list<int> data;
 		ask Thematic.Thematic_experiment[0]
 		{
 			data <- agent_with_data collect each.data;
 			write("data1 " + data);
 		}
		return data;
 	}
 	
 	list<agent_with_data> get_agent
 	{
 		list<agent_with_data> agts;
 		ask Thematic.Thematic_experiment[0]
 		{
 			agts <- list<agent_with_data>(agent_with_data);
 		}
 		return agts;
 	}
 	
 	action get_thematic_world_size
 	{
 		ask Thematic.Thematic_experiment[0]
 		{
 			ask simulation
 			{
 				thematic_world_lenght <- self.world_lenght;
 				thematic_world_height <- self.world_height;
 			}
 		}
 		write("thematic_world_lenght "+ thematic_world_lenght);
 		write("thematic_world_height "+ thematic_world_height);
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
	
	float compute_local_average
	{
		float average;
		ask DataAccess_Agent
		{
			list data <- get_data();
			average <- mean(data);
		}
		return average;
	}
	
	float compute_global_average
	{
		if(MPI_RANK = 0)
		{
			write("start compute_global_average for root");
			list<float> locals_average;
			ask Communication_Agent_MPI
			{
				write("asking Communication_Agent_MPI " + local_average);
				locals_average <- gather(local_average, 0);
				write("gathered locals_average "+locals_average);
			}
			write("mean(locals_average "+mean(locals_average));
			return mean(locals_average);
		}else
		{	
			write("start compute_global_average for " + MPI_RANK);
			ask Communication_Agent_MPI
			{
				do gather(local_average, 0);	
			}
			return 0.0;
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

grid Partitionning_Agent_Grid width: 4 height: 4
{
	int number_of_agent_inside_me <- 0;
	int MPI_RANK;
	
	reflex micro_agent_inside_me
	{
		list<agent> agts;
		ask DataAccess_Agent
		{
			agts <- (get_agent());
		}
		list agent_inside_self <- ((agts) inside self.shape);
		//write("AGENT INSIDE ME("+self+") " + agent_inside_self);
		
		number_of_agent_inside_me <- length(agent_inside_self);
	}
	
	reflex print_number_inside_me
	{
		write("Number of agent inside me("+self+") " + number_of_agent_inside_me);
	}
}

species LoadBalancing_Agent
{
	action load_balance
	{
		
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
		display thematic 
		{
			//species agent_with_data;	
	    	grid Partitionning_Agent_Grid border: #black ;
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
