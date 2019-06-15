package gaml.types;

import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@type(name = "matlab_engine", 
	id = GamaMatlabEngineType.id, 
	wraps = { GamaMatlabEngine.class }, 
	concept = { IConcept.TYPE }, 
	doc = @doc("The matlab_engine type defined in the MATLAB plugin"))
public class GamaMatlabEngineType extends GamaType<GamaMatlabEngine>{
	public final static int id = IType.AVAILABLE_TYPES + 424242;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc("matlab_engine cast will always return nil, except for a matlab_engine")
	public GamaMatlabEngine cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof GamaMatlabEngine) return (GamaMatlabEngine) obj;
		return null;
	}

	@Override
	public GamaMatlabEngine getDefault() {
		return null;
	}

}
