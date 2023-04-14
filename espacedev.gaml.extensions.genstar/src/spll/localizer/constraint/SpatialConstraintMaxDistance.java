package spll.localizer.constraint;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.types.Types;

public class SpatialConstraintMaxDistance extends ASpatialConstraint {

	private Map<IShape, Double> distanceToEntities;

	public SpatialConstraintMaxDistance(IList<IShape> distanceToEntities,
			Double distance) {
		this.distanceToEntities = distanceToEntities.stream().collect(Collectors
				.toMap(Function.identity(), entity -> distance));
	}
	
	public SpatialConstraintMaxDistance(Map<IShape, Double> distanceToEntities) {
		this.distanceToEntities = distanceToEntities;
	}
	
	@Override
	public IList<IShape> getCandidates(IScope scope, IList<IShape> nests) {
		return GamaListFactory.createWithoutCasting(Types.GEOMETRY,nests.stream().filter(nest -> distanceToEntities.keySet()
				.stream().anyMatch(entity -> entity.euclidianDistanceTo(nest) <= distanceToEntities.get(entity))).toList());
		
		/*		.sorted((c1, c2) -> Double.compare(
						distanceToEntities.keySet().stream().mapToDouble(entity -> c1.getGeometry().getCentroid()
								.distance(entity.getGeometry())).min().getAsDouble(),
						distanceToEntities.keySet().stream().mapToDouble(entity -> c2.getGeometry().getCentroid()
								.distance(entity.getGeometry())).min().getAsDouble()))*/
				
	}

	@Override
	public boolean updateConstraint(IShape nest) {
		return false;
	}

	@Override
	public void relaxConstraintOp(IList<IShape> distanceToEntities) {
		distanceToEntities.stream().forEach(entity -> 
			this.distanceToEntities.put(entity, 
					this.distanceToEntities.get(entity)+this.increaseStep));

	}

}
