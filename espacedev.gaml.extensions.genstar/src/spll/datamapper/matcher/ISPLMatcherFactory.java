package spll.datamapper.matcher;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opengis.referencing.operation.TransformException;

import msi.gama.metamodel.shape.IShape;
import msi.gama.util.IList;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLMatcherFactory<V extends ISPLVariable, T> {

	public List<ISPLMatcher<V, T>> getMatchers(IShape geoData, 
			IList<IList<IShape>> ancillaryEntities) 
					throws IOException, TransformException, InterruptedException, ExecutionException;

	public List<ISPLMatcher<V, T>> getMatchers(IList<IShape> entities,IList<IList<IShape>> regressorsEntities) 
					throws IOException, TransformException, InterruptedException, ExecutionException;
	
}
