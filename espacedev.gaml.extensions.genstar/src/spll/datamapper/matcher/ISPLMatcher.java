package spll.datamapper.matcher;

import msi.gama.metamodel.shape.IShape;

public interface ISPLMatcher<V, T> {

	public T getValue();
	
	public boolean expandValue(T expand);
	
	public V getVariable();

	public IShape getEntity();
	
	public String toString();

}
