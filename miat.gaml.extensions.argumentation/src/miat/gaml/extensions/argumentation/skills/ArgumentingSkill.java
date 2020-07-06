package miat.gaml.extensions.argumentation.skills;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
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
import msi.gama.util.GamaList;
import msi.gama.util.GamaListCollectionWrapper;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.GamaPair;
import msi.gama.util.IList;
import msi.gama.util.IMap;
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
import net.sf.jargsemsat.jargsemsat.alg.Misc;
import net.sf.jargsemsat.jargsemsat.datastructures.DungAF;

@skill(name = "argumenting")
@vars({ @variable(name = "argumentation_graph", type = IType.GRAPH),
		@variable(name = "crit_importance", type = IType.MAP) })
public class ArgumentingSkill extends Skill {
	static final String ARGUMENTATION_GRAPH = "argumentation_graph";
	static final String CRIT_IMPORTANCE = "crit_importance";

	public static int BLANK = 0;
	public static int IN = 1;
	public static int OUT = 2;
	public static int MUST_OUT = 3;
	public static int UNDEC = 4;

	@getter(ARGUMENTATION_GRAPH)
	public GamaGraph<GamaArgument, ?> getArgGraph(final IAgent agent) {
		return (GamaGraph<GamaArgument, ?>) agent.getAttribute(ARGUMENTATION_GRAPH);
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

	@action(name = "evaluate_argument", args = {
			@arg(name = "argument", type = GamaArgumentType.id, optional = false, doc = @doc("the argument to evaluate")) }, doc = @doc(value = "evaluate the strength  of an argument for the agent", returns = "the strength of argument", examples = {
					@example("float val <- evaluate_argument(one_argument);") }))
	public Double primEvaluateArg(final IScope scope) throws GamaRuntimeException {
		final GamaArgument a = scope.hasArg("argument") ? (GamaArgument) scope.getArg("argument", GamaArgumentType.id)
				: null;
		double val = 0;
		IAgent ag = scope.getAgent();
		Map<String, Double> agVal = getCritImp(ag);
		for (String c : a.getCriteria().keySet()) {
			val += a.getCriteria().get(c) * agVal.get(c);
		}
		return val;
	}

	private DungAF toDungAF(IGraph graph) {
		List<String> args = new ArrayList<>();
		for (Object v : graph.getVertices()) {
			args.add(((GamaArgument) v).getId());
		}
		List<String[]> attacks = new ArrayList<>();
		for (Object e : graph.getEdges()) {
			String[] pair = new String[2];
			pair[0] = ((GamaArgument) graph.getEdgeSource(e)).getId();
			pair[1] = ((GamaArgument) graph.getEdgeTarget(e)).getId();
			attacks.add(pair);
		}
		return new DungAF(args, attacks);
	}

	private Map<String, GamaArgument> argumentPerName(IGraph graph) {
		Map<String, GamaArgument> argName = new Hashtable<>();
		for (Object v : graph.getVertices()) {
			argName.put(((GamaArgument) v).getId(), (GamaArgument) v);
		}
		return argName;
	}

	private IList<IList> toGAMAList(IGraph graph, HashSet<HashSet<String>> extension) {
		Map<String, GamaArgument> argName = argumentPerName(graph);
		IList<IList> result = GamaListFactory.create();
		for (HashSet<String> ext : extension) {
			IList ags = GamaListFactory.create();
			for (String a : ext) {
				ags.add(argName.get(a));
			}
			result.add(ags);
		}
		return result;
	}

	@action(name = "preferred_extensions", args = {
			@arg(name = "graph", type = IType.GRAPH, optional = true, doc = @doc("the graph to evaluate")) }, doc = @doc(value = "evaluate the preferred extensions of an argument graph", returns = "a list of list of arguments representing the preferred extensions", examples = {
					@example("list<list<argument>> results <- preferred_extensions(a_graph);") }))
	public IList<IList> primComputePreferedExtension(final IScope scope) throws GamaRuntimeException {
		IGraph graph = (IGraph) scope.getArg("graph", IType.GRAPH);
		if (graph == null)
			graph = getArgGraph(scope.getAgent());
		DungAF dungAF = toDungAF(graph);
		return toGAMAList(graph, dungAF.getPreferredExts());
	}

	@action(name = "complete_extensions", args = {
			@arg(name = "graph", type = IType.GRAPH, optional = true, doc = @doc("the graph to evaluate")) }, doc = @doc(value = "evaluate the complete extensions of an argument graph", returns = "a list of list of arguments representing the complete extensions", examples = {
					@example("list<list<argument>> results <-complete_extensions(a_graph);") }))
	public IList<IList> primComputeCompletedExtension(final IScope scope) throws GamaRuntimeException {
		IGraph graph = (IGraph) scope.getArg("graph", IType.GRAPH);
		if (graph == null)
			graph = getArgGraph(scope.getAgent());
		DungAF dungAF = toDungAF(graph);
		return toGAMAList(graph, dungAF.getCompleteExts());
	}

	@action(name = "stable_extensions", args = {
			@arg(name = "graph", type = IType.GRAPH, optional = true, doc = @doc("the graph to evaluate")) }, doc = @doc(value = "evaluate the stable extensions of an argument graph", returns = "a list of list of arguments representing the stable extensions", examples = {
					@example("list<list<argument>> results <-stable_extensions(a_graph);") }))
	public IList<IList> primComputeStableExtension(final IScope scope) throws GamaRuntimeException {
		IGraph graph = (IGraph) scope.getArg("graph", IType.GRAPH);
		if (graph == null)
			graph = getArgGraph(scope.getAgent());
		DungAF dungAF = toDungAF(graph);
		return toGAMAList(graph, dungAF.getStableExts());
	}

	@action(name = "semi_stable_extensions", args = {
			@arg(name = "graph", type = IType.GRAPH, optional = true, doc = @doc("the graph to evaluate")) }, doc = @doc(value = "evaluate the semi stable extensions of an argument graph", returns = "a list of list of arguments representing the semi stable extensions", examples = {
					@example("list<list<argument>> results <-semi_stable_extensions(a_graph);") }))
	public IList<IList> primComputeSemiStableExtension(final IScope scope) throws GamaRuntimeException {
		IGraph graph = (IGraph) scope.getArg("graph", IType.GRAPH);
		if (graph == null)
			graph = getArgGraph(scope.getAgent());
		DungAF dungAF = toDungAF(graph);
		return toGAMAList(graph, dungAF.getSemiStableExts());
	}

	@action(name = "extensions", args = {
			@arg(name = "graph", type = IType.GRAPH, optional = true, doc = @doc("the graph to evaluate")) },

			doc = @doc(value = "evaluate the extensions of an argument graph", returns = "a list of list of arguments representing the extensions", examples = {
					@example("list<list<argument>> results <- extensions();") }))
	public IList<IList> primComputeExtension(final IScope scope) throws GamaRuntimeException {
		return primComputePreferedExtension(scope);
	}

	@action(name = "update_graph", doc = @doc(value = "update the weight of the argumentation graph", examples = {
			@example("do update_graph;") }))
	public void primUpdateArgumentationGraph(final IScope scope) throws GamaRuntimeException {
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

	@action(name = "simplify_graph", doc = @doc(value = "simplify the argumentation graph", examples = {
			@example("do simplify_graph;") }))
	public IGraph primSimplifyArgumentationGraph(final IScope scope) throws GamaRuntimeException {
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

	@action(name = "evaluate_conclusion", args = {
			@arg(name = "arguments", type = IType.CONTAINER, optional = false, doc = @doc("the list of arguments to evaluate")) }, doc = @doc(value = "evaluate the conclusion that can be taken from a list of arguments", returns = "the conclusion of the list of arguments", examples = {
					@example("float val <- evaluate_conclusion(args);") }))
	public Double primEvaluateConcl(final IScope scope) throws GamaRuntimeException {
		final IList args = scope.hasArg("arguments") ? (IList) scope.getArg("arguments", IType.LIST) : null;
		if (args == null || args.isEmpty())
			return 0.0;
		double val = 0;
		IAgent ag = scope.getAgent();
		final ISpecies context = ag.getSpecies();
		final IStatement.WithArgs evalArgAct = context.getAction("evaluate_argument");
		final Arguments argsTNR = new Arguments();
		double sum = 0;
		for (Object obj : args) {
			GamaArgument arg = (GamaArgument) obj;
			argsTNR.put("argument", ConstantExpressionDescription.create(arg));
			evalArgAct.setRuntimeArgs(scope, argsTNR);
			Double w = (Double) evalArgAct.executeOn(scope);
			sum += w;
			val += ((arg.getConclusion().equals("+")) ? 1.0 : ((arg.getConclusion().equals("-")) ? -1.0 : 0.0)) * w;
		}
		return val / sum;
	}

	@action(name = "add_argument",
			args = { @arg(name = "argument", type = GamaArgumentType.id, optional = false, doc = @doc("the argument to add")),
					@arg(name = "graph", type = IType.GRAPH, optional = false, doc = @doc("the global argumentation graph with all the arguments and attacks")) }, doc = @doc(value = "add and arguments and all the attacks to the agent argumentation graph", examples = {
							@example("do add_argument(new_agrument, reference_graph);") }))
	public boolean primAddArguments(final IScope scope) throws GamaRuntimeException {
		IGraph<GamaArgument, Object> graph = (IGraph<GamaArgument, Object>) getArgGraph(scope.getAgent());
		final IGraph<GamaArgument, Object> refGraph = scope.hasArg("graph") ? (IGraph) scope.getArg("graph", IType.GRAPH) : null;
		final GamaArgument initial_arg = scope.hasArg("argument") ? (GamaArgument) scope.getArg("argument", GamaArgumentType.id) : null;
		GamaArgument arg = (GamaArgument) initial_arg.copy(scope);
		if ((graph != null) && (initial_arg != null) && !(graph.containsVertex(arg))) {
			graph.addVertex(arg);
			if (refGraph != null) {
				Set edges = (Set) refGraph.outgoingEdgesOf(arg);
				for (Object e : edges) {
					GamaArgument t = refGraph.getEdgeTarget(e);
					if (graph.containsVertex(t))
						graph.addEdge(arg, t);
				}
				edges = refGraph.incomingEdgesOf(arg);
				for (Object e : edges) {
					GamaArgument s = refGraph.getEdgeSource(e);
					if (graph.containsVertex(s))
						graph.addEdge(s, arg);
				}
			}
			return true;
		}
		return false;
	}

	@action(name = "remove_argument",
			args = { @arg(name = "argument", type = GamaArgumentType.id, optional = false, doc = @doc("the argument to remove")) }, doc = @doc(value = "remove and arguments and all the attacks concerning this argument from the agent argumentation graph", examples = {
					@example("do remove_argument(an_agrument);") }))
	public boolean primRemoveArguments(final IScope scope) throws GamaRuntimeException {
		IGraph graph = getArgGraph(scope.getAgent());
		final GamaArgument arg = scope.hasArg("argument") ? (GamaArgument) scope.getArg("argument", GamaArgumentType.id)
				: null;
		if ((graph != null) && (arg != null) && (graph.getVertices().contains(arg))) {
			graph.removeVertex(arg);

			return true;
		}
		return false;
	}

	@action(name = "make_decision", doc = @doc(value = "make decision concerning the option proposed by the argumentation graph", returns = "a pair best extension:value: if the value is > 0, pro option; if value < 0, cons option, elsewere neutral", examples = {
			@example("do make_decision;") }))
	public GamaPair<IList<GamaArgument>, Double>  primMakeDecision(final IScope scope) throws GamaRuntimeException {
		return ((GamaPair<IList<GamaArgument>, Double>) scope.getAgent().getSpecies().getAction("get_best_extension").executeOn(scope));
	}
	
	@action(name = "get_best_extension")
	public GamaPair<IList<GamaArgument>, Double> primGetBestExtension(final IScope scope) throws GamaRuntimeException {
	
		final IStatement evaluateExtensions = scope.getAgent().getSpecies().getAction("evaluate_extensions");
		IMap<IList<GamaArgument>, Double> extensionsEvaluations = (IMap<IList<GamaArgument>, Double>) evaluateExtensions.executeOn(scope);
		
		Iterator<Map.Entry<IList<GamaArgument>, Double>> it = extensionsEvaluations.entrySet().iterator();
		Map.Entry<IList<GamaArgument>, Double> res = it.hasNext() ? it.next() : null;
		while(it.hasNext()) {
			Map.Entry<IList<GamaArgument>, Double> ext = it.next();
			if (Math.abs(ext.getValue()) > Math.abs(res.getValue())) {
				res = ext;
			}
		}
		return new GamaPair<IList<GamaArgument>, Double>(res.getKey(), res.getValue(), Types.LIST, Types.FLOAT);
	}
	
	@action(name = "evaluate_extensions")
	public IMap<IList<GamaArgument>, Double> primEvaluateExtensions(final IScope scope) throws GamaRuntimeException {
		IAgent ag = scope.getAgent();
		final ISpecies context = ag.getSpecies();
		final IStatement updateGraphArg = context.getAction("update_graph");
		updateGraphArg.executeOn(scope);

		final IStatement simplifyGraph = context.getAction("simplify_graph");
		IGraph simplifiedGraph = (IGraph) simplifyGraph.executeOn(scope);

		final IStatement.WithArgs extensionComputation = context.getAction("extensions");
		final Arguments argsComputeExts = new Arguments();
		argsComputeExts.put("graph", ConstantExpressionDescription.create(simplifiedGraph));
		extensionComputation.setRuntimeArgs(scope, argsComputeExts);
		IList<IList<GamaArgument>> extensions = (IList<IList<GamaArgument>>) extensionComputation.executeOn(scope);
		
		IMap<IList<GamaArgument>, Double> resultMap = GamaMapFactory.create(Types.LIST, Types.FLOAT);
		
		final IStatement.WithArgs valExtension = context.getAction("evaluate_conclusion");
		final Arguments argsComputeValExtension = new Arguments();
		IList args = extensions.get(0);
		for (IList ext : extensions) {
			argsComputeValExtension.put("arguments", ConstantExpressionDescription.create(ext));
			valExtension.setRuntimeArgs(scope, argsComputeValExtension);
			resultMap.addValueAtIndex(scope, ext, (Double) valExtension.executeOn(scope));
		}
		return resultMap;
	}
}
