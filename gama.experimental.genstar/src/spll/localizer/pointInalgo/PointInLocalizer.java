package spll.localizer.pointInalgo;

import java.util.List;


import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;

public interface PointInLocalizer {

	public GamaPoint pointIn(IScope scope, IShape geom);

	public List<GamaPoint> pointIn(IScope scope,IShape geom, int nb);
	
}
