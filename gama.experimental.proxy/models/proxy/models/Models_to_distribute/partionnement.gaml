/**
* Name: partionnement
* Distributed KMEAN algorithm for partionning agent
* Author: Lucas Grosjean
* Tags: MPI, distributed algorithm, distribution model, distribution agent, HPC
*/


model partionnement


species KMEAN skills:[MPI_SKILL]
{
	int my_MPIRANK <- MPI_RANK;				// my MPI rank on the MPI network
	list<point> points; 					// position of agents in the current simulation
	list<KMEAN> other_KMEAN_position;		// position of all KMEAN agents in the distributed simulation
	map<int, list<point>> points_to_send; 	// points closer to other KMEAN agents than me
	
	action update_other_KMEAN_position
	{
		int size_MPI <- MPI_SIZE;
		map<int, list<point>> send_my_location_to_all;
		loop rankMPI from: 0 to: size_MPI
		{
			if(rankMPI != my_MPIRANK)
			{			
				send_my_location_to_all[rankMPI] <- list<point>(location);
			}
		}
		map<int, list<KMEAN>> other_KMEAN <- MPI_ALLTOALL(send_my_location_to_all); // we send our position to everyone and we receive everyone position
		loop rankMPI from: 0 to: size_MPI
		{
			other_KMEAN_position <- other_KMEAN_position + other_KMEAN[rankMPI][0];
		}
	}
	
	action compute_closest_KMEAN_for_each_points
	{
		loop current_point over: points
		{
			KMEAN closest_KMEAN <- other_KMEAN_position closest_to current_point; // closest KMEAN point to that point
			points_to_send[closest_KMEAN.my_MPIRANK] <- points_to_send[closest_KMEAN.my_MPIRANK] + current_point; // add that point to the list of point to send to that KMEAN
		}
	}
	
	action exchange_updated_closest_points
	{
		map<int, list<point>> points_closer_to_me <- MPI_ALLTOALL(points_to_send); // we send agent closer to other KMEAN than me and receive points closer to me than other KMEAN
		
		int size_MPI <- MPI_SIZE;
		loop rankMPI from: 0 to: size_MPI
		{
			points <- points + points_closer_to_me[rankMPI]; // we add the new points to our list of points
		}
	}
	
	action update_location
	{
		location <- mean(points collect each.location); // we move the centroid in the middle of the closest point
	}
	
	
	action distributed_KMEAN
	{
		do update_other_KMEAN_position;
		do compute_closest_KMEAN_for_each_points;
		do exchange_updated_closest_points;
		do update_location;
	}
}