package gama.experimental.argumentation.types;

import gama.core.common.interfaces.IValue;
import gama.core.metamodel.agent.IAgent;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@vars({ @variable(name = "id", type = IType.STRING), 
		@variable(name = "option", type = IType.STRING),
		@variable(name = "conclusion", type = IType.STRING),
		@variable(name = "statement", type = IType.STRING), 
		@variable(name = "rationale", type = IType.STRING),
		@variable(name = "criteria", type = IType.MAP), 
		@variable(name = "actor", type = IType.AGENT),
		@variable(name = "source_type", type = IType.NONE) })
public class GamaArgument  implements IValue{
	private String id = "0";
	private String option = "";
	private String conclusion = "";
	private String statement = "";
	private String rationale = "";
	private IMap<String, Double> criteria = null;
	private IAgent actor = null;
	private String sourceType = "";
	
	public GamaArgument(String id, String option, String conclusion, String statement, String rationale,
			IMap<String, Double> criteria, IAgent actor, String sourceType) {
		super();
		this.id = id;
		this.option = option;
		this.conclusion = conclusion;
		this.statement = statement;
		this.rationale = rationale;
		this.criteria = criteria;
		this.actor = actor;
		this.sourceType = sourceType;
	}

	@getter("id")
	public String getId() {
		return id;
	}

	@getter("option")
	public String getOption() {
		return option;
	}
	
	@getter("conclusion")
	public String getConclusion() {
		return conclusion;
	}

	@getter("statement")
	public String getStatement() {
		return statement;
	}
	
	@getter("rationale")
	public String getRationale() {
		return rationale;
	}

	@getter("criteria")
	public IMap<String, Double> getCriteria() {
		return criteria;
	}

	@getter("actor")
	public IAgent getActor() {
		return actor;
	}

	@getter("source_type")
	public String getSourceType() {
		return sourceType;
	}

	public String serialize(boolean includingBuiltIn) {
		return id;
	}
	
	@Override
	public JsonValue serializeToJson(Json json) {
		return null;
	} 	

	@Override
	public IType<?> getGamlType() {
		return Types.get(GamaArgumentType.id);
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return serialize(true);
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		return new GamaArgument(id, option, conclusion, statement, rationale, criteria.copy(scope), actor, sourceType);
	}

	@Override
	public String toString() {
		return id ;
	}
	
	public void setActor(IAgent actor) {
		this.actor = actor;
	}
	
	@Override
	public boolean equals(final Object other) {
		if (this == other) {return true;}
		if (!(other instanceof GamaArgument)) {
			return false;
		}
		return this.id.equals(((GamaArgument) other).id);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}
}
