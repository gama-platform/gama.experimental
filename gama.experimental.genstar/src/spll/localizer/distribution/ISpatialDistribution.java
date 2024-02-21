package spll.localizer.distribution;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.IList;

/**
 * Define the higher order concept to define and to draw a spatial entity from a discret distribution of spatial candidates
 * 
 * @author kevinchapuis
 *
 */
public interface ISpatialDistribution<IShape> {

	/**
	 * Draw a spatial entity from a list of candidates, given that it will be link to the provided population entity 
	 * @param entity
	 * @param candidates
	 * @return
	 */
	public IShape getCandidate(IScope scope, IAgent entity, IList<? extends IShape> candidates);
		
	/**
	 * Draw a spatial entity from a pre-determined set of candidate to be bind with given population entity
	 * @param entity
	 * @return
	 */
	public IShape getCandidate(IScope scope, IAgent entity);
	
	/**
	 * 
	 * @param candidates
	 */
	public void setCandidate(IList<? extends IShape> candidates);
	
	/**
	 * 
	 * @return
	 */
	public IList<IShape> getCandidates(IScope scope);

	
	public void removeNest(IShape n);

}
