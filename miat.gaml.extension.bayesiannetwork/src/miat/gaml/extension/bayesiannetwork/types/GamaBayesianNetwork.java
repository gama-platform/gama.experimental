package miat.gaml.extension.bayesiannetwork.types;

import java.util.List;

import cc.kave.repackaged.jayes.BayesNet;
import cc.kave.repackaged.jayes.BayesNode;
import cc.kave.repackaged.jayes.inference.IBayesInferer;
import cc.kave.repackaged.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.json.Json;
import msi.gama.util.file.json.JsonValue;
import msi.gaml.types.IType;

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
		return json.valueOf(stringValue(null));
	}
		
}
