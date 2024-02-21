package gama.experimental.bayesiannetwork.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.kave.repackaged.jayes.BayesNode;
import gama.experimental.bayesiannetwork.types.GamaBayesianNetwork;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMap;
import gama.core.util.GamaMapFactory;
import gama.gaml.operators.Cast;

public class BayesianNetworkOperator {

	@operator (
			value = { "create_node" },
			category = { "bayesian_network" },
			concept = { "bayesian_network"})
	public static GamaBayesianNetwork createNode(IScope scope, GamaBayesianNetwork network, String node) {
		if (network == null) return null;
		network.getNetwork().createNode(node);
			
		return network;
	}
	
	@operator (
			value = { "add_node_outcome" },
			category = { "bayesian_network" },
			concept = { "bayesian_network"})
	public static GamaBayesianNetwork addNodeOutcome(IScope scope, GamaBayesianNetwork network, String node, String outcome) {
		if (network == null) return null;
		BayesNode bn = network.getNetwork().getNode(node);
		if (bn != null)
			bn.addOutcome(outcome);
		else GamaRuntimeException.error("Node " + node + " does not exist in network " + network.getId(), scope);
		return network;
	}
	
	@operator (
			value = { "add_node_parent" },
			category = { "bayesian_network" },
			concept = { "bayesian_network"})
	public static GamaBayesianNetwork addNodeParent(IScope scope, GamaBayesianNetwork network, String node, String parent) {
		if (network == null) return null;
		BayesNode bn = network.getNetwork().getNode(node);
		BayesNode bnP = network.getNetwork().getNode(parent);
		
		if (bn == null) GamaRuntimeException.error("Node " + node + " does not exist in network " + network.getId(), scope);
		else if (bnP == null) GamaRuntimeException.error("Node " + parent + " does not exist in network " + network.getId(), scope);
		else { 
			List<BayesNode> p = new ArrayList<BayesNode>();
			p.addAll(bn.getParents());
			p.add(bnP);
			bn.setParents(p);
		}
		return network;
	}
	
	@operator (
			value = { "add_node_probabilities" },
			category = { "bayesian_network" },
			concept = { "bayesian_network"})
	public static GamaBayesianNetwork addNodeProbabilities(IScope scope, GamaBayesianNetwork network, String node, GamaMap<Object,Object> probabilities) {
		if (network == null) return null;
		BayesNode bn = network.getNetwork().getNode(node);
		
		if (bn == null) GamaRuntimeException.error("Node " + node + " does not exist in network " + network.getId(), scope);
		else { 
			if (bn.getParents().isEmpty()) {
				int N = bn.getOutcomeCount();
				
				double proba[] = new double[N];
				
				for (int i = 0; i < N; i++) {
					Object obj =  probabilities.get(bn.getOutcomes().get(i));
					if (obj == null || (!(obj instanceof Double) && !(obj instanceof Integer))) {
						 GamaRuntimeException.error("problem with probabilities " + probabilities + ": inconsistency with network " + network.getId(), scope);
					} else {
						proba[i] = Cast.asFloat(scope, obj);
					}
					
				}
				bn.setProbabilities(proba);
			} else {
				List<Map<String, String>> list = GamaListFactory.create();
				
				combinaison(list,GamaMapFactory.create(),0,bn);
				double proba[] = new double[list.size()* bn.getOutcomeCount()];
				int index = 0;
				for (Map<String, String> comb : list) {
					Map<String,Object> vals2 = (Map<String, Object>) probabilities.get(comb);
					for (String outcome : bn.getOutcomes()) {
							Object obj =  vals2.get(outcome);
							if (obj == null || (!(obj instanceof Double) && !(obj instanceof Integer))) {
								 GamaRuntimeException.error("problem with probabilities " + probabilities + ": inconsistency with network " + network.getId(), scope);
							} else {
								proba[index] = Cast.asFloat(scope, obj);
							}
							index ++;
						}
					} 
				bn.setProbabilities(proba);	
			}
		}
		return network;
	}
	
	static void combinaison(List<Map<String, String>> list,Map<String, String> current, int currentIndex, BayesNode node) {
		if (currentIndex >= node.getParents().size()) {
			list.add(current);
		} else {
			BayesNode p = node.getParents().get(currentIndex);
			for (String v : p.getOutcomes()) {
				Map<String,String> c = GamaMapFactory.create();
				c.putAll(current);
				c.put(p.getName(),v);
				combinaison(list,c,currentIndex+1,node);
			}
		}
		
	}
	
	@operator (
			value = { "add_node_evidence" },
			category = { "bayesian_network" },
			concept = { "bayesian_network"})
	public static GamaBayesianNetwork addNodeEvidence(IScope scope, GamaBayesianNetwork network, String node, String outcome) {
		if (network == null) return null;
		network.getInference().setNetwork(network.getNetwork());
		
		BayesNode bn = network.getNetwork().getNode(node);
		if (bn == null) GamaRuntimeException.error("Node " + node + " does not exist in network " + network.getId(), scope);
		else network.getInference().addEvidence(bn, outcome);
		return network;
		
	}
	
	@operator (
			value = { "get_beliefs" },
			category = { "bayesian_network" },
			concept = { "bayesian_network"})
	public static GamaMap<String,Double> getBeliefs(IScope scope, GamaBayesianNetwork network, String node) {
		GamaMap<String,Double> beliefs = (GamaMap) GamaMapFactory.create();
		if (network == null) return beliefs;
		
		BayesNode bn = network.getNetwork().getNode(node);
		if (bn == null) GamaRuntimeException.error("Node " + node + " does not exist in network " + network.getId(), scope);
		else {
			double d[] = network.getInference().getBeliefs(bn);
			for (int i = 0; i < d.length; i++) {
				beliefs.put(bn.getOutcomes().get(i), d[i]);
			}
		}
		return beliefs;
		
	}
	
}
