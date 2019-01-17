package escape.gaml.operator;

import escape.gama.preprocessing.PedestrianNetwork;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gaml.types.IType;

public class Operators {
	@operator (
			value = { "generate_pedestrian_network" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL},
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION})
	@doc (
			value = "A list of lines forming a graph computed from an open space that can be used by the pedestrian skill")


	public static IList<IShape>  generatePedestrianNetwork(IScope scope, IList<String> obstaclesPath, String boundsPath, double valTolClip,double valTolTri,
				double valFiltering, boolean openAreaManagement,double valDistForOpenArea, double valDensityOpenArea, boolean randomDist, boolean cleanNetwork  ){
	   
		return PedestrianNetwork.generateNetwork(scope, obstaclesPath, boundsPath, valTolClip,valTolTri,
				valFiltering, openAreaManagement,valDistForOpenArea, valDensityOpenArea, randomDist,  cleanNetwork );
	   
	}
	
	@operator (
			value = { "generate_pedestrian_network" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL},
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION})
	@doc (
			value = "A list of lines forming a graph computed from an open space that can be used by the pedestrian skill")


	public static IList<IShape>  generatePedestrianNetwork(IScope scope, IList<String> obstaclesPath, String boundsPath ){
	   
		return PedestrianNetwork.generateNetwork(scope, obstaclesPath, boundsPath, 0.1,0.1,	0.0, false,0.0, 0.0, true, false );
	   
	}


}
