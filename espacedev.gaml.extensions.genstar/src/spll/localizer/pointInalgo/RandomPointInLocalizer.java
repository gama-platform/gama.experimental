package spll.localizer.pointInalgo;

import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Spatial.Punctal;

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
