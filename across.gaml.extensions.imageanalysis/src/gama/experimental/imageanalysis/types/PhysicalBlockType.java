package gama.experimental.imageanalysis.types;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.matrix.GamaIntMatrix;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;


@type(name = "block", id = PhysicalBlockType.id, wraps = { PhysicalBlock.class }, concept = { IConcept.TYPE, "block" })
public class PhysicalBlockType extends GamaType<PhysicalBlock> {
//	public final static int id = IType.AVAILABLE_TYPES + 175769875;
	public final static int id = IType.AVAILABLE_TYPES + 190720223;

	@Override
	public PhysicalBlock getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canCastToConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PhysicalBlock cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		if(obj instanceof PhysicalBlock) {
			return (PhysicalBlock) obj;
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
		value = "block_with",
		content_type = ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE,
		can_be_const = true,
		category = { IOperatorCategory.CASTING },
		concept = { IConcept.CAST, IConcept.CONTAINER })
@doc (
		value = "creates a block with a size provided by the first operand, and filled with the second operand",
		comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.",
		see = { IKeyword.MATRIX, "as_matrix" })
public static PhysicalBlock matrix_with(final IScope scope, final String typeName, final GamaPoint size, final IExpression init, final IShape shape, final boolean parallel) {
	if (size == null) throw GamaRuntimeException.error("A nil size is not allowed for blocks", scope);
	
	return new PhysicalBlock(scope, typeName,(int)size.x, (int)size.y, init, shape, parallel);
}
	
}

