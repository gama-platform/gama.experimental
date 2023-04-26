package espacedev.gaml.extensions.genstar.statement;

import static msi.gama.precompiler.ISymbolKind.SEQUENCE_STATEMENT;

import espacedev.gaml.extensions.genstar.localisation.IGenstarLinker;
import espacedev.gaml.extensions.genstar.statement.SpatialLinkerStatement.LocaliseValidator;
import espacedev.gaml.extensions.genstar.utils.GenStarConstant;
import espacedev.gaml.extensions.genstar.utils.GenStarGamaUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gaml.compilation.IDescriptionValidator;
import msi.gaml.compilation.annotations.validator;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.StatementDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.AbstractStatementSequence;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.IStatement.WithArgs;
import msi.gaml.statements.RemoteSequence;
import msi.gaml.types.IType;

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
										 """)) })
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
	
	// -----------------
	
	/** The sequence. */
	private final RemoteSequence sequence;
	
	public SpatialLinkerStatement(IDescription desc) {
		super(desc);
		this.nests = getFacet(GenStarConstant.NESTS);
		this.entities = getFacet(GenStarConstant.ENTITIES);
		this.distribution = getFacet(GenStarConstant.DISTRIBUTION);
		this.nestAttribute = getFacet(GenStarConstant.NESTATTRIBUTES);
		
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
