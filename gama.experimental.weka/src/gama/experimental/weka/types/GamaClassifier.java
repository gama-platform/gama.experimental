package gama.experimental.weka.types;

import java.util.Map;

import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class GamaClassifier  implements IValue{
	private Classifier classifier;
	private Instances dataset;

	private Map<String,IList<String>> valsNominal;
	
	public String serialize(boolean includingBuiltIn) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public JsonValue serializeToJson(Json json) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public IType<?> getGamlType() {
		return Types.get(GamaClassifierType.id);
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		if (classifier == null) return "empty classifier";
		return classifier.toString();
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public Instances getDataset() {
		return dataset;
	}

	public void setDataset(Instances dataset) {
		this.dataset = dataset;
	}

	public Map<String, IList<String>> getValsNominal() {
		return valsNominal;
	}

	public void setValsNominal(Map<String, IList<String>> valsNominal) {
		this.valsNominal = valsNominal;
	}
	
}
