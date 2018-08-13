package gama_analyzer;


import msi.gama.metamodel.population.IPopulation;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

@species(name = "AgGroupAnalizer")
public class AgentGroupAnalizer extends ClusterBuilder  {
	
	public AgentGroupAnalizer(final IPopulation s, final int index) throws GamaRuntimeException {
			super(s,index);
	}
	
//	@action(name = "creation_cluster")
	public void creationCluster(final IScope scope) throws GamaRuntimeException  {
	}
	
}
