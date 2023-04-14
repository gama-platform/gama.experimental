package spll.localizer.constraint;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.operators.Containers;
import msi.gaml.operators.Spatial.Queries;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.types.Types;

public class SpatialConstraintLocalization extends ASpatialConstraint {

	IShape bounds;
	protected IList<IShape> geoms;
	
	public SpatialConstraintLocalization(IShape bounds) {
		super();
		this.bounds = bounds;
	}

	@Override
	public IList<IShape> getCandidates(IScope scope, IList<IShape> nests) {
		if (bounds == null) return nests;
		
		//System.out.println("nests: " + nests.size());
		IList<IShape> cands = null;
		if (geoms != null && !geoms.isEmpty()) {
			cands = (IList<IShape>) Queries.overlapping(null, geoms, bounds);
			cands.removeIf(a -> !a.getGeometry().getLocation().intersects(bounds));
			if (nests != null) {
				cands = Containers.inter(GAMA.getRuntimeScope(), cands, nests);
			}
		} else {
			cands = GamaListFactory.createWithoutCasting(Types.GEOMETRY, nests.stream().filter(a -> a.getLocation().intersects(bounds)).toList());
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

	public IList<IShape> getGeoms() {
		return geoms;
	}

	public void setGeoms(IList<IShape> geoms) {
		this.geoms = geoms;
	}


	


}
