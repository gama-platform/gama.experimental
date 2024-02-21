/*******************************************************************************************************
 *
 * GamaPopGeneratorType.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package espacedev.gaml.extensions.genstar.type;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.runtime.IScope;
import gama.gaml.types.GamaType;

/**
 * The Class GamaPopGeneratorType.
 */
@type (
		name = GamaPopGeneratorType.GENERATORNAME,
		id = 938373948,
		wraps = { GamaPopGenerator.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE },
		doc = { @doc ("Represents a population generator that can be used to create agents") })
public class GamaPopGeneratorType extends GamaType<GamaPopGenerator> {

	public static final String GENERATORNAME = "gen_population_generator"; 
	
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		this.init(104, 938373948, "population_generator", GamaPopGenerator.class);
	}

	@Override
	@doc ("Cast any gaml variable into a GamaPopGenerator used in generation process")
	public GamaPopGenerator cast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj instanceof GamaPopGenerator gpg) return gpg;
		return new GamaPopGenerator();
	}

	@Override
	public GamaPopGenerator getDefault() { return null; }

}
