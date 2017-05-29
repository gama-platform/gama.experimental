package femto_st.gama.mpi;

public interface IMPISkill {
	String MPI_NETWORK = "MPI_Network";

	String MPI_INIT = "MPI_INIT";	
	String MPI_RANK = "MPI_RANK";
	String MPI_SIZE = "MPI_SIZE";
	
	String MPI_SEND = "MPI_SEND";
	String DEST = "dest";
	String STAG = "stag";
	
	String MPI_RECV = "MPI_RECV";
	String SOURCE = "source";
	String RTAG = "rtag";	
}
