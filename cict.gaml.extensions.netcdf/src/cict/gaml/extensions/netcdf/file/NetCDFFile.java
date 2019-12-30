/*********************************************************************************************
 *
 *
 * 'GamaTextFile.java', in plugin 'msi.gama.core', is part of the source code of the GAMA modeling and simulation
 * platform. (c) 20072014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gamaplatform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package cict.gaml.extensions.netcdf.file;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import javax.swing.JOptionPane;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gama.util.file.GamaFile;
import msi.gama.util.file.GamaImageFile;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.types.IContainerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayFloat;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.image.ImageArrayAdapter;

@file(name = "nc", extensions = {
		"nc" }, buffer_type = IType.MAP, buffer_content = IType.LIST, buffer_index = IType.STRING, concept = {
				IConcept.FILE, IConcept.R }, doc = @doc("Represents multi-dimensional arrays encoded in NetCDF format"))
public class NetCDFFile extends GamaFile<IMap<String, IList<?>>, IList<?>> {
	NetcdfDataset ds = null;
	private GridDataset gridDataset;
	private int ntimes = 1;
	boolean forward = true;

	final IMap<String, IList<?>> ncdata = GamaMapFactory.create(Types.STRING, Types.LIST);

	public NetCDFFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	public NetCDFFile(final IScope scope, final String pathName, final IContainer<?, ?> p) {
		super(scope, pathName);
	}

	@Override
	public String _stringValue(final IScope scope) throws GamaRuntimeException {
		getContents(scope);
		final StringBuilder sb = new StringBuilder(getBuffer().length(scope) * 200);
		for (final Object s : getBuffer().iterable(scope)) {
			sb.append(s).append("\n"); // TODO Factorize the different calls to
										// "new line" ...
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/*
	 * (nonJavadoc)
	 *
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) {
			return;
		}
		initializeNetCDF(scope);
		setBuffer(ncdata);
	}

	private void initializeNetCDF(final IScope scope) {
		final String NCFile = getPath(scope);
		NetcdfFile dataFile = null;
		try {

			dataFile = NetcdfFile.open(NCFile, null);
			final List<Variable> lv = dataFile.getVariables();
			for (final Variable v : lv) {
				final String varName = v.getName();
				System.out.println("\n" + varName + " " + v.getDataType() + " " + v.getShape().length);
				final List<Dimension> ld = v.getDimensionsAll();
				for (final Dimension d : ld) {
					System.out.println(d.getName() + " " + d.getLength());
				}

				// Variable lon_rho = dataFile.findVariable();

				final Array theArray = v.read();

				// latArray = (ArrayFloat) v.read();

				IList<?> gl = null;
//				if (theArray instanceof ArrayFloat.D0) {
//					// gl = transformVar2(theArray);
//				}
//				if (theArray instanceof ArrayFloat.D1) {
//					// gl = transformVar2(theArray);
//				}
//				if (theArray instanceof ArrayFloat.D2) {
//					gl = transformVar2(theArray);
//				}
//				if (theArray instanceof ArrayFloat.D3) {
//					gl = transformVar3(theArray);
//				}
//				if (theArray instanceof ArrayFloat.D4) {
//					// gl = transformVar4(theArray);
//				}
//				if (theArray instanceof ArrayFloat.D5) {
//					// gl = transformVar5(theArray);
//				}

				ncdata.put(varName, gl);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@operator(value = "openDataSet", can_be_const = false, category = IOperatorCategory.MATRIX)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static Boolean openDataSet(final IScope scope, final NetCDFFile netcdf) {
		if (netcdf == null || scope == null) {
			return false;
		} else {

			if (netcdf.ds == null) {
				String netCDF_File = netcdf.getFile(scope).getAbsolutePath();
				try {
					netcdf.ds = NetcdfDataset.openDataset(netCDF_File, true, null);
					if (netcdf.ds == null) {
						JOptionPane.showMessageDialog(null, "NetcdfDataset.open cant open " + netCDF_File);
						return null;
					}

					netcdf.gridDataset = new ucar.nc2.dt.grid.GridDataset(netcdf.ds, new Formatter());

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

	@operator(value = "readDataSlice", can_be_const = false, category = IOperatorCategory.MATRIX)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static IMatrix readDataSlice(final IScope scope, final NetCDFFile netcdf, int nbGrid, int t_index,
			int z_index, int y_index, int x_index) {
		if (netcdf == null || scope == null) {
			return new GamaIntMatrix(0, 0);
		} else {
			if (netcdf.ds != null) {

				List<?> grids = netcdf.gridDataset.getGrids();
				GridDatatype grid = null;
				if (nbGrid >= grids.size()) {
					nbGrid = 0;
				}
				if (grids.size() > 0)
					grid = (GridDatatype) grids.get(nbGrid);// TODO number of the map
				if (grid != null) {
					GridCoordSystem gcsys = grid.getCoordinateSystem();
					if (gcsys.getTimeAxis() != null)
						netcdf.ntimes = (int) gcsys.getTimeAxis().getSize();

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
		final IMatrix matrix = new GamaIntMatrix(w, h);
		double min = MAMath.getMinimum(ma); // LOOK we need missing values to be removed !!
		double max = MAMath.getMaximum(ma);

		double scale = (max - min);
		if (scale > 0.0)
			scale = 255.0 / scale;
		IndexIterator ii = ma.getIndexIterator();
		for (int i = 0; i <h; i++) {
			for (int j = 0; j <w; j++) {

				double val = ii.getDoubleNext();
				double sval = ((val - min) * scale);
				matrix.set(scope, j, i, sval);
			}
		}
		return matrix;

	}

	@operator(value = "getTimeAxisSize", can_be_const = false, category = IOperatorCategory.MATRIX)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static Integer getTimeAxisSize(final IScope scope, final NetCDFFile netcdf, int nbGrid) {
		if (netcdf == null || scope == null) {
			return -1;
		} else {

			if (netcdf.ds != null) {

				List<?> grids = netcdf.gridDataset.getGrids();
				GridDatatype grid = null;
				if (nbGrid >= grids.size()) {
					nbGrid = 0;
				}
				if (grids.size() > 0)
					grid = (GridDatatype) grids.get(nbGrid);// TODO number of the map
				if (grid != null) {
					GridCoordSystem gcsys = grid.getCoordinateSystem();
					if (gcsys.getTimeAxis() != null)
						netcdf.ntimes = (int) gcsys.getTimeAxis().getSize();

					return netcdf.ntimes;

				}
			}
		}

		return -1;
	}

	@operator(value = "getGridsSize", can_be_const = false, category = IOperatorCategory.FILE)
	@doc(value = "general operator to manipylate multidimension netcdf data.")
	public static Integer getGridsSize(final IScope scope, final NetCDFFile netcdf) {
		if (netcdf == null || scope == null) {
			return -1;
		} else {
			if (netcdf.ds != null) {
				List<?> grids = netcdf.gridDataset.getGrids();
				return grids.size();
			}
		}

		return -1;
	}

//	@operator (
//			value = "fetch",
//			can_be_const = false,
//			category = IOperatorCategory.LIST)
//	@doc (
//			value = "general operator to manipylate multidimension netcdf data.")
//	public static IList<Integer> reduce_dimension(final IScope scope, final String varName, final IList<?> offsets) {
//		final String NCFile = "";
//		if (varName == null) { return GamaListFactory.create(scope, Types.NO_TYPE, 0); }
//		if (scope == null) {
//			return GamaListFactory.create(scope, Types.NO_TYPE, 0);
//		} else {
//			// NetcdfFile dataFile = null;
//			//
//			// try {
//			//
//			// dataFile = NetcdfFile.open(NCFile, null);
//			//
//			// Variable v = dataFile.findVariable(varName);
//			//
//			// Array a = v.read();
//			// int sum = 0;
//			// int prev = 0;
//			// for (int index = offsets.length(scope) - 1; index > 1; index--) {
//			// int idx = Cast.asInt(scope, offsets.get(index));
//			// sum += idx > 0 ? (prev + idx) : idx;
//			// prev += Cast.asInt(scope, a.getShape()[index]);
//			// }
//			//
//			// return GamaListFactory.create(scope, Types.NO_TYPE,
//			// a.getObject(sum));
//			// } catch (IOException e) {
//			// e.printStackTrace();
//			// } finally {
//			// if (dataFile != null)
//			// try {
//			// dataFile.close();
//			// } catch (IOException ioe) {
//			// throw GamaRuntimeException.error("NetCDFExecutionException " +
//			// ioe.getMessage(), scope);
//			// }
//			// }
//		}
//		return GamaListFactory.create(scope, Types.NO_TYPE, 0);
//	}
//	
//	public static void main(final String args[]) throws Exception {
//
//		// Open the file and check to make sure it's valid.
//		final String filename = "E:\\tos_O1_2001-2002.nc";
//		NetcdfFile dataFile = null;
//
//		try {
//			dataFile = NetcdfFile.open(filename, null);
//			
//			final List<Variable> lv = dataFile.getVariables();
//
//			for (final Variable v : lv) {
//				System.out.println("\n" + v.getName() + " " + v.getDataType() + " " + v.getShape().length);
//				final List<Dimension> ld = v.getDimensionsAll();
//				for (final Dimension d : ld) {
//					System.out.println(d.getName() + " " + d.getLength());
//				}
//			}
//
//			System.out.println("\n\n\n");
//
//			final List<Dimension> ld = dataFile.getDimensions();
//			for (final Dimension d : ld) {
//				System.out.println(d.getName() + " " + d.getLength());
//			}
//
//			System.out.println("\n\n\n");
//
//			final List<Attribute> la = dataFile.getGlobalAttributes();
//			for (final Attribute a : la) {
//				System.out.println(a.getName() + " " + a.getDataType() + " " + a.getValues());
//			}
////			int[] shape;
////			float[] latsIn;
////			final Variable zeta = dataFile.findVariable("zeta");
////
////			ArrayFloat.D3 latArray3;
////
////			latArray3 = (ArrayFloat.D3) zeta.read();
////
////			shape = latArray3.getShape();
////
////			latsIn = new float[shape[0] * shape[1] * shape[2]];
////			for (int i = 0; i < shape[0]; i++) {
////				for (int j = 0; j < shape[1]; j++) {
////					for (int k = 0; k < shape[2]; k++) {
////						latsIn[i * j * k] = latArray3.get(i, j, k);
////						// System.out.println(latsIn[i*j*k]);
////					}
////				}
////			}
//
//			// Variable lon_rho = dataFile.findVariable("lon_rho");
//			//
//			// ArrayFloat.D2 latArray;
//			//
//			// latArray = (ArrayFloat.D2) lon_rho.read();
//			//
//			// shape = latArray.getShape();
//			//
//			//
//			// latsIn = new float[shape[0] * shape[1]];
//			// for (int i = 0; i < shape[0]; i++) {
//			// for (int j = 0; j < shape[1]; j++) {
//			// latsIn[i * j] = latArray.get(i, j);
//			// System.out.println(latsIn[i]);
//			// }
//			// }
//
//			//
//			//
//			// // Check the coordinate variable data.
//			// for (int lat = 0; lat < NLAT; lat++)
//			// if (latsIn[lat] != START_LAT + 5. * lat)
//			// System.err.println("ERROR reading variable latitude");
//			//
//			// // Check longitude values.
//			// for (int lon = 0; lon < NLON; lon++)
//			// if (lonsIn[lon] != START_LON + 5. * lon)
//			// System.err.println("ERROR reading variable longitude");
//
//		} catch (final java.io.IOException e) {
//			System.out.println(" fail = " + e);
//			e.printStackTrace();
//		} finally {
//			if (dataFile != null) {
//				try {
//					dataFile.close();
//				} catch (final IOException ioe) {
//					ioe.printStackTrace();
//				}
//			}
//		}
//		System.out.println("*** SUCCESS reading example file!");
//
//	}
	// public void initializeNetCDF1(final IScope scope) {
	// final String NCFile = getPath();
	// NetcdfFile dataFile = null;
	// try {
	//
	// dataFile = NetcdfFile.open(NCFile, null);
	//
	// // Retrieve the variable named "data"
	// ListIterator<Variable> vi = dataFile.getVariables().listIterator();
	//
	// while (vi.hasNext()) {
	// Variable v = vi.next();
	// System.out.println("" + v.getFullName() + " " + v.getShape().length);
	// for (int i = 0; i < v.getShape().length; i++) {
	//
	// }
	// Array a = v.read();
	// ArrayList res = new ArrayList<> ();
	// while (a.hasNext()) {
	// res.add(a.next());
	// }
	// ncdata.put(v.getFullName(), GamaListFactory.create(scope, Types.NO_TYPE,
	// res));
	//
	// }
	//
	// // The file is closed no matter what by putting inside a try/catch
	// // block.
	// } catch (java.io.IOException e) {
	// throw GamaRuntimeException.error("NetCDFExecutionException " +
	// e.getMessage(), scope);
	//
	// } finally {
	// if (dataFile != null)
	// try {
	// dataFile.close();
	// } catch (IOException ioe) {
	// throw GamaRuntimeException.error("NrtCDFExecutionException " +
	// ioe.getMessage(), scope);
	// }
	// }
	//
	// System.out.println("*** SUCCESS reading example file simple_xy.nc!");
	//
	// }
//

//	private IList<?> transformVar2(final Array var) throws Exception {
//		final IList<IList<Float>> gl = GamaListFactory.create();
//
//		int[] shape;
//		float[] latsIn;
//		shape = var.getShape();
//		latsIn = new float[shape[0] * shape[1]];
//
//		for (int i = 0; i < shape[0]; i++) {
//			final IList<Float> gl1 = GamaListFactory.create();
//			for (int j = 0; j < shape[1]; j++) {
//				gl1.add(((ArrayFloat.D2) var).get(i, j));
//			}
//			gl.add(gl1);
//		}
//		return gl;
//
//	}

//
//	private IList<?> transformVar3(final Array var) throws Exception {
//		final IList<IList<IList<Float>>> gl = GamaListFactory.create();
//
//		final int[] shape = var.getShape();
//		for (int i = 0; i < shape[0]; i++) {
//			final IList<IList<Float>> gl1 = GamaListFactory.create();
//			for (int j = 0; j < shape[1]; j++) {
//				final IList<Float> gl2 = GamaListFactory.create();
//				for (int k = 0; k < shape[2]; k++) {
//					gl2.add(((ArrayFloat.D3) var).get(i, j, k));
//				}
//				gl1.add(gl2);
//			}
//			gl.add(gl1);
//		}
//		return gl;
//
//	}
//
//	private IList<?> transformVar4(final Array var) throws Exception {
//		final IList<IList<IList<IList<Float>>>> gl = GamaListFactory.create();
//
//		final int[] shape = var.getShape();
//		for (int i = 0; i < shape[0]; i++) {
//			final IList<IList<IList<Float>>> gl1 = GamaListFactory.create();
//			for (int j = 0; j < shape[1]; j++) {
//				final IList<IList<Float>> gl2 = GamaListFactory.create();
//				for (int k = 0; k < shape[2]; k++) {
//					final IList<Float> gl3 = GamaListFactory.create();
//					for (int l = 0; l < shape[3]; l++) {
//						gl3.add(((ArrayFloat.D4) var).get(i, j, k, l));
//					}
//					gl2.add(gl3);
//				}
//				gl1.add(gl2);
//			}
//			gl.add(gl1);
//		}
//		return gl;
//
//	}
//
//	private IList<?> transformVar5(final Array var) throws Exception {
//		final IList<IList<IList<IList<IList<Float>>>>> gl = GamaListFactory.create();
//
//		final int[] shape = var.getShape();
//		for (int i = 0; i < shape[0]; i++) {
//			final IList<IList<IList<IList<Float>>>> gl1 = GamaListFactory.create();
//			for (int j = 0; j < shape[1]; j++) {
//				final IList<IList<IList<Float>>> gl2 = GamaListFactory.create();
//				for (int k = 0; k < shape[2]; k++) {
//					final IList<IList<Float>> gl3 = GamaListFactory.create();
//					for (int l = 0; l < shape[3]; l++) {
//						final IList<Float> gl4 = GamaListFactory.create();
//						for (int m = 0; m < shape[4]; m++) {
//							gl4.add(((ArrayFloat.D5) var).get(i, j, k, l, m));
//						}
//						gl3.add(gl4);
//					}
//					gl2.add(gl3);
//				}
//				gl1.add(gl2);
//			}
//			gl.add(gl1);
//		}
//		return gl;
//
//	}

	private static String computeVariable(final String string) {
		final String[] tokens = string.split("<");
		return tokens[0];
	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContainerType getGamlType() {
		final IType ct = getBuffer() == null ? Types.NO_TYPE : getBuffer().getGamlType().getContentType();
		return Types.FILE.of(ct);
	}

}