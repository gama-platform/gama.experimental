package spll.localizer.constraint;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gaml.operators.Containers;
import msi.gaml.operators.Spatial.Queries;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.types.Types;

public class SpatialConstraintLocalization extends ASpatialConstraint {

	IShape bounds;
	protected IContainer<?, ? extends IShape> geoms;
	
	public SpatialConstraintLocalization(IShape bounds) {
		super();
		this.bounds = bounds;
	}

	@Override
	public IList<IShape> getCandidates(IScope scope, IContainer<?, ? extends IShape> nests) {
		if (bounds == null) return (nests == null ? null : (IList<IShape>) (nests.listValue(scope, Types.GEOMETRY, false)));
		IList<IShape> cands = null;
		if (geoms != null ) {
			cands = (IList<IShape>) Queries.overlapping(scope, geoms, bounds);
			//System.out.println("bounds "+ bounds);
			if (cands.isEmpty()) return cands;
			
			IShape cu= Transformations.convex_hull(scope, bounds);
			cands.removeIf(a -> !a.getGeometry().getLocation().intersects(cu));
			if (nests != null) {
				cands = Containers.inter(GAMA.getRuntimeScope(), cands, nests);
			}
		} else {
			cands = (IList<IShape>) GamaListFactory.createWithoutCasting(Types.GEOMETRY, nests.listValue(scope, Types.GEOMETRY, false).stream().filter(a -> a.getLocation().intersects(bounds)).toList());
		}
		return cands;
	}

	@Override
	public boolean updateConstraint(IShape nest) {
		return false;
	}

	@Override
	public void relaxConstraintOp(IList<IShape> nests) {
		if (bounds != null) 
			bounds = Transformations.enlarged_by(GAMA.getRuntimeScope(), bounds, increaseStep);
		else 
			currentValue = maxIncrease;
	}
	
	// ---------------------- //
	
	public IShape getBounds() {
		return bounds;
	}

	public void setBounds(IShape bounds) {
		this.bounds = bounds;
		currentValue = 0.0;
		constraintLimitReach = false;
	}

	public IContainer<?, ? extends IShape> getGeoms() {
		return geoms;
	}

	public void setGeoms(IContainer<?, ? extends IShape> geoms) {
		this.geoms = geoms;
	}


	


}
