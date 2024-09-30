package MPISkill;

import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.extension.serialize.gaml.SerialisationOperators;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import mpi.MPI;
import mpi.MPIException;

@vars ({ @variable (
		name = IMPISkill.MPI_RANK,
		type = IType.INT,
		doc = @doc ("MPI Rank of the GAMA instance")),
	 @variable (
		name = IMPISkill.MPI_SIZE,
		type = IType.INT,
		doc = @doc ("Size of MPI world")) })
@skill (
		name = IMPISkill.MPI_SKILL,
		concept = { IConcept.COMMUNICATION, IConcept.SKILL })		
public class MPISkill extends Skill 
{

	static
	{
		DEBUG.OFF();
	}

	@getter(IMPISkill.MPI_SIZE)
	public int mpi_size(final IScope scope)
	{
		//DEBUG.LOG("getMPISIZE");
		try {
			return MPI.COMM_WORLD.getSize();
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}


	@getter(IMPISkill.MPI_RANK)
	public int mpi_rank(final IScope scope)
	{
		try {
			//DEBUG.LOG("getMPIRANK " + MPI.COMM_WORLD.getRank());	
			return MPI.COMM_WORLD.getRank();
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@action (
			name = IMPISkill.MPI_SEND,
			args = { 
					@arg (
							name = IMPISkill.MESG,
							type = IType.NONE,
							doc = @doc ("mesg message")),
					@arg (
							name = IMPISkill.DEST,
							type = IType.INT,
							doc = @doc ("dest destinataire")),
					@arg (
							name = IMPISkill.STAG,
							type = IType.INT,
							doc = @doc ("stag message tag")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void mpi_send(final IScope scope) {

		final Object mesg = scope.getArg(IMPISkill.MESG, IType.NONE);
		final int dest = ((Integer) scope.getArg(IMPISkill.DEST, IType.INT)).intValue();
		final int stag = ((Integer) scope.getArg(IMPISkill.STAG, IType.INT)).intValue();

		DEBUG.LOG("mesg = " + mesg);
		DEBUG.LOG("dest = " + dest);
		DEBUG.LOG("stag = " + stag);
		MPIFunctions.MPI_SEND(scope, mesg, dest, stag);
	}

	@action (
			name = IMPISkill.MPI_RECV,
			args = { 
					@arg (
							name = IMPISkill.SOURCE,
							type = IType.INT,
							doc = @doc ("source sender")),
					@arg (
							name = IMPISkill.RTAG,
							type = IType.INT,
							doc = @doc ("rtag message tag")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public Object mpi_recv(final IScope scope) {
		
		final int source = ((Integer) scope.getArg(IMPISkill.SOURCE, IType.INT)).intValue();
		final int rtag = ((Integer) scope.getArg(IMPISkill.RTAG, IType.INT)).intValue();

		DEBUG.OUT("source = " + source);
		DEBUG.OUT("rtag = " + rtag);
		return MPIFunctions.MPI_RECV(scope, source, rtag);
	}
	
	@action (
			name = IMPISkill.MPI_GATHER,
				args = { 
						@arg (
							name = IMPISkill.MESG,
							type = IType.NONE,
							doc = @doc ("mesg message")),
						@arg (
							name = IMPISkill.DEST,
							type = IType.INT,
							doc = @doc ("recipient of the message")) },
				doc = @doc (
				value = "",
				returns = "",
				examples = { @example ("") }))
	public IList mpi_gatherv(final IScope scope)
	{

		int recipient = (Integer) scope.getArg(IMPISkill.DEST, IType.INT);
		final IList mesg = (IList) scope.getArg(IMPISkill.MESG, IType.LIST);
		
		DEBUG.OUT("mpi gatherV : " + mesg.length(scope));
		DEBUG.OUT("recipient : " + recipient);
		
		for(var auto : mesg)
		{
			DEBUG.OUT("sending : " + auto);
		}
		return MPIFunctions.MPI_GATHERV(scope, mesg, recipient);
	}
	
	@action (
			name = IMPISkill.MPI_ALLTOALL,
				args = { 
						@arg (
							name = IMPISkill.MESG,
							type = IType.MAP,
							doc = @doc ("mesg message"))},
				doc = @doc (
				value = "",
				returns = "",
				examples = { @example ("") }))
	public IMap<Integer, IList<?>> mpi_alltoall(final IScope scope)
	{
		final IMap<Integer, List<?>> mesg = (IMap<Integer,  List<?>>) scope.getArg(IMPISkill.MESG, IType.MAP);
		DEBUG.OUT("imap ALLTOALL : " + mesg);
		return MPIFunctions.MPI_ALLTOALLV(scope, mesg);
	}
	
	
	@action (
			name = IMPISkill.MPI_BARRIER,
			args = {},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void doBarrier(final IScope scope) {
		try 
		{
			DEBUG.OUT("MPI BARRIER WAITING = "+ MPI.COMM_WORLD.getRank());
			MPI.COMM_WORLD.barrier();
			DEBUG.OUT("MPI BARRIER END = "+ MPI.COMM_WORLD.getRank());
		} catch (final MPIException mpiex) 
		{
			DEBUG.OUT("MPI barrier Error" + mpiex);
		}
	}
	
	@action (
			name = IMPISkill.MPI_FINALIZE,
			args = {},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void mpi_finalize(final IScope scope) {
		MPIFunctions.MPI_FINALIZE();
	}
	
	@action (
			name = IMPISkill.MPI_INIT_MULTIPLE,
			args = {},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void mpi_init(final IScope scope) {
		MPIFunctions.MPI_INIT();
	}
	
	@action (
			name = IMPISkill.MPI_INIT,
			args = {},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void mpi_init_multiple(final IScope scope) {
		MPIFunctions.MPI_INIT_MULTIPLE_THREAD();
	}
	
	@action (
			name = "testSerialize",
			args = {@arg (
					name = IMPISkill.MESG,
					type = IType.LIST,
					doc = @doc ("mesg message"))},
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public IList testSerialize(final IScope scope) 
	{
		final IList mesg = (IList) scope.getArg(IMPISkill.MESG, IType.LIST);
		String conversion = SerialisationOperators.serialize(scope, mesg);
		DEBUG.OUT("conversion : " + conversion);
		
		final IList rcvMesg = (IList) SerialisationOperators.unserialize(scope, conversion);
		DEBUG.OUT("rcvMesg "+rcvMesg);
		
		return rcvMesg;
	}
}
