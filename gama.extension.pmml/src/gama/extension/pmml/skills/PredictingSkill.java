package gama.extension.pmml.skills;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.file.GamaFile;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import jakarta.xml.bind.JAXBException;

@skill(name="predicting",
doc = @doc("Add the capacity to load and manipulate machine learning models saved in pmml files.")
)
@vars({
	@variable(name="models", type=IType.LIST, doc = @doc("The list of all loaded models"))
})
public class PredictingSkill extends Skill {


	public static final String EVALUATOR_NAME 	= "evaluator_name";
	public static final String EVALUATOR_FILE 	= "evaluator_file";
	public static final String ARGUMENTS		= "arguments";
	
	
	private Map<String, Evaluator> evaluators;
	private String latestEvaluator;
	
	
	PredictingSkill(){
		evaluators = new HashMap<String, Evaluator>();
	}
	
	
	
	@action(name="load_evaluator",
			args =  {
				@arg(	name = PredictingSkill.EVALUATOR_NAME,
						type = IType.STRING,
						doc = @doc("The name of the evaluator. It will be required later to retrieve on which evaluator to perform operations (verify, predict etc.)")
				),
				@arg(	name = PredictingSkill.EVALUATOR_FILE,
						type = IType.FILE,
						doc = @doc("The file containing the predictor to load.")
				)
			}
		)
	public boolean loadEvaluator(final IScope scope) {
		
		try {
			
			final String name = scope.getTypedArgIfExists(EVALUATOR_NAME, IType.STRING); 
			final GamaFile evaluatorFile = scope.getTypedArgIfExists(EVALUATOR_FILE, IType.FILE);
			
			evaluators.put(name, new LoadingModelEvaluatorBuilder().load(evaluatorFile.getFile(scope)).build());
			latestEvaluator = name;
		} catch (IOException | ParserConfigurationException | SAXException | JAXBException e) {
			e.printStackTrace();
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(e, scope), false);
			return false;
		}
		return true;
	}
	
	
	@action(name="verify_evaluator",
			args = {
					@arg(	name=PredictingSkill.EVALUATOR_NAME, 
							type = IType.STRING, 
							doc = @doc("The name of the evaluator to verify. If not provided the latest loaded will be used."),
							optional = true
					)
			},
			doc = @doc("Verifies the evaluator. Returns false if the evaluator hasn't been loaded yet or if the verification failed.")
			)
	public Boolean verifyEvaluator(final IScope scope) {
		String name = scope.getTypedArgIfExists(EVALUATOR_NAME, IType.STRING);
		String key = Strings.isNullOrEmpty(name) ? latestEvaluator : name;
		if (evaluators.containsKey(key)) {
			try {
				var evalVerified = evaluators.get(key).verify();
				evaluators.put(key, evalVerified);
				return true;				
			}
			catch (Exception ex) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
				return false;
			}
		}
		return false;
	}
	
	@action(name="get_input_field_names",
			args = {
					@arg(	name=PredictingSkill.EVALUATOR_NAME, 
							type = IType.STRING, 
							doc = @doc("The name of the evaluator from which to fetch input fields. If not provided the latest loaded will be used."),
							optional = true
					)
			},
			doc = @doc("Fetch the input fields. Returns a list of the field names. In case of error or if the evaluator doesn't exist, returns an empty list.")
			)
	public IList<String> getInputFieldNames(final IScope scope){
		String name = scope.getTypedArgIfExists(EVALUATOR_NAME, IType.STRING);
		String key = Strings.isNullOrEmpty(name) ? latestEvaluator : name;
		var ret = GamaListFactory.<String>create();
		if (evaluators.containsKey(key)) {
			try {
				for(var field : evaluators.get(key).getInputFields()) {
					ret.add(field.getName());
				}				
			}
			catch (Exception ex) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
				return GamaListFactory.EMPTY_LIST;
			}
		}
		return ret;
	}
	
	@action(name="get_target_field_names",
			args = {
					@arg(	name=PredictingSkill.EVALUATOR_NAME, 
							type = IType.STRING, 
							doc = @doc("The name of the evaluator from which to fetch target fields. If not provided the latest loaded will be used."),
							optional = true
					)
			},
			doc = @doc("Fetch the target fields. Returns a list of the field names. In case of error or if the evaluator doesn't exist, returns an empty list.")
			)
	public IList<String> getTargetFieldNames(final IScope scope){
		String name = scope.getTypedArgIfExists(EVALUATOR_NAME, IType.STRING);
		String key = Strings.isNullOrEmpty(name) ? latestEvaluator : name;
		var ret = GamaListFactory.<String>create();
		if (evaluators.containsKey(key)) {
			try {
				for(var field : evaluators.get(key).getTargetFields()) {
					ret.add(field.getName());
				}				
			}
			catch (Exception ex) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
				return GamaListFactory.EMPTY_LIST;
			}
		}
		return ret;
	}
	
	@action(name="get_output_field_names",
			args = {
					@arg(	name=PredictingSkill.EVALUATOR_NAME, 
							type = IType.STRING, 
							doc = @doc("The name of the evaluator from which to fetch output fields. If not provided the latest loaded will be used."),
							optional = true
					)
			},
			doc = @doc("Fetch the output fields. Returns a list of the field names. In case of error or if the evaluator doesn't exist, returns an empty list.")
			)
	public IList<String> getOutputFieldNames(final IScope scope){
		String name = scope.getTypedArgIfExists(EVALUATOR_NAME, IType.STRING);
		String key = Strings.isNullOrEmpty(name) ? latestEvaluator : name;
		var ret = GamaListFactory.<String>create();
		if (evaluators.containsKey(key)) {
			try {
				for(var field : evaluators.get(key).getOutputFields()) {
					ret.add(field.getName());
				}				
			}
			catch (Exception ex) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
				return GamaListFactory.EMPTY_LIST;
			}
		}
		return ret;
	}
	
	@action(name="evaluate",
			args = {
					@arg(	name=PredictingSkill.EVALUATOR_NAME, 
							type = IType.STRING, 
							doc = @doc("The name of the evaluator to call. If not provided the latest loaded will be used."),
							optional = true
					),
					@arg(	name=PredictingSkill.ARGUMENTS, 
					type = IType.MAP,
					doc = @doc("The map containing all the arguments to evaluate the model. The keys representing the name of the fields and the value being their actual value."),
					optional = false
			)
			},
			doc = @doc("Evaluates a model given a set of parameters. Returns a map of the field names associated with their values. In case of error or if the evaluator doesn't exist, returns an empty map.")
			)
	public IMap<String, ?> evaluateEvaluator(final IScope scope){
		String name = scope.getTypedArgIfExists(EVALUATOR_NAME, IType.STRING);
		String key = Strings.isNullOrEmpty(name) ? latestEvaluator : name;
		IMap<String, ?> args = scope.getTypedArgIfExists(ARGUMENTS, IType.MAP);
		
		//If we cannot fetch the arguments or cannot find the evaluator, we return an empty map
		if (!evaluators.containsKey(key)) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning("Cannot find evaluator: '" + key +"' make sure it has been loaded with the `load_evaluator` action before calling this action", scope), false);
			return GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		}
		
		if (args == null) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning("Value for the `arguments` facet is nil or not a map", scope), false);
			return GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		}
		
		try {
			var results = evaluators.get(key).evaluate(args);
			return GamaMapFactory.create(scope, Types.STRING, Types.NO_TYPE, results);			
		}
		catch (Exception ex) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
			return GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		}

		
	}

	@getter("models")
	public IList<String> getAllLoadedModels(){
		return GamaListFactory.create(Types.LIST, evaluators.keySet().stream()); 
	}
	
	
}
