/*******************************************************************************************************
 *
 * IDequeOperator.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.util.deque;

import irit.gama.precompiler.IConceptIrit;
import irit.gama.precompiler.IOperatorCategoryIrit;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

/**
 * Deque interface used for Queue and Stack types
 * 
 * @author Jean-François Erdelyi
 */
public interface IDequeOperator<T> {
	/**
	 * Pop operator must be redefined in queue and stack classes
	 */
	@operator(value = "pop", can_be_const = true, category = { IOperatorCategoryIrit.QUEUE,
			IOperatorCategoryIrit.STACK }, type = ITypeProvider.CONTENT_TYPE_AT_INDEX
					+ 1, concept = { IConceptIrit.QUEUE, IConceptIrit.STACK })
	@doc(value = "retrieves and removes the first available element of this container, or returns null if this container is empty", masterDoc = true, comment = "the pop operator behavior depends on the nature of the operand", usages = {
			@usage(value = "pop return and remove the first object of the container", examples = {
					@example(value = "pop(stack([1, 2]))", equals = "2"),
					@example(value = "pop(queue([1, 2]))", equals = "1") }) })
	T pop(IScope scope) throws GamaRuntimeException;
}
