package spll.localizer.pointInalgo;

import java.util.List;


import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;

public interface PointInLocalizer {

	public GamaPoint pointIn(IScope scope, IShape geom);

	public List<GamaPoint> pointIn(IScope scope,IShape geom, int nb);
	
}
