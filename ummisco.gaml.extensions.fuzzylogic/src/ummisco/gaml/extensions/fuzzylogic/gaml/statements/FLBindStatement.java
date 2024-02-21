package ummisco.gaml.extensions.fuzzylogic.gaml.statements;

import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

import ummisco.gaml.extensions.fuzzylogic.gaml.statements.FLBindStatement.FuzzyLogicBindStatementSerializer;
import ummisco.gaml.extensions.fuzzylogic.gaml.statements.FLBindStatement.FuzzyLogicBindStatementValidator;
import ummisco.gaml.extensions.fuzzylogic.utils.IFLKeyword;
import ummisco.gaml.extensions.fuzzylogic.utils.validator.FuzzyLogicStatementValidator;

/**
 * The class SetVariableStatement.
 *
 * @author gaudou
 * @since 11 march 20
 *
 */
@symbol (
	name = IFLKeyword.FL_BIND,
	kind = ISymbolKind.SINGLE_STATEMENT,
	with_sequence = false,
	concept = { IFLKeyword.FL_CONCEPT })
@doc (value = "`" + IFLKeyword.FL_BIND + "` allows to bind an agent attribute to a FIS variable or output.")
@inside (
	kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT})
@facets (
	value = { 
		@facet (
			name = IFLKeyword.FL_ATTRIBUTE,
			type = { IType.NONE },
			optional = false,
			doc = { @doc ("an attribute of the current agent") }),
		@facet (
			name = IFLKeyword.FL_VARIABLE,
			type = IType.STRING,
			optional = true,
			doc = { @doc ("the name of a FIS variable") }),
		@facet (
			name = IFLKeyword.FL_OUTPUT,
			type = IType.STRING,
			optional = true,
			doc = { @doc ("the name of a FIS output") })
	}, 
	omissible = IFLKeyword.FL_ATTRIBUTE)
@validator (FuzzyLogicBindStatementValidator.class)
@serializer (FuzzyLogicBindStatementSerializer.class)
public class FLBindStatement extends AbstractStatement {

	public static class FuzzyLogicBindStatementValidator extends FuzzyLogicStatementValidator {
		@Override
		public void validate(final StatementDescription description) {
			super.validate(description);
			final IExpression att = description.getFacetExpr(IFLKeyword.FL_ATTRIBUTE);
			final IExpression var = description.getFacetExpr(IFLKeyword.FL_VARIABLE);
			final IExpression out = description.getFacetExpr(IFLKeyword.FL_OUTPUT);
			
			if( (var == null) && (out == null) ) {
				description.error("" + IFLKeyword.FL_BIND + " requiers at least either " + IFLKeyword.FL_VARIABLE + " or " + IFLKeyword.FL_OUTPUT + "` facet.",
						IGamlIssue.MISSING_ARGUMENT);		
			}

			if( (var != null) && (out != null) ) {
				description.error("" + IFLKeyword.FL_BIND + " requiers no more than one facet among " + IFLKeyword.FL_VARIABLE + " and " + IFLKeyword.FL_OUTPUT + " facets.",
						IGamlIssue.CONFLICTING_FACETS);		
			}		
		
			// Check that attributes exist
			IDescription superDesc = description.getEnclosingDescription();
			while (! (superDesc instanceof SpeciesDescription) ) {
				superDesc = superDesc.getEnclosingDescription();
			}
			SpeciesDescription superSpeciesDescr = (SpeciesDescription) superDesc;
			
			if( (att != null) && (superSpeciesDescr.getAttribute(att.getName()) == null)) {
				description.error("The attribute " + att.getName() + " does not exist.",
						IGamlIssue.UNKNOWN_FIELD);		
			}
		}
	}
	
	
	public static class FuzzyLogicBindStatementSerializer extends SymbolSerializer<StatementDescription> {

		@Override
		protected void serialize(final SymbolDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		//TODO TO COMPLETE
			sb.append(IFLKeyword.FL_BIND).append(";");
		}
	}


	protected IExpression attribute;
	protected IExpression fis_variable, fis_output;	
	
	public FLBindStatement(IDescription desc) {
		super(desc);
		
		attribute = getFacet(IFLKeyword.FL_ATTRIBUTE);
		fis_variable = getFacet(IFLKeyword.FL_VARIABLE);
		fis_output = getFacet(IFLKeyword.FL_OUTPUT);
	}
	
	/**
	 * @see msi.gaml.commands.AbstractCommand#privateExecuteIn(msi.gama.runtime.IScope)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override	
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
		Object attributeName = attribute.value(scope);

		if(fis_variable != null) {
			String variableName = (String) fis_variable.value(scope);
			
			Map<String,String> vars = (Map) agt.getAttribute(IFLKeyword.FL_ATT_VARIABLES);
			if(vars == null) {
				throw GamaRuntimeException.error("The FIS has not been initialized.", scope);
			}
			vars.put(variableName, attributeName.toString());
			agt.setAttribute(IFLKeyword.FL_ATT_VARIABLES, vars);			
		}
		
		if(fis_output != null) {
			String outputName = (String) fis_output.value(scope);

			Map<String,String> outs = (Map) agt.getAttribute(IFLKeyword.FL_ATT_OUTPUTS);
			if(outs == null) {
				throw GamaRuntimeException.error("The FIS has not been initialized.", scope);
			}
			outs.put(outputName, attributeName.toString());
			agt.setAttribute(IFLKeyword.FL_ATT_OUTPUTS, outs);			
		}
		
		return null; 
	}
}
