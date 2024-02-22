/*******************************************************************************************************
 *
 * TrafficOperator.java, in plugin gama.experimental.switchproject.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.experimental.switchproject.gaml.operators;

import java.util.List;
import java.util.Map.Entry;

import gama.experimental.switchproject.gama.common.interfaces.IKeywordIrit;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.metamodel.topology.graph._SpatialEdge;
import gama.core.metamodel.topology.graph._SpatialVertex;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.runtime.IScope;
import gama.core.util.IContainer;
import gama.core.util.graph.IGraph;

/**
 * Traffic operator
 * 
 * @author Jean-François Erdelyi
 */
public class TrafficOperator {
	@operator(
			value = "as_traffic_graph", 
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2, 
			index_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1, 
			concept = { IConcept.GRAPH, IConcept.TRANSPORT })
	@doc(
			value = "creates a graph from the list/map of roads given as operand and connect the node to the road", 
			examples = {
				@example(
					value = "as_traffic_graph(roads, nodes): build a graph while using the road agents as roads and the node agents as nodes", 
					isExecutable = false) 
				}, 
			see = {"as_intersection_graph", "as_distance_graph", "as_edge_graph" })
	@no_test
	@SuppressWarnings("unchecked")
	public static IGraph<?, ?> asTrafficGraph(final IScope scope, final IContainer<?, ?> roads, final IContainer<?, ?> nodes) {
		final IGraph<?, ?> graph = new GamaSpatialGraph(roads, nodes, scope);
		
		// Set in/out node of roads agents
		for (final Entry<?, ?> entry : graph._internalEdgeMap().entrySet()) {
			IAgent source = (IAgent) ((_SpatialEdge) entry.getValue()).getSource();
			if (source != null) {
				((IAgent) entry.getKey()).setAttribute(IKeywordIrit.NODE_IN, source);
			}

			IAgent target = (IAgent) ((_SpatialEdge) entry.getValue()).getTarget();
			if (target != null) {
				((IAgent) entry.getKey()).setAttribute(IKeywordIrit.NODE_OUT, target);
			}
		}
		
		// Set in/out roads of nodes agents
		for (final Entry<?, ?> entry : graph._internalVertexMap().entrySet()) {
			for (final Object edge : ((_SpatialVertex) entry.getValue()).getInEdges()) {
				((List<IAgent>) ((IAgent) entry.getKey()).getAttribute(IKeywordIrit.ROADS_IN)).add((IAgent) edge);
			}
			
			for (final Object edge : ((_SpatialVertex) entry.getValue()).getOutEdges()) {
				((List<IAgent>) ((IAgent) entry.getKey()).getAttribute(IKeywordIrit.ROADS_OUT)).add((IAgent) edge);
			}
		}
		
		return graph;
	}
}
