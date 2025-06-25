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
 * The Class Memory.
 */
@type (
		name = "memory",
		id = MemoryType.id,
		wraps = { Memory.class },
		concept = { IConcept.TYPE, MCPConstants.LLM_MODEL })
@doc ("represents a memory linked to a chat model — the memory will be used to construct the message sent to the chat model")
public class MemoryType extends GamaType<Memory> {

	/** The Constant id. */
	public final static int id = IType.AVAILABLE_TYPES + 3427183;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object as a memory")
	public Memory cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Memory p) return p;
		
		return null; 
	}

	@Override
	public Memory getDefault() { return null; }

	@Override
	public Memory deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return null;
	}

}
