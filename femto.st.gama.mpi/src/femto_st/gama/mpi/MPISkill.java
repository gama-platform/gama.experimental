package femto_st.gama.mpi;

import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;

import mpi.*;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

@vars({ @var(name = IMPISkill.MPI_RANK, type = IType.INT, doc = @doc("Init MPI Brocker"))})
@skill(name=IMPISkill.MPI_NETWORK, concept = { IConcept.GUI, IConcept.COMMUNICATION, IConcept.SKILL })
public class MPISkill extends Skill{
	static boolean isMPIInit = false;
	
	
	private void initialize(final IScope scope) {
		isMPIInit = true;
	}
	
	private void startSkill(final IScope scope) {
		initialize(scope);
		registerSimulationEvent(scope);
	}
	
	@action(name = IMPISkill.MPI_INIT, args = {}, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public void mpiInit(IScope scope)
	{
		if(isMPIInit)
			return;
		
		String[] arg={};
		try {
			MPI.Init(arg);
			isMPIInit = true;
		} catch (MPIException e) {
			System.out.println("MPI Init Error" + e);
		}
	}
	
	@action(name = IMPISkill.MPI_SIZE, args = {}, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public int getMPISIZE(IScope scope)
	{
		  int size = 2;
		    try {
		    size = MPI.COMM_WORLD.getSize();

		    } catch (MPIException mpiex) {
		    	System.out.println("MPI Size Error"+mpiex);
		    }
		
		return size;
	}
	
	@action(name = IMPISkill.MPI_RANK, args = {}, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public int getMPIRANK(IScope scope)
	{
		  int rank = 2;
		    try {
		    rank = MPI.COMM_WORLD.getRank();

		    } catch (MPIException mpiex) {
		    	System.out.println("MPI rank Error"+mpiex);
		    }
		
		return rank;
	}
	
	@action(name = IMPISkill.MPI_SEND, args = {
			@arg(name = IMPISkill.DEST, type = IType.INT, doc = @doc("dest destinataire")),
			@arg(name = IMPISkill.STAG, type = IType.INT, doc = @doc("stag message tag"))
	}, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public void send(IScope scope)
	{
		int dest = ((Integer) scope.getArg(IMPISkill.DEST, IType.INT)).intValue();
		int stag = ((Integer) scope.getArg(IMPISkill.STAG, IType.INT)).intValue();
		
		int message[] = new int [1];
		message[0] = 14;
		try {
		    MPI.COMM_WORLD.send( message, 1, MPI.INT, dest, stag);

	    } catch (MPIException mpiex) {
	    	System.out.println("MPI send Error"+mpiex);
	    }
	}
	
	@action(name = IMPISkill.MPI_RECV, args = {
			@arg(name = IMPISkill.SOURCE, type = IType.INT, doc = @doc("source sender")),
			@arg(name = IMPISkill.RTAG, type = IType.INT, doc = @doc("rtag message tag"))
	}, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public int recv(IScope scope)
	{
		int source = ((Integer) scope.getArg(IMPISkill.SOURCE, IType.INT)).intValue();
		int rtag = ((Integer) scope.getArg(IMPISkill.RTAG, IType.INT)).intValue();
		
		int message[] = new int [1];
		try {
		    MPI.COMM_WORLD.recv( message, 1, MPI.INT, source, rtag);
		    System.out.println("message = " + message[0]);
	    } catch (MPIException mpiex) {
	    	System.out.println("MPI send Error"+mpiex);
	    }
		
		return message[0];
	}
	
		
	private void finalizeMPI(final IScope scope)
	{
		try {
			MPI.Finalize();
			isMPIInit = false;
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registerSimulationEvent(final IScope scope) {
			scope.getSimulation().postDisposeAction(scope1 -> {
				finalizeMPI(scope1);
			return null;
		});
	}

	
	
}
