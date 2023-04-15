package spll.localizer.distribution.function;

import java.util.function.BiFunction;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
/**
 * TODO javadoc
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public interface ISpatialComplexFunction<N extends Number> extends BiFunction<IAgent,IShape, N> {

	/**
	 * TODO: javadoc
	 * 
	 * @param entities
	 * @param candidates
	 */
	public void updateFunctionState(IScope scope, IList<IAgent> entities, IList<IShape> candidates);
	
	/**
	 * TODO
	 */
	public void clear();
	
}
