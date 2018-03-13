package gama_analyzer;


import msi.gama.metamodel.population.IPopulation;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

@species(name = "AgGroupAnalizer")
public class AgentGroupAnalizer extends ClusterBuilder  {
	
	public AgentGroupAnalizer(final IPopulation s) throws GamaRuntimeException {
			super(s);
	}
	
//	@action(name = "creation_cluster")
	public void creationCluster(final IScope scope) throws GamaRuntimeException  {
	}
	
}
