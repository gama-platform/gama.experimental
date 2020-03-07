package miat.gaml.extension.bayesiannetwork.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.kave.repackaged.jayes.BayesNode;
import miat.gaml.extension.bayesiannetwork.types.GamaBayesianNetwork;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gaml.operators.Cast;

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
	public static GamaBayesianNetwork addNodeProbabilities(IScope scope, GamaBayesianNetwork network, String node, GamaMap<String,Object> probabilities) {
		if (network == null) return null;
		BayesNode bn = network.getNetwork().getNode(node);
		
		if (bn == null) GamaRuntimeException.error("Node " + node + " does not exist in network " + network.getId(), scope);
		else { 
			int N = 0;
			int out = bn.getOutcomeCount();
			if (bn.getParents().isEmpty()) {
				N = out;
			} else {
				for (BayesNode nn : bn.getParents()) {
					N += out * nn.getOutcomeCount();
				}
			}
			double proba[] = new double[N];
			if (bn.getParents().isEmpty()) {
				for (int i = 0; i < N; i++) {
					Object obj =  probabilities.get(bn.getOutcomes().get(i));
					if (obj == null || (!(obj instanceof Double) && !(obj instanceof Integer))) {
						 GamaRuntimeException.error("problem with probabilities " + probabilities + ": inconsistency with network " + network.getId(), scope);
					} else {
						proba[i] = Cast.asFloat(scope, obj);
					}
					
				}	
			} else {
				int index = 0;
				for (BayesNode par : bn.getParents()) {
					Map vals = (Map) probabilities.get(par.getName());
					for (String outcomeP : par.getOutcomes()) {
						Map vals2 = (Map) vals.get(outcomeP); 
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
				}
			}
			
			bn.setProbabilities(proba);
			
		}
		return network;
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
