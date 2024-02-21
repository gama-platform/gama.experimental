/*******************************************************************************************************
 *
 * GenerateStatement.java, in espacedev.gaml.extensions.genstar, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package espacedev.gaml.extensions.genstar.statement;

import java.util.List;
import java.util.Map;

import espacedev.gaml.extensions.genstar.generator.IGenstarGenerator;
import espacedev.gaml.extensions.genstar.statement.GenerateStatement.GenerateValidator;
import espacedev.gaml.extensions.genstar.utils.GenStarConstant;
import espacedev.gaml.extensions.genstar.utils.GenStarGamaUtils;
import gama.core.common.interfaces.IKeyword;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.simulation.SimulationPopulation;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.types.SpeciesConstantExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.Facets;
import gama.gaml.statements.IStatement;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gaml.compiler.gaml.Facet;

import one.util.streamex.StreamEx;

/**
 * The Class GenerateStatement.
 */
@symbol (
		name = IKeyword.GENERATE,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		with_args = true,
		category = { IOperatorCategory.GENSTAR },
		concept = { IConcept.SPECIES },
		remote_context = true)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
						name = IKeyword.SPECIES,
						type = { IType.SPECIES, IType.AGENT },
						optional = true,
						doc = @doc ("The species of the agents to be created.")),
				@facet (
						name = IKeyword.FROM,
						type = IType.NONE,
						optional = false,
						doc = @doc (
								value = """
										To specify the input data used to inform the generation process. Various data input can be used:
										 * list of csv_file: can be aggregated or micro data
										 * matrix: describe the joint distribution of two attributes
										 * genstar generator: a dedicated gaml type to enclose various genstar options all in one""")),
				@facet (
						/*
						 * make those attributes like in csv map to directly recognize species' attributes rather than
						 * use string with potential mispells
						 */
						name = GenStarConstant.GSATTRIBUTES,
						type = { IType.MAP },
						optional = false,
						doc = @doc ("To specify the explicit link between agent attributes and file based attributes")),
				@facet (
						name = IKeyword.NUMBER,
						type = IType.INT,
						optional = true,
						doc = @doc (
								value = """
										To specify the number of created agents interpreted as an int value.
										If facet is ommited or value is 0 or less, generator will treat data used in the 'from' facet as contingencies
										(i.e. a count of entities) and infer a number to generate (if distribution is used, then only one entity will be created""")),
				@facet (
						name = GenStarConstant.GSGENERATOR,
						type = { IType.STRING },
						optional = true,
						doc = @doc ("To specify the type of generator you want to use: as of now there is only DS (or DirectSampling) available")) },
		omissible = IKeyword.SPECIES)

@doc (
		value = "Allows to create a synthetic population of agent from a set of given rules",
		usages = { @usage (
				value = "The synthax to create a minimal synthetic population from aggregated file is:",
				examples = { @example (
						value = """
								generate species:people number: 10000
								from:[csv_file("../includes/Age & Sexe-Tableau 1.csv",";")]
								attributes:["Age"::["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans",
								"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans",
								"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans",
								"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"],
								"Sexe"::["Hommes", "Femmes"]];""",
						isExecutable = false) }) })
@validator (GenerateValidator.class)
public class GenerateStatement extends AbstractStatementSequence implements IStatement.WithArgs {

	/** The init. */
	private Arguments init;

	/** The algorithm. */
	private final IExpression from;
	
	/** The number. */
	private final IExpression number;
	
	/** The species. */
	private final IExpression species;
	
	/** The attributes. */
	private final IExpression attributes;
	
	/** The algorithm. */
	private final IExpression algorithm;

	/** The returns. */
	private final String returns;

	/** The sequence. */
	private final RemoteSequence sequence;

	/**
	 * Instantiates a new generate statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public GenerateStatement(final IDescription desc) {
		super(desc);
		returns = getLiteral(IKeyword.RETURN);
		from = getFacet(IKeyword.FROM);
		number = getFacet(IKeyword.NUMBER);
		species = getFacet(IKeyword.SPECIES);

		attributes = getFacet(GenStarConstant.GSATTRIBUTES);
		algorithm = getFacet(GenStarConstant.GSGENERATOR);

		sequence = new RemoteSequence(description);
		sequence.setName("commands of generate ");
		setName(IKeyword.GENERATE);
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IList<? extends IAgent> privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// First, we compute the number of agents to create
		final Integer max = number == null ? null : Cast.asInt(scope, number.value(scope));
		if (from == null && max != null && max <= 0) return GamaListFactory.EMPTY_LIST;

		// Next, we compute the species to instantiate
		final IPopulation pop = findPopulation(scope);
		// A check is made in order to address issues #2621 and #2611
		if (pop == null || pop.getSpecies() == null)
			throw GamaRuntimeException.error("Impossible to determine the species of the agents to generate", scope);
		checkPopulationValidity(pop, scope);

		// We grab whatever initial data are input
		final List<Map<String, Object>> inits = GamaListFactory.create(Types.MAP, max == null ? 10 : max);
		final Object source = from.value(scope);

		// Only one generator according to data input type (type of the source Object)
		StreamEx.of(GenStarGamaUtils.getGamaGenerator()).findFirst(g -> g.sourceMatch(scope, source))
				.orElseThrow(IllegalArgumentException::new).generate(scope, inits, max, source, attributes.value(scope),
						algorithm == null ? null : algorithm.value(scope), init, this);

		// and we create and return the agent(s)
		final IList<? extends IAgent> agents = pop.createAgents(scope, inits.size(), inits, false, false, sequence);
		if (returns != null) { scope.setVarValue(returns, agents); }
		return agents;
	}

	/**
	 * Fill with user init.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	public void fillWithUserInit(final IScope scope, final Map values) {
		if (init == null) return;
		scope.pushReadAttributes(values);
		try {
			init.forEachFacet((k, v) -> {
				values.put(k, v.getExpression().value(scope));
				return true;
			});
		} finally {
			scope.popReadAttributes();
		}
	}

	// ------------------------------------------------------------------------------------------------ //
	// ------------------------------------------------------------------------------------------------ //
	// //
	// Copy pasted from the CreateStatement way to init agents //
	// //
	// ------------------------------------------------------------------------------------------------ //
	// ------------------------------------------------------------------------------------------------ //

	/**
	 * Find population.
	 *
	 * @param scope
	 *            the scope
	 * @return the i population
	 */
	@SuppressWarnings ("rawtypes")
	private IPopulation findPopulation(final IScope scope) {
		final IAgent executor = scope.getAgent();
		if (species == null) return executor.getPopulationFor(description.getSpeciesContext().getName());
		ISpecies s = Cast.asSpecies(scope, species.value(scope));
		if (s == null) {// A last attempt in order to fix #2466
			final String potentialSpeciesName = species.getDenotedType().getSpeciesName();
			if (potentialSpeciesName != null) { s = scope.getModel().getSpecies(potentialSpeciesName); }
		}
		if (s == null) throw GamaRuntimeException.error(
				"No population of " + species.serializeToGaml(false) + " is accessible in the context of " + executor + ".",
				scope);
		return executor.getPopulationFor(s);
	}

	/**
	 * A check made in order to address issues #2621 and #2611
	 *
	 * @param pop
	 * @param scope
	 * @throws GamaRuntimeException
	 */
	@SuppressWarnings ("rawtypes")
	private void checkPopulationValidity(final IPopulation pop, final IScope scope) throws GamaRuntimeException {
		if (pop instanceof SimulationPopulation && !(scope.getAgent() instanceof ExperimentAgent))
			throw GamaRuntimeException.error("Simulations can only be created within experiments", scope);
		final SpeciesDescription sd = pop.getSpecies().getDescription();
		final String error = sd.isAbstract() ? "abstract" : sd.isMirror() ? "a mirror" : sd.isBuiltIn() ? "built-in"
				: sd.isGrid() ? "a grid" : null;
		if (error != null) throw GamaRuntimeException.error(sd.getName() + "is " + error + " and cannot be instantiated.", scope);
	}

	/**
	 * make the validator coherent with 'must contains' facets
	 *
	 * @author kevinchapuis
	 *
	 */
	public static class GenerateValidator implements IDescriptionValidator<StatementDescription> {

		@SuppressWarnings ({ "unchecked", "rawtypes" })
		@Override
		public void validate(final StatementDescription description) {
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

			if (species instanceof SpeciesConstantExpression) {
				final boolean abs = sd.isAbstract();
				final boolean mir = sd.isMirror();
				final boolean gri = sd.isGrid();
				final boolean bui = sd.isBuiltIn();
				if (abs || mir || gri || bui) {
					final String p = abs ? "abstract" : mir ? "a mirror" : gri ? "a grid" : bui ? "built-in" : "";
					description.error(sd.getName() + " is " + p + " and cannot be instantiated", WRONG_TYPE, SPECIES);
					return;
				}
			} else if (!(sd instanceof ModelDescription)) {
				description.info(
						"The actual species will be determined at runtime. This can lead to errors if it cannot be instantiated",
						WRONG_TYPE, SPECIES);
			}

			if (sd instanceof ModelDescription && !(description.getSpeciesContext() instanceof ExperimentDescription)) {
				description.error("Simulations can only be created within experiments", WRONG_CONTEXT, SPECIES);
				return;
			}

			final SpeciesDescription callerSpecies = description.getSpeciesContext();
			final SpeciesDescription macro = sd.getMacroSpecies();
			if (macro == null) {
				description.error("The macro-species of " + species + " cannot be determined");
				return;
				// hqnghi special case : create instances of model from
				// model
			}
			if (macro instanceof ModelDescription && callerSpecies instanceof ModelDescription) {

				// end-hqnghi
			} else if (callerSpecies != macro && !callerSpecies.hasMacroSpecies(macro)
					&& !callerSpecies.hasParent(macro)) {
				description.error(
						"No instance of " + macro.getName() + " available for creating instances of " + sd.getName());
				return;
			}

			final IExpression exp = description.getFacetExpr(FROM);
			if (exp != null) {
				final IType type = exp.getGamlType();

				if (type.id() != 938373948) {
					boolean found = false;
					List<IType> types = StreamEx.of(GenStarGamaUtils.getGamaGenerator())
							.map(IGenstarGenerator::sourceType).toList();
					for (final IType genType : types) {
						found = genType.isAssignableFrom(type);
						if (found) { break; }
					}
					if (type == Types.MATRIX) {
						// TODO verify that x,y matrix match possible attributes values
					}
					if (!found) {
						description.warning(
								"Facet 'from' expects an expression with one of the following types: " + types,
								WRONG_TYPE, FROM);
					}
				}
			}
			
			final Arguments facets = description.getPassedArgs();
			facets.forEachFacet((s, e) -> {
				boolean error = !sd.isExperiment() && !sd.hasAttribute(s);
				if (error) {
					description.error("Attribute " + s + " is not defined in species " + species.getName(), UNKNOWN_VAR);
				}
				return !error;
			});

		}

	}

	@Override
	public void setFormalArgs(final Arguments args) { init = args; }


	@Override
	public void enterScope(final IScope scope) {
		if (returns != null) { scope.addVarWithValue(returns, null); }
		super.enterScope(scope);
	}

}
