package spll.localizer.distribution;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;

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
	public IShape getCandidate(IScope scope, IAgent entity, IList<IShape> candidates);
		
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
	public void setCandidate(IList<IShape> candidates);
	
	/**
	 * 
	 * @return
	 */
	public IList<IShape> getCandidates(IScope scope);

}
