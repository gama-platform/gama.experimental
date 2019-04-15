package miat.gaml.extensions.argumentation.types;

import msi.gama.common.interfaces.IValue;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars({ @variable(name = "id", type = IType.STRING), @variable(name = "option", type = IType.STRING),
	@variable(name = "conclusion", type = IType.STRING),
	@variable(name = "statement", type = IType.STRING), @variable(name = "rationale", type = IType.STRING),
	@variable(name = "criteria", type = IType.MAP), @variable(name = "actor", type = IType.AGENT),
	@variable(name = "source_type", type = IType.STRING) })
public class GamaArgument  implements IValue{
	private String id = "0" ;
	private String option = "";
	private String conclusion = "";
	private String statement = "";
	private String rationale = "";
	private GamaMap<String, Double> criteria = null;
	private IAgent actor = null;
	private String sourceType = "";
	
	
	
	public GamaArgument(String id, String option, String conclusion, String statement, String rationale,
			GamaMap<String, Double> criteria, IAgent actor, String sourceType) {
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
	public GamaMap<String, Double> getCriteria() {
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

	@Override
	public String serialize(boolean includingBuiltIn) {
		return "("+ id + "," +(actor != null ? actor : "") + ":" + statement + " -> " + rationale + " - " + criteria; 
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


	
	
}
