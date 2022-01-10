	GAMA-MPI MODELS
	---------------
	
# Run models:
- adapt GAMA_HEADLESS_HOM in startMpiModel to use the right platform (win/mac/linux)
- ./startMpiModel model.xml NB_PROC

# Directories
## samples:
- first_mpi: just MPI_RANK and MPI_SIZE -> works fine
- communicatkion: with MPI_SEND et MPI_RECV -> plante sur send

# parallelPredatorPrey:
- predatorPrey.gaml: base model used by the controllers

- predatorPreyParallelControler2Proc.gaml: 
  synopsis: controler for only two processors
  state: should be working
  
- predatorPreyParallelControler2ProcOnlyLocation.gaml: 
  synopsis: same but only the locations are exchanged, for tests.
  state: should be working

- predatorPreyParallelStarterGeneric.gaml: 
  synopsis: in the new archi the controller is started by the starter, 
  state: ongoing dev
- predatorPreyParallelControlerGeneric.gaml: 
  synopsis: generic version, 
  state: on going dev


	     
 

