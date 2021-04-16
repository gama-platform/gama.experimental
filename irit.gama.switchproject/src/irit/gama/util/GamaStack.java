/*******************************************************************************************************
 *
 * GamaStack.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.util;

import java.util.Collection;

import msi.gama.runtime.IScope;
import msi.gaml.types.IType;
import irit.gama.util.deque.GamaDeque;
import irit.gama.util.deque.IDequeOperator;
import irit.gaml.types.TypesIrit;

/**
 * Stack type used by GAML type
 * 
 * @author Jean-François Erdelyi
 */
public class GamaStack<T> extends GamaDeque<T> implements IDequeOperator<T> {

	// ############################################
	// Attributes

	/**
	 * The serializable class does not declare a static final serialVersionUID field
	 * of type long
	 */
	private static final long serialVersionUID = 1L;

	// ############################################
	// Constructors

	/**
	 * Constructor
	 */
	public GamaStack(IType<?> contentsType) {
		super(TypesIrit.STACK.of(contentsType));
	}

	/**
	 * Constructor with data
	 */
	public GamaStack(IType<?> contentsType, T[] values) {
		super(TypesIrit.STACK.of(contentsType), values);
	}

	/**
	 * Constructor with data
	 */
	public GamaStack(IType<?> contentsType, Collection<T> values) {
		super(TypesIrit.STACK.of(contentsType), values);
	}

	// ############################################
	// Override : IDequeOperator

	/**
	 * Pop data from queue (LIFO)
	 */
	@Override
	public T pop(IScope scope) {
		return pollLast();
	}
}
