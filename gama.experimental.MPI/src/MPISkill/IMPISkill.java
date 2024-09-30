package MPISkill;

public interface IMPISkill {
	
	String MPI_SKILL = "MPI_SKILL";
	
	String MPI_RANK = "MPI_RANK";
	String MPI_SIZE = "MPI_SIZE";
    String MPI_BARRIER = "MPI_BARRIER";
    String MPI_FINALIZE = "MPI_FINALIZE";
    String MPI_INIT = "MPI_INIT";
    String MPI_INIT_MULTIPLE = "MPI_INIT_MULTIPLE";
	
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
    String MPI_ALLTOALL2 = "MPI_ALLTOALL2";
	String SIZE = "size_of_message";
    
    
    String MPI_GATHERV = "MPI_GATHERV";
    String MPI_SCATTER = "MPI_SCATTER";
    String MPI_SCATTERV = "MPI_SCATTERV";

	Integer REQUEST_TYPE = 1;
	Integer REQUEST_READ = 2;
	Integer REQUEST_WRITE = 3;
}
