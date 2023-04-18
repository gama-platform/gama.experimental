package spll.datamapper.matcher;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaField;

public interface ISPLMatcherFactory<V, T> { 

	public List<ISPLMatcher<V, T>> getMatchers(IScope scope, IShape geoData, 
			GamaField  regressorsField) 
					throws IOException, TransformException, InterruptedException, ExecutionException;

	public List<ISPLMatcher<V, T>> getMatchers(IScope scope,IList<IShape> entities,GamaField regressorsField) 
			 		throws IOException, TransformException, InterruptedException, ExecutionException;
	
}
