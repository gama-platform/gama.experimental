/*******************************************************************************************************
 *
 * msi.gama.util.file.GamaGridFile.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package cict.gaml.extensions.geotiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Formatter;
import java.util.List;

import javax.swing.JOptionPane;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader; 
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
import msi.gama.common.geometry.Envelope3D;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.GamaGisFile;
import msi.gama.util.file.GamaGridFile;
import msi.gama.util.matrix.GamaFloatMatrix;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;
import msi.gaml.types.Types; 
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GamaGeotiffFile   {

//    	GamaGridReader reader;
	IMatrix coverage;
//	GridCoverage2D coverage;
//	public int nbBands;
 
//	class GamaGridReader {
//
//		int numRows, numCols;
//		IShape geom;
//		Number noData = -9999;
//
//		GamaGridReader(final IScope scope, final GamaGisFile ggf, final InputStream fis, final boolean fillBuffer)
//				throws GamaRuntimeException {
////			setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
//			AbstractGridCoverage2DReader store = null;
//			try {
////				if (fillBuffer) {
////					scope.getGui().getStatus(scope).beginSubStatus("Reading file " + getName(scope));
////				}
//				// Necessary to compute it here, because it needs to be passed
//				// to the Hints
//				final CoordinateReferenceSystem crs = ggf.getExistingCRS(scope);
//				if (isTiff(scope)) {
//					if (crs == null) {
//						store = new GeoTiffReader(getFile(scope));
//					} else {
//						store = new GeoTiffReader(getFile(scope),
//								new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
//					}
//					noData = ((GeoTiffReader) store).getMetadata().getNoData();
//				} else {
//					if (crs == null) {
//						store = new ArcGridReader(fis);
//					} else {
//						store = new ArcGridReader(fis, new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
//					}
//				}
//				final GeneralEnvelope genv = store.getOriginalEnvelope();
//				numRows = store.getOriginalGridRange().getHigh(1) + 1;
//				numCols = store.getOriginalGridRange().getHigh(0) + 1;
//				final Envelope3D env = Envelope3D.of(genv.getMinimum(0), genv.getMaximum(0), genv.getMinimum(1), genv.getMaximum(1), 0, 0);
//				computeProjection(scope, env);
//				final Envelope envP = gis.getProjectedEnvelope();
//				final double cellHeight = envP.getHeight() / numRows;
//				final double cellWidth = envP.getWidth() / numCols;
//				final IList<IShape> shapes = GamaListFactory.create(Types.GEOMETRY);
//				final double originX = envP.getMinX();
//				final double originY = envP.getMinY();
//				final double maxY = envP.getMaxY();
//				final double maxX = envP.getMaxX();
//				shapes.add(new GamaPoint(originX, originY));
//				shapes.add(new GamaPoint(maxX, originY));
//				shapes.add(new GamaPoint(maxX, maxY));
//				shapes.add(new GamaPoint(originX, maxY));
//				shapes.add(shapes.get(0));
//				geom = GamaGeometryType.buildPolygon(shapes);
//				if (!fillBuffer) { return; }
//
//				final GamaPoint p = new GamaPoint(0, 0);
//				coverage = store.read(null);
//				final double cmx = cellWidth / 2;
//				final double cmy = cellHeight / 2;
//				boolean doubleValues = false;
//				boolean floatValues = false;
//				boolean intValues = false;
//				boolean longValues = false;
//				boolean byteValues = false;
//				final double cellHeightP = genv.getSpan(1) / numRows;
//				final double cellWidthP = genv.getSpan(0) / numCols;
//				final double originXP = genv.getMinimum(0);
//				final double maxYP = genv.getMaximum(1);
//				final double cmxP = cellWidthP / 2;
//				final double cmyP = cellHeightP / 2;
//
//				for (int i = 0, n = numRows * numCols; i < n; i++) {
//					scope.getGui().getStatus(scope).setSubStatusCompletion(i / (double) n);
//					final int yy = i / numCols;
//					final int xx = i - yy * numCols;
//					p.x = originX + xx * cellWidth + cmx;
//					p.y = maxY - (yy * cellHeight + cmy);
//					GamaShape rect = (GamaShape) GamaGeometryType.buildRectangle(cellWidth, cellHeight, p);
//					final Object vals = coverage.evaluate(
//							new DirectPosition2D(originXP + xx * cellWidthP + cmxP, maxYP - (yy * cellHeightP + cmyP)));
//					if (i == 0) {
//						doubleValues = vals instanceof double[];
//						intValues = vals instanceof int[];
//						byteValues = vals instanceof byte[];
//						longValues = vals instanceof long[];
//						floatValues = vals instanceof float[];
//					}
//					if (gis == null) {
//						rect = new GamaShape(rect.getInnerGeometry());
//					} else {
//						rect = new GamaShape(gis.transform(rect.getInnerGeometry()));
//					}
//					if (doubleValues) {
//						final double[] vd = (double[]) vals;
//						if (i == 0) {
//							nbBands = vd.length;
//						}
//						rect.setAttribute("grid_value", vd[0]);
//						rect.setAttribute("bands", GamaListFactory.create(scope, Types.FLOAT, vd));
//					} else if (intValues) {
//						final int[] vi = (int[]) vals;
//						if (i == 0) {
//							nbBands = vi.length;
//						}
//						final double v = Double.valueOf(vi[0]);
//						rect.setAttribute("grid_value", v);
//						rect.setAttribute("bands", GamaListFactory.create(scope, Types.FLOAT, vi));
//					} else if (longValues) {
//						final long[] vi = (long[]) vals;
//						if (i == 0) {
//							nbBands = vi.length;
//						}
//						final double v = Double.valueOf(vi[0]);
//						rect.setAttribute("grid_value", v);
//						rect.setAttribute("bands", GamaListFactory.create(scope, Types.FLOAT, vi));
//					} else if (floatValues) {
//						final float[] vi = (float[]) vals;
//						if (i == 0) {
//							nbBands = vi.length;
//						}
//						final double v = Double.valueOf(vi[0]);
//						rect.setAttribute("grid_value", v);
//						rect.setAttribute("bands", GamaListFactory.create(scope, Types.FLOAT, vi));
//					} else if (byteValues) {
//						final byte[] bv = (byte[]) vals;
//						if (i == 0) {
//							nbBands = bv.length;
//						}
//						if (bv.length == 1) {
//							final double v = Double.valueOf(((byte[]) vals)[0]);
//							rect.setAttribute("grid_value", v);
//						} else if (bv.length == 3) {
//							final int red = bv[0] < 0 ? 256 + bv[0] : bv[0];
//							final int green = bv[0] < 0 ? 256 + bv[1] : bv[1];
//							final int blue = bv[0] < 0 ? 256 + bv[2] : bv[2];
//							rect.setAttribute("grid_value", (red + green + blue) / 3.0);
//						}
//						rect.setAttribute("bands", GamaListFactory.create(scope, Types.FLOAT, bv));
//					}
//					((IList) getBuffer()).add(rect);
//				}
//			} catch (final Exception e) {
//				final GamaRuntimeException ex = GamaRuntimeException.error(
//						"The format of " + getFile(scope).getName() + " is not correct. Error: " + e.getMessage(),
//						scope);
//				ex.addContext("for file " + getFile(scope).getPath());
//				throw ex;
//			} finally {
//				if (store != null) {
//					store.dispose();
//				}
//				scope.getGui().getStatus(scope).endSubStatus("Opening file " + getName(scope));
//			}
//		}
//
//	} 
	
	
//	@operator(value = "readDataSlice", can_be_const = false, category = IOperatorCategory.MATRIX)
//	@doc(value = "general operator to manipylate multidimension netcdf data.")
//	public static IMatrix readDataSlice(final IScope scope, final GamaNetCDFFile netcdf, int nbGrid, int t_index,
//			int z_index, int y_index, int x_index) {
//		if (netcdf == null || scope == null) {
//			return new GamaIntMatrix(0, 0);
//		} else {
//			if (netcdf.reader != null) {
//
//				List<?> grids = netcdf.reader.gridDataset.getGrids();
//				GridDatatype grid = null;
//				if (nbGrid >= grids.size()) {
//					nbGrid = 0;
//				}
//				if (grids.size() > 0)
//					grid = (GridDatatype) grids.get(nbGrid);// TODO number of the map
//				if (grid != null) {
//					GridCoordSystem gcsys = grid.getCoordinateSystem();
//					if (gcsys.getTimeAxis() != null)
//						netcdf.reader.ntimes = (int) gcsys.getTimeAxis().getSize();
//
//					Array ma;
//					try {
//						ma = grid.readDataSlice(t_index, z_index, y_index, x_index);
//						if (ma.getRank() == 3)
//							ma = ma.reduce();
//
//						if (ma.getRank() == 3)
//							ma = ma.slice(0, 0); // we need 2D
//
//						int h = ma.getShape()[0];
//						int w = ma.getShape()[1];
//
//						return matrixValue(scope, ma, h, w);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//
//		return new GamaIntMatrix(0, 0);
//	}
}
