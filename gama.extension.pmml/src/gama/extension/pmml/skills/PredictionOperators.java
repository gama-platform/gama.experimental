package gama.extension.pmml.skills;

import static gama.core.common.util.FileUtils.constructAbsoluteFilePath;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.xml.sax.SAXException;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.extension.pmml.types.PMMLEvaluator;
import gama.gaml.types.Types;
import jakarta.xml.bind.JAXBException;

public class PredictionOperators {
	
	
	
	@operator(value="load_eval_prediction",
			can_be_const = false)
	@doc ("Loads the PMML evaluator saved in the given file and returns it as an object of type unknown.")
	@no_test
	public static Evaluator loadEvaluator(final IScope scope,final String fileName) {
		
		try {
			var evaluatorFile = new File(constructAbsoluteFilePath(scope, fileName, true));
			return new LoadingModelEvaluatorBuilder().load(evaluatorFile).build();
		} catch (IOException | ParserConfigurationException | SAXException | JAXBException e) {
			e.printStackTrace();
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(e, scope), false);
			return null;
		}
	}
	

	@operator(value="verify_evaluator",
			doc = @doc("Verifies the evaluator. Returns false if the evaluator hasn't been loaded yet or if the verification failed.")
			)
	public static Boolean verifyEvaluator(final IScope scope, final PMMLEvaluator evaluator) {
		try {
			var evalVerified = evaluator.verify();
			return evalVerified != null;				
		}
		catch (Exception ex) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
			return false;
		}
	}


	@operator(value="get_input_field_names",
			doc = @doc("Fetch the input fields. Returns a list of the field names. In case of error or if the evaluator doesn't exist, returns an empty list.")
			)
	public static IList<String> getInputFieldNames(final IScope scope, final PMMLEvaluator evaluator){
		var ret = GamaListFactory.<String>create();
		try {
			for(var field : evaluator.getInputFields()) {
				ret.add(field.getName());
			}				
		}
		catch (Exception ex) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
			return GamaListFactory.EMPTY_LIST;
		}
		return ret;
	}
	
	@operator(value="get_target_field_names",
			doc = @doc("Fetch the target fields. Returns a list of the field names. In case of error or if the evaluator doesn't exist, returns an empty list.")
			)
	public static IList<String> getTargetFieldNames(final IScope scope, final PMMLEvaluator evaluator){

		var ret = GamaListFactory.<String>create();
		try {
			for(var field : evaluator.getTargetFields()) {
				ret.add(field.getName());
			}				
		}
		catch (Exception ex) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
			return GamaListFactory.EMPTY_LIST;
		}
		return ret;
	}
	
	@operator(value="get_output_field_names",
			doc = @doc("Fetch the output fields. Returns a list of the field names. In case of error or if the evaluator doesn't exist, returns an empty list.")
			)
	public static IList<String> getOutputFieldNames(final IScope scope, final PMMLEvaluator evaluator){
		var ret = GamaListFactory.<String>create();
		try {
			for(var field : evaluator.getOutputFields()) {
				ret.add(field.getName());
			}				
		}
		catch (Exception ex) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
			return GamaListFactory.EMPTY_LIST;
		}
		return ret;
	}
	
	
	@operator(value="evaluate",
			doc = @doc("Evaluates a model given a set of parameters. Returns a map of the field names associated with their values. In case of error or if the evaluator doesn't exist, returns an empty map.")
			)
	public static IMap<String, ?> evaluateEvaluator(final IScope scope, final PMMLEvaluator evaluator, IMap arguments){
		
		if (arguments == null) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning("Value for the `arguments` facet is nil or not a map", scope), false);
			return GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		}
		
		try {
			var jpmml_results = evaluator.evaluate(arguments);
			var results = EvaluatorUtil.decodeAll(jpmml_results);
			return GamaMapFactory.create(scope, Types.STRING, Types.NO_TYPE, results);			
		}
		catch (Exception ex) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), false);
			return GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		}

		
	}

	

}
