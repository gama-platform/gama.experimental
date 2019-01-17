package escape.gama.preprocessing;

import java.io.File;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.GamaShapeFile;
import msi.gaml.operators.Spatial;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.types.Types;

public class PedestrianNetwork {

	
	public static IList<IShape> generateNetwork(IScope scope, IList<String> obstaclesPath, String boundsPath, double valTolClip,double valTolTri,
			double valFiltering, boolean openAreaManagement,double valDistForOpenArea, double valDensityOpenArea, boolean randomDist, boolean cleanNetwork  ){
    	
    	IList<GamaShapeFile> obstacleShapefiles = GamaListFactory.create(Types.FILE);
    	for (String path : obstaclesPath) {
    		File f = new File(path);
        	if (f.exists()) {
        		GamaShapeFile fa = new GamaShapeFile(scope,f.getAbsolutePath());
        		obstacleShapefiles.add(fa);
        	}
    	}
      if (obstacleShapefiles.isEmpty()) {
    		throw GamaRuntimeException.error( "You have to provide at least one obstacle shapefile", scope);
    	}
    	
    	File f = new File(boundsPath);
    	GamaShapeFile boundsShape = null;
    	if (f.exists()) {
    		boundsShape = new GamaShapeFile(scope,f.getAbsolutePath());
        }
    	Geometry bounds = null;
    	if (boundsShape ==null) {
    		for (GamaShapeFile shp : obstacleShapefiles) {
    			if (bounds == null)  bounds = shp.computeEnvelope(scope).toGeometry();
    			else bounds = bounds.union(shp.computeEnvelope(scope).toGeometry());
    		}
    	} else {
    		bounds = Spatial.Operators.union(scope, boundsShape.getContents(scope)).getInnerGeometry();
    		
    	}
    	IShape area = new GamaShape(bounds);
    	for (GamaShapeFile shp : obstacleShapefiles) {
    		for (IShape obs : shp.getContents(scope)) { 
	    		area = Operators.minus(scope, area, obs);
	    	}
    	}
    	if (area == null) {
    		GamaRuntimeException.error("Get a null area when computing the background geometry", scope);
    	}
    	
    	area = keepMainGeom(area);
    	if (openAreaManagement) {
    		area = managementOpenArea(scope,area,valDistForOpenArea, valDensityOpenArea, randomDist);
    	}
    	
     	
    	IList<IShape> lines = Transformations.skeletonize(scope, area, valTolClip, valTolTri, true);
    	if (valFiltering > 0.0) {
    		IShape areaTmp = Spatial.Transformations.reduced_by(scope, area, valFiltering);
    		lines.removeIf(l -> Spatial.Properties.overlaps(scope, l, areaTmp));
    	}
    	
    	
    	IShape unionL = Operators.union(scope, lines);
    	lines = Transformations.clean(scope,  (IList<IShape>) unionL.getGeometries(), 0.0, true, cleanNetwork);
    	
		return lines;
    	   	
    }
	
	 public static IShape managementOpenArea(IScope scope, IShape area,double valDistForOpenArea, double valDensityOpenArea, boolean randomDist ) {
	    	
	    	
	    	IShape areaTmp = Spatial.Transformations.reduced_by(scope, area, valDistForOpenArea);
	    	if (areaTmp != null) {
	    		List<GamaPoint> pts = GamaListFactory.create(Types.GEOMETRY);
				for (IShape g : areaTmp.getGeometries()) {
	    			if (g == null || g.getArea() == 0) continue;
	    			long nbPoints = Math.round(g.getArea() * valDensityOpenArea);
	    			if (nbPoints == 0) continue;
	    			if (randomDist) {
	    				for (int i = 0; i < nbPoints;i++) {
	    					pts.add(Spatial.Punctal.any_location_in(scope, g).toGamaPoint());
	    				}
	    			}
	    			else {
						double dimension = Math.sqrt(g.getArea() / nbPoints);
						List<IShape> squares = Spatial.Transformations.toSquares(scope, g, dimension);
						for (IShape sq : squares) pts.add(sq.getCentroid());
					}
	    		}
				for (GamaPoint pt : pts) {
					area = Operators.minus(scope, area, Spatial.Transformations.enlarged_by(scope, pt,0.01,5));
				}
	    	}
	    	return area;
	    }
	 

	    public static IShape keepMainGeom(IShape inGeom) {
	    	IShape result = inGeom;
	    	if (inGeom.getGeometries().size() > 1) {
	        	double maxArea = 0;
	            IShape g = null;
	            for (IShape s : inGeom.getGeometries()) {
	            	if (s.getArea() > maxArea) {
	            		maxArea = s.getArea();
	            		g = s;
	            	}
	            }
	            result = g;
	        }
	    	return result;
	    }
	    
	    
}
