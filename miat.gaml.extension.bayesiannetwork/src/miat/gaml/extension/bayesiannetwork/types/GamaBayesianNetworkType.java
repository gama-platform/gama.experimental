package miat.gaml.extension.bayesiannetwork.types;

import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

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
