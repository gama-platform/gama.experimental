/*******************************************************************************************************
 *
 * PredicateType.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.experimental.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.experimental.constants.MCPConstants;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

/**
 * The Class PredicateType.
 */
@type (
		name = "tool_provider",
		id = ToolProviderType.id,
		wraps = { ToolProvider.class },
		concept = { IConcept.TYPE, MCPConstants.LLM_MODEL })
@doc ("represents a tool provider that is in charge of executing a GAMA action or an external tool when it is invoked by the assistant during a conversation")
public class ToolProviderType extends GamaType<ToolProvider> {

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 146657;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object as a tool provider")
	public ToolProvider cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof ToolProvider p) return p;
		
		return null; 
	}

	@Override
	public ToolProvider getDefault() { return null; }

	@Override
	public ToolProvider deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return null;
	}

}
