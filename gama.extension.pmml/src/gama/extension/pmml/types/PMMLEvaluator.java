package gama.extension.pmml.types;


import java.util.List;
import java.util.Map;

import org.dmg.pmml.MiningFunction;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;

import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;

public class PMMLEvaluator implements Evaluator, IValue{
	
	
	private final Evaluator internalEvaluator;
	
	public PMMLEvaluator(final Evaluator eval) {
		internalEvaluator = eval;
	}
	
	
	public static PMMLEvaluator from(final Evaluator eval) {
		return new PMMLEvaluator(eval);
	}
	
	@Override
	public JsonValue serializeToJson(Json json) {
		return json.typedObject(getGamlType());
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return super.toString();
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		//TODO: this is not really a copy, we just create a new pointer on the same evaluator
		return new PMMLEvaluator(internalEvaluator);
	}


	@Override
	public List<InputField> getInputFields() {
		return internalEvaluator.getInputFields();
	}


	@Override
	public List<InputField> getActiveFields() {
		return internalEvaluator.getActiveFields();
	}


	@Override
	public List<TargetField> getTargetFields() {
		return internalEvaluator.getTargetFields();
	}


	@Override
	public List<OutputField> getOutputFields() {
		return internalEvaluator.getOutputFields();
	}

	@Override
	public String getSummary() {
		return internalEvaluator.getSummary();
	}


	@Override
	public MiningFunction getMiningFunction() {
		return internalEvaluator.getMiningFunction();
	}


	@Override
	public Evaluator verify() {
		return internalEvaluator.verify();
	}


	@Override
	public Map<String, ?> evaluate(Map<String, ?> arguments) {
		return internalEvaluator.evaluate(arguments);
	}

}
