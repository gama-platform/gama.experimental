package ird.maelia.bioraffinerie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.util.FileUtils;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.csv.CsvReader;
import gama.gaml.types.IType;

@species(name= "bioraffinerie", doc = @doc("represente une bioraffinerie"))
@vars({
	@variable(name = Bioraffinerie.PREDICTION_MODULE_PATH, type= IType.STRING, doc = @doc("The path to the root of the prediction module"))
})
public class Bioraffinerie extends GamlAgent {

	public static final String PREDICTION_MODULE_PATH = "prediction_module_path";
	
	protected String predictionModulePath;

	public Bioraffinerie(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}


	@getter(PREDICTION_MODULE_PATH)
	public String getPredictionModulePath() {
		return predictionModulePath;
	}
	@setter(PREDICTION_MODULE_PATH)
	public void setPredictionModulePath(String s) {
		predictionModulePath = s;
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	@action(name = "predict",
			args = {
					@arg(name = "prediction_script", type = IType.STRING, optional = false),
					@arg(name = "prediction_data_path", type = IType.STRING, optional = false),
					@arg(name = "input_var", type = IType.LIST, optional = false),
					@arg(name = "output_path", type = IType.STRING, optional = true),
					@arg(name = "python_interpreter", type = IType.STRING, optional = true),
			})
	public IList<IList<String>> predict(final IScope scope) {
		int exitVal = -1;
		IList<IList<String>> ret = GamaListFactory.create();
		GamaRuntimeException error = null;
		
		//get the input columns
		String interpreter_path = scope.getTypedArg("python_interpreter", IType.STRING);
		String predict_csv_path = scope.getTypedArg("prediction_data_path", IType.STRING);
		String script_path = scope.getTypedArg("prediction_script", IType.STRING);
		String inputVars = String.join(" ", scope.<List<String>>getTypedArg("input_var", IType.LIST));
		String output_path = scope.getTypedArgIfExists("output_path", IType.STRING, scope.getModel().getProjectPath());
		String command = "";
		
		try {
			String interpreter = interpreter_path != null 
					? FileUtils.constructAbsoluteFilePath(scope, interpreter_path, true) 
					: "python";
			String predict_csv = FileUtils.constructAbsoluteFilePath(scope, predict_csv_path, true);
			String output = FileUtils.constructAbsoluteFilePath(scope, output_path, false);
			String script = FileUtils.constructAbsoluteFilePath(scope, script_path, true);
			command = interpreter +  " "
					+ script + " "
					+ "--predict " + predict_csv + " "
					+ "--output_path " + output + " "
					+ "--input_var " + inputVars;
			Process p = Runtime.getRuntime().exec(command);
			exitVal = p.waitFor();
		}catch( Exception e) {
			error = GamaRuntimeException.create(e, scope);
			e.printStackTrace();
		}
		
		if (exitVal != 0 ) { // a problem happened
			if (error == null) {
				error = GamaRuntimeException.create(new Exception("Process ended with bad exit value: " + exitVal + "\nCommand run: " + command), scope);
			}
			GAMA.reportAndThrowIfNeeded(scope, error, true);
			return null;
		}
		try (var reader = new CsvReader(output_path + File.separator + "predicted_data.csv", ',')){
			var header = true;
			while(reader.readRecord()) {
				if (! header) {
					IList<String> line = GamaListFactory.create();
					for(var s : reader.getValues()) {
						line.add(s);
					}						
					ret.add(line);
				}
				header = false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
}
