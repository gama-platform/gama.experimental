package espacedev.gaml.extensions.genstar.statement;

import static gama.annotations.precompiler.ISymbolKind.SEQUENCE_STATEMENT;

import espacedev.gaml.extensions.genstar.localisation.IGenstarLinker;
import espacedev.gaml.extensions.genstar.statement.SpatialLinkerStatement.LocaliseValidator;
import espacedev.gaml.extensions.genstar.utils.GenStarConstant;
import espacedev.gaml.extensions.genstar.utils.GenStarGamaUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IStatement.WithArgs;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;

@symbol (
		name = GenStarConstant.GSLINK,
		kind = SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		with_args = true,
		category = { IOperatorCategory.GENSTAR },
		concept = { IConcept.AGENT_LOCATION, IConcept.SPECIES },
		remote_context = true)
@inside (
		kinds = { ISymbolKind.BEHAVIOR,  SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
						name = GenStarConstant.ENTITIES,
						type = IType.CONTAINER ,
						optional = false,
						doc = @doc ("The species of the agents to be linked.")),
				 @facet (
							name = GenStarConstant.NESTS,
							type = IType.CONTAINER ,
							optional = false,
							doc = @doc ("The species of the agents linked with the species.")),
				
				 @facet (
							name = GenStarConstant.NESTATTRIBUTES,
							type = IType.STRING,
							optional = false,
							doc = @doc (
									value = """
											To specify the attribute of the nest
											 """)),
				
				@facet (
						name = GenStarConstant.DISTRIBUTION,
						type = IType.STRING,
						optional = true,
						doc = @doc (
								value = """
										The type of distribution to use ('area', 'uniform',)
										 """)),
				 @facet (
							name = GenStarConstant.PARAMETERS,
							type = IType.MAP ,
							optional = true,
							doc = @doc ("The parameters of the linker"))})
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
public class SpatialLinkerStatement extends AbstractStatementSequence implements WithArgs {
 
	
	/** The nests. */
	private final IExpression nests;
	
	
	/** The distribution. */
	private final IExpression distribution;
	
	/** The species. */
	private final IExpression entities;
		
	/** The nest attibute. */
	private final IExpression nestAttribute;
	

	/** The parameters of the linker. */
	private final IExpression parameters;
	
	// -----------------
	
	/** The sequence. */
	private final RemoteSequence sequence;
	
	public SpatialLinkerStatement(IDescription desc) {
		super(desc);
		this.nests = getFacet(GenStarConstant.NESTS);
		this.entities = getFacet(GenStarConstant.ENTITIES);
		this.distribution = getFacet(GenStarConstant.DISTRIBUTION);
		this.nestAttribute = getFacet(GenStarConstant.NESTATTRIBUTES);
		this.parameters = getFacet(GenStarConstant.PARAMETERS);
		
		sequence = new RemoteSequence(description);
		sequence.setName("commands for the localisation");
		setName(GenStarConstant.GSLOCALISE);
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public IList<? extends IAgent> privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// Just retrieve the population of agent to be localised
		// TODO : see if we can transfer this to any geometry
		IList<IAgent> pop = null;
		Object valSpecies = entities.value(scope);
		if (valSpecies instanceof ISpecies) {
			pop = (IList<IAgent>) ((ISpecies) valSpecies).getPopulation(scope);
		} else {
			pop = Cast.asList(scope, valSpecies);
		}
		
		IList<IShape> nest = null;
		Object valNest = nests.value(scope);
		if (valNest instanceof ISpecies) {
			nest = (IList<IShape>) ((ISpecies) valNest).getPopulation(scope);
		} else {
			nest = Cast.asList(scope, valNest);
		}
		
		// TODO select among several localiser when they will be defined
		IGenstarLinker genl = GenStarGamaUtils.getGamaLinker()[0]; 
		
//		StreamEx.of(GenStarGamaUtils.getGamaLocaliser()).findFirst(g -> g.sourceMatch(scope, source))
//				.orElseThrow(IllegalArgumentException::new).generate(scope, inits, max, source, attributes.value(scope),
//						algorithm == null ? null : algorithm.value(scope), init, this);

		// TODO fill in the proper set of attributes to localise population
		genl.link(scope, pop, nest, this);
		
		return pop;
	} 
	 
	
	
	
	public IExpression getDistribution() {
		return distribution;
	}




	public static class LocaliseValidator implements IDescriptionValidator<StatementDescription> {

		@Override
		public void validate(StatementDescription description) {
			
			// ****** 
			// Minimal check on species - not taking into account all the specific cases copy/past from CreateStatement 
			
		/*	final IExpression entities = description.getFacetExpr(ENTITIES);
			// If the species cannot be determined, issue an error and leave validation
			if (entities == null) {
				description.error("The species is not found", ENTITIES, SPECIES);
				return;
			}

			

			final IExpression nest = description.getFacetExpr(GenStarConstant.NESTS);
			// If the species cannot be determined, issue an error and leave validation
			if (nest == null) {
				description.error("The nest species is not found", UNKNOWN_SPECIES, GenStarConstant.NESTS);
				return;
			}*/
			
		}
		
	}




	public IExpression getParameters() {
		return parameters;
	}

	public IExpression getNestAttribute() {
		return nestAttribute;
	}

	public IExpression getNests() {
		return nests;
	}

	public IExpression getEntities() {
		return entities;
	}

	@Override
	public void setFormalArgs(Arguments args) {
		// TODO Auto-generated method stub
		
	}
	
}
