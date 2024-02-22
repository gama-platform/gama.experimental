package MPISkill;

public interface IMPISkill {
	
	String MPI_SKILL = "MPI_SKILL";
	
	String MPI_RANK = "MPI_RANK";
	String MPI_SIZE = "MPI_SIZE";
    String MPI_BARRIER = "MPI_BARRIER";
    String MPI_FINALIZE = "MPI_FINALIZE";
	
	String MPI_SEND = "MPI_SEND";
	String MESG = "mesg";
	String SNDSIZE = "sndsize";
	String DEST = "dest";
	String STAG = "stag";
	
	String MPI_RECV = "MPI_RECV";
	String RCVSIZE = "rcvsize";
	String SOURCE = "source";
	String RTAG = "rtag";
	
    String MPI_GATHER = "MPI_GATHER";
    String MPI_ALLTOALL = "MPI_ALLTOALL";
	String SIZE = "size_of_message";
    
    
    String MPI_GATHERV = "MPI_GATHERV";
    String MPI_SCATTER = "MPI_SCATTER";
    String MPI_SCATTERV = "MPI_SCATTERV";

	Integer REQUEST_TYPE = 0;
	Integer REQUEST_READ = 1;
	Integer REQUEST_WRITE = 2;
}
