package spll.localizer.pointInalgo;

import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.operators.Spatial.Punctal;

public class RandomPointInLocalizer implements PointInLocalizer{
	
	
	@Override
	public GamaPoint pointIn(IScope scope, IShape geom) {
		return Punctal.any_location_in(scope, geom);
	
		
	}
	@Override
	public IList<GamaPoint> pointIn(IScope scope, IShape geom, int nb) {
		IList<GamaPoint> points = GamaListFactory.create();
		for (int i = 0; i < nb; i++)
			points.add(pointIn(scope, geom));
		return points;
	}
}