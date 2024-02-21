package spll.localizer.distribution.function;

import java.util.function.BiFunction;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.IList;
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
