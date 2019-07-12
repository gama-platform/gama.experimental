package gama_analyzer;

import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaFloatMatrix;
import msi.gama.util.matrix.GamaMatrix;
import msi.gama.util.matrix.GamaObjectMatrix;
import msi.gaml.types.Types;

public class StorableData {

	public GamaMap varmap_reverse;
	public GamaMap<Integer, String> varmap; // varname-colnb map //liste des variables
	public GamaMap<Integer, String> numvarmap; // varname-colnb map for numerical variables //listes des variables
												// numériques
	public GamaMap<Integer, String> qualivarmap; // varname-colnb map the rest
	public GamaObjectMatrix metadatahistory; // sim, step, groupid, ruleid, supgroupid, supruleid, poplist, popsize
	public GamaObjectMatrix lastdetailedvarvalues; // one line per agent //option to deactivate it
	public GamaFloatMatrix averagehistory; // one line per step
	public GamaFloatMatrix stdevhistory;// one line per step
	public GamaFloatMatrix minhistory;// one line per step
	public GamaFloatMatrix maxhistory;// one line per step
	public GamaObjectMatrix distribhistoryparams; // one line per step, params for the distribhistory: cl et st: xmin=st
													// * 2^cl le nb de clust max est un param global
	public GamaObjectMatrix distribhistory; // one line per step, one GamaIntMatrix per cell

	public GamaMap<Integer, String> getVarmap() {
		return varmap;
	}

	public void setVarmap(final GamaMap<Integer, String> varmap) {
		this.varmap = varmap;
	}

	public GamaMap<Integer, String> getNumvarmap() {
		return numvarmap;
	}

	public void setNumvarmap(final GamaMap<Integer, String> numvarmap) {
		this.numvarmap = numvarmap;
	}

	public GamaMap<Integer, String> getQualivarmap() {
		return qualivarmap;
	}

	public void setQualivarmap(final GamaMap<Integer, String> qualivarmap) {
		this.qualivarmap = qualivarmap;
	}

	public GamaMatrix<?> getMetadatahistory() {
		return metadatahistory;
	}

	public void setMetadatahistory(final GamaObjectMatrix metadatahistory) {
		this.metadatahistory = metadatahistory;
	}

	public GamaMatrix<?> getLastdetailedvarvalues() {
		return lastdetailedvarvalues;
	}

	public void setLastdetailedvarvalues(final GamaObjectMatrix lastdetailedvarvalues) {
		this.lastdetailedvarvalues = lastdetailedvarvalues;
	}

	public GamaFloatMatrix getAveragehistory() {
		return averagehistory;
	}

	public void setAveragehistory(final GamaFloatMatrix averagehistory) {
		this.averagehistory = averagehistory;
	}

	public GamaFloatMatrix getStdevhistory() {
		return stdevhistory;
	}

	public void setStdevhistory(final GamaFloatMatrix stdevhistory) {
		this.stdevhistory = stdevhistory;
	}

	public GamaFloatMatrix getMinhistory() {
		return minhistory;
	}

	public void setMinhistory(final GamaFloatMatrix minhistory) {
		this.minhistory = minhistory;
	}

	public GamaFloatMatrix getMaxhistory() {
		return maxhistory;
	}

	public void setMaxhistory(final GamaFloatMatrix maxhistory) {
		this.maxhistory = maxhistory;
	}

	public GamaMatrix<?> getDistribhistoryparams() {
		return distribhistoryparams;
	}

	public void setDistribhistoryparams(final GamaObjectMatrix distribhistoryparams) {
		this.distribhistoryparams = distribhistoryparams;
	}

	public GamaMatrix<?> getDitribhistory() {
		return distribhistory;
	}

	public void setDitribhistory(final GamaObjectMatrix ditribhistory) {
		this.distribhistory = ditribhistory;
	}

	Boolean isAgentCreated;

	public Boolean getIsAgentCreated() {
		return isAgentCreated;
	}

	public void setIsAgentCreated(final Boolean isAgentCreated) {
		this.isAgentCreated = isAgentCreated;
	}

	public void init(final IScope scope) {
		isAgentCreated = false;
		varmap = (GamaMap<Integer,String>) GamaMapFactory.create(Types.INT, Types.STRING);
		numvarmap = (GamaMap<Integer,String>) GamaMapFactory.create(Types.INT, Types.STRING);
		qualivarmap = (GamaMap<Integer,String>) GamaMapFactory.create(Types.INT, Types.STRING);
		metadatahistory = new GamaObjectMatrix(0, 0, msi.gaml.types.Types.NO_TYPE);
		lastdetailedvarvalues = new GamaObjectMatrix(0, 0, msi.gaml.types.Types.NO_TYPE);
		averagehistory = new GamaFloatMatrix(0, 0);
		stdevhistory = new GamaFloatMatrix(0, 0);
		minhistory = new GamaFloatMatrix(0, 0);
		maxhistory = new GamaFloatMatrix(0, 0);
		distribhistoryparams = new GamaObjectMatrix(0, 0, msi.gaml.types.Types.NO_TYPE);
		distribhistory = new GamaObjectMatrix(0, 0, msi.gaml.types.Types.NO_TYPE);
		final IList<Integer> premlist = GamaListFactory.create(Types.NO_TYPE);
		premlist.add(0);
		distribhistory.set(scope, 0, 0, premlist);
	}
}
