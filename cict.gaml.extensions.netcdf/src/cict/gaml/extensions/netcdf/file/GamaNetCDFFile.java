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
package cict.gaml.extensions.netcdf.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
// ArcGridReader still requires input streams
import java.io.StringBufferInputStream;
import java.nio.channels.FileChannel;
import java.util.Formatter;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.factory.Hints;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

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
import msi.gama.util.file.GamaGridFile;
import msi.gama.util.matrix.GamaFloatMatrix;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;

@file(name = "netcdf", extensions = {
		"nc" }, buffer_type = IType.LIST, buffer_content = IType.GEOMETRY, buffer_index = IType.INT, concept = {
				IConcept.GRID, IConcept.ASC, IConcept.TIF,
				IConcept.FILE }, doc = @doc("Represents multi-dimensional arrays encoded in NetCDF format"))
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GamaNetCDFFile extends GamaGridFile {

	GamaNetCDFReader reader;
	IMatrix coverage;
//	GridCoverage2D coverage;
//	public int nbBands;

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes
		return GamaListFactory.EMPTY_LIST;
	}

	private GamaNetCDFReader createReader(final IScope scope, final boolean fillBuffer) {
		if (reader == null) {
			final File gridFile = getFile(scope);
			gridFile.setReadable(true);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(gridFile);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				reader = new GamaNetCDFReader(scope, fis, fillBuffer);
			} catch (final GamaRuntimeException e) {
				// A problem appeared, likely related to the wrong format of the
				// file (see Issue 412)
				GAMA.reportError(scope,
						GamaRuntimeException.warning(
								"The format of " + getName(scope) + " is incorrect. Attempting to read it anyway.",
								scope),
						false);

//				reader = fixFileHeader(scope,fillBuffer);
			}
		}
		return reader;
	}

//	public GamaNetCDFReader fixFileHeader(IScope scope,final boolean fillBuffer) {
//		final StringBuilder text = new StringBuilder();
//		final String NL = System.getProperty("line.separator");
//
//		try (Scanner scanner = new Scanner(getFile(scope))) {
//			// final int cpt = 0;
//			while (scanner.hasNextLine()) {
//				final String line = scanner.nextLine();
//
//				if (line.contains("dx")) {
//					text.append(line.replace("dx", "cellsize") + NL);
//				} else if (line.contains("dy")) {
//					// continue;
//				} else {
//					text.append(line + NL);
//				}
//
//				// if (cpt < 10) {}
//				// else {
//				// text.append(line + NL);
//				// }
//			}
//		} catch (final FileNotFoundException e2) {
//			final GamaRuntimeException ex = GamaRuntimeException.error(
//					"The format of " + getName(scope) + " is not correct. Error: " + e2.getMessage(), scope);
//			ex.addContext("for file " + getPath(scope));
//			throw ex;
//		}
//
//		text.append(NL);
//		// fis = new StringBufferInputStream(text.toString());
//		return new GamaNetCDFReader(scope, new StringBufferInputStream(text.toString()), fillBuffer);
//	}

	class GamaNetCDFReader {

		int numRows, numCols;
		IShape geom;
		Number noData = -9999;
		NetcdfDataset ds = null;
		private GridDataset gridDataset;
		private int ntimes = 1;
		boolean forward = true;

		GamaNetCDFReader(final IScope scope, final InputStream fis, final boolean fillBuffer)
				throws GamaRuntimeException {
			setBuffer(GamaListFactory.<IShape>create(Types.GEOMETRY));
//			AbstractGridCoverage2DReader store = null;
			try {
				if (fillBuffer) {
					scope.getGui().getStatus(scope).beginSubStatus("Reading file " + getName(scope));
				}
				// Necessary to compute it here, because it needs to be passed
				// to the Hints
				final CoordinateReferenceSystem crs = getExistingCRS(scope);

				if (ds == null) {
					String netCDF_File = getFile(scope).getAbsolutePath();
					try {
						ds = NetcdfDataset.openDataset(netCDF_File, true, null);
						if (ds != null) {
							gridDataset = new ucar.nc2.dt.grid.GridDataset(ds, new Formatter());

							List<?> grids = gridDataset.getGrids();
							GridDatatype grid = null;
							int nbGrid = 0;
							if (nbGrid >= grids.size()) {
								nbGrid = 0;
							}
							if (grids.size() > 0)
								grid = (GridDatatype) grids.get(nbGrid);// TODO number of the map
							if (grid != null) {
								GridCoordSystem gcsys = grid.getCoordinateSystem();
								if (gcsys.getTimeAxis() != null)
									ntimes = (int) gcsys.getTimeAxis().getSize();

								Array ma;
								try {
									int t_index = 1;
									int z_index = 0;
									int y_index = -1;
									int x_index = -1;
									ma = grid.readDataSlice(t_index, z_index, y_index, x_index);
									if (ma.getRank() == 3)
										ma = ma.reduce();

									if (ma.getRank() == 3)
										ma = ma.slice(0, 0); // we need 2D

									numCols = ma.getShape()[0];
									numRows = ma.getShape()[1];
									coverage = matrixValue(scope, ma, numCols, numRows);
//									setBuffer(matrixValue(scope, ma, h, w));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					} catch (FileNotFoundException ioe) {
						ioe.printStackTrace();
					} catch (Throwable ioe) {
						ioe.printStackTrace();
					}
				}

//				if (crs == null) {
//					store = new ArcGridReader(fis);
//				} else {
//					store = new ArcGridReader(fis, new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
//				}

//				final GeneralEnvelope genv = store.getOriginalEnvelope();
//				numRows = store.getOriginalGridRange().getHigh(1) + 1;
//				numCols = store.getOriginalGridRange().getHigh(0) + 1;
				final Envelope3D env = Envelope3D.of(0, 100, 0, 100, 0, 0);
				computeProjection(scope, env);
				final Envelope envP = gis.getProjectedEnvelope();
				final double cellHeight = envP.getHeight() / numRows;
				final double cellWidth = envP.getWidth() / numCols;
				final IList<IShape> shapes = GamaListFactory.create(Types.GEOMETRY);
				final double originX = envP.getMinX();
				final double originY = envP.getMinY();
				final double maxY = envP.getMaxY();
				final double maxX = envP.getMaxX();
				shapes.add(new GamaPoint(originX, originY));
				shapes.add(new GamaPoint(maxX, originY));
				shapes.add(new GamaPoint(maxX, maxY));
				shapes.add(new GamaPoint(originX, maxY));
				shapes.add(shapes.get(0));
				geom = GamaGeometryType.buildPolygon(shapes);
				if (!fillBuffer) {
					return;
				}

				final GamaPoint p = new GamaPoint(0, 0);
//				coverage = store.read(null);
				final double cmx = cellWidth / 2;
				final double cmy = cellHeight / 2;
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
				
				for (int i = 0, n = numRows * numCols; i < n; i++) {
					scope.getGui().getStatus(scope).setSubStatusCompletion(i / (double) n);
					final int yy = i / numCols;
					final int xx = i - yy * numCols;
					p.x = originX + xx * cellWidth + cmx;
					p.y = maxY - (yy * cellHeight + cmy);
					GamaShape rect = (GamaShape) GamaGeometryType.buildRectangle(cellWidth, cellHeight, p);
					final double vals = (double) coverage.get(scope, yy, xx);
//					if (i == 0) {
//						doubleValues = vals instanceof double[];
//						intValues = vals instanceof int[];
//						byteValues = vals instanceof byte[];
//						longValues = vals instanceof long[];
//						floatValues = vals instanceof float[];
//					}
					if (gis == null) {
						rect = new GamaShape(rect.getInnerGeometry());
					} else {
						rect = new GamaShape(gis.transform(rect.getInnerGeometry()));
					}
//					if (doubleValues) {
//						final double[] vd = (double[]) vals;
//						if (i == 0) {
//							nbBands = vd.length;
//						}
						rect.setAttribute("grid_value", vals);
						rect.setAttribute("bands", GamaListFactory.create(scope, Types.FLOAT, vals));
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
					nbBands=1;
					((IList) getBuffer()).add(rect);
				}
			} catch (final Exception e) {
				final GamaRuntimeException ex = GamaRuntimeException.error(
						"The format of " + getFile(scope).getName() + " is not correct. Error: " + e.getMessage(),
						scope);
				ex.addContext("for file " + getFile(scope).getPath());
				ex.printStackTrace();
				throw ex;
			} finally {
//				if (store != null) {
//					store.dispose();
//				}
				scope.getGui().getStatus(scope).endSubStatus("Opening file " + getName(scope));
			}
		}

	}

	@doc(value = "This file constructor allows to read a asc file or a tif (geotif) file", examples = {
			@example(value = "file f <- grid_file(\"file.asc\");", isExecutable = false) })

	public GamaNetCDFFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
	}

	@doc(value = "This file constructor allows to read a asc file or a tif (geotif) file specifying the coordinates system code, as an int (epsg code)", examples = {
			@example(value = "file f <- grid_file(\"file.asc\", 32648);", isExecutable = false) })
	public GamaNetCDFFile(final IScope scope, final String pathName, final Integer code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	@doc(value = "This file constructor allows to read a asc file or a tif (geotif) file specifying the coordinates system code (epg,...,), as a string ", examples = {
			@example(value = "file f <- grid_file(\"file.asc\",\"EPSG:32648\");", isExecutable = false) })
	public GamaNetCDFFile(final IScope scope, final String pathName, final String code) {
		super(scope, pathName, code);
	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		fillBuffer(scope);
		return gis.getProjectedEnvelope();
	}

	public Envelope computeEnvelopeWithoutBuffer(final IScope scope) {
		if (gis == null) {
			createReader(scope, false);
		}
		return gis.getProjectedEnvelope();
	}

	@Override
	protected void fillBuffer(final IScope scope) {
		if (getBuffer() != null) {
			return;
		}
		createReader(scope, true);
	}

	public int getNbRows(final IScope scope) {
		if(reader==null) {
			createReader(scope, true);
		}
		return reader.numRows;
	}

	public int getNbCols(final IScope scope) {
		if(reader==null) {
			createReader(scope, true);
		}
		return reader.numCols;
	}

	public boolean isTiff(final IScope scope) {
		return getExtension(scope).equals("tif");
	}

	@Override
	public IShape getGeometry(final IScope scope) {
		if(reader==null) {
			createReader(scope, true);
		}
		return reader.geom;
	}

	@Override
	protected CoordinateReferenceSystem getOwnCRS(final IScope scope) {
		final File source = getFile(scope);
		// check to see if there is a projection file
		// getting name for the prj file
		final String sourceAsString;
		sourceAsString = source.getAbsolutePath();
		final int index = sourceAsString.lastIndexOf('.');
		final StringBuffer prjFileName;
		if (index == -1) {
			prjFileName = new StringBuffer(sourceAsString);
		} else {
			prjFileName = new StringBuffer(sourceAsString.substring(0, index));
		}
		prjFileName.append(".prj");

		// does it exist?
		final File prjFile = new File(prjFileName.toString());
		if (prjFile.exists()) {
			// it exists then we have to read it
			PrjFileReader projReader = null;
			try (FileInputStream fip = new FileInputStream(prjFile); final FileChannel channel = fip.getChannel();) {
				projReader = new PrjFileReader(channel);
				return projReader.getCoordinateReferenceSystem();
			} catch (final FileNotFoundException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				return null;
			} catch (final IOException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				return null;
			} catch (final FactoryException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				return null;
			} finally {
				if (projReader != null) {
					try {
						projReader.close();
					} catch (final IOException e) {
						// warn about the error but proceed, it is not fatal
						// we have at least the default crs to use
						return null;
					}
				}
			}
		} else if (isTiff(scope)) {
			try {
				final GeoTiffReader store = new GeoTiffReader(getFile(scope));
				return store.getCoordinateReferenceSystem();
			} catch (final DataSourceException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	// public static RenderedImage getImage(final String pathName) {
	// return GAMA.run(new InScope<RenderedImage>() {
	//
	// @Override
	// public RenderedImage run(final IScope scope) {
	// GamaGridFile file = new GamaGridFile(scope, pathName);
	// file.createReader(scope, true);
	// return file.coverage.getRenderedImage();
	// }
	// });
	// }

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		reader = null;
//		if (coverage != null) {
//			coverage.dispose(true);
//		}
//		coverage = null;
	}

	public GridCoverage2D getCoverage() {
		return null;
	}

	public Double valueOf(final IScope scope, final ILocation loc) {
		if (getBuffer() == null) {
			fillBuffer(scope);
		}

		Object vals = null;
		try {
			vals = coverage.get(scope, (int) loc.getX(), (int) loc.getY());
		} catch (final Exception e) {
			vals = reader.noData.doubleValue();
		}
		final boolean doubleValues = vals instanceof double[];
		final boolean intValues = vals instanceof int[];
		final boolean byteValues = vals instanceof byte[];
		final boolean longValues = vals instanceof long[];
		final boolean floatValues = vals instanceof float[];
		Double val = null;
		if (doubleValues) {
			final double[] vd = (double[]) vals;
			val = vd[0];
		} else if (intValues) {
			final int[] vi = (int[]) vals;
			val = Double.valueOf(vi[0]);
		} else if (longValues) {
			final long[] vi = (long[]) vals;
			val = Double.valueOf(vi[0]);
		} else if (floatValues) {
			final float[] vi = (float[]) vals;
			val = Double.valueOf(vi[0]);
		} else if (byteValues) {
			final byte[] bv = (byte[]) vals;
			if (bv.length == 3) {
				final int red = bv[0] < 0 ? 256 + bv[0] : bv[0];
				final int green = bv[0] < 0 ? 256 + bv[1] : bv[1];
				final int blue = bv[0] < 0 ? 256 + bv[2] : bv[2];
				val = (red + green + blue) / 3.0;
			} else {
				val = Double.valueOf(((byte[]) vals)[0]);
			}
		}
		return val;
	}

	@operator(value = "openDataSet", can_be_const = false, category = IOperatorCategory.MATRIX)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static Boolean openDataSet(final IScope scope, final GamaNetCDFFile netcdf) {
		if (netcdf == null || scope == null) {
			return false;
		} else {

			if (netcdf.reader == null) {
				String netCDF_File = netcdf.getFile(scope).getAbsolutePath();
				try {
					netcdf.reader.ds = NetcdfDataset.openDataset(netCDF_File, true, null);
					if (netcdf.reader == null) {
						JOptionPane.showMessageDialog(null, "NetcdfDataset.open cant open " + netCDF_File);
						return null;
					}

					netcdf.reader.gridDataset = new ucar.nc2.dt.grid.GridDataset(netcdf.reader.ds, new Formatter());

				} catch (FileNotFoundException ioe) {
					JOptionPane.showMessageDialog(null,
							"NetcdfDataset.open cant open " + netCDF_File + "\n" + ioe.getMessage());
					ioe.printStackTrace();
				} catch (Throwable ioe) {
					ioe.printStackTrace();
				}
				return true;
			}
		}

		return false;
	}

	@operator(value = "getTimeAxisSize", can_be_const = false, category = IOperatorCategory.MATRIX)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static Integer getTimeAxisSize(final IScope scope, final GamaNetCDFFile netcdf, int nbGrid) {
		if (netcdf == null || scope == null) {
			return -1;
		} else {

			if (netcdf.reader != null) {

				List<?> grids = netcdf.reader.gridDataset.getGrids();
				GridDatatype grid = null;
				if (nbGrid >= grids.size()) {
					nbGrid = 0;
				}
				if (grids.size() > 0)
					grid = (GridDatatype) grids.get(nbGrid);// TODO number of the map
				if (grid != null) {
					GridCoordSystem gcsys = grid.getCoordinateSystem();
					if (gcsys.getTimeAxis() != null)
						netcdf.reader.ntimes = (int) gcsys.getTimeAxis().getSize();

					return netcdf.reader.ntimes;

				}
			}
		}

		return -1;
	}

	@operator(value = "getGridsSize", can_be_const = false, category = IOperatorCategory.FILE)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static Integer getGridsSize(final IScope scope, final GamaNetCDFFile netcdf) {
		if (netcdf == null || scope == null) {
			return -1;
		} else {
			if (netcdf.reader != null) {
				List<?> grids = netcdf.reader.gridDataset.getGrids();
				return grids.size();
			}
		}

		return -1;
	}

	@operator(value = "readDataSlice", can_be_const = false, category = IOperatorCategory.MATRIX)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static IMatrix readDataSlice(final IScope scope, final GamaNetCDFFile netcdf, int nbGrid, int t_index,
			int z_index, int y_index, int x_index) {
		if (netcdf == null || scope == null) {
			return new GamaIntMatrix(0, 0);
		} else {
			if (netcdf.reader != null) {

				List<?> grids = netcdf.reader.gridDataset.getGrids();
				GridDatatype grid = null;
				if (nbGrid >= grids.size()) {
					nbGrid = 0;
				}
				if (grids.size() > 0)
					grid = (GridDatatype) grids.get(nbGrid);// TODO number of the map
				if (grid != null) {
					GridCoordSystem gcsys = grid.getCoordinateSystem();
					if (gcsys.getTimeAxis() != null)
						netcdf.reader.ntimes = (int) gcsys.getTimeAxis().getSize();

					Array ma;
					try {
						ma = grid.readDataSlice(t_index, z_index, y_index, x_index);
						if (ma.getRank() == 3)
							ma = ma.reduce();

						if (ma.getRank() == 3)
							ma = ma.slice(0, 0); // we need 2D

						int h = ma.getShape()[0];
						int w = ma.getShape()[1];

						return matrixValue(scope, ma, h, w);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return new GamaIntMatrix(0, 0);
	}

	private static IMatrix matrixValue(final IScope scope, final Array ma, int h, int w) {
		final IMatrix matrix = new GamaFloatMatrix(w, h);
		double min = MAMath.getMinimum(ma); // LOOK we need missing values to be removed !!
		double max = MAMath.getMaximum(ma);

		double scale = (max - min);
		if (scale > 0.0)
			scale = 255.0 / scale;
		IndexIterator ii = ma.getIndexIterator();
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {

				double val = ii.getDoubleNext();
				double sval = ((val - min) * scale);
				matrix.set(scope, j, i, sval);
			}
		}
		return matrix;

	}

}
