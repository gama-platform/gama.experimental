/*******************************************************************************************************
 *
 * DefaultPopulationFactory.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package proxyPopulation;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.IPopulationFactory;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.grid.GamaSpatialMatrix;
import gama.core.metamodel.topology.grid.GridPopulation;
import gama.core.metamodel.topology.grid.IGridAgent;
import gama.core.runtime.IScope;
import gama.dev.DEBUG;
import gama.gaml.species.ISpecies;

/**
 * Factory of ProxyPopulation
 * 
 * @author Lucas Grosjean
 *
 */
public class ProxyPopulationFactory implements IPopulationFactory {

	static
	{
		DEBUG.ON();
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <E extends IAgent> IPopulation<E> createRegularPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		DEBUG.OUT("Creating new ProxyPopulation" + species.getName());
		return (IPopulation<E>) new ProxyPopulation(host, species);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IPopulation<IGridAgent> createGridPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		final ITopology t = GridPopulation.buildGridTopology(scope, species, host);
		final GamaSpatialMatrix m = (GamaSpatialMatrix) t.getPlaces();
		return new GridPopulation(m, t, host, species);
	}
}
