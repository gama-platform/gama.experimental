package gama.extension.pmml.types;

import org.jpmml.evaluator.Evaluator;

import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.extension.pmml.skills.PredictionOperators;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

@type(name = "evaluator", id = PMMLEvaluatorType.id, wraps = { PMMLEvaluator.class }, concept = { IConcept.TYPE, "pmml" })
public class PMMLEvaluatorType extends GamaType<PMMLEvaluator>{

	final static int id = IType.AVAILABLE_TYPES + 4532236;

	@Override
	public PMMLEvaluator getDefault() {
		return null;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public PMMLEvaluator cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if (obj instanceof PMMLEvaluator eval) {
			return eval;
		}
		else if (obj instanceof Evaluator eval) {
			return new PMMLEvaluator(eval);
		}
		else if (obj instanceof String path) {
			return new PMMLEvaluator(PredictionOperators.loadEvaluator(scope, path));
		}
		return null;
	}
}
