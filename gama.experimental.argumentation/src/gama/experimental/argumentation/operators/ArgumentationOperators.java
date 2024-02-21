package gama.experimental.argumentation.operators;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import gama.experimental.argumentation.types.GamaArgument;
import gama.experimental.argumentation.types.GamaArgumentType;
import gama.core.metamodel.agent.IAgent;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMap;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaFile;
import gama.core.util.graph.GamaGraph;
import gama.core.util.graph.IGraph;
import gama.core.util.matrix.IMatrix;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

public class ArgumentationOperators {

	
	@operator (
			value = { "load_graph" },
			category = { "argumentation" },
			concept = { "argumentation"})
	public static IGraph<GamaArgument, Object> loadGraph(IScope scope, GamaFile f, IList<GamaArgument> arguments) {
		GamaGraph<GamaArgument, Object> graph = new GamaGraph(scope, Types.get(GamaArgumentType.id), Types.PAIR);
		graph.setDirected(true);
		Map<String, GamaArgument> argNames = arguments.stream().collect(Collectors.toMap(GamaArgument::getId,
                Function.identity()));
		for (Object obj : f.getContents(scope).iterable(scope)) {
			String line = obj.toString();
			if (line.contains("arg")) {
				String n = line.replace("arg(","").replace (").", "");
				if (argNames.containsKey(n)){
					graph.addVertex(argNames.get(n));
				}
			} else 	if (line.contains("att(")) {
				String[] str = line.split(",");
				if (str.length == 2) {
					String a1 = str[0].replace("att(","");
					String a2 = str[1].replace (").","");
 					GamaArgument arg1 = argNames.get(a1);
 					GamaArgument arg2  = argNames.get(a2);
					if (arg1 != null && arg2 != null) {
						graph.addEdge(arg1, arg2); 
					} 
				}
			}
		}
		
		return graph;
	}
	
	@operator (
			value = { "load_arguments" },
			category = { "argumentation" },
			concept = { "argumentation"})
	public static IList<GamaArgument> loadArguments(IScope scope, String option, GamaFile f) {
		IList<GamaArgument> args = GamaListFactory.create();
		IMatrix<String> mat = f.getContents(scope).matrixValue(scope, Types.STRING, false);
		for (int i = 0; i < mat.getRows(scope); i++) {
			String n = mat.get(scope, 0, i);
			if (n != null && !n.isEmpty()) {
				String conclusion = mat.get(scope, 1,i); 
				GamaMap<String, Double> criteria = (GamaMap<String,Double>) GamaMapFactory.create(Types.STRING,Types.FLOAT);
				for (int j = 2; j < mat.getCols(scope); j++) {
					String val = mat.get(scope, j,i) ;
					if ((val != null )) {
						if (val.contains("::")) {
							String[] p = val.split("::");
							criteria.put(p[0], Double.valueOf(p[1]));
						} else {
							criteria.put(val, 1.0);
						}
					}
				}
				GamaArgument arg = new GamaArgument(n, option, conclusion, "", "", criteria, null, "");
				args.add(arg);
			}
		}
		
		return args;
	}
	 
	@operator (
			value = { "load_myChoice_arguments" },
			category = { "argumentation" },
			concept = { "argumentation"})
	public static IList<GamaArgument> loadMyChoiceArguments(IScope scope, GamaFile f){
		IList<GamaArgument> args = GamaListFactory.create();
		IMatrix<String> mat = f.getContents(scope).matrixValue(scope, Types.STRING, false);
		for (int i = 1; i < mat.getRows(scope); i++) {
			String id = mat.get(scope, 0, i);
			String option = mat.get(scope, 2, i);
			String conclusion = mat.get(scope, 3, i);
			GamaMap<String, Double> criteria = (GamaMap<String, Double>) GamaMapFactory.create(Types.STRING, Types.FLOAT);
			criteria.put(mat.get(scope, 4, i), 1.0);
			String source_type = mat.get(scope, 16, i);
			args.add(new GamaArgument(id, option, conclusion, "", "", criteria, null, source_type));
		}
		return args;
	}
	
	
	
	@operator (
			value = {"set_actor"},
			category = {"argumentation"},
			concept = {"argumentation"})
	public static GamaArgument setActor(GamaArgument argument, IAgent newActor) {
		argument.setActor(newActor);
		return argument;
	}
}
