package gama.experimental.argumentation.types;

import gama.core.metamodel.agent.IAgent;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMap;
import gama.core.util.GamaMapFactory;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;


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
		} else if (obj instanceof GamaMap) {
			GamaMap m = (GamaMap) obj;
			GamaArgument arg = new GamaArgument(
					m.containsKey("id") ? (String)m.get("id"): "",
					m.containsKey("option") ? (String)m.get("option"): "",
					m.containsKey("conclusion") ?(String) m.get("conclusion"): "0",
					m.containsKey("statement") ? (String)m.get("statement"): "",
					m.containsKey("rationale") ? (String)m.get("rationale"): "",
					m.containsKey("criteria") ? (GamaMap<String, Double>)m.get("criteria"): GamaMapFactory.create(),
					(IAgent)m.get("actor"),
					m.containsKey("source_type") ? (String)m.get("source_type"): "");
			return arg;
		}
		return null;
	}

	@Override
	public GamaArgument getDefault() {
		return null;
	}

}
