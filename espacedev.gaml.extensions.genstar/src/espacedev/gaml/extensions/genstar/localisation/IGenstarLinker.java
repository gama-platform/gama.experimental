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

import espacedev.gaml.extensions.genstar.statement.SpatialLinkerStatement;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IContainer;

/**
 * Main interface to define new spatial linker process based on Genstar
 * 
 * @author kevinchapuis
 *
 */
public interface IGenstarLinker {
	
	/**
	 * Main method that will link a given population to given spacial entities based on several options passed through 
	 * the LocaliseStatement facets
	 * 
	 * @param scope
	 * @param locStatement
	 */
	void link(IScope scope, final IContainer<?, IAgent>  pop, final IContainer<?, IShape>  nests, SpatialLinkerStatement locStatement);
	
}
