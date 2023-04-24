package spll.datamapper.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import core.util.GSPerformanceUtil;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;
import msi.gaml.operators.Cast;

public class SPLAreaMatcherFactory implements ISPLMatcherFactory<String, Double> {

	private int matcherCount = 0;

	public SPLAreaMatcherFactory() {
	}

	@Override
	public List<ISPLMatcher<String, Double>> getMatchers(IScope scope, IShape geoData, 
			GamaField regressorsField)  
			throws IOException, TransformException, InterruptedException, ExecutionException { 
		IList l = GamaListFactory.create();
		l.add(geoData);
		return getMatchers(scope, l, regressorsField);
	}

	@Override
	public List<ISPLMatcher<String, Double>> getMatchers(IScope scope, IList<IShape> entities,GamaField regressorsField) { 
	/*	GSPerformanceUtil gspu = new GSPerformanceUtil("Start processing regressors' data");
		gspu.setObjectif(entities.size());
		List<ISPLMatcher<String, Double>> varList = entities
				.stream().map(entity -> getMatchers(scope, entity, 
						regressorsField.getCellsIntersecting(scope, entity).iterator(), 
						this.variables, gspu))
				.flatMap(list -> list.stream()).toList();
		gspu.sysoStempMessage("-------------------------\n"
				+ "process ends up with "+varList.size()+" collected matches");
		return varList;*/
		return null;
	}

	// ----------------------------------------------------------- //

	/*
	 * TODO: could be optimise
	 */
	private List<ISPLMatcher<String, Double>> getMatchers(IScope scope, IShape entity,
			Iterator<? extends IShape> geoData, 
					GSPerformanceUtil gspu) {
		List<ISPLMatcher<String, Double>> areaMatcherList = new ArrayList<>();
		while(geoData.hasNext()){
			IShape geoEntity = geoData.next(); 
			/*for(String prop : variables){
				if (!geoEntity.hasAttribute(prop)) continue;
				Double value = Cast.asFloat(scope, geoEntity.getAttribute(prop));
				if (value == null) continue;
				Optional<ISPLMatcher<String, Double>> potentialMatch = areaMatcherList
						.stream().filter(varMatcher -> varMatcher.getVariable().equals(prop)).findFirst();
				if(potentialMatch.isPresent()){
					// IF Variable is already matched, update area
					potentialMatch.get().expandValue(geoEntity.getArea());
				} else {
					// ELSE create Variable based on the feature and create SPLAreaMatcher with basic area
					//if(!geoEntity.getPropertyAttribute(prop).equals(value))
					areaMatcherList.add(new SPLAreaMatcher(entity, prop, geoEntity.getArea()));
				}
			}*/
		}
		if(gspu != null && ((++matcherCount+1)/gspu.getObjectif() * 100) % 10 == 0d)
			gspu.sysoStempPerformance((matcherCount+1)/gspu.getObjectif(), this);
		return areaMatcherList;
	}

}
