package across.gaml.extensions.imageanalysis.types;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.type;
//import miat.gaml.extensions.argumentation.types.GamaArgument;
//import miat.gaml.extensions.argumentation.types.GamaArgumentType;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@type(name = "pattern", id = PatternBlockType.id, wraps = { PatternBlock.class }, concept = { IConcept.TYPE, "pattern" })
public class PatternBlockType extends GamaType<PatternBlock> {
	public final static int id = IType.AVAILABLE_TYPES + 4563231;

	@Override
	public PatternBlock getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canCastToConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PatternBlock cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		if(obj instanceof PatternBlock) {
			return (PatternBlock) obj;
		}

		return null;
	}


/**
 * Matrix with.
 *
 * @param scope
 *            the scope 
 * @param size
 *            the size
 * @param init
 *            the init
 * @return the i matrix
 */
@operator (
		value = "pattern_with",
		content_type = ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE,
		can_be_const = true,
		category = { IOperatorCategory.CASTING },
		concept = { IConcept.CAST, IConcept.CONTAINER })
@doc (
		value = "creates a pattern block with a size provided by the first operand, and filled with the second operand",
		comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.",
		see = { IKeyword.MATRIX, "as_matrix" })
public static PatternBlock matrix_with(final IScope scope, final String typeName, final GamaPoint size, final IExpression init) {
	if (size == null) throw GamaRuntimeException.error("A nil size is not allowed for patterns", scope);
	
	return new PatternBlock(scope, typeName,(int)size.x, (int)size.y, init);
}

@operator (
		value = "with_matrix",
		content_type = ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE,
		can_be_const = true,
		category = { IOperatorCategory.CASTING },
		concept = { IConcept.CAST, IConcept.CONTAINER })
@doc (
		value = "creates a pattern block with a size provided by the first operand, and filled with the second operand",
		comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.",
		see = { IKeyword.MATRIX, "as_matrix" })
public static PatternBlock matrix_with(final IScope scope, final PatternBlock pattern, final GamaIntMatrix matrix ) {
	pattern.setMatrix(matrix);
	return pattern;
}
	
}

