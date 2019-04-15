package miat.gaml.extensions.argumentation.skills;

import java.util.Map;
import java.util.Set;

import miat.gaml.extensions.argumentation.types.GamaArgument;
import miat.gaml.extensions.argumentation.types.GamaArgumentType;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.IList;
import msi.gama.util.graph.GamaGraph;
import msi.gama.util.graph.IGraph;
import msi.gaml.descriptions.ConstantExpressionDescription;
import msi.gaml.operators.Graphs;
import msi.gaml.skills.Skill;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.IStatement;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@skill(name = "argumenting")
@vars({
	  @variable(name = "argumentation_graph", type = IType.GRAPH),
	  @variable(name = "crit_importance", type = IType.MAP)
	})
public class ArgumentingSkill extends Skill{
	static final String ARGUMENTATION_GRAPH = "argumentation_graph";
	static final String CRIT_IMPORTANCE = "crit_importance";
	
	@getter(ARGUMENTATION_GRAPH)
	public IGraph getArgGraph(final IAgent agent) {
	    return (IGraph) agent.getAttribute(ARGUMENTATION_GRAPH);
	}

	@setter(ARGUMENTATION_GRAPH)
	public void setArgGraph(final IAgent agent, final IGraph s) {
	    agent.setAttribute(ARGUMENTATION_GRAPH, s);
	}
	
	@getter(CRIT_IMPORTANCE)
	public GamaMap getCritImp(final IAgent agent) {
	    return (GamaMap) agent.getAttribute(CRIT_IMPORTANCE);
	}

	@setter(CRIT_IMPORTANCE)
	public void setCritImpo(final IAgent agent, final GamaMap s) {
	    agent.setAttribute(CRIT_IMPORTANCE, s);
	}
	
	
	@action (
			name = "evaluate_argument",
			args = {@arg (
							name = "argument",
							type = GamaArgumentType.id,
							optional = false,
							doc = @doc ("the argument to evaluate"))},
			doc = @doc (
					value = "evaluate the strength  of an argument for the agent",
					returns = "the strength of argument",
					examples = { @example ("float val <- evaluate_argument(one_argument);") }))
	public Double primEvaluateArg(final IScope scope) throws GamaRuntimeException {
		final GamaArgument a = scope.hasArg("argument")? (GamaArgument)scope.getArg("argument", GamaArgumentType.id) : null;
		double val = 0;
		IAgent ag = scope.getAgent();
		Map<String,Double> agVal = getCritImp(ag);
		for (String c : a.getCriteria().keySet()) {
			val += a.getCriteria().get(c) * agVal.get(c);
		}
		return val;
	}
	
	@action (
			name = "preferred_extension",
				args = {@arg (
							name = "graph",
							type = IType.GRAPH,
							optional = true,
							doc = @doc ("the graph to evaluate"))},
			doc = @doc (
					value = "evaluate the preferred extension of an argument graph",
					returns = "a list of list of arguments representing the preferred extension",
					examples = { @example ("list<list<argument>> results <- preferred_extension(a_graph);") }))
	public IList<IList> primComputePreferedExtension(final IScope scope) throws GamaRuntimeException {
		IList<IList<GamaArgument>> preferedExt = GamaListFactory.create();
		IGraph graph = (IGraph) scope.getArg("graph", IType.GRAPH);
		if (graph == null)graph = getArgGraph(scope.getAgent());
		GamaGraph dualGraph = new GamaGraph<>(scope, true, Types.get(GamaArgumentType.id), Types.NO_TYPE);
		for (Object v1 : graph.getVertices()) {
			for (Object v2 : graph.getVertices()) {
				if (v1 != v2) {
					dualGraph.addEdge(v1, v2);	
				}
			}
		}
		for (Object e : graph.getEdges()) {
			dualGraph.removeEdge(e);
		}
		return Graphs.getMaximalCliques(scope, dualGraph);
	}
	
	@action (
			name = "extension",
					args = {@arg (
							name = "graph",
							type = IType.GRAPH,
							optional = true,
							doc = @doc ("the graph to evaluate"))},
			
					doc = @doc (
							value = "evaluate the extension of an argument graph",
							returns = "a list of list of arguments representing the extension",
							examples = { @example ("list<list<argument>> results <- extension();") }))
	public IList<IList> primComputeExtension(final IScope scope) throws GamaRuntimeException {
		return primComputePreferedExtension(scope);
	}
	
	@action (
			name = "update_graph",
			doc = @doc (
					value = "update the weight of the argumentation graph",
					examples = { @example ("do update_graph;") }))
	public void primUpdateArgumentationGraph(final IScope scope) throws GamaRuntimeException {
		IList<IList<GamaArgument>> preferedExt = GamaListFactory.create();
		IGraph graph = getArgGraph(scope.getAgent());
		final ISpecies context = scope.getAgent().getSpecies();
		final IStatement.WithArgs evalArgAct = context.getAction("evaluate_argument");
		final Arguments argsTNR = new Arguments();
		
		for (Object v : graph.getVertices()) {
			argsTNR.put("argument", ConstantExpressionDescription.create(v));
			evalArgAct.setRuntimeArgs(scope, argsTNR);
			Double val = (Double) evalArgAct.executeOn(scope);
			Set edges = graph.outgoingEdgesOf(v);
			for (Object e : edges) {
				graph.setEdgeWeight(e, val);
			}
		}
	}
	
	@action (
			name = "simplify_graph",
			doc = @doc (
					value = "simplify the argumentation graph",
					examples = { @example ("do simplify_graph;") }))
	public IGraph primSimplifyArgumentationGraph(final IScope scope) throws GamaRuntimeException {
		IList<IList<GamaArgument>> preferedExt = GamaListFactory.create();
		IGraph graph = (IGraph) getArgGraph(scope.getAgent()).copy(scope);
		IList edges = (IList) graph.getEdges().copy(scope);
		for (Object e1 : edges) {
			if (graph.containsEdge(e1)) {
				Object s = graph.getEdgeSource(e1);
				Object t = graph.getEdgeTarget(e1);
				Object e2 = graph.getEdge(t, s);
				if (e2 != null) {
					Double w1 = graph.getWeightOf(e1);
					Double w2 = graph.getWeightOf(e2);
					if (w1 > w2) 
						graph.removeEdge(e2);
					else if (w2 < w1)
						graph.removeEdge(e1);
				}
			}
		}
		return graph;
	}
	
	
	@action (
			name = "evaluate_conclusion",
			args = {@arg (
							name = "arguments",
							type = IType.CONTAINER,
							optional = false,
							doc = @doc ("the list of arguments to evaluate"))},
			doc = @doc (
					value = "evaluate the conclusion that can be taken from a list of arguments",
					returns = "the conclusion of the list of arguments",
					examples = { @example ("float val <- evaluate_conclusion(args);") }))
	public Double primEvaluateConcl(final IScope scope) throws GamaRuntimeException {
		final IList args = scope.hasArg("arguments")? (IList)scope.getArg("arguments", IType.LIST) : null;
		double val = 0;
		IAgent ag = scope.getAgent();
		final ISpecies context = ag.getSpecies();
		final IStatement.WithArgs evalArgAct = context.getAction("evaluate_argument");
		final Arguments argsTNR = new Arguments();
		
		for (Object obj : args) {
			GamaArgument arg = (GamaArgument) obj;
			argsTNR.put("argument", ConstantExpressionDescription.create(arg));
			evalArgAct.setRuntimeArgs(scope, argsTNR);
			Double w = (Double) evalArgAct.executeOn(scope);
			val += ((arg.getConclusion().equals("+")) ? 1.0 : ((arg.getConclusion().equals( "-")) ? -1.0 : 0.0)) * w;
		}
		return val;
	}
	
	@action (
			name = "add_argument",
			
			args = {@arg (
					name = "argument",
					type = GamaArgumentType.id,
					optional = false,
					doc = @doc ("the argument to add")),
					@arg (
					name = "graph",
					type = IType.GRAPH,
					optional = false,
					doc = @doc ("the global argumentation graph with all the arguments and attacks"))
					},
			doc = @doc (
					value = "add and arguments and all the attacks to the agent argumentation graph",
					examples = { @example ("do add_argument(new_agrument, reference_graph);") }))
	public boolean primAddArguments(final IScope scope) throws GamaRuntimeException {
		IGraph graph = getArgGraph(scope.getAgent());
		final IGraph refGraph = scope.hasArg("graph")? (IGraph)scope.getArg("graph", IType.GRAPH) : null;
		final GamaArgument arg = scope.hasArg("argument")? (GamaArgument)scope.getArg("argument", GamaArgumentType.id) : null;
		if ((graph != null) && (arg != null) && !(graph.getVertices().contains(arg))) {
			graph.addVertex(arg);
			if (refGraph != null) {
				Set edges = refGraph.outgoingEdgesOf(arg);
				for (Object e : edges) {
					Object t = refGraph.getEdgeTarget(e);
					if (graph.getVertices().contains(t)) 
						graph.addEdge(arg, t);
				}
				edges = refGraph.incomingEdgesOf(arg);
				for (Object e : edges) {
					Object s = refGraph.getEdgeSource(e);
					if (graph.getVertices().contains(s)) 
						graph.addEdge(s,arg);
				}
			}
			return true;
		}
		return false;
	}
	
	
	@action (
			name = "make_decision",
			doc = @doc (
					value = "make decision concerning the option proposed by the argumentation graph",
					returns = "a float value: if the value is > 0, pro option; if value < 0, cons option, elsewere neutral",
					examples = { @example ("do make_decision;") }))
	public Double primMakeDecision(final IScope scope) throws GamaRuntimeException {
		IAgent ag = scope.getAgent();
		final ISpecies context = ag.getSpecies();
		final IStatement updateGraphArg = context.getAction("update_graph");
		updateGraphArg.executeOn(scope);
		
		final IStatement simplifyGraph = context.getAction("simplify_graph");
		IGraph simplifiedGraph = (IGraph) simplifyGraph.executeOn(scope);
		

		final IStatement.WithArgs extensionComputation = context.getAction("extension");
		final Arguments args = new Arguments();
		args.put("graph", ConstantExpressionDescription.create(simplifiedGraph));
		extensionComputation.setRuntimeArgs(scope, args);
		IList<IList> extensions = (IList<IList>) extensionComputation.executeOn(scope);
		
		final IStatement.WithArgs valExtension = context.getAction("evaluate_conclusion");
		final Arguments args2 = new Arguments();
		Double valmax = 0.0;
		for (IList ext : extensions) {
			args2.put("arguments", ConstantExpressionDescription.create(ext));
			valExtension.setRuntimeArgs(scope, args2);
			Double val = (Double) valExtension.executeOn(scope);
			if (Math.abs(val) > Math.abs(valmax))
				valmax = val;
		}
		return valmax;
	}
	
}
