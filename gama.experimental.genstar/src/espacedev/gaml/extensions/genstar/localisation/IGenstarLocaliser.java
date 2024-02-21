/*******************************************************************************************************
 *
 * IGenstarGenerator.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package espacedev.gaml.extensions.genstar.localisation;

import espacedev.gaml.extensions.genstar.statement.LocaliseStatement;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.IContainer;

/**
 * Main interface to define new localisation process based on Genstar
 * 
 * @author kevinchapuis
 *
 */
public interface IGenstarLocaliser {
	
	/**
	 * Main method that will localise a given population based on several options passed through 
	 * the LocaliseStatement facets
	 * 
	 * @param scope
	 * @param locStatement
	 */
	void localise(IScope scope, final IContainer<?, IAgent>  pop, Object nest, LocaliseStatement locStatement);
	
}
