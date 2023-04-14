package spll.localizer.constraint;

import java.util.List;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;

/**
 * Represents a spatial constraint which might return the candidates compliant 
 * with this constraint. Might be relaxed.
 * 
 * @author Samuel Thiriot
 */
public interface ISpatialConstraint {
	
	/**
	 * Return a filtered list of possible nest considering the spatial constraint defined
	 * 
	 * @param filteredCandidates : a set of geography to locate synthetic entities in
	 * @return
	 */
	public IList<IShape> getCandidates(IScope scope, IList<IShape> filteredCandidates);
	
	/**
	 * A way to update the constraint considering a set of possible spatial entities to locate synthetic entities in.
	 * <br/> For example, you may want to update the number of available slots in a building considering that you may have already place people in.
	 * 
	 * @param nest
	 * @return
	 */
	public boolean updateConstraint(IShape nest);
	
	/**
	 * How the constraints should be relaxed
	 * 
	 * @param nests
	 */
	public void relaxConstraint(IList<IShape> nests);
	
	/**
	 * The priority of the constraint (int)
	 * 
	 * @return
	 */
	public int getPriority();
	
	/**
	 * Either if the constraint limit have been reached or not
	 *
	 * @return
	 */
	public boolean isConstraintLimitReach();
	
	/**
	 * The current constraint value expressed as a double
	 * 
	 * @return
	 */
	public double getCurrentValue();
	
}
