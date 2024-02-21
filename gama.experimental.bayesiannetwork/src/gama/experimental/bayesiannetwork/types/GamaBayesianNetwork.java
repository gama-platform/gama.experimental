package gama.experimental.bayesiannetwork.types;

import java.util.List;

import cc.kave.repackaged.jayes.BayesNet;
import cc.kave.repackaged.jayes.BayesNode;
import cc.kave.repackaged.jayes.inference.IBayesInferer;
import cc.kave.repackaged.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import gama.core.common.interfaces.IValue;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;

@vars({ @variable(name = "id", type = IType.STRING),
	@variable(name = "nodes", type = IType.LIST) })
public class GamaBayesianNetwork  implements IValue{
	private BayesNet network;
	private IBayesInferer inference ;
	
	public GamaBayesianNetwork(String id) {
		super();
		network = new BayesNet();
		inference = new JunctionTreeAlgorithm();
		network.setName(id);
	}
	
	public GamaBayesianNetwork(BayesNet net) {
		super();
		network = net;
		inference = new JunctionTreeAlgorithm();
		inference.setNetwork(network);
	}

	@getter("id")
	public String getId() {
		return network.getName();
	}

	@getter("nodes")
	public IList<String> getNodes() {
		List<BayesNode> nodes = network.getNodes();
		IList<String> nodesName = GamaListFactory.EMPTY_LIST;
		for (BayesNode n : nodes) nodesName.add(n.getName());
		return nodesName;
	}
	
	
	public BayesNet getNetwork() {
		return network;
	}

	public void setNetwork(BayesNet network) {
		this.network = network;
	}
	

	public IBayesInferer getInference() {
		return inference;
	}

	public void setInference(IBayesInferer inference) {
		this.inference = inference;
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return getId() + " -> " + getNodes();
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		GamaBayesianNetwork bayes = new GamaBayesianNetwork(this.network);
		return bayes;
	}

	@Override
	public JsonValue serializeToJson(Json json) {
		// TODO Auto-generated method stub
		return null;
	}
		
}
