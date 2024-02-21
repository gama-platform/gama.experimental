/*******************************************************************************************************
 *
 * ISpatialEntityFunction.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling
 * and simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package spll.localizer.distribution.function;

import java.util.function.Function;

import gama.core.metamodel.shape.IShape;

/**
 *
 * javadoc
 *
 * @author kevinchapuis
 *
 * @param <N>
 */
public interface ISpatialEntityFunction<N extends Number> extends Function<IShape, N> {

	/**
	 * javadoc
	 *
	 * @param entity
	 */
	default void updateFunctionState(final IShape entity) {}

}
