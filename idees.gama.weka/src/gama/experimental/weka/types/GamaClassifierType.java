package gama.experimental.weka.types;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

@type(name = "classifier", id = GamaClassifierType.id, wraps = { GamaClassifier.class }, concept = { IConcept.TYPE, IConcept.STATISTIC })
public class GamaClassifierType extends GamaType<GamaClassifier> {

	public final static int id = IType.AVAILABLE_TYPES + 54736255;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public GamaClassifier cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof GamaClassifier) {
			return (GamaClassifier) obj;
		}
		return null;
	}

	@Override
	public GamaClassifier getDefault() {
		return null;
	}

}
