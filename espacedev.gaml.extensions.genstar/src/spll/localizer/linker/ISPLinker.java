package spll.localizer.linker;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import core.metamodel.entity.IEntity;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import spll.localizer.constraint.ISpatialConstraint;
import spll.localizer.distribution.ISpatialDistribution;

/**
 * Encapsulate the process that binds an entity of a population to a spatial
 * entity
 * <p>
 * Basically made of a spatial distribution that define probability of each
 * spatial entity candidate to be chosen to be bind with the entity, and spatial
 * constraints that will filter candidates to only keep acceptable ones
 * 
 * @author kevinchapuis
 *
 * @param <SD>
 */
public interface ISPLinker<IShape> {

	public enum ConstraintsReleaseRule {
		PRIORITY, LINEAR;
	}
	
	public void assignLink(IScope scope, IList<IAgent> entities, IList<? extends IShape> candidates, String attributeName);
	/**
	 * Main method to link an entity to one candidate draw from a IList. There is two aspect involve:
	 * <ul>
	 *  <li> a {@link ISpatialDistribution} to draw spatial entities from
	 *  <li> a IList of {@link ISpatialConstraint} to prior filter candidates spatial entities
	 * </ul>
	 * 
	 * @param entity
	 * @param candidates
	 * @return
	 */
	public Optional<IShape> getCandidate(IScope scope, IAgent entity,
			IList<IShape> candidates);
	
	/**
	 * Main method to link a set of synthetic entities with a chosen spatial entity candidate drawn from a IList.
	 * This involves the same element as in {@link #getCandidate(IEntity, IList)} except that filtering is made once
	 * 
	 * @param entity
	 * @param candidates
	 * @return
	 */
	public Map<IAgent, Optional<IShape>>  getCandidates(IScope scope,IList<IAgent> entities,
			IList<IShape> candidates);

	/**
	 * Set the distribution to be used to sort linked places
	 * 
	 * @param distribution
	 */
	public void setDistribution(ISpatialDistribution<IShape> distribution);
	
	/**
	 * The distribution to be used
	 * 
	 * @return {@link ISpatialDistribution} the spatial distribution used to draw a candidate
	 */
	public ISpatialDistribution<IShape> getDistribution();

	/**
	 * Filter the IList of given candidate to fit {@link ISPLinker} constraints requirement without releasing them
	 * 
	 * @param candidates
	 * @return the IList of fitting spatial entities that are acceptable candidates
	 */
	public IList<IShape> filter(IScope scope,
			IList<IShape> candidates);
	
	/**
	 * Filter the IList of given candidate with constraint release
	 * 
	 * @param candidates
	 * @return the IList of fitting spatial entities that are acceptable candidates
	 */
	public IList<IShape> filterWithRelease(IScope scope,
			IList<IShape> candidates);
	
	/**
	 * Set a new list of constraints
	 * 
	 * @param constraints
	 */
	public void setConstraints(List<ISpatialConstraint> constraints);
	
	/**
	 * Add constraints to filter candidates with
	 * 
	 * @param constraints
	 */
	public void addConstraints(ISpatialConstraint... constraints);

	/**
	 * Get back the IList of constaints
	 * 
	 * @return {@link List} The list of spatial constraints to filter candidates for a link
	 */
	public List<ISpatialConstraint> getConstraints();
	
	/**
	 * The rule to release constraints
	 * 
	 * @return {@link ConstraintsReleaseRule}
	 */
	public ConstraintsReleaseRule getConstraintsReleaseRule();

	/**
	 * Set the rule to be used to release constraints
	 * 
	 * @param rule
	 */
	public void setConstraintsReleaseRule(ConstraintsReleaseRule rule);
	
}
