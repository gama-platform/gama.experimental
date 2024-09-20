package gama.experimental.argumentation.species;

import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.util.IList;
import gama.experimental.argumentation.types.GamaArgument;
import gama.experimental.argumentation.types.GamaArgumentType;
import gama.gaml.types.IType;

@species (
		name = "abstract_communication_channel")
@vars ({
	 @variable(name = "type", type = IType.STRING),
		@variable(name = "arguments", type = IType.LIST, of = GamaArgumentType.id)
				
})
public class CommunicationChannel extends GamlAgent {

	static final String TYPE = "type";
	static final String ARGUMENTS = "arguments";
	
	
	public CommunicationChannel(IPopulation<? extends IAgent> s, int index) {
		super(s, index);
	}
	

	@getter(TYPE)
	public String getType(final IAgent agent) {
		return (String) agent.getAttribute(TYPE);
	}

	@setter(TYPE)
	public void setType(final IAgent agent, final String v) {
		agent.setAttribute(TYPE, v);
	}
	
	@getter(ARGUMENTS)
	public static IList<GamaArgument> getArguments(final IAgent agent) {
		return (IList<GamaArgument>) agent.getAttribute(ARGUMENTS);
	}

	@setter(ARGUMENTS)
	public void setArguments(final IAgent agent, final IList<GamaArgument> v) {
		agent.setAttribute(ARGUMENTS, v);
	}
	

}
