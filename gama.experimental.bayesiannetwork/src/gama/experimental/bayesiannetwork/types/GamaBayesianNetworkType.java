package gama.experimental.bayesiannetwork.types;

import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

@type(name = "bayesian_network", id = GamaBayesianNetworkType.id, wraps = { GamaBayesianNetwork.class }, concept = { IConcept.TYPE, "Bayesian Network" })
public class GamaBayesianNetworkType extends GamaType<GamaBayesianNetwork> {

	public final static int id = IType.AVAILABLE_TYPES + 1231029875;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public GamaBayesianNetwork cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof GamaBayesianNetwork) {
			return (GamaBayesianNetwork) obj;
		} 
		if (obj instanceof String) {
			return new GamaBayesianNetwork((String)obj);
		}
		return null;
	}

	@Override
	public GamaBayesianNetwork getDefault() {
		return null;
	}

}
