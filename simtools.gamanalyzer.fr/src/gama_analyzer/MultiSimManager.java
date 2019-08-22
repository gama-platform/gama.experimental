package gama_analyzer;

import java.awt.Color;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.types.Types;

public class MultiSimManager {

	int hasard = (int) (Math.random() * 10000) + 1;

	public MultiSimManager() {
		super();
	}

	public MultiSimManager(final IList<IAgent> agentGroupFollowerList, final IList<StorableData> storableDataList,
			final IList<GroupIdRule> groupIdRuleList, final IList<Color> simColorList,
			final IList<Double> at_cycle_manager, final IList<IList<Double>> at_var_manager,
			final IList<Object> identifiants) {

		super();
		this.agentGroupFollowerList = agentGroupFollowerList;
		this.storableDataList = storableDataList;
		this.groupIdRuleList = groupIdRuleList;
		this.idSimList = idSimList;
		this.simColorList = simColorList;
	}

	IList<IAgent> agentGroupFollowerList = GamaListFactory.create(Types.AGENT);
	IList<StorableData> storableDataList = GamaListFactory.create(Types.NO_TYPE);
	IList<GroupIdRule> groupIdRuleList = GamaListFactory.create(Types.NO_TYPE);
	IList idSimList = GamaListFactory.create(Types.NO_TYPE);
	IList<Color> simColorList = GamaListFactory.create(Types.NO_TYPE);

	public IList<IAgent> getAgentGroupFollowerList() {
		return agentGroupFollowerList;
	}

	public void setAgentGroupFollowerList(final IList<IAgent> agentGroupFollowerList) {
		this.agentGroupFollowerList = agentGroupFollowerList;
	}

	public IList<StorableData> getStorableDataList() {
		return storableDataList;
	}

	public void setStorableDataList(final IList<StorableData> storableDataList) {
		this.storableDataList = storableDataList;
	}

	public IList<GroupIdRule> getGroupIdRuleList() {
		return groupIdRuleList;
	}

	public void setGroupIdRuleList(final IList<GroupIdRule> groupIdRuleList) {
		this.groupIdRuleList = groupIdRuleList;
	}

	public IList<Object> getIdSimList() {
		return idSimList;
	}

	public void setIdSimList(final IList<Object> idSimList) {
		this.idSimList = idSimList;
	}

	public IList<Color> getSimColorList() {
		return simColorList;
	}

	public void setSimColorList(final IList<Color> simColorList) {
		this.simColorList = simColorList;
	}
}
