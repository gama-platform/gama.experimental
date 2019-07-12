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

import java.io.IOException;
import java.util.List;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.file.GamaFile;
import msi.gaml.types.IContainerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

@file (
		name = "nc",
		extensions = { "nc" },
		buffer_type = IType.MAP,
		buffer_content = IType.LIST,
		buffer_index = IType.STRING,
		concept = { IConcept.FILE, IConcept.R },
		doc = @doc ("Represents multi-dimensional arrays encoded in NetCDF format"))
public class NetCDFFile extends GamaFile<GamaMap<String, IList<?>>, IList<?>> {

	final GamaMap<String, IList<?>> ncdata = (GamaMap<String,IList<?>>) GamaMapFactory.create(Types.STRING, Types.LIST);

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
		if (getBuffer() != null) { return; }
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

				GamaList<?> gl = null;
				if (theArray instanceof ArrayFloat.D0) {
					// gl = transformVar2(theArray);
				}
				if (theArray instanceof ArrayFloat.D1) {
					// gl = transformVar2(theArray);
				}
				if (theArray instanceof ArrayFloat.D2) {
					gl = transformVar2(theArray);
				}
				if (theArray instanceof ArrayFloat.D3) {
					gl = transformVar3(theArray);
				}
				if (theArray instanceof ArrayFloat.D4) {
					// gl = transformVar4(theArray);
				}
				if (theArray instanceof ArrayFloat.D5) {
					// gl = transformVar5(theArray);
				}

				ncdata.put(varName, gl);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private GamaList<?> transformVar2(final Array var) throws Exception {
		final GamaList<GamaList<Float>> gl = (GamaList<GamaList<Float>>) GamaListFactory.create();

		int[] shape;
		float[] latsIn;
		shape = var.getShape();
		latsIn = new float[shape[0] * shape[1]];

		for (int i = 0; i < shape[0]; i++) {
			final GamaList<Float> gl1 = (GamaList<Float>) GamaListFactory.create();
			for (int j = 0; j < shape[1]; j++) {
				gl1.add(((ArrayFloat.D2) var).get(i, j));
			}
			gl.add(gl1);
		}
		return gl;

	}

	private GamaList<?> transformVar3(final Array var) throws Exception {
		final GamaList<GamaList<GamaList<Float>>> gl = (GamaList<GamaList<GamaList<Float>>>) GamaListFactory.create();

		final int[] shape = var.getShape();
		for (int i = 0; i < shape[0]; i++) {
			final GamaList<GamaList<Float>> gl1 = (GamaList<GamaList<Float>>) GamaListFactory.create();
			for (int j = 0; j < shape[1]; j++) {
				final GamaList<Float> gl2 = (GamaList<Float>) GamaListFactory.create();
				for (int k = 0; k < shape[2]; k++) {
					gl2.add(((ArrayFloat.D3) var).get(i, j, k));
				}
				gl1.add(gl2);
			}
			gl.add(gl1);
		}
		return gl;

	}

	private GamaList<?> transformVar4(final Array var) throws Exception {
		final GamaList<GamaList<GamaList<GamaList<Float>>>> gl =
				(GamaList<GamaList<GamaList<GamaList<Float>>>>) GamaListFactory.create();

		final int[] shape = var.getShape();
		for (int i = 0; i < shape[0]; i++) {
			final GamaList<GamaList<GamaList<Float>>> gl1 =
					(GamaList<GamaList<GamaList<Float>>>) GamaListFactory.create();
			for (int j = 0; j < shape[1]; j++) {
				final GamaList<GamaList<Float>> gl2 = (GamaList<GamaList<Float>>) GamaListFactory.create();
				for (int k = 0; k < shape[2]; k++) {
					final GamaList<Float> gl3 = (GamaList<Float>) GamaListFactory.create();
					for (int l = 0; l < shape[3]; l++) {
						gl3.add(((ArrayFloat.D4) var).get(i, j, k, l));
					}
					gl2.add(gl3);
				}
				gl1.add(gl2);
			}
			gl.add(gl1);
		}
		return gl;

	}

	private GamaList<?> transformVar5(final Array var) throws Exception {
		final GamaList<GamaList<GamaList<GamaList<GamaList<Float>>>>> gl =
				(GamaList<GamaList<GamaList<GamaList<GamaList<Float>>>>>) GamaListFactory.create();

		final int[] shape = var.getShape();
		for (int i = 0; i < shape[0]; i++) {
			final GamaList<GamaList<GamaList<GamaList<Float>>>> gl1 =
					(GamaList<GamaList<GamaList<GamaList<Float>>>>) GamaListFactory.create();
			for (int j = 0; j < shape[1]; j++) {
				final GamaList<GamaList<GamaList<Float>>> gl2 =
						(GamaList<GamaList<GamaList<Float>>>) GamaListFactory.create();
				for (int k = 0; k < shape[2]; k++) {
					final GamaList<GamaList<Float>> gl3 = (GamaList<GamaList<Float>>) GamaListFactory.create();
					for (int l = 0; l < shape[3]; l++) {
						final GamaList<Float> gl4 = (GamaList<Float>) GamaListFactory.create();
						for (int m = 0; m < shape[4]; m++) {
							gl4.add(((ArrayFloat.D5) var).get(i, j, k, l, m));
						}
						gl3.add(gl4);
					}
					gl2.add(gl3);
				}
				gl1.add(gl2);
			}
			gl.add(gl1);
		}
		return gl;

	}

	public static void main(final String args[]) throws Exception {

		// Open the file and check to make sure it's valid.
		final String filename = "D:/roms-benguela.nc";
		NetcdfFile dataFile = null;

		try {
			dataFile = NetcdfFile.open(filename, null);
			final List<Variable> lv = dataFile.getVariables();

			for (final Variable v : lv) {
				System.out.println("\n" + v.getName() + " " + v.getDataType() + " " + v.getShape().length);
				final List<Dimension> ld = v.getDimensionsAll();
				for (final Dimension d : ld) {
					System.out.println(d.getName() + " " + d.getLength());
				}
			}

			System.out.println("\n\n\n");

			final List<Dimension> ld = dataFile.getDimensions();
			for (final Dimension d : ld) {
				System.out.println(d.getName() + " " + d.getLength());
			}

			System.out.println("\n\n\n");

			final List<Attribute> la = dataFile.getGlobalAttributes();
			for (final Attribute a : la) {
				System.out.println(a.getName() + " " + a.getDataType() + " " + a.getValues());
			}
			int[] shape;
			float[] latsIn;
			final Variable zeta = dataFile.findVariable("zeta");

			ArrayFloat.D3 latArray3;

			latArray3 = (ArrayFloat.D3) zeta.read();

			shape = latArray3.getShape();

			latsIn = new float[shape[0] * shape[1] * shape[2]];
			for (int i = 0; i < shape[0]; i++) {
				for (int j = 0; j < shape[1]; j++) {
					for (int k = 0; k < shape[2]; k++) {
						latsIn[i * j * k] = latArray3.get(i, j, k);
						// System.out.println(latsIn[i*j*k]);
					}
				}
			}

			// Variable lon_rho = dataFile.findVariable("lon_rho");
			//
			// ArrayFloat.D2 latArray;
			//
			// latArray = (ArrayFloat.D2) lon_rho.read();
			//
			// shape = latArray.getShape();
			//
			//
			// latsIn = new float[shape[0] * shape[1]];
			// for (int i = 0; i < shape[0]; i++) {
			// for (int j = 0; j < shape[1]; j++) {
			// latsIn[i * j] = latArray.get(i, j);
			// System.out.println(latsIn[i]);
			// }
			// }

			//
			//
			// // Check the coordinate variable data.
			// for (int lat = 0; lat < NLAT; lat++)
			// if (latsIn[lat] != START_LAT + 5. * lat)
			// System.err.println("ERROR reading variable latitude");
			//
			// // Check longitude values.
			// for (int lon = 0; lon < NLON; lon++)
			// if (lonsIn[lon] != START_LON + 5. * lon)
			// System.err.println("ERROR reading variable longitude");

		} catch (final java.io.IOException e) {
			System.out.println(" fail = " + e);
			e.printStackTrace();
		} finally {
			if (dataFile != null)
				try {
					dataFile.close();
				} catch (final IOException ioe) {
					ioe.printStackTrace();
				}
		}
		System.out.println("*** SUCCESS reading example file sfc_pres_temp.nc!");

	}

	@operator (
			value = "fetch",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "general operator to manipylate multidimension netcdf data.")
	public static IList<Integer> reduce_dimension(final IScope scope, final String varName, final IList<?> offsets) {
		final String NCFile = "";
		if (varName == null) { return GamaListFactory.create(scope, Types.NO_TYPE, 0); }
		if (scope == null) {
			return GamaListFactory.create(scope, Types.NO_TYPE, 0);
		} else {
			// NetcdfFile dataFile = null;
			//
			// try {
			//
			// dataFile = NetcdfFile.open(NCFile, null);
			//
			// Variable v = dataFile.findVariable(varName);
			//
			// Array a = v.read();
			// int sum = 0;
			// int prev = 0;
			// for (int index = offsets.length(scope) - 1; index > 1; index--) {
			// int idx = Cast.asInt(scope, offsets.get(index));
			// sum += idx > 0 ? (prev + idx) : idx;
			// prev += Cast.asInt(scope, a.getShape()[index]);
			// }
			//
			// return GamaListFactory.create(scope, Types.NO_TYPE,
			// a.getObject(sum));
			// } catch (IOException e) {
			// e.printStackTrace();
			// } finally {
			// if (dataFile != null)
			// try {
			// dataFile.close();
			// } catch (IOException ioe) {
			// throw GamaRuntimeException.error("NetCDFExecutionException " +
			// ioe.getMessage(), scope);
			// }
			// }
		}
		return GamaListFactory.create(scope, Types.NO_TYPE, 0);
	}

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
	// ArrayList res = new ArrayList<>();
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