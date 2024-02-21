package ummisco.gaml.extensions.fuzzylogic.gaml.statements;

import java.util.Map;

import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.file.GamaFile;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

import net.sourceforge.jFuzzyLogic.FIS;

import ummisco.gaml.extensions.fuzzylogic.gaml.statements.FLBindStatement.FuzzyLogicBindStatementSerializer;
import ummisco.gaml.extensions.fuzzylogic.utils.IFLKeyword;
import ummisco.gaml.extensions.fuzzylogic.utils.validator.FuzzyLogicStatementValidator;
import ummisco.gaml.extensions.fuzzylogic.gaml.statements.FLInitFIS.FLInitFISValidator;

@symbol (
		name = IFLKeyword.FL_INIT_FIS,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IFLKeyword.FL_CONCEPT })
@doc (value = "`" + IFLKeyword.FL_INIT_FIS + "` allows to init a FIS from a file.")
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT})
@facets (
		value = { 
			@facet (
				name = IFLKeyword.FL_FROM,
				type = { IType.FILE },
				optional = false,
				doc = { @doc ("the file containing the FIS description") })
		}, 
		omissible = IFLKeyword.FL_FROM)
@validator (FLInitFISValidator.class)
@serializer (FuzzyLogicBindStatementSerializer.class)
public class FLInitFIS extends AbstractStatement {

	public static class FLInitFISValidator extends FuzzyLogicStatementValidator {
		@Override
		public void validate(StatementDescription description) {
			super.validate(description);
			// TODO Auto-generated method stub	
		}
		
	}

	public static class FuzzyLogicBindStatementSerializer extends SymbolSerializer<StatementDescription> {

		@Override
		protected void serialize(final SymbolDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		//TODO TO COMPLETE
			sb.append(IFLKeyword.FL_INIT_FIS).append(";");
		}
	}	
	
	protected IExpression file;	
	
	public FLInitFIS(IDescription desc) {
		super(desc);
		file = getFacet(IFLKeyword.FL_FROM);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
		final IAgent agt = scope.getAgent();
		
		// Initialise the FIS 
		GamaFile<?, ?> fclFile = (GamaFile<?, ?>) file.value(scope);
		FIS f = FIS.load(fclFile.getPath(scope),true) ;
		agt.setAttribute(IFLKeyword.FL_ATT_FIS, f);
		
		// Initialise the variables and outputs binding maps
		Map<String,String> vars = GamaMapFactory.create(Types.STRING, Types.STRING);
		agt.setAttribute(IFLKeyword.FL_ATT_VARIABLES, vars);			

		Map<String,String> outputs = GamaMapFactory.create(Types.STRING, Types.STRING);
		agt.setAttribute(IFLKeyword.FL_ATT_OUTPUTS, outputs);		
		return null;
	}

}
