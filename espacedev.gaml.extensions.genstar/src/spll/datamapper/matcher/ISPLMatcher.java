package spll.datamapper.matcher;

import msi.gama.metamodel.shape.IShape;
import spll.datamapper.variable.ISPLVariable;

public interface ISPLMatcher<V extends ISPLVariable, T> {

	public String getName();
	
	public T getValue();
	
	public boolean expandValue(T expand);
	
	public V getVariable();

	public IShape getEntity();
	
	public String toString();

}
