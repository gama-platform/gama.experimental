package miat.gaml.extensions.argumentation.types;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
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
		} else if (obj instanceof GamaMap) {
			GamaMap m = (GamaMap) obj;
			GamaArgument arg = new GamaArgument(
					m.containsKey("id") ? (String)m.get("id"): "",
					m.containsKey("option") ? (String)m.get("option"): "",
					m.containsKey("conclusion") ?(String) m.get("conclusion"): "0",
					m.containsKey("statement") ? (String)m.get("statement"): "",
					m.containsKey("rationale") ? (String)m.get("rationale"): "",
					m.containsKey("criteria") ? (GamaMap<String, Double>)m.get("criteria"): (GamaMap<String, Double>) GamaMapFactory.create(),
					(IAgent)m.get("actor"),
					m.containsKey("sourceType") ? (String)m.get("sourceType"): "");
			return arg;
		}
		return null;
	}

	@Override
	public GamaArgument getDefault() {
		return null;
	}

}
