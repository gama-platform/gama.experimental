package gama.experimental.gampy;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.onnxruntime.runner.OnnxRuntimeRunner;

import gama.core.metamodel.agent.IAgent;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@skill (name = "gampy",
	concept = { IConcept.SKILL },
	doc = @doc ("gamapy doc"))
@vars ({
	@variable (
			name = "model_path",
			type = IType.STRING,
			init = "",
			doc = @doc ("path to NN model")),
	@variable (
			name = "model_type",
			type = IType.STRING,
			init = "",
			doc = @doc ("type of NN model"))
})

public class GampySkills extends Skill {
	public final static String MODEL_PATH = "model_path";
	// tensorflow/keras, pytorch (onnx), scikit-learn
	public final static String MODEL_TYPE = "model_type"; 
	public final static String INTPUT_SIZE = "input_size";
	public final static String OUTPUT_SIZE = "output_size";
	
	public final static String PREDICT = "predict";
	public final static String INPUT_VALUE = "input_value";
	public final static String NB_FEATURES = "nb_features";
	public final static String INPUT_VARS = "input_var";
	public final static String OUTPUT_VARS = "output_var";
	
	public MultiLayerNetwork kerasModel = null;
	public OnnxRuntimeRunner torchModel = null;
	
	@getter (MODEL_TYPE)
	public String getModelType(final IAgent agent) {
		return (String) agent.getAttribute(MODEL_TYPE);
	}
	
	@setter (MODEL_TYPE)
	public void setModelType(final IAgent agent, final String val) {
		agent.setAttribute(MODEL_TYPE, val);
	}
	
	@getter (MODEL_PATH)
	public String getModelPath(final IAgent agent) {
		return (String) agent.getAttribute(MODEL_PATH);
	}
	
	@setter (MODEL_PATH)
	public void setModelPath(final IScope scope, final IAgent agent, final String val) {
		agent.setAttribute(MODEL_PATH, val);
		// load model
//		if (!new File(fileName).exists()) throw new FileNotFoundException("File " + fileName + " does not exist.");
		
		String filePath = scope.getModel().getWorkingPath() + '/' + val;
		if ("keras".equals(getModelType(agent))) {
			try {				
				kerasModel = KerasModelImport.importKerasSequentialModelAndWeights(filePath);
			} catch (Exception e) {
				System.out.println(e);
			}
		} else if ("pytorch".equals(getModelType(agent))) {
			File myFile = new File (filePath);
			torchModel = OnnxRuntimeRunner.builder()
		            .modelUri(myFile.getAbsolutePath())
		            .build();
		} else {
			System.out.println("Not a right format");
		}
	}
	
	@action (
			name = PREDICT,
			args = {@arg (
						name = INPUT_VALUE,
						type = IType.LIST,
						optional = false,
						doc = @doc ("input value for NN")),
					@arg (
						name = NB_FEATURES,
						type = IType.INT,
						optional = false,
						doc = @doc ("size of input value")),
					@arg (
						name = INPUT_VARS,
						type = IType.STRING,
						optional = true,
						doc = @doc ("input variables for pytorch model")),
					@arg (
						name = OUTPUT_VARS,
						type = IType.STRING,
						optional = true,
						doc = @doc ("output variables for pytorch model")),
			},

			doc = @doc (
					value = "action to predict with NN",
					examples = { @example ("") }))
	
	public IList<IList<Double>> primPredict(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final Object inputValueObj = scope.getArg(INPUT_VALUE, IType.NONE);
		ArrayList<ArrayList> inputValue  = (ArrayList<ArrayList>) inputValueObj;
		final Object nbFeaturesObj = scope.getArg(NB_FEATURES, IType.NONE);
		int nbFeatures = (int) nbFeaturesObj;
		
		IList<IList<Double>> predictions = GamaListFactory.create(Types.FLOAT);

		if ("keras".equals(getModelType(agent))) {
			predictions = predictKerasModel(scope, agent, nbFeatures, inputValue);
		} else {
			final Object inputVarsObj = scope.getArg(INPUT_VARS, IType.NONE);
			String inputVars  = (String) inputVarsObj;
			final Object outputVarsObj = scope.getArg(OUTPUT_VARS, IType.NONE);
			String outputVars  = (String) outputVarsObj;
			
			predictions = predictTorchModel(scope, agent, nbFeatures, inputValue, inputVars, outputVars);
		}
		
		return predictions;
	}

	public IList<IList<Double>> predictKerasModel (final IScope scope, final IAgent agent, final int nbFeatures, final ArrayList<ArrayList> inputValue) {
		int nbRows = inputValue.size();
		INDArray inputData = Nd4j.zeros(nbRows, nbFeatures);
		for (int i=0; i<nbRows; i++) {
			for (int j=0; j<nbFeatures; j++)
				inputData.putScalar(new int[] {i*nbFeatures + j}, (double) inputValue.get(i).get(j));
		}	

		// get the prediction
		INDArray output = kerasModel.output(inputData);
		int nbOutputRows = (int) output.shape()[0];
		int nbOutputColumns = (int) output.shape()[1];
		
		// convert output to IList
		IList<IList<Double>> predictions = GamaListFactory.create(Types.FLOAT);
		
		for (int i=0; i<nbOutputRows; i++) {
			IList<Double> row = GamaListFactory.create(Types.FLOAT);
			for (int j=0; j<nbOutputColumns; j++) {
				row.add(output.getDouble(i * nbOutputColumns + j));
			}
			
			predictions.add(row);
		}

		return predictions;
	}

	public IList<IList<Double>> predictTorchModel (final IScope scope, final IAgent agent, final int nbFeatures, final ArrayList<ArrayList> inputValue, final String inputVar, final String outputVar) {   
		Map<String,INDArray> inputs = new LinkedHashMap<>();
		
		int nbRows = inputValue.size();
		INDArray inputData = Nd4j.zeros(nbRows, nbFeatures);
		for (int i=0; i<nbRows; i++) {
			for (int j=0; j<nbFeatures; j++)
				inputData.putScalar(new int[] {i*nbFeatures + j}, (double) inputValue.get(i).get(j));
		}
		
		inputs.put(inputVar, inputData);
		Map<String, INDArray> exec = torchModel.exec(inputs);
		INDArray output = exec.get(outputVar);
		
		int nbOutputRows = (int) output.shape()[0];
		int nbOutputColumns = (int) output.shape()[1];
		
		// convert output to IList
		IList<IList<Double>> predictions = GamaListFactory.create(Types.FLOAT);
		
		for (int i=0; i<nbOutputRows; i++) {
			IList<Double> row = GamaListFactory.create(Types.FLOAT);
			for (int j=0; j<nbOutputColumns; j++) {
				row.add(output.getDouble(i * nbOutputColumns + j));
			}
			
			predictions.add(row);
		}
		
		return predictions;
	}
}
