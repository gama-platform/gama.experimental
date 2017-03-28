package femto_st.gama.mpi;

import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;

import mpi.MPI;
import mpi.MPIException;
import msi.gama.precompiler.GamlAnnotations.action;
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
			System.out.println("MIP Init Error" + e);
		}
	}
	
	@action(name = IMPISkill.MPI_RANK, args = {}, doc = @doc(value = "", returns = "", examples = { @example("") }))
	public int getMPIRANK(IScope scope)
	{
		  int rank = 2;
		    try {
		    rank = MPI.COMM_WORLD.getRank();

		    } catch (MPIException mpiex) {
		    	System.out.println("MIP Error"+mpiex);
		    }
		
		return rank;
	}
	
	
	private void initialize(final IScope scope) {
		isMPIInit = true;
	}

	private void startSkill(final IScope scope) {
		initialize(scope);
		registerSimulationEvent(scope);
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
