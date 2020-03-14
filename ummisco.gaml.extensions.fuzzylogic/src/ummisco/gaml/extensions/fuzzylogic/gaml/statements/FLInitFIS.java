package ummisco.gaml.extensions.fuzzylogic.gaml.statements;

import java.util.Map;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.file.GamaFile;
import msi.gaml.compilation.annotations.serializer;
import msi.gaml.compilation.annotations.validator;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.StatementDescription;
import msi.gaml.descriptions.SymbolDescription;
import msi.gaml.descriptions.SymbolSerializer;
import msi.gaml.expressions.IExpression;
import msi.gaml.statements.AbstractStatement;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

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
