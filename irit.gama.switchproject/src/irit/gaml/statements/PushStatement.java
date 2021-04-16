/*******************************************************************************************************
 *
 * PushStatement.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gaml.statements;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IContainer;
import msi.gama.util.graph.IGraph;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.AbstractStatement;
import msi.gaml.types.IType;

import irit.gama.common.interfaces.IKeywordIrit;
import irit.gama.precompiler.IConceptIrit;
import irit.gama.precompiler.ITypeIrit;
import irit.gama.util.GamaQueue;
import irit.gama.util.deque.GamaDeque;
import irit.gama.util.deque.IDequeOperator;

/**
 * Push statement used by Queue and Stack types
 * 
 * @author Jean-François Erdelyi
 */
@symbol(name = IKeywordIrit.PUSH, kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false, concept = {
		IConceptIrit.STACK, IConceptIrit.QUEUE })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER }, symbols = IKeyword.CHART)
@doc(value = "Allows to add, i.e. to insert, a new element in a deque", usages = {
		@usage(value = "The new element can be added either at the end of the deque", examples = {
				@example(value = "push expr to: stack;		// Add at the end", isExecutable = false),
				@example(value = "push expr to: queue;		// Add at the end", isExecutable = false) }) })
@facets(value = {
		@facet(name = IKeyword.ITEM, type = IType.NONE, optional = false, doc = {
				@doc("any expression to add in the deque") }),
		@facet(name = IKeyword.TO, type = { ITypeIrit.STACK, ITypeIrit.QUEUE }, optional = false, doc = {
				@doc("the stack or queue") }), }, omissible = IKeyword.ITEM)
@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class PushStatement extends AbstractStatement {

	// ############################################
	// Attributs

	/**
	 * Expressions Item
	 */
	final IExpression itemExp;

	/**
	 * Expressions To
	 */
	final IExpression toExp;

	// ############################################
	// Constructor

	/**
	 * Push constructor
	 */
	public PushStatement(IDescription desc) {
		super(desc);

		// Get facets
		itemExp = getFacet(IKeyword.ITEM);
		toExp = getFacet(IKeyword.TO);

		// Save data from facets
		String toName = (toExp != null) ? toExp.literalValue() : null;

		// Set name
		setName("push to " + toName);
	}

	// ############################################
	// Methods

	/**
	 * Return object casted into GamaDeque if possible, null otherwise
	 */
	private GamaDeque identifyContainer(final IScope scope, final IExpression toExp) throws GamaRuntimeException {
		final Object cont = toExp.value(scope);
		if (cont instanceof GamaDeque) {
			return (GamaDeque) cont;
		}
		return null;
	}

	/**
	 * Main fonction
	 */
	@Override
	protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
		// Cast dequeu if possible
		GamaDeque to = identifyContainer(scope, toExp);
		// Get value form facet
		Object data = itemExp.value(scope);

		// Check if deque and data are not null and insert data
		if (to != null && data != null) {
			to.addLast(data);
		}
		return to;
	}

}
