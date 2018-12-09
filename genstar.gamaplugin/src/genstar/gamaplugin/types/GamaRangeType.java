package genstar.gamaplugin.types;

import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gaml.operators.Cast;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@type(name = "gen_range", id = GamaRangeType.id, wraps = { GamaRange.class }, concept = { IConcept.TYPE }, doc = @doc("The range type defined in the genstar plugin"))
public class GamaRangeType extends GamaType<GamaRange>{
	public final static int id = IType.AVAILABLE_TYPES + 3524246;

	@Override
	public boolean canCastToConst() {
		return true;
	}


	@Override
	@SuppressWarnings({ "rawtypes"})	
	public GamaRange cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof GamaRange) return (GamaRange) obj;
		if (obj instanceof GamaList) {
			GamaList list = (GamaList) obj;
			if (list.size() == 2) {
				return new GamaRange(Cast.asFloat(scope, list.get(0)),Cast.asFloat(scope, list.get(1)));
			} 
			return null;
		}
		if (obj instanceof String) {
			String[] list = ((String) obj).split("->");
			if (list.length == 2) {
				return new GamaRange(Cast.asFloat(scope, list[0]),Cast.asFloat(scope, list[1]));
			} 
			return null;
		}
		// if(obj instanceof Gama)
		return null;
	}

	@Override
	public GamaRange getDefault() {
		return null;
	}

}
