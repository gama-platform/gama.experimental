package across.gaml.extensions.imageanalysis.operators;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.shape.GamaPoint;
//import miat.gaml.extensions.argumentation.types.GamaArgument;
//import miat.gaml.extensions.argumentation.types.GamaArgumentType;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.test;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.GamaMatrixType;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@type(name = "Lego", id = LegoType.id, wraps = { Lego.class }, concept = { IConcept.TYPE, "Lego" })
public class LegoType extends GamaType<Lego> {
//	public final static int id = IType.AVAILABLE_TYPES + 175769875;
	public final static int id = IType.AVAILABLE_TYPES + 190720223;

	@Override
	public Lego getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canCastToConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Lego cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		if(obj instanceof Lego) {
			System.out.println(((Lego)obj).getVal());
			return (Lego) obj;
		}
		if(obj instanceof IMatrix) {
			System.out.println(((Lego)obj).getVal());
			return (Lego) obj;
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
		value = "lego_with",
		content_type = ITypeProvider.SECOND_CONTENT_TYPE_OR_TYPE,
		can_be_const = true,
		category = { IOperatorCategory.CASTING },
		concept = { IConcept.CAST, IConcept.CONTAINER })
@doc (
		value = "creates a lego with a size provided by the first operand, and filled with the second operand",
		comment = "Note that both components of the right operand point should be positive, otherwise an exception is raised.",
		see = { IKeyword.MATRIX, "as_matrix" })
@test ("{2,2} matrix_with (1) = matrix([1,1],[1,1])")
public static Lego matrix_with(final IScope scope, final String typeName, final GamaPoint size, final IExpression init) {
	if (size == null) throw GamaRuntimeException.error("A nil size is not allowed for Lego", scope);
	
	return new Lego(scope, typeName,(int)size.x, (int)size.y, init);
}
	
}


//@type(name = "argument", id = GamaArgumentType.id, wraps = { GamaArgument.class }, concept = { IConcept.TYPE, "Argumentation" })
//public class GamaArgumentType extends GamaType<GamaArgument> {
//
//	public final static int id = IType.AVAILABLE_TYPES + 175769875;
//
//	@Override
//	public boolean canCastToConst() {
//		return true;
//	}
//
//	@Override
//	public GamaArgument cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
//		if (obj instanceof GamaArgument) {
//			return (GamaArgument) obj;
//		} else if (obj instanceof GamaMap) {
//			GamaMap m = (GamaMap) obj;
//			GamaArgument arg = new GamaArgument(
//					m.containsKey("id") ? (String)m.get("id"): "",
//					m.containsKey("option") ? (String)m.get("option"): "",
//					m.containsKey("conclusion") ?(String) m.get("conclusion"): "0",
//					m.containsKey("statement") ? (String)m.get("statement"): "",
//					m.containsKey("rationale") ? (String)m.get("rationale"): "",
//					m.containsKey("criteria") ? (GamaMap<String, Double>)m.get("criteria"): GamaMapFactory.create(),
//					(IAgent)m.get("actor"),
//					m.containsKey("source_type") ? (String)m.get("source_type"): "");
//			return arg;
//		}
//		return null;
//	}
//
//	@Override
//	public GamaArgument getDefault() {
//		return null;
//	}
//
//}
