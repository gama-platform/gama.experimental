package miat.gaml.extensions.argumentation.types;

import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@type(name = "argument", id = GamaArgumentType.id, wraps = { GamaArgument.class }, concept = { IConcept.TYPE, "Argumentation" })
public class GamaArgumentType extends GamaType<GamaArgument> {

	public final static int id = IType.AVAILABLE_TYPES + 175769875;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public GamaArgument cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof GamaArgument) {
			return (GamaArgument) obj;
		}
		return null;
	}

	@Override
	public GamaArgument getDefault() {
		return null;
	}

}
