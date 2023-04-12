package espacedev.gaml.extensions.genstar.statement;

import static msi.gama.common.interfaces.IKeyword.FROM;
import static msi.gama.common.interfaces.IKeyword.SPECIES;
import static msi.gama.precompiler.ISymbolKind.SEQUENCE_STATEMENT;

import java.util.Arrays;

import espacedev.gaml.extensions.genstar.localisation.IGenstarLocaliser;
import espacedev.gaml.extensions.genstar.statement.LocaliseStatement.LocaliseValidator;
import espacedev.gaml.extensions.genstar.type.GamaPopGeneratorType;
import espacedev.gaml.extensions.genstar.utils.GenStarConstant;
import espacedev.gaml.extensions.genstar.utils.GenStarGamaUtils;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gaml.compilation.IDescriptionValidator;
import msi.gaml.compilation.annotations.validator;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.descriptions.StatementDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.AbstractStatementSequence;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.IStatement.WithArgs;
import msi.gaml.statements.RemoteSequence;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import one.util.streamex.StreamEx;

@symbol (
		name = GenStarConstant.GSLOCALISE,
		kind = SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		with_args = true,
		category = { IOperatorCategory.GENSTAR },
		concept = { IConcept.AGENT_LOCATION, IConcept.SPECIES },
		remote_context = true)
@inside (
		kinds = { SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
						name = SPECIES,
						type = { IType.SPECIES, IType.AGENT },
						optional = false,
						doc = @doc ("The species of the agents to be localised.")),
				@facet (
						name = FROM,
						type = IType.NONE,
						optional = false,
						doc = @doc (
								value = """
										To specify the input data used to inform the localisation process. Various data input can be used: <br/>
										 * a list of geometry <br/>
										 * a shapefile or a rasterfile
										 """)),
				@facet (
						name = GenStarConstant.GSFEATURE,
						type = { IType.STRING },
						optional = true,
						doc = @doc ("To specify the attribute of a geometry to be used to inform localisation process")) })
@doc (
		value = "Allows to localise a set of agent within a given set of geometries",
		usages = { @usage (
				value = "The synthax to monitor the simplest localisation process",
				examples = { @example (
						value = """
								localise species:people from:[shape_file("../includes/minimalexample.shp")];
								""",
						isExecutable = false) }) })
@validator (LocaliseValidator.class)
public class LocaliseStatement extends AbstractStatementSequence implements WithArgs {

	/** The init. */
	private Arguments init;
	
	// ----------------
	
	/** The algorithm. */
	private final IExpression from;
	
	/** The species. */
	private final IExpression species;
	
	/** The feature within a geometry to custom the localisation process **/
	private final IExpression feature;
	
	
	// -----------------
	
	/** The sequence. */
	private final RemoteSequence sequence;
	
	public LocaliseStatement(IDescription desc) {
		super(desc);
		this.from = getFacet(IKeyword.FROM);
		this.species = getFacet(IKeyword.SPECIES);
		this.feature = getFacet(GenStarConstant.GSFEATURE);
				
		sequence = new RemoteSequence(description);
		sequence.setName("commands for the localisation");
		setName(GenStarConstant.GSLOCALISE);
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IList<? extends IAgent> privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// Just retrieve the population of agent to be localised
		// TODO : see if we can transfer this to any geometry
		IPopulation pop = null;
		if (species == null) pop = scope.getAgent().getPopulationFor(description.getSpeciesContext().getName());
		else {
			ISpecies s = Cast.asSpecies(scope, species.value(scope));
			if (s == null) {// A last attempt in order to fix #2466
				final String potentialSpeciesName = species.getDenotedType().getSpeciesName();
				if (potentialSpeciesName != null) { s = scope.getModel().getSpecies(potentialSpeciesName); }
			}
			if (s == null) throw GamaRuntimeException.error(
					"No population of " + species.serialize(false) + " is accessible in the context of " + scope.getAgent() + ".",
					scope);
			pop = scope.getAgent().getPopulationFor(s);
		}
		
		// TODO select among several localiser when they will be defined
		IGenstarLocaliser genl = GenStarGamaUtils.getGamaLocaliser()[0]; 
		
//		StreamEx.of(GenStarGamaUtils.getGamaLocaliser()).findFirst(g -> g.sourceMatch(scope, source))
//				.orElseThrow(IllegalArgumentException::new).generate(scope, inits, max, source, attributes.value(scope),
//						algorithm == null ? null : algorithm.value(scope), init, this);

		// TODO fill in the proper set of attributes to localise population
		genl.localise(scope, pop, from.value(scope), this);
		
		return pop;
	}
	
	@Override
	public void setFormalArgs(Arguments args) { init = args; }

	public static class LocaliseValidator implements IDescriptionValidator<StatementDescription> {

		@Override
		public void validate(StatementDescription description) {
			
			// ****** 
			// Minimal check on species - not taking into account all the specific cases copy/past from CreateStatement 
			
			final IExpression species = description.getFacetExpr(SPECIES);
			// If the species cannot be determined, issue an error and leave validation
			if (species == null) {
				description.error("The species is not found", UNKNOWN_SPECIES, SPECIES);
				return;
			}

			final SpeciesDescription sd = species.getGamlType().getDenotedSpecies();
			if (sd == null) {
				description.error("The species to instantiate cannot be determined", UNKNOWN_SPECIES, SPECIES,
						species.getName());
				return;
			}
			
			// ******
			// Check on the various possible cases of From input types
			final IExpression exp = description.getFacetExpr(FROM);
			if (exp != null) {
				final IType type = exp.getGamlType();

				if (type.id() != 938373948) { // type id 938373948 is the one of the IValue GamaPopGenerator
					boolean found = false;
					if (type == Types.LIST) {
						// TODO verify if the list is actually a list of geometries
						found = true;
					}
					if (type == Types.FILE) {
						// TODO verify if the file is actually a shape or raster file
						found = true;
					}
					if (!found) {
						description.warning(
								"Facet 'from' expects an expression with one of the following types: " 
										+ Arrays.asList(Types.LIST,Types.FILE,GamaPopGeneratorType.GENERATORNAME),
								WRONG_TYPE, FROM);
					}
				}
			}
			
		}
		
	}
	
}
