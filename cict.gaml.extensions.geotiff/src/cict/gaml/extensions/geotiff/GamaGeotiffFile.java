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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.factory.Hints;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.GamlAnnotations.no_test;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.GamaGridFile;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@file(name = "gama_tiff", extensions = {
		"tiff" }, buffer_type = IType.LIST, buffer_content = IType.GEOMETRY, buffer_index = IType.INT, concept = {
				IConcept.GRID, IConcept.ASC, IConcept.TIF,
				IConcept.FILE }, doc = @doc("Represents multi-dimensional arrays encoded in NetCDF format"))
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GamaGeotiffFile extends GamaGridFile {

	GamaGridReader reader;
	// IMatrix coverage;
	GridCoverage2D coverage;
	public int nbBands;

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes
		return GamaListFactory.EMPTY_LIST;
	}

	private GamaGridReader createReader(final IScope scope, final boolean fillBuffer) {
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
				reader = new GamaGridReader(scope, fis, fillBuffer);
			} catch (final GamaRuntimeException e) {
				// A problem appeared, likely related to the wrong format of the
				// file (see Issue 412)
				GAMA.reportError(scope,
						GamaRuntimeException.warning(
								"The format of " + getName(scope) + " is incorrect. Attempting to read it anyway.",
								scope),
						false);

				// reader = fixFileHeader(scope,fillBuffer);
			}
		}
		return reader;
	}

	class GamaGridReader {

		int numRows, numCols;
		private IShape geom;
		Number noData = -9999;
		final InputStream fis = null;

		AbstractGridCoverage2DReader store = null;
		GeneralEnvelope genv = null;
		Envelope3D env = null;
		Envelope envP = null;
		double cellHeight = 0;
		double cellWidth = 0;
		double originX = 0;
		double originY = 0;
		double maxY = 0;
		// final double maxX = envP.getMaxX();
		GamaPoint p = null;
		double cmx = 0;
		double cmy = 0;
		boolean doubleValues = false;
		boolean floatValues = false;
		boolean intValues = false;
		boolean longValues = false;
		boolean byteValues = false;
		double cellHeightP = 0;
		double cellWidthP = 0;
		double originXP = 0;
		double maxYP = 0;
		double cmxP = 0;
		double cmyP = 0;

		public AbstractGridCoverage2DReader getStore(final IScope scope) {
			// AbstractGridCoverage2DReader store = null;
			try {

				if (store == null) {
					final CoordinateReferenceSystem crs = getExistingCRS(scope);
					if (isTiff(scope)) {
						if (crs == null) {
							store = new GeoTiffReader(getFile(scope));
						} else {
							store = new GeoTiffReader(getFile(scope),
									new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
						}
						noData = ((GeoTiffReader) store).getMetadata().getNoData();
					} else {
						if (crs == null) {
							store = new ArcGridReader(fis);
						} else {
							store = new ArcGridReader(fis, new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
						}
					}
					// scope.getSimulation().postDisposeAction(scope1 -> {
					//// store.dispose();
					// return null;
					// });
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return store;
		}

		GamaGridReader(final IScope scope, final InputStream fis, final boolean fillBuffer)
				throws GamaRuntimeException {
			setBuffer(GamaListFactory.<IShape>create(Types.GEOMETRY));
			genv = getStore(scope).getOriginalEnvelope();
			numRows = getStore(scope).getOriginalGridRange().getHigh(1) + 1;
			numCols = getStore(scope).getOriginalGridRange().getHigh(0) + 1;
			env = Envelope3D.of(genv.getMinimum(0), genv.getMaximum(0), genv.getMinimum(1), genv.getMaximum(1), 0, 0);
			computeProjection(scope, env);
			envP = gis.getProjectedEnvelope();
			cellHeight = envP.getHeight() / numRows;
			cellWidth = envP.getWidth() / numCols;
			// final IList<IShape> shapes =
			// GamaListFactory.create(Types.GEOMETRY);
			originX = envP.getMinX();
			originY = envP.getMinY();
			maxY = envP.getMaxY();
			// final double maxX = envP.getMaxX();
			try {
				coverage = getStore(scope).read(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cmx = cellWidth / 2;
			cmy = cellHeight / 2;

			cellHeightP = genv.getSpan(1) / numRows;
			cellWidthP = genv.getSpan(0) / numCols;
			originXP = genv.getMinimum(0);
			maxYP = genv.getMaximum(1);
			cmxP = cellWidthP / 2;
			cmyP = cellHeightP / 2;
			// final int yy = 0;// i / numCols;
			// final int xx = 0;// i - yy * numCols;
			// p.x = originX + xx * cellWidth + cmx;
			// p.y = maxY - (yy * cellHeight + cmy);
		}

		public IShape getGeom() {
			if (geom == null) {

				final Envelope envP = gis.getProjectedEnvelope();
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
			}
			return geom;
		}

		public IList read(final IScope scope, final int xx, final int yy) {
			IList<Object> result = null;

			final Object vals = coverage.evaluate(
					new DirectPosition2D(originXP + xx * cellWidthP + cmxP, maxYP - (yy * cellHeightP + cmyP)));
			doubleValues = vals instanceof double[];
			intValues = vals instanceof int[];
			byteValues = vals instanceof byte[];
			longValues = vals instanceof long[];
			floatValues = vals instanceof float[];

			if (doubleValues) {
				final double[] vd = (double[]) vals;
				nbBands = vd.length;
				result = GamaListFactory.create(scope, Types.FLOAT, vd);
			} else if (intValues) {
				final int[] vi = (int[]) vals;
				nbBands = vi.length;
				result = GamaListFactory.create(scope, Types.FLOAT, vi);
			} else if (longValues) {
				final long[] vi = (long[]) vals;
				nbBands = vi.length;
				result = GamaListFactory.create(scope, Types.FLOAT, vi);
			} else if (floatValues) {
				final float[] vi = (float[]) vals;
				nbBands = vi.length;
				result = GamaListFactory.create(scope, Types.FLOAT, vi);
			} else if (byteValues) {
				final byte[] bv = (byte[]) vals;
				nbBands = bv.length;
				result = GamaListFactory.create(scope, Types.FLOAT, bv);
			}

			return result;
		}
	}

	@no_test
	@operator(value = "read_bands", can_be_const = false, category = IOperatorCategory.LIST)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static IList read_bands(final IScope scope, final GamaGeotiffFile tiff, int xx, int yy) {
		if (tiff == null || scope == null) {
			return GamaListFactory.create();
		} else {
			if (tiff.reader != null) {
				return tiff.reader.read(scope, xx, yy);
			}
		}

		return GamaListFactory.create();
	}

	@no_test
	@operator(value = "read_bands", can_be_const = false, category = IOperatorCategory.LIST)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static IList read_bands(final IScope scope, final GamaGeotiffFile tiff, final ILocation loc) {
		IList<Object> result = null;

		Object vals = null;
		try {
			vals = tiff.coverage.evaluate(new DirectPosition2D(loc.getLocation().getX(), loc.getLocation().getY()));
		} catch (final Exception e) {
			vals = tiff.reader.noData.doubleValue();
		}
		final boolean doubleValues = vals instanceof double[];
		final boolean intValues = vals instanceof int[];
		final boolean byteValues = vals instanceof byte[];
		final boolean longValues = vals instanceof long[];
		final boolean floatValues = vals instanceof float[]; 
		if (doubleValues) {
			final double[] vd = (double[]) vals; 
			result = GamaListFactory.create(scope, Types.FLOAT, vd);
		} else if (intValues) {
			final int[] vi = (int[]) vals; 
			result = GamaListFactory.create(scope, Types.FLOAT, vi);
		} else if (longValues) {
			final long[] vi = (long[]) vals; 
			result = GamaListFactory.create(scope, Types.FLOAT, vi);
		} else if (floatValues) {
			final float[] vi = (float[]) vals; 
			result = GamaListFactory.create(scope, Types.FLOAT, vi);
		} else if (byteValues) {
			final byte[] bv = (byte[]) vals;
			result = GamaListFactory.create(scope, Types.FLOAT, bv);
		} 
		return result;
	}

	@doc(value = "This file constructor allows to read a asc file or a tif (geotif) file", examples = {
			@example(value = "file f <- grid_file(\"file.asc\");", isExecutable = false) })

	public GamaGeotiffFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
		createReader(scope, false);
	}

	@doc(value = "This file constructor allows to read a asc file or a tif (geotif) file specifying the coordinates system code, as an int (epsg code)", examples = {
			@example(value = "file f <- grid_file(\"file.asc\", 32648);", isExecutable = false) })
	public GamaGeotiffFile(final IScope scope, final String pathName, final Integer code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	@doc(value = "This file constructor allows to read a asc file or a tif (geotif) file specifying the coordinates system code (epg,...,), as a string ", examples = {
			@example(value = "file f <- grid_file(\"file.asc\",\"EPSG:32648\");", isExecutable = false) })
	public GamaGeotiffFile(final IScope scope, final String pathName, final String code) {
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
		if (reader == null) {
			createReader(scope, true);
		}
		return reader.numRows;
	}

	public int getNbCols(final IScope scope) {
		if (reader == null) {
			createReader(scope, true);
		}
		return reader.numCols;
	}

	public boolean isTiff(final IScope scope) {
		return getExtension(scope).equals("tif");
	}

	@Override
	public IShape getGeometry(final IScope scope) {
		if (reader == null) {
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
		// if (coverage != null) {
		// coverage.dispose(true);
		// }
		// coverage = null;
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
			vals = coverage.evaluate(new DirectPosition2D(loc.getLocation().getX(), loc.getLocation().getY()));
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
}
