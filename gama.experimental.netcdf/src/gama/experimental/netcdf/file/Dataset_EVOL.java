package gama.experimental.netcdf.file;

/** import java.io */
import java.io.File;
import java.io.IOException;
import ucar.ma2.Index;

/** import java.net */
import java.net.URI;

/** import java.util */
import java.util.ArrayList;
//import java.util.Collections;

/** local import */
//import ichthyop.core.Simulation;
//import ichthyop.ui.MainFrame;

//import ichthyop.util.Resources;
//import ichthyop.util.NCComparator;

/** import netcdf */
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/** import pour lire les ncml*/
import ucar.nc2.ncml.NcMLReader;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.NcMLReader;

/**
 * The class manages the NetCDF dataset from input files or OPeNDAP location and
 * provides methods that constitute the numerical core of the lagrangian model.
 * <br>
 * Historically this class has always been the numerical core of the model. It
 * would have been more appropriate to separate the IO functions that manipulate
 * he model input files from the numerical methods that process the data, but
 * the organisation of this class have never been brought into question. <br>
 * The class manages the model input files, being local files or an OPeNDAP
 * location. It is in charge to find the appropriate data in time dimension,
 * function of the current time of the simulation. It also feeds the
 * {@code Particle} object with interpolated data of velocity, position,
 * temperature, salinity, etc... depending on the options. It provides all
 * numerical methods to simulate the transport of the particle:
 * <ul>
 * <li>horizontal advection
 * <li>vertical advection
 * <li>horizontal dispersion
 * <li>vertical dispersion
 * </ul>
 * The class also provides methods to easily switch from the computational grid
 * (x-y-z coordinates) to the geodesic space (lon-lat-depth coordinates).
 *
 * <p>
 * Copyright: Copyright (c) 2007 - Free software under GNU GPL
 * </p>
 *
 * @author P.Verley
 */
public abstract class Dataset_EVOL {

	///////////////////////////////
	// Declaration of the variables
	///////////////////////////////
	/**
	 * Grid dimension
	 */
	static int nx, ny, nz;
	/**
	 * Origin for grid index
	 */
	static int ipo, jpo;
	/**
	 * Number of time records in current NetCDF file
	 */
	// static int nbTimeRecords_init; // pour le premier mois
	static int nbTimeRecords; // Pour vérification chaque moi (car change dans certaines sorties ROMS)
	/**
	 * The NetCDF dataset
	 */
	static NetcdfFile ncIn;
	/**
	 * Longitude at rho point.
	 */
	public static double[][] lonRho;
	/**
	 * Latitude at rho point.
	 */
	public static double[][] latRho;
	/**
	 * Bathymetry
	 */
	static double[][] hRho;
	/**
	 * Mask: water = 1, cost = 0
	 */
	static byte[][] maskRho;
	static byte[][] masku;
	static byte[][] maskv;
	/**
	 * Ocean free surface elevetation at current time
	 */
	static float[][] zeta_tp0;
	/**
	 * /** Ocean free surface elevetation at time t + dt
	 */
	static float[][] zeta_tp1;
	/**
	 * Zonal component of the velocity field at current time
	 */
	static float[][][] u_tp0;
	/**
	 * Zonal component of the velocity field at time t + dt
	 */
	static float[][][] u_tp1;
	/**
	 * Meridional component of the velocity field at current time
	 */
	static float[][][] v_tp0;
	/**
	 * Meridional component of the velocity field at time t + dt
	 */
	static float[][][] v_tp1;
	/**
	 * Vertical component of the velocity field at current time
	 */
	static float[][][] w_tp0;
	/**
	 * Vertical component of the velocity field at time t + dt
	 */
	static float[][][] w_tp1;
	// pour la prof de la thermocline (ou de mélange)
	static float[][] hbl_tp0;
	static float[][] hbl_tp1;
	/**
	 * Water salinity at time t + dt
	 */
	private static float[][][] salt_tp1;
	/**
	 * Water salinity at current time
	 */
	private static float[][][] salt_tp0;
	/**
	 * Water temperature at current time
	 */
	private static float[][][] temp_tp0;
	/**
	 * Water temperature at time t + dt
	 */
	private static float[][][] temp_tp1;
	/**
	 * Large zooplankton concentration at current time
	 *
	 * PISCES : float NCHL(time, s_rho, eta_rho, xi_rho) ; NCHL:long_name =
	 * "averaged Chlorophyll in nano" ; NCHL:units = "mg Chl m-3" ; NCHL:field =
	 * "NCHL, scalar, series" ; float DCHL(time, s_rho, eta_rho, xi_rho) ;
	 * DCHL:long_name = "averaged Chlorophyll in diatoms" ; DCHL:units = "mg Chl
	 * m-3" ; DCHL:field = "DCHL, scalar, series" ;
	 * 
	 * static String strDiatoms, strNanoPhyto, strMicroZoo, strMesoZoo, strO2;
	 */
	private static float[][][] Diatoms_tp0, NanoPhyto_tp0, MicroZoo_tp0, MesoZoo_tp0, O2_tp0;
	private static float[][][] Diatoms_tp1, NanoPhyto_tp1, MicroZoo_tp1, MesoZoo_tp1, O2_tp1;

	private static float[][] Diatoms_integ_tp0, NanoPhyto_integ_tp0, MicroZoo_integ_tp0, MesoZoo_integ_tp0;
	private static float[][] Diatoms_integ_tp1, NanoPhyto_integ_tp1, MicroZoo_integ_tp1, MesoZoo_integ_tp1;

	private static float[][][] Phyto_tp0, Zoo_tp0, Det_tp0, NO3_tp0, Chla_tp0;
	private static float[][][] Phyto_tp1, Zoo_tp1, Det_tp1, NO3_tp1, Chla_tp1;

	private static float[][][] NanoPhytoChl_tp0, DiatomsChl_tp0;
	private static float[][][] NanoPhytoChl_tp1, DiatomsChl_tp1;
	/**
	 * Vertical diffusion coefficient at time t + dt
	 */
	private static float[][][] kv_tp1;
	/**
	 * Vertical diffusion coefficient at current time
	 */
	private static float[][][] kv_tp0;
	/**
	 * Depth at rho point
	 */
	static double[][][] z_rho_cst;
	/**
	 * Depth at w point at current time. Takes account of free surface elevation.
	 */
	static double[][][] z_w_tp0;
	/**
	 * Depth at w point at time t + dt Takes account of free surface elevation.
	 */
	static double[][][] z_w_tp1;
	/**
	 * Depth at w point. The free surface elevation is disregarded.
	 */
	static double[][][] z_w_cst;
	/**
	 * Geographical boundary of the domain
	 */
	private static double latMin, lonMin, latMax, lonMax;
	/**
	 * Maximum depth [meter] of the domain
	 */
	private static double depthMax;
	/**
	 * Time step [days] between two records in NetCDF dataset
	 */
	public static int dt_HyMo;
	/**
	 * List on NetCDF input files in which dataset is read.
	 */
	static private ArrayList<String> listInputFiles;
	/**
	 * Index of the current file read in the {@code listInputFiles}
	 */
	static private int indexFile;
	/**
	 * Time t + dt expressed in seconds
	 */
	static double time_tp1;
	/**
	 * Current rank in NetCDF dataset
	 */
	private int rank;
	/**
	 * Time arrow: forward = +1, backward = -1
	 */
	private static int time_arrow;
	/**
	 * Name of the Dimension in NetCDF file
	 */
	static String strXiDim, strEtaDim, strZDim, strTimeDim;
	/**
	 * Name of the Variable in NetCDF file
	 */
	static String strU, strV, strW, strOmega, strHBL, strTp, strSal, strTime, strZeta;
	/**
	 * Name of the Variable in NetCDF file
	 */
	static String strLon, strLat, strMask_rho, strBathy;
	// static String strMask_u,strMask_v;
	/**
	 * Name of the Variable in NetCDF file
	 */
	static String strKv;
	/**
	 * Name of the Variable in NetCDF file
	 */
	// PISCES
	static String strDiatoms, strNanoPhyto, strMicroZoo, strMesoZoo, strO2;
	static String strDiatomsChl, strNanoPhytoChl;
	static String strPhytot, strZootot;

	static String strDiatoms_int, strNanoPhyto_int, strMicroZoo_int, strMesoZoo_int, strGoc_int, strPoc_int;
	// NPZD
	static String strPhyto, strZoo, strDet, strNO3, strCHLA;

	/**
	 * Determines whether or not the temperature field should be read in the NetCDF
	 * file, function of the user's options.
	 */
	private static boolean FLAG_TP;
	private static boolean FLAG_W;
	/**
	 * Determines whether or not the salinity field should be read in the NetCDF
	 * file, function of the user's options.
	 */
	private static boolean FLAG_SAL;
	/**
	 * Determines whether or not the turbulent diffusivity should be read in the
	 * NetCDF file, function of the user's options.
	 */
	private static boolean FLAG_VDISP, FLAG_HBL;
	/**
	 * Determines whether or not the plankton concentration fields should be read in
	 * the NetCDF file, function of the user's options.
	 */
	private static boolean FLAG_PLANKTON_PISCES, FLAG_PLANKTON_NPZD, FLAG_PLANKTON_PISCES_tot;
	/**
	 * Mersenne Twister pseudo random number generator
	 * 
	 * @see ichthyop.util.MTRandom
	 */

	// Pour l'héritage de la région:
	public static String directory_roms, sufixe, directory_Suplementary_data, oxyclin_filename, PonteOBS_filename,
			PonteCLM_filename;
	static double[][] pm, pn;
	String strThetaS, strThetaB, strHc, strPn, strPm;
	public static int yearlist100[];
	static String filtre_roms_output;
	static int prof_potentielles[];
	static double prof_ponte_min, prof_ponte_max;
	public static double lat_min;
	public static double lat_max;
	public static double lon_min;
	public static double lon_max;
	static int prof_talu, SIMU, sponge_km;
	public static int bathy_max;
	public static String region, main_output_directory, Clim_chlaSeaWiFS;

	///////////////////////////////
	// Declaration of the constants
	///////////////////////////////
	////////////////////////////
	// Definition of the methods
	////////////////////////////
	/**
	 * Sets up the {@code Dataset}. The method first sets the appropriate variable
	 * names, loads the first NetCDF dataset and extract the time non-dependant
	 * information, such as grid dimensions, geographical boundaries, depth at sigma
	 * levels.
	 * 
	 * @throws an
	 *             IOException if an error occurs while setting up the
	 *             {@code Dataset}
	 */
	public void setUp() throws IOException {

		getFieldsName();
		openLocation(directory_roms);
		getDimNC();
		readConstantField();
		getDimGeogArea();
		getCstSigLevels();
		z_w_tp0 = getSigLevels();
	}

	// Construire le nom des fichier roms, avec l'année et le mois:
	// ( Doit etre adapter pour chaque config)
	abstract void getFieldsName();

	static String get_filename(int year, int month) {
		// Structure du nom des fichier netcdf roms :
		String fileName = directory_roms + region + "_Y" + year + "M" + month + sufixe;
		// String fileName = directory_roms + "NCML/NCML_per_MONTH/union_plankton_Y" +
		// year + "M" +month + ".ncml";
		// String fileName = "/Volumes/SIMU_PEDRO/SENEGAL/union_plankton.ncml";
		// String fileName = "/Users/timbrochier/Desktop/tmp/TEST/union_plankton_Y" +
		// year +"M" +month + ".ncml";

		return fileName;
	}

	/**
	 * Reads the dimensions of the NetCDF dataset
	 * 
	 * @throws an
	 *             IOException if an error occurs while reading the dimensions.
	 */
	private void getDimNC() throws IOException {

		try {
			nx = ncIn.findDimension(strXiDim).getLength();
			ny = ncIn.findDimension(strEtaDim).getLength();
			nz = ncIn.findDimension(strZDim).getLength();
		} catch (NullPointerException e) {
			throw new IOException(
					"Problem reading dimensions from dataset " + ncIn.getLocation() + " : " + e.getMessage());
		}
		System.out.println("nx = " + nx);
		System.out.println("ny = " + ny);
		System.out.println("nz = " + nz);

		ipo = jpo = 0;
	}

	/**
	 * Gets cell dimension [meter] in the XI-direction.
	 */
	double getdxi(int j, int i) {
		return (pm[j][i] != 0) ? (1 / pm[j][i]) : 0.d;
	}

	/**
	 * Gets cell dimension [meter] in the ETA-direction.
	 */
	double getdeta(int j, int i) {
		return (pn[j][i] != 0) ? (1 / pn[j][i]) : 0.d;
	}

	/**
	 * <p>
	 * The functions computes the 2nd order approximate derivative at index i
	 * </p>
	 * <code>diff2(X, i) == diff(diff(X), i) == diff(diff(X))[i]</code>
	 * 
	 * @param x
	 *            double[]
	 * @param i
	 *            int
	 * @return double
	 */
	private double diff2(double[] X, int k) {

		int length = X.length;
		/** Returns NaN if size <= 2 */
		if (length < 3) {
			return Double.NaN;
		}

		/**
		 * This return statement traduces the natural spline hypothesis M(0) = M(nz - 1)
		 * = 0
		 */
		if ((k == 0) || (k == (length - 1))) {
			return 0.d;
		}

		return (X[k + 1] - 2.d * X[k] + X[k - 1]);
	}

	/**
	 * Computes the depth at w points, taking account of the free surface elevation.
	 * 
	 * @return a double[][][], the depth at w point.
	 */
	static double[][][] getSigLevels() {

		// -----------------------------------------------------
		// Daily recalculation of z_w and z_r with zeta

		double[][][] z_w_tmp = new double[nz + 1][ny][nx];
		double[][][] z_w_cst_tmp = z_w_cst;

		// System.out.print("Calculation of the s-levels\n");

		for (int i = nx; i-- > 0;) {
			for (int j = ny; j-- > 0;) {
				if (zeta_tp1[j][i] == 999.f) {
					zeta_tp1[j][i] = 0.f;
				}
				for (int k = 0; k < nz + 1; k++) {
					z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i]
							+ zeta_tp1[j][i] * (1.f + z_w_cst_tmp[k][j][i] / hRho[j][i]);
				}
			}
		}
		z_w_cst_tmp = null;
		return z_w_tmp;
	}

	/**
	 * Determines the geographical boundaries of the domain in longitude, latitude
	 * and depth.
	 */
	private void getDimGeogArea() {

		// --------------------------------------
		// Calculate the Physical Space extrema

		lonMin = Double.MAX_VALUE;
		lonMax = -lonMin;
		latMin = Double.MAX_VALUE;
		latMax = -latMin;
		depthMax = 0.d;
		int i = nx;
		int j = 0;

		while (i-- > 0) {
			j = ny;
			while (j-- > 0) {
				if (lonRho[j][i] >= lonMax) {
					lonMax = lonRho[j][i];
				}
				if (lonRho[j][i] <= lonMin) {
					lonMin = lonRho[j][i];
				}
				if (latRho[j][i] >= latMax) {
					latMax = latRho[j][i];
				}
				if (latRho[j][i] <= latMin) {
					latMin = latRho[j][i];
				}

				if (hRho[j][i] >= depthMax) {
					depthMax = hRho[j][i];
				}
			}
		}
		// System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " +
		// latMin + " latmax " + latMax);
		// System.out.println("depth max " + depthMax);

		double double_tmp;
		if (lonMin > lonMax) {
			double_tmp = lonMin;
			lonMin = lonMax;
			lonMax = double_tmp;
		}

		if (latMin > latMax) {
			double_tmp = latMin;
			latMin = latMax;
			latMax = double_tmp;
		}
	}

	/**
	 * Initializes the {@code Dataset}. Opens the file holding the first time of the
	 * simulation. Checks out the existence of the fields required by the current
	 * simulation. Sets all fields at time for the first time step.
	 * 
	 * @throws an
	 *             IOException if a required field cannot be found in the NetCDF
	 *             dataset.
	 */
	public void init() throws IOException {
		System.out.println("initialisation... ");
		time_arrow = 1;
		// long t0 = Simulation.get_t0();
		// ici on ne repere plus les fichiers par leur "t0" mais par leur nom
		// directement:
		// open(getFile(t0));
		String fileName = get_filename(1980, 1);
		open(fileName);

		nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
		System.out.println(
				"Number of records per month in the ROMS outputs file : " + nbTimeRecords + " (updated each month)");

		// 10/01/2012 : Ici on lit le scrum-time aux 1er et 2eme pas de temps du netcdf.
		// Si un seul pas de temps par fichier mensuel, il faut lire le 1ire le 1er pas
		// de temps du mois suivant...
		// Par mesure de simplification dans ce cas, on fixe directement dt_HyMo = 30
		// jours
		if (nbTimeRecords > 1) {
			get_dt_HyMo();
		} else {
			dt_HyMo = 30; // (30 jours)
			System.out.println("Time step in DAYS between two records in NetCDF dataset : dt_HyMo = " + dt_HyMo);
		}

		FLAG_W = false;
		if (ncIn.findVariable(strW) == null) {
			System.out.println(" ATENTION !!! Il n'y a pas " + strW + " dans les sorties, il sera donc recalculé!!! ");
			System.out.println(" 16 avril 2014 : On neglige l'advection verticale --> on ne recalcule plus W ");

			// throw new IOException(
			// "Vertical momentum field " +
			// strW + " not found in file " + ncIn.getLocation());
		} else {
			System.out.println("**--> La vitesse verticale  " + strW
					+ " POURRAIT ETRE lue dans les sorties (mais elle est recalculée pour l'instant) <--**");
			FLAG_W = true;
		}

		/** salinity */
		FLAG_SAL = false;
		if (ncIn.findVariable(strSal) == null) {
			throw new IOException("Salinity field " + strSal + " not found in file " + ncIn.getLocation());
		} else {
			FLAG_SAL = true;
		}

		/** temperature */
		FLAG_TP = false;
		if (ncIn.findVariable(strTp) == null) {
			throw new IOException("Temperature field " + strTp + " not found in file " + ncIn.getLocation());
		} else {
			FLAG_TP = true;
		}

		/** prof de couche de melange */
		FLAG_HBL = false;
		if (ncIn.findVariable(strHBL) == null) {
			System.out.println("Field " + strHBL + " not found in file " + ncIn.getLocation());
		} else {
			FLAG_HBL = true;
		}

		/** vertical diffusivity */
		FLAG_VDISP = false;

		FLAG_PLANKTON_NPZD = false;

		// 6 nov 2016 : pour lecture champs Phytot et Zootot:
		FLAG_PLANKTON_PISCES_tot = true;
		FLAG_PLANKTON_PISCES = false;

		// if (Simulation.flagGrowth ){//|| Simulation.flagSwim_O2) { // rétablir si
		// lecture O2 Pisces necessaire pour swim (retablir dans Poissons et dans
		// Dataset_EVOL)
		if (FLAG_PLANKTON_PISCES) {

		}

		if (FLAG_PLANKTON_NPZD) {

		}

		// System.out.println("rank = " + findCurrentRank(t0));
		setAllFieldsTp1AtTime(rank = 0);

		// time_tp1 = t0;

	}

	private void get_dt_HyMo() {

		try {
			// ucar.ma2.ArrayFloat.D1 xTimeTp1 = (ucar.ma2.ArrayFloat.D1)
			// ncIn.findVariable(strTime).read();
			Array xTimeTp1 = ncIn.findVariable(strTime).read();// time_tp1 =
																// xTimeTp1.getFloat(xTimeTp1.getIndex().set(0));
																// //xTimeTp1.getFloat(xTimeTp1.getIndex().set(0));
			// time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(1));
			time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(1));
			double time_tp0 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(0));
			// double time_tp1 = xTimeTp1.get(1);
			// double time_tp0 = xTimeTp1.get(0);

			// double [] time_tp1_tmp = (double[]) xTimeTp1.copyTo1DJavaArray();
			// //xTimeTp1.getFloat(xTimeTp1.getIndex().set(0));
			// time_tp1 = xTimeTp1.get(1);//time_tp1_tmp[0];

			// time_tp1 -= time_tp1 % 60;
			// time_tp0 -= time_tp0 % 60;

			dt_HyMo = (int) Math.round(Math.abs(time_tp1 - time_tp0) / 3600 / 24);
			System.out.println("Time step in DAYS between two records in NetCDF dataset : dt_HyMo = " + dt_HyMo);
		} catch (IOException e) {
			System.out.println("Problem extracting dt_HyMo : " + e.getMessage());
		}
	}

	/**
	 * Updates time dependant fields at specified time.
	 * 
	 * @param time
	 *            a long, the current time [second] of the simulation.
	 * @throws an
	 *             IOException if an error occurs while setting the fields at time.
	 */
	public static void setAllFieldsAtTime() throws IOException {
		// Modif 26 mai 2009 : dernier mois de l'année --> 1er moi de l'année SUIVANTE

		// System.out.println(" -----> 111 XXXXXXXXXXXXXXXXXXXXXXXX rank = " + rank);

		u_tp0 = u_tp1;
		v_tp0 = v_tp1;
		w_tp0 = w_tp1;
		zeta_tp0 = zeta_tp1;
		temp_tp0 = temp_tp1;
		salt_tp0 = salt_tp1;
		hbl_tp0 = hbl_tp1;
		kv_tp0 = kv_tp1;

		Diatoms_tp0 = Diatoms_tp1;
		NanoPhyto_tp0 = NanoPhyto_tp1;
		MicroZoo_tp0 = MicroZoo_tp1;
		MesoZoo_tp0 = MesoZoo_tp1;
		O2_tp0 = O2_tp1;

		Diatoms_integ_tp0 = Diatoms_integ_tp1;
		NanoPhyto_integ_tp0 = NanoPhyto_integ_tp1;
		MicroZoo_integ_tp0 = MicroZoo_integ_tp1;
		MesoZoo_integ_tp0 = MesoZoo_integ_tp1;

		Phyto_tp0 = Phyto_tp1;
		Zoo_tp0 = Zoo_tp1;
		Det_tp0 = Det_tp1;
		NO3_tp0 = NO3_tp1;
		Chla_tp0 = Chla_tp1;

		NanoPhytoChl_tp0 = NanoPhytoChl_tp1;
		DiatomsChl_tp0 = DiatomsChl_tp1;

		if (z_w_tp1 != null) {
			z_w_tp0 = z_w_tp1;
		}
	}

	/**
	 * Reads time dependant variables in NetCDF dataset at specified rank.
	 * 
	 * @param rank
	 *            an int, the rank of the time dimension in the NetCDF dataset.
	 * @throws an
	 *             IOException if an error occurs while reading the variables.
	 */
	static void setAllFieldsTp1AtTime(int rank) throws IOException {

		int[] origin = new int[] { rank, 0, jpo, ipo };
		// double time_tp0 = time_tp1;

		try {
			// System.out.println("Lecture des tp1 ...");
			u_tp1 = (float[][][]) ncIn.findVariable(strU).read(origin, new int[] { 1, nz, ny, (nx - 1) }).reduce()
					.copyToNDJavaArray();

			v_tp1 = (float[][][]) ncIn.findVariable(strV).read(origin, new int[] { 1, nz, (ny - 1), nx }).reduce()
					.copyToNDJavaArray();

			// TIMOTHEE 3 Juillet 2009
			// POUR L'INSTANT NE MARCHE PAS --> on force FLAG_W = false;
			FLAG_W = false;
			if (FLAG_W) {

				w_tp1 = (float[][][]) ncIn.findVariable(strW).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();

				// A FAIRE : Remplir le niveaux sigma nz+1 (au dessus de la surface avec
				// les memes vitesses que le niveau nz.
				// Vérifier que tout est OK...
			}

			Array xTimeTp1 = ncIn.findVariable(strTime).read();
			time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(rank));
			// time_tp1 -= time_tp1 % 60;
			xTimeTp1 = null;
			zeta_tp1 = (float[][]) ncIn.findVariable(strZeta).read(new int[] { rank, 0, 0 }, new int[] { 1, ny, nx })
					.reduce().copyToNDJavaArray();

			if (FLAG_TP) {
				temp_tp1 = (float[][][]) ncIn.findVariable(strTp).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
			}

			if (FLAG_SAL) {
				salt_tp1 = (float[][][]) ncIn.findVariable(strSal).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
			}

			if (FLAG_VDISP) {
				kv_tp1 = (float[][][]) ncIn.findVariable(strKv).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
			}

			if (FLAG_PLANKTON_PISCES) {
				Diatoms_tp1 = (float[][][]) ncIn.findVariable(strDiatoms).read(origin, new int[] { 1, nz, ny, nx })
						.reduce().copyToNDJavaArray();
				NanoPhyto_tp1 = (float[][][]) ncIn.findVariable(strNanoPhyto).read(origin, new int[] { 1, nz, ny, nx })
						.reduce().copyToNDJavaArray();
				MicroZoo_tp1 = (float[][][]) ncIn.findVariable(strMicroZoo).read(origin, new int[] { 1, nz, ny, nx })
						.reduce().copyToNDJavaArray();
				MesoZoo_tp1 = (float[][][]) ncIn.findVariable(strMesoZoo).read(origin, new int[] { 1, nz, ny, nx })
						.reduce().copyToNDJavaArray();
				// Vendredi 13 Septembre 2013 : on vire l'O2 (pas dans les sorties de pedro)
				// O2_tp1 =
				// (float[][][]) ncIn.findVariable(strO2).
				// read(origin, new int[]{1, nz, ny, nx}).reduce().
				// copyToNDJavaArray();
			}

			if (FLAG_PLANKTON_PISCES_tot) {
				Phyto_tp1 = (float[][][]) ncIn.findVariable(strPhytot).read(origin, new int[] { 1, nz, ny, nx })
						.reduce().copyToNDJavaArray();
				Zoo_tp1 = (float[][][]) ncIn.findVariable(strZootot).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
			}

			if (FLAG_PLANKTON_NPZD) {
				Phyto_tp1 = (float[][][]) ncIn.findVariable(strPhyto).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
				Zoo_tp1 = (float[][][]) ncIn.findVariable(strZoo).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
				Det_tp1 = (float[][][]) ncIn.findVariable(strDet).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
				NO3_tp1 = (float[][][]) ncIn.findVariable(strNO3).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
				Chla_tp1 = (float[][][]) ncIn.findVariable(strCHLA).read(origin, new int[] { 1, nz, ny, nx }).reduce()
						.copyToNDJavaArray();
			}

		} catch (IOException e) {
			throw new IOException("P1 - Problem extracting fields at location " + ncIn.getLocation().toString() + " : "
					+ e.getMessage());
		} catch (InvalidRangeException e) {
			throw new IOException("P2 - Problem extracting fields at location " + ncIn.getLocation().toString() + " : "
					+ e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException("P3 - Problem extracting fields at location " + ncIn.getLocation().toString() + " : "
					+ e.getMessage());
		}

		if (!FLAG_W) {
			// mercredi 16 avril 2014, Ngor : on arrete de calculer W
			// (les distri verticales sont fixee selon obs)
			// z_w_tp1 = getSigLevels();
			// w_tp1 = computeW();
		}
	}

	/**
	 * Computes the vertical velocity vector.
	 *
	 * @see ichthyop.io.Dataset#computeW for details about the method
	 */
	static float[][][] computeW() {

		System.out.println("Compute vertical velocity (ce sera cool quand aurra le w dans les sorties ROMS)");
		double[][][] Huon = new double[nz][ny][nx];
		double[][][] Hvom = new double[nz][ny][nx];
		double[][][] z_w_tmp = z_w_tp1;

		// ---------------------------------------------------
		// Calculation Coeff Huon & Hvom
		for (int k = nz; k-- > 0;) {
			for (int i = 0; i++ < nx - 1;) {
				for (int j = ny; j-- > 0;) {
					Huon[k][j][i] = (((z_w_tmp[k + 1][j][i] - z_w_tmp[k][j][i])
							+ (z_w_tmp[k + 1][j][i - 1] - z_w_tmp[k][j][i - 1])) / (pn[j][i] + pn[j][i - 1]))
							* u_tp1[k][j][i - 1];
				}
			}
			for (int i = nx; i-- > 0;) {
				for (int j = 0; j++ < ny - 1;) {
					Hvom[k][j][i] = (((z_w_tmp[k + 1][j][i] - z_w_tmp[k][j][i])
							+ (z_w_tmp[k + 1][j - 1][i] - z_w_tmp[k][j - 1][i])) / (pm[j][i] + pm[j - 1][i]))
							* v_tp1[k][j - 1][i];
				}

			}
		}

		// ---------------------------------------------------
		// Calcultaion of w(i, j, k)
		double[] wrk = new double[nx];
		double[][][] w_double = new double[nz + 1][ny][nx];

		for (int j = ny - 1; j-- > 0;) {
			for (int i = nx; i-- > 0;) {
				w_double[0][j][i] = 0.f;
			}

			for (int k = 0; k++ < nz;) {
				for (int i = nx - 1; i-- > 0;) {
					w_double[k][j][i] = w_double[k - 1][j][i] + (float) (Huon[k - 1][j][i] - Huon[k - 1][j][i + 1]
							+ Hvom[k - 1][j][i] - Hvom[k - 1][j + 1][i]);
				}

			}
			for (int i = nx; i-- > 0;) {
				wrk[i] = w_double[nz][j][i] / (z_w_tmp[nz][j][i] - z_w_tmp[0][j][i]);
			}

			for (int k = nz; k-- >= 2;) {
				for (int i = nx; i-- > 0;) {
					w_double[k][j][i] += -wrk[i] * (z_w_tmp[k][j][i] - z_w_tmp[0][j][i]);
				}

			}
			for (int i = nx; i-- > 0;) {
				w_double[nz][j][i] = 0.f;
			}

		}

		// ---------------------------------------------------
		// Boundary Conditions
		for (int k = nz + 1; k-- > 0;) {
			for (int j = ny; j-- > 0;) {
				w_double[k][j][0] = w_double[k][j][1];
				w_double[k][j][nx - 1] = w_double[k][j][nx - 2];
			}

		}
		for (int k = nz + 1; k-- > 0;) {
			for (int i = nx; i-- > 0;) {
				w_double[k][0][i] = w_double[k][1][i];
				w_double[k][ny - 1][i] = w_double[k][ny - 2][i];
			}

		}

		// ---------------------------------------------------
		// w * pm * pn
		float[][][] w = new float[nz + 1][ny][nx];
		for (int i = nx; i-- > 0;) {
			for (int j = ny; j-- > 0;) {
				for (int k = nz + 1; k-- > 0;) {
					w[k][j][i] = (float) (w_double[k][j][i] * pm[j][i] * pn[j][i]);
				}

			}
		}
		// ---------------------------------------------------
		// Return w
		return w;

	}

	/**
	 * Computes the depth of the specified sigma level and the x-y particle
	 * location.
	 * 
	 * @param xRho
	 *            a double, x-coordinate of the grid point
	 * @param yRho
	 *            a double, y-coordinate of the grid point
	 * @param k
	 *            an int, the index of the sigma level
	 * @return a double, the depth [meter] at (x, y, k)
	 */
	public static double getDepth(double xRho, double yRho, int k) {
		final int i = (int) xRho;
		final int j = (int) yRho;
		double hh = 0.d;
		final double dx = (xRho - i);
		final double dy = (yRho - j);
		double co = 0.d;
		for (int ii = 0; ii < 2; ii++) {
			for (int jj = 0; jj < 2; jj++) {
				if (isInWater(i + ii, j + jj)) {
					co = Math.abs((1 - ii - dx) * (1 - jj - dy));
					double z_r = 0.d;
					z_r = z_rho_cst[k][j + jj][i + ii] + (double) zeta_tp0[j + jj][i + ii]
							* (1.d + z_rho_cst[k][j + jj][i + ii] / hRho[j + jj][i + ii]);
					hh += co * z_r;
				}

			}
		}
		return (hh);
	}

	/**
	 * Determines whether or not the specified grid cell(i, j) is in water.
	 * 
	 * @param i
	 *            an int, i-coordinate of the cell
	 * @param j
	 *            an intn the j-coordinate of the cell
	 * @return <code>true</code> if cell(i, j) is in water, <code>false</code>
	 *         otherwise.
	 */
	public static boolean isInWater(int i, int j) {

		// return (maskRho[j][i] * masku[j][i] *maskv[j][i] > 0);
		// 11 oct 2017 : echec de cet essais car pas possible de lire masku et maskv
		// (grille ij decalee)
		return (maskRho[j][i] > 0);
	}

	/**
	 * Determines whether the specified {@code RohPoint} is in water.
	 * 
	 * @param ptRho
	 *            the RhoPoint
	 * @return <code>true</code> if the {@code RohPoint} is in water,
	 *         <code>false</code> otherwise.
	 * @see #isInWater(int i, int j)
	 */
	public static boolean isInWater(double xRho, double yRho) {
		try {
			return (maskRho[(int) Math.round(yRho)][(int) Math.round(xRho)] > 0);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * Determines whether or not the specified grid point is close to cost line. The
	 * method first determines in which quater of the cell the grid point is
	 * located, and then checks wether or not its cell and the three adjacent cells
	 * to the quater are in water.
	 *
	 * @param pGrid
	 *            a double[] the coordinates of the grid point
	 * @return <code>true</code> if the grid point is close to cost,
	 *         <code>false</code> otherwise.
	 *
	 *         static boolean isCloseToCost(double[] pGrid) { int i, j, ii, jj; i =
	 *         (int) (Math.round(pGrid[0])); j = (int) (Math.round(pGrid[1])); ii =
	 *         (i - (int) pGrid[0]) == 0 ? 1 : -1; jj = (j - (int) pGrid[1]) == 0 ?
	 *         1 : -1; return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) &&
	 *         isInWater(i, j + jj)); } static boolean isCloseToCost(double xRho,
	 *         double yRho) { int i, j, ii, jj; i = (int) (Math.round(xRho)); j =
	 *         (int) (Math.round(yRho)); ii = (i - (int) xRho) == 0 ? 1 : -1; jj =
	 *         (j - (int) yRho) == 0 ? 1 : -1; return !(isInWater(i + ii, j) &&
	 *         isInWater(i + ii, j + jj) && isInWater(i, j + jj)); }
	 */

	// modif TIM: plus contraingnant que la version ci-dessus
	// necessaire pour eviter d'interpoler avec des temperatures de 0°C
	// retablit le 10 juillet 2017 (après les simu du papier!)
	static boolean isCloseToCost(double[] pGrid) {

		int i, j;
		i = (int) (Math.floor(pGrid[0]));
		j = (int) (Math.floor(pGrid[1]));
		return !(isInWater(i, j) && isInWater(i + 1, j) && isInWater(i, j + 1) && isInWater(i + 1, j + 1));
	}

	static boolean isCloseToCost(double xRho, double yRho) {

		int i, j;
		i = (int) (Math.floor(xRho));
		j = (int) (Math.floor(yRho));
		return !(isInWater(i, j) && isInWater(i + 1, j) && isInWater(i, j + 1) && isInWater(i + 1, j + 1));
	}
	// * */

	/**
	 * Transforms the depth at specified x-y particle location into z coordinate
	 *
	 * @param xRho
	 *            a double, the x-coordinate
	 * @param yRho
	 *            a double, the y-coordinate
	 * @param depth
	 *            a double, the depth of the particle
	 * @return a double, the z-coordinate corresponding to the depth
	 */
	public static double depth2z(double xRho, double yRho, double depth) {

		// -----------------------------------------------
		// Return z[grid] corresponding to depth[meters]
		double z = 0.d;
		int lk = nz - 1;
		while ((lk > 0) && (getDepth(xRho, yRho, lk) > depth)) {
			lk--;
		}

		if (lk == (nz - 1)) {
			z = (double) lk;
		} else {
			double pr = getDepth(xRho, yRho, lk);
			z = Math.max(0.d, (double) lk + (depth - pr) / (getDepth(xRho, yRho, lk + 1) - pr));
		}

		return (z);
	}

	/**
	 * Transforms the depth at specified i-j grid location into z coordinate
	 *
	 * @param i
	 *            an int, the i-coordinate
	 * @param j
	 *            an int, the j-coordinate
	 * @param depth
	 *            a double, the depth of the particle
	 * @return a double, the z-coordinate corresponding to the depth
	 */
	public static double depth2z(int i, int j, double depth) {
		// -----------------------------------------------
		// Return z[grid] corresponding to depth[meters]
		double z = 0.d;
		int lk = nz - 1;
		while ((lk > 0) && (z_rho_cst[lk][j][i] > depth)) {
			lk--;
		}

		if (lk == (nz - 1)) {
			z = (double) lk;
		} else {
			z = Math.max(0.d, lk + (depth - z_rho_cst[lk][j][i]) / (z_rho_cst[lk + 1][j][i] - z_rho_cst[lk][j][i]));
		}

		return (z);
	}

	/**
	 *
	 * @param x
	 *            double
	 * @param y
	 *            double
	 * @param z
	 *            double
	 * @return double
	 */
	static double z2depth(double x, double y, double z) {

		final double kz = Math.max(0.d, Math.min(z, (double) nz - 1.00001f));
		final int i = (int) Math.floor(x);
		final int j = (int) Math.floor(y);
		final int k = (int) Math.floor(kz);
		double depth = 0.d;
		final double dx = x - (double) i;
		final double dy = y - (double) j;
		final double dz = kz - (double) k;
		double co = 0.d;
		double z_r;
		for (int ii = 0; ii < 2; ii++) {
			for (int jj = 0; jj < 2; jj++) {
				for (int kk = 0; kk < 2; kk++) {
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
					if (isInWater(i + ii, j + jj)) {
						z_r = z_rho_cst[k + kk][j + jj][i + ii] + (double) zeta_tp0[j + jj][i + ii]
								* (1.d + z_rho_cst[k + kk][j + jj][i + ii] / hRho[j + jj][i + ii]);
						depth += co * z_r;
					}
				}
			}
		}
		return depth;
	}

	/**
	 * Transforms the specified 3D grid coordinates into geographical coordinates.
	 * It merely does a trilinear spatial interpolation of the surrounding grid nods
	 * geographical coordinates.
	 * 
	 * @param xRho
	 *            a double, the x-coordinate
	 * @param yRho
	 *            a double, the y-coordinate
	 * @param zRho
	 *            a double, the z-coordinate
	 * @return a double[], the corresponding geographical coordinates (latitude,
	 *         longitude, depth)
	 */
	public static double[] grid2Geo(double xRho, double yRho, double zRho) {

		// --------------------------------------------------------------------
		// Computational space (x, y , z) => Physical space (lat, lon, depth)

		final double kz = Math.max(0.d, Math.min(zRho, (double) nz - 1.00001f));
		final int i = (int) Math.floor(xRho);
		final int j = (int) Math.floor(yRho);
		final int k = (int) Math.floor(kz);
		double latitude = 0.d;
		double longitude = 0.d;
		double depth = 0.d;
		final double dx = xRho - (double) i;
		final double dy = yRho - (double) j;
		final double dz = kz - (double) k;
		double co = 0.d;
		double z_r;
		for (int ii = 0; ii < 2; ii++) {
			for (int jj = 0; jj < 2; jj++) {
				for (int kk = 0; kk < 2; kk++) {
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
					latitude += co * latRho[j + jj][i + ii];
					longitude += co * lonRho[j + jj][i + ii];
					if (isInWater(i + ii, j + jj)) {
						z_r = z_rho_cst[k + kk][j + jj][i + ii] + (double) zeta_tp0[j + jj][i + ii]
								* (1.d + z_rho_cst[k + kk][j + jj][i + ii] / hRho[j + jj][i + ii]);

						depth += co * z_r;
					}
				}
			}
		}

		return (new double[] { latitude, longitude, depth });
	}

	/**
	 * * Transforms the specified 2D grid coordinates into geographical coordinates.
	 * It merely does a bilinear spatial interpolation of the surrounding grid nods
	 * geographical coordinates.
	 * 
	 * @param xRho
	 *            a double, the x-coordinate
	 * @param yRho
	 *            a double, the y-coordinate
	 * @return a double[], the corresponding geographical coordinates (latitude,
	 *         longitude)
	 * @param xRho
	 *            double
	 * @param yRho
	 *            double
	 * @return double[]
	 */
	public static double[] grid2Geo(double xRho, double yRho) {

		// --------------------------------------------------------------------
		// Computational space (x, y , z) => Physical space (lat, lon, depth)

		final double ix = Math.max(0.00001f, Math.min(xRho, (double) nx - 1.00001f));
		final double jy = Math.max(0.00001f, Math.min(yRho, (double) ny - 1.00001f));

		final int i = (int) Math.floor(ix);
		final int j = (int) Math.floor(jy);
		double latitude = 0.d;
		double longitude = 0.d;
		final double dx = ix - (double) i;
		final double dy = jy - (double) j;
		double co = 0.d;
		for (int ii = 0; ii < 2; ii++) {
			for (int jj = 0; jj < 2; jj++) {
				co = Math.abs((1 - ii - dx) * (1 - jj - dy));
				latitude += co * latRho[j + jj][i + ii];
				longitude += co * lonRho[j + jj][i + ii];
			}

		}
		return (new double[] { latitude, longitude });
	}

	/**
	 * Transforms the specified 2D geographical coordinates into a grid coordinates.
	 *
	 * The algorithme has been adapted from a function in ROMS/UCLA code, originally
	 * written by Alexander F. Shchepetkin and Hernan G. Arango. Please find below
	 * an extract of the ROMS/UCLA documention.
	 *
	 * <pre>
	 *  Checks the position to find if it falls inside the whole domain.
	 *  Once it is established that it is inside, find the exact cell to which
	 *  it belongs by successively dividing the domain by a half (binary
	 *  search).
	 * </pre>
	 * 
	 * @param lon
	 *            a double, the longitude of the geographical point
	 * @param lat
	 *            a double, the latitude of the geographical point
	 * @return a double[], the corresponding grid coordinates (x, y)
	 * @see #isInsidePolygone
	 */
	public static double[] geo2Grid(double lon, double lat) {

		// --------------------------------------------------------------------
		// Physical space (lat, lon) => Computational space (x, y)

		boolean found;
		int imin, imax, jmin, jmax, i0, j0;
		double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

		xgrid = -1.;
		ygrid = -1.;
		found = isInsidePolygone(0, nx - 1, 0, ny - 1, lon, lat);
		// -------------------------------------------
		// Research surrounding grid-points
		if (found) {
			imin = 0;
			imax = nx - 1;
			jmin = 0;
			jmax = ny - 1;
			while (((imax - imin) > 1) | ((jmax - jmin) > 1)) {
				if ((imax - imin) > 1) {
					i0 = (imin + imax) / 2;
					found = isInsidePolygone(imin, i0, jmin, jmax, lon, lat);
					if (found) {
						imax = i0;
					} else {
						imin = i0;
					}
				}
				if ((jmax - jmin) > 1) {
					j0 = (jmax + jmin) / 2;
					found = isInsidePolygone(imin, imax, jmin, j0, lon, lat);
					if (found) {
						jmax = j0;
					} else {
						jmin = j0;
					}
				}
			}

			// --------------------------------------------
			// Trilinear interpolation
			dy1 = latRho[jmin + 1][imin] - latRho[jmin][imin];
			dx1 = lonRho[jmin + 1][imin] - lonRho[jmin][imin];
			dy2 = latRho[jmin][imin + 1] - latRho[jmin][imin];
			dx2 = lonRho[jmin][imin + 1] - lonRho[jmin][imin];

			c1 = lon * dy1 - lat * dx1;
			c2 = lonRho[jmin][imin] * dy2 - latRho[jmin][imin] * dx2;
			deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
			deltax = (deltax - lonRho[jmin][imin]) / dx2;
			xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

			c1 = lonRho[jmin][imin] * dy1 - latRho[jmin][imin] * dx1;
			c2 = lon * dy2 - lat * dx2;
			deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
			deltay = (deltay - latRho[jmin][imin]) / dy1;
			ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
		}

		return (new double[] { xgrid, ygrid });
	}

	/**
	 * Determines whether the specified geographical point (lon, lat) belongs to the
	 * is inside the polygon defined by (imin, jmin) & (imin, jmax) & (imax, jmax) &
	 * (imax, jmin).
	 *
	 * <p>
	 * The algorithm has been adapted from a function in ROMS/UCLA code, originally
	 * written by Alexander F. Shchepetkin and Hernan G. Arango. Please find below
	 * an extract of the ROMS/UCLA documention.
	 * </p>
	 * 
	 * <pre>
	 * Given the vectors Xb and Yb of size Nb, defining the coordinates
	 * of a closed polygon,  this function find if the point (Xo,Yo) is
	 * inside the polygon.  If the point  (Xo,Yo)  falls exactly on the
	 * boundary of the polygon, it still considered inside.
	 * This algorithm does not rely on the setting of  Xb(Nb)=Xb(1) and
	 * Yb(Nb)=Yb(1).  Instead, it assumes that the last closing segment
	 * is (Xb(Nb),Yb(Nb)) --> (Xb(1),Yb(1)).
	 *
	 * Reference:
	 * Reid, C., 1969: A long way from Euclid. Oceanography EMR,
	 * page 174.
	 *
	 * Algorithm:
	 *
	 * The decision whether the point is  inside or outside the polygon
	 * is done by counting the number of crossings from the ray (Xo,Yo)
	 * to (Xo,-infinity), hereafter called meridian, by the boundary of
	 * the polygon.  In this counting procedure,  a crossing is counted
	 * as +2 if the crossing happens from "left to right" or -2 if from
	 * "right to left". If the counting adds up to zero, then the point
	 * is outside.  Otherwise,  it is either inside or on the boundary.
	 *
	 * This routine is a modified version of the Reid (1969) algorithm,
	 * where all crossings were counted as positive and the decision is
	 * made  based on  whether the  number of crossings is even or odd.
	 * This new algorithm may produce different results  in cases where
	 * Xo accidentally coinsides with one of the (Xb(k),k=1:Nb) points.
	 * In this case, the crossing is counted here as +1 or -1 depending
	 * of the sign of (Xb(k+1)-Xb(k)).  Crossings  are  not  counted if
	 * Xo=Xb(k)=Xb(k+1).  Therefore, if Xo=Xb(k0) and Yo>Yb(k0), and if
	 * Xb(k0-1) < Xb(k0) < Xb(k0+1),  the crossing is counted twice but
	 * with weight +1 (for segments with k=k0-1 and k=k0). Similarly if
	 * Xb(k0-1) > Xb(k0) > Xb(k0+1), the crossing is counted twice with
	 * weight -1 each time.  If,  on the other hand,  the meridian only
	 * touches the boundary, that is, for example, Xb(k0-1) < Xb(k0)=Xo
	 * and Xb(k0+1) < Xb(k0)=Xo, then the crossing is counted as +1 for
	 * segment k=k0-1 and -1 for segment k=k0, resulting in no crossing.
	 *
	 * Note 1: (Explanation of the logical condition)
	 *
	 * Suppose  that there exist two points  (x1,y1)=(Xb(k),Yb(k))  and
	 * (x2,y2)=(Xb(k+1),Yb(k+1)),  such that,  either (x1 < Xo < x2) or
	 * (x1 > Xo > x2).  Therefore, meridian x=Xo intersects the segment
	 * (x1,y1) -> (x2,x2) and the ordinate of the point of intersection
	 * is:
	 *                y1*(x2-Xo) + y2*(Xo-x1)
	 *            y = -----------------------
	 *                         x2-x1
	 * The mathematical statement that point  (Xo,Yo)  either coinsides
	 * with the point of intersection or lies to the north (Yo>=y) from
	 * it is, therefore, equivalent to the statement:
	 *
	 *      Yo*(x2-x1) >= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 > 0
	 * or
	 *      Yo*(x2-x1) <= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 < 0
	 *
	 * which, after noting that  Yo*(x2-x1) = Yo*(x2-Xo + Xo-x1) may be
	 * rewritten as:
	 *
	 *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) >= 0,   if   x2-x1 > 0
	 * or
	 *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) <= 0,   if   x2-x1 < 0
	 *
	 * and both versions can be merged into  essentially  the condition
	 * that (Yo-y1)*(x2-Xo)+(Yo-y2)*(Xo-x1) has the same sign as x2-x1.
	 * That is, the product of these two must be positive or zero.
	 * </pre>
	 *
	 * @param imin
	 *            an int, i-coordinate of the area left corners
	 * @param imax
	 *            an int, i-coordinate of the area right corners
	 * @param jmin
	 *            an int, j-coordinate of the area left corners
	 * @param jmax
	 *            an int, j-coordinate of the area right corners
	 * @param lon
	 *            a double, the longitude of the geographical point
	 * @param lat
	 *            a double, the latitude of the geographical point
	 * @return <code>true</code> if (lon, lat) belongs to the polygon,
	 *         <code>false</code>otherwise.
	 */
	public static boolean isInsidePolygone(int imin, int imax, int jmin, int jmax, double lon, double lat) {

		// --------------------------------------------------------------
		// Return true if (lon, lat) is insidide the polygon defined by
		// (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)

		// -----------------------------------------
		// Build the polygone
		int nb, shft;
		double[] xb, yb;
		boolean isInPolygone = true;

		nb = 2 * (jmax - jmin + imax - imin);
		xb = new double[nb + 1];
		yb = new double[nb + 1];
		shft = 0 - imin;
		for (int i = imin; i <= (imax - 1); i++) {
			xb[i + shft] = lonRho[jmin][i];
			yb[i + shft] = latRho[jmin][i];
		}

		shft = 0 - jmin + imax - imin;
		for (int j = jmin; j <= (jmax - 1); j++) {
			xb[j + shft] = lonRho[j][imax];
			yb[j + shft] = latRho[j][imax];
		}

		shft = jmax - jmin + 2 * imax - imin;
		for (int i = imax; i >= (imin + 1); i--) {
			xb[shft - i] = lonRho[jmax][i];
			yb[shft - i] = latRho[jmax][i];
		}

		shft = 2 * jmax - jmin + 2 * (imax - imin);
		for (int j = jmax; j >= (jmin + 1); j--) {
			xb[shft - j] = lonRho[j][imin];
			yb[shft - j] = latRho[j][imin];
		}

		xb[nb] = xb[0];
		yb[nb] = yb[0];

		// ---------------------------------------------
		// Check if {lon, lat} is inside polygone
		int inc, crossings;
		double dx1, dx2, dxy;
		crossings = 0;

		for (int k = 0; k < nb; k++) {

			if (xb[k] != xb[k + 1]) {
				dx1 = lon - xb[k];
				dx2 = xb[k + 1] - lon;
				dxy = dx2 * (lat - yb[k]) - dx1 * (yb[k + 1] - lat);
				inc = 0;
				if ((xb[k] == lon) & (yb[k] == lat)) {
					crossings = 1;
				} else if (((dx1 == 0.) & (lat >= yb[k])) | ((dx2 == 0.) & (lat >= yb[k + 1]))) {
					inc = 1;
				} else if ((dx1 * dx2 > 0.) & ((xb[k + 1] - xb[k]) * dxy >= 0.)) {
					inc = 2;
				}

				if (xb[k + 1] > xb[k]) {
					crossings += inc;
				} else {
					crossings -= inc;
				}
			}
		}
		if (crossings == 0) {
			isInPolygone = false;
		}

		return (isInPolygone);
	}

	/**
	 * Interpolates the temperature field at particle location and specified time.
	 *
	 * @param pGrid
	 *            a double[], the particle grid coordinates.
	 * @param time
	 *            a double, the current time [second] of the simulation
	 * @return a double, the sea water temperature [celsius] at particle location.
	 *         Returns <code>NaN</code> if the temperature field could not be found
	 *         in the NetCDF dataset.
	 * @throws an
	 *             ArrayIndexOutOfBoundsException if the particle is out of the
	 *             domain.
	 */
	public static double getTemperature(double xRho, double yRho, double zRho, double frac)
			throws ArrayIndexOutOfBoundsException {

		if (!FLAG_TP) {
			return Double.NaN;
		}

		double co, CO, x, tp;
		int n = isCloseToCost(xRho, yRho) ? 1 : 2;

		// frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

		// -----------------------------------------------------------
		// Interpolate the temperature fields
		// in the computational grid.
		int i = (int) xRho;// (int) pGrid[0];
		int j = (int) yRho; // (int) pGrid[1];
		double kz = Math.max(0.d, Math.min(zRho, (double) nz - 1.00001f));
		int k = (int) kz;
		double dx = xRho - (double) i;
		double dy = yRho - (double) j;
		double dz = kz - (double) k;
		tp = 0.d;
		CO = 0.d;
		for (int kk = 0; kk < 2; kk++) {
			for (int jj = 0; jj < n; jj++) {
				for (int ii = 0; ii < n; ii++) {
					{
						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
						CO += co;
						x = 0.d;
						try {
							x = (1.d - frac) * temp_tp0[k + kk][j + jj][i + ii]
									+ frac * temp_tp1[k + kk][j + jj][i + ii];
							tp += x * co;
						} catch (ArrayIndexOutOfBoundsException e) {
							throw new ArrayIndexOutOfBoundsException(
									"Problem interpolating temperature field : " + e.getMessage());
						}

					}
				}
			}
		}
		if (CO != 0) {
			tp /= CO;
		}

		return tp;
	}

	/**
	 * Interpolates the salinity field at particle location and specified time.
	 *
	 * @param pGrid
	 *            a double[], the particle grid coordinates.
	 * @param time
	 *            a double, the current time [second] of the simulation
	 * @return a double, the sea water salinity [psu] at particle location. Returns
	 *         <code>NaN</code> if the salinity field could not be found in the
	 *         NetCDF dataset.
	 * @throws an
	 *             ArrayIndexOutOfBoundsException if the particle is out of the
	 *             domain.
	 */
	public static double getSalinity(double[] pGrid, double frac) throws ArrayIndexOutOfBoundsException {

		if (!FLAG_SAL) {
			return Double.NaN;
		}

		double co, CO, x, sal;
		int n = isCloseToCost(pGrid) ? 1 : 2;

		// frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

		// -----------------------------------------------------------
		// Interpolate the temperature fields
		// in the computational grid.
		int i = (int) pGrid[0];
		int j = (int) pGrid[1];
		double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
		int k = (int) kz;
		double dx = pGrid[0] - (double) i;
		double dy = pGrid[1] - (double) j;
		double dz = kz - (double) k;
		sal = 0.d;
		CO = 0.d;
		for (int kk = 0; kk < 2; kk++) {
			for (int jj = 0; jj < n; jj++) {
				for (int ii = 0; ii < n; ii++) {
					{
						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
						CO += co;
						x = 0.d;
						try {
							x = (1.d - frac) * salt_tp0[k + kk][j + jj][i + ii]
									+ frac * salt_tp1[k + kk][j + jj][i + ii];
							sal += x * co;
						} catch (ArrayIndexOutOfBoundsException e) {
							throw new ArrayIndexOutOfBoundsException(
									"Problem interpolating salinity field : " + e.getMessage());
						}

					}
				}
			}
		}
		if (CO != 0) {
			sal /= CO;
		}

		return sal;
	}

	public static double getHBL(double xRho, double yRho, double frac) throws ArrayIndexOutOfBoundsException {
		if (!FLAG_HBL) {
			return Double.NaN;
		}
		double co, CO, x, hbl;
		int n = isCloseToCost(xRho, yRho) ? 1 : 2;
		// -----------------------------------------------------------
		// Interpolate the hbl field
		// in the computational grid.
		int i = (int) xRho;
		int j = (int) yRho;
		double dx = xRho - (double) i;
		double dy = yRho - (double) j;

		hbl = 0.d;
		CO = 0.d;
		for (int jj = 0; jj < n; jj++) {
			for (int ii = 0; ii < n; ii++) {
				{
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
					CO += co;
					x = 0.d;
					try {
						x = (1.d - frac) * hbl_tp0[j + jj][i + ii] + frac * hbl_tp1[j + jj][i + ii];
						hbl += x * co;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new ArrayIndexOutOfBoundsException(
								"Problem interpolating salinity field : " + e.getMessage());
					}
				}
			}
		}
		if (CO != 0) {
			hbl /= CO;
		}
		return hbl;
	}

	// GET ZETA (AJOUT DU 12 janvier 2010 pour le TEST sur les tourbillons
	public static double getZETA(double xRho, double yRho, double frac) throws ArrayIndexOutOfBoundsException {
		double co, CO, x, zeta;
		int n = isCloseToCost(xRho, yRho) ? 1 : 2;
		// -----------------------------------------------------------
		// Interpolate the zeta field
		// in the computational grid.
		int i = (int) xRho;
		int j = (int) yRho;
		double dx = xRho - (double) i;
		double dy = yRho - (double) j;

		zeta = 0.d;
		CO = 0.d;
		for (int jj = 0; jj < n; jj++) {
			for (int ii = 0; ii < n; ii++) {
				{
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
					CO += co;
					x = 0.d;
					try {
						x = (1.d - frac) * zeta_tp0[j + jj][i + ii] + frac * zeta_tp1[j + jj][i + ii];
						zeta += x * co;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new ArrayIndexOutOfBoundsException(
								"Problem interpolating salinity field : " + e.getMessage());
					}
				}
			}
		}
		if (CO != 0) {
			zeta /= CO;
		}
		return zeta;
	}
	// ------------------------------------------------- //

	public static double[] get_uvw(double[] pGrid, double frac, int dt) throws ArrayIndexOutOfBoundsException {
		// System.out.println("DANS get_uvw : w_tp1.length = " + w_tp1.length);

		double co, CO;
		int n = isCloseToCost(pGrid) ? 1 : 2;

		double x_chiant, dw, du, dv, x_euler;

		// -----------------------------------------------------------
		// Interpolate the velocity, temperature and salinity fields
		// in the computational grid.

		double ix, jy, kz;
		ix = pGrid[0];
		jy = pGrid[1];
		kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

		// System.out.println("kz = " + kz + " nz = " + nz);

		du = 0.d;
		dv = 0.d;
		dw = 0.d;
		// x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
		// Timothee : (frac calculé dans Poisson.deplace)
		x_euler = frac;

		try {
			// -----------------------
			// Get dw

			int i = (int) ix;
			int j = (int) jy;
			int k = (int) Math.round(kz);

			double dx = ix - (double) i;
			double dy = jy - (double) j;
			double dz = kz - (double) k;
			CO = 0.d;

			for (int ii = 0; ii < n; ii++) {
				for (int jj = 0; jj < n; jj++) {
					for (int kk = 0; kk < 2; kk++) {
						// if (isInWater(i + ii, j + jj)) {

						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (.5d - (double) kk - dz));
						CO += co;
						x_chiant = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii]
								+ x_euler * w_tp1[k + kk][j + jj][i + ii];
						dw += 2.d * x_chiant * co / (z_w_tp0[Math.min(k + kk + 1, nz)][j + jj][i + ii]
								- z_w_tp0[Math.max(k + kk - 1, 0)][j + jj][i + ii]);
					}

				}
			}

			if (CO != 0) {
				dw /= CO;
				// System.out.println("dw = " + dw);
			}

			// ------------------------
			// Get du
			// kz = Math.min(kz, nz - 1.00001f);
			i = (int) Math.round(ix);
			k = (int) kz;
			dx = ix - (double) i;
			dz = kz - (double) k;
			CO = 0.d;
			for (int ii = 0; ii < 2; ii++) {
				for (int jj = 0; jj < n; jj++) {
					for (int kk = 0; kk < 2; kk++) {
						// if (isInWater(i + ii, j + jj)) {
						{
							co = Math.abs(
									(.5d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
							CO += co;
							x_chiant = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1]
									+ x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
							du += .5d * x_chiant * co * (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j + jj][i + ii]);
						}

					}
				}
			}
			if (CO != 0) {
				du /= CO;
			}
			// System.out.println("du = " + du);

			// -------------------------
			// Get dv
			i = (int) ix;
			j = (int) Math.round(jy);
			dx = ix - (double) i;
			dy = jy - (double) j;
			CO = 0.d;
			for (int kk = 0; kk < 2; kk++) {
				for (int jj = 0; jj < 2; jj++) {
					for (int ii = 0; ii < n; ii++) {
						// if (isInWater(i + ii, j + jj)) {
						{
							co = Math.abs(
									(1.d - (double) ii - dx) * (.5d - (double) jj - dy) * (1.d - (double) kk - dz));
							CO += co;
							x_chiant = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i + ii]
									+ x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
							dv += .5d * x_chiant * co * (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j + jj][i + ii]);
						}

					}
				}
			}
			if (CO != 0) {
				dv /= CO;
			}
			// System.out.println("dv = " + dv);

		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Problem interpolating velocity fields : " + e.getMessage()
					+ " --> Pos : ix = " + ix + "  jy = " + jy + "  kz = " + kz);
		}

		// il faut vérifier que les vitesses restent possibles :
		// vitesse impossible si dv (nbre de mailles par seconde) est supérier à 1
		// (si dv ou du >1, la particule traverse une maille du modèle par seconde!!)

		if (du > 1) {
			System.err.println("! WARNING : CFL broken for u " + (float) du);
			System.out.println("ix,jy,kz : " + ix + " , " + jy + " , " + kz);
		}

		if (dv > 1) {
			System.err.println("! WARNING : CFL broken for v " + (float) dv);
			System.out.println("ix,jy,kz : " + ix + " , " + jy + " , " + kz);
		}

		// ---- Passage des nb maille/seconde au nb maille/ pas de temps (dt_advec) ---
		// "Simulation.dt_advec*3600" = pas de temps de l'advection en secondes
		/*
		 * du *= Simulation.dt_advec * 60;//3600; dv *= Simulation.dt_advec * 60;
		 * //3600; dw *= Simulation.dt_advec * 60; //3600;
		 */
		// 6 avril 2014
		du *= dt * 60;// 3600;
		dv *= dt * 60; // 3600;
		dw *= dt * 60; // 3600;
		return (new double[] { du, dv, dw });
	}

	/**
	 * Interpolates the prey concentration fields at particle location and specified
	 * time: large phytoplankton, small zooplankton and large zooplankton.
	 *
	 * @param pGrid
	 *            a double[], the particle grid coordinates.
	 * @param time
	 *            a double, the current time [second] of the simulation
	 * @return a double, the concentration [mMol/m3] of arge phytoplankton, small
	 *         zooplankton and large zooplankton at particle location. Returns
	 *         <code>NaN</code> if the prey concentration fields could not be found
	 *         in the NetCDF dataset.
	 * @throws an
	 *             ArrayIndexOutOfBoundsException if the particle is out of the
	 *             domain.
	 */
	public static double[] getPlankton_PISCES(double[] pGrid, double frac) {

		if (!FLAG_PLANKTON_PISCES) {
			return new double[] { Double.NaN, Double.NaN, Double.NaN };
		}

		double co, CO, x, Diatoms, NanoPhyto, MicroZoo, MesoZoo;// , O2;

		// frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

		// -----------------------------------------------------------
		// Interpolate the plankton concentration fields
		// in the computational grid.
		int i = (int) pGrid[0];
		int j = (int) pGrid[1];
		final double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
		int k = (int) kz;
		// System.out.println("i " + i + " j " + j + " k " + k);
		double dx = pGrid[0] - (double) i;
		double dy = pGrid[1] - (double) j;
		double dz = kz - (double) k;
		Diatoms = 0.d;
		NanoPhyto = 0.d;
		MicroZoo = 0.d;
		MesoZoo = 0.d;
		// O2 =
		// 0.d;

		CO = 0.d;
		for (int kk = 0; kk < 2; kk++) {
			for (int jj = 0; jj < 2; jj++) {
				for (int ii = 0; ii < 2; ii++) {
					if (isInWater(i + ii, j + jj)) {
						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
						CO += co;
						x = 0.d;
						x = (1.d - frac) * Diatoms_tp0[k + kk][j + jj][i + ii]
								+ frac * Diatoms_tp1[k + kk][j + jj][i + ii];
						Diatoms += x * co;

						x = (1.d - frac) * NanoPhyto_tp0[k + kk][j + jj][i + ii]
								+ frac * NanoPhyto_tp1[k + kk][j + jj][i + ii];
						NanoPhyto += x * co;

						x = (1.d - frac) * MicroZoo_tp0[k + kk][j + jj][i + ii]
								+ frac * MicroZoo_tp1[k + kk][j + jj][i + ii];
						MicroZoo += x * co;

						x = (1.d - frac) * MesoZoo_tp0[k + kk][j + jj][i + ii]
								+ frac * MesoZoo_tp1[k + kk][j + jj][i + ii];
						MesoZoo += x * co;

						// x =
						// (1.d - frac) * O2_tp0[k + kk][j + jj][i + ii] + frac * O2_tp1[k + kk][j +
						// jj][i + ii];
						// O2 +=
						// x * co;
					}

				}
			}
		}
		if (CO != 0) {
			Diatoms /= CO;
			NanoPhyto /= CO;
			MicroZoo /= CO;
			MesoZoo /= CO;
			// O2 /= CO;
		}

		return new double[] { Diatoms, NanoPhyto, MicroZoo, MesoZoo }; // , O2};
	}

	// 6 nov 2016: onlit des sorties de PISCES dans les quelles phyto = phyto 1+
	// phyto2 et zoo = zoo1+zoo2

	public static double[] getPlankton_PISCES_tot(double[] pGrid, double frac) {

		double co, CO, x, Phytot, Zootot;// , O2;

		// frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

		// -----------------------------------------------------------
		// Interpolate the plankton concentration fields
		// in the computational grid.
		int i = (int) pGrid[0];
		int j = (int) pGrid[1];
		final double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
		int k = (int) kz;
		// System.out.println("i " + i + " j " + j + " k " + k);
		double dx = pGrid[0] - (double) i;
		double dy = pGrid[1] - (double) j;
		double dz = kz - (double) k;
		Phytot = 0.d;
		Zootot = 0.d;

		CO = 0.d;
		for (int kk = 0; kk < 2; kk++) {
			for (int jj = 0; jj < 2; jj++) {
				for (int ii = 0; ii < 2; ii++) {
					if (isInWater(i + ii, j + jj)) {
						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
						CO += co;
						x = 0.d;
						x = (1.d - frac) * Phyto_tp0[k + kk][j + jj][i + ii] + frac * Phyto_tp1[k + kk][j + jj][i + ii];
						Phytot += x * co;

						x = (1.d - frac) * Zoo_tp0[k + kk][j + jj][i + ii] + frac * Zoo_tp1[k + kk][j + jj][i + ii];
						Zootot += x * co;
					}

				}
			}
		}
		if (CO != 0) {
			Phytot /= CO;
			Zootot /= CO;
		}

		return new double[] { Phytot, Zootot }; // , O2};
	}
	// fin ajout 6 nov 2016

	public static double[] getPlankton_NPZD(double[] pGrid, double frac) {

		if (!FLAG_PLANKTON_NPZD) {
			System.out.println(" PAS DE CHAMPS DE PLANKTON ISSUS DE ROMS_NPZD ");

			return new double[] { Double.NaN, Double.NaN, Double.NaN };
		}

		double co, CO, x, phyto, zoo, det, NO3, chla;

		// frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

		// -----------------------------------------------------------
		// Interpolate the plankton concentration fields
		// in the computational grid.
		int i = (int) pGrid[0];
		int j = (int) pGrid[1];
		final double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
		int k = (int) kz;
		// System.out.println("i " + i + " j " + j + " k " + k);
		double dx = pGrid[0] - (double) i;
		double dy = pGrid[1] - (double) j;
		double dz = kz - (double) k;
		phyto = 0.d;
		zoo = 0.d;
		det = 0.d;
		NO3 = 0.d;
		chla = 0.d;
		CO = 0.d;
		for (int kk = 0; kk < 2; kk++) {
			for (int jj = 0; jj < 2; jj++) {
				for (int ii = 0; ii < 2; ii++) {
					if (isInWater(i + ii, j + jj)) {
						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
						CO += co;
						x = 0.d;
						x = (1.d - frac) * Phyto_tp0[k + kk][j + jj][i + ii] + frac * Phyto_tp1[k + kk][j + jj][i + ii];
						phyto += x * co;

						x = (1.d - frac) * Zoo_tp0[k + kk][j + jj][i + ii] + frac * Zoo_tp1[k + kk][j + jj][i + ii];
						zoo += x * co;

						x = (1.d - frac) * Det_tp0[k + kk][j + jj][i + ii] + frac * Det_tp1[k + kk][j + jj][i + ii];
						det += x * co;

						x = (1.d - frac) * NO3_tp0[k + kk][j + jj][i + ii] + frac * NO3_tp1[k + kk][j + jj][i + ii];
						NO3 += x * co;

						x = (1.d - frac) * Chla_tp0[k + kk][j + jj][i + ii] + frac * Chla_tp1[k + kk][j + jj][i + ii];
						chla += x * co;
					}

				}
			}
		}
		if (CO != 0) {
			phyto /= CO;
			zoo /= CO;
			det /= CO;
			NO3 /= CO;
			chla /= CO;
		}

		return new double[] { phyto, zoo, det, NO3, chla };
	}

	public static double[] getChla(double[] pGrid, double frac) {

		double co, CO, x, NanoPhytoChl, DiatomsChl;

		// frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

		// -----------------------------------------------------------
		// Interpolate the plankton concentration fields
		// in the computational grid.
		int i = (int) pGrid[0];
		int j = (int) pGrid[1];
		final double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
		int k = (int) kz;
		// System.out.println("i " + i + " j " + j + " k " + k);
		double dx = pGrid[0] - (double) i;
		double dy = pGrid[1] - (double) j;
		double dz = kz - (double) k;
		NanoPhytoChl = 0.d;
		DiatomsChl = 0.d;
		CO = 0.d;
		for (int kk = 0; kk < 2; kk++) {
			for (int jj = 0; jj < 2; jj++) {
				for (int ii = 0; ii < 2; ii++) {
					if (isInWater(i + ii, j + jj)) {
						co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
						CO += co;
						x = 0.d;
						x = (1.d - frac) * DiatomsChl_tp0[k + kk][j + jj][i + ii]
								+ frac * DiatomsChl_tp1[k + kk][j + jj][i + ii];
						NanoPhytoChl += x * co;
						x = (1.d - frac) * NanoPhytoChl_tp0[k + kk][j + jj][i + ii]
								+ frac * NanoPhytoChl_tp1[k + kk][j + jj][i + ii];
						DiatomsChl += x * co;
					}
				}
			}
		}
		if (CO != 0) {
			NanoPhytoChl /= CO;
			DiatomsChl /= CO;
		}
		return new double[] { DiatomsChl, NanoPhytoChl };
	}

	/**
	 * Gets the list of NetCDF input files that satisfy the file filter and sorts
	 * them according to the chronological order induced by the
	 * {@code NCComparator}.
	 *
	 * @param path
	 *            a String, the path of the folder that contains the model input
	 *            files.
	 * @return an ArrayList, the list of the input files sorted in time.
	 * @throws an
	 *             IOException if an exception occurs while scanning the input
	 *             files.
	 */
	private ArrayList<String> getInputList(String path) throws IOException {

		ArrayList<String> list = null;
		filtre_roms_output = "*" + sufixe;
		File inputPath = new File(path);
		String fileMask = filtre_roms_output;

		if (list.size() > 1) {
			System.out.println("on a un total de " + list.size() + "fichiers " + filtre_roms_output + " disponibles");
			// Collections.sort(list, new NCComparator(strTime));
		}

		return list;
	}

	/**
	 * Opens the NetCDF dataset from the specified location. If the
	 * <code>rawPath</code> is an OPeNDAP URL, it directly opens it. If the
	 * <code>rawPath</code> is a local path, the application first lists the files
	 * of the folder by a call to {@code getInputList} method and then opens the
	 * first file of the list.
	 *
	 * @param rawPath
	 *            a String, the location of the dataset in URI format. It can be
	 *            local path or an OPeNDAP URL.
	 * @throws an
	 *             IOException if an erroc occurs when opening the dataset.
	 * @see java.net.URI for details about URI syntax
	 */
	private void openLocation(String rawPath) throws IOException {

		URI uriCurrent = new File("").toURI();
		// String path = URI.create(rawPath).getPath();
		String path = uriCurrent.resolve(URI.create(rawPath)).getPath();

		if (isDirectory(path)) {
			listInputFiles = getInputList(path);
			// EVOL2008 :
			System.out.println(
					"on ouvre le premier fichier netcdf (qui doit contenir les info de grille) pour en extraire les champs constants : "); // +
																																			// listInputFiles.get(0));
			// EVOL2009 :
			// String fileName = directory_roms + region + "_grd" + sufixe;
			// System.out.println("on ouvre le fichier netcdf 'grille' pour en extraire les
			// champs constants : " + fileName);
		}
		// open(listInputFiles.get(0));

		String fileName = get_filename(1980, 1);
		open(fileName);

		// EVOL2009 :

		// String fileName = directory_roms + region + "_grd" + sufixe;
		// open(fileName);
	}

	/**
	 * Loads the NetCDF dataset from the specified filename.
	 * 
	 * @param filename
	 *            a String that can be a local pathname or an OPeNDAP URL.
	 * @throws IOException
	 */
	private static void open(String filename) throws IOException {

		try {
			if (ncIn == null || (new File(ncIn.getLocation()).compareTo(new File(filename)) != 0)) {
				// MainFrame.getStatusBar().setMessage(Resources.MSG_OPEN +
				// filename);
				System.out.println("On ouvre le fichier : " + filename);
				ncIn = NetcdfDataset.openFile(filename, null);
				nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();

				// modif tim 11 dec 2007:
				// Array xTimeTp1 = ncIn.findVariable(strTime).read();
				// System.out.println(" rank = "+ rank);
				// double t0 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(rank));
				// time_tp1 -= time_tp1 % 60;
				// xTimeTp1 = null;
				// System.out.print("1er scrum_time = " + t0 + "\n");
				// fin modif

			}
			// System.out.print("POINT 2 Open dataset " + filename + "\n");

		} catch (IOException e) {
			throw new IOException("Problem opening dataset " + filename + " - " + e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException(
					"Problem reading " + strTimeDim + " dimension at location " + filename + " : " + e.getMessage());
		}
	}

	/**
	 * Determines whether or not the x-y particle location is on edge of the domain.
	 * 
	 * @param x
	 *            a double, the x-coordinate
	 * @param y
	 *            a double, the y-coordinate
	 * @return <code>true</code> if the particle is on edge of the domain
	 *         <code>false</code> otherwise.
	 */ // TIMOTHEE : ON THE EDGE OF LE DOMAIN QUe ON A SPECIFIER, RT NON PAS LA GRILLE
		// ROMS EN ENTIER
	static boolean isOnEdge(double x, double y) {
		return ((x > (nx - 3.0f)) || (x < 3.0f) || (y > (ny - 3.0f)) || (y < 3.0f));
	}

	/**
	 * Computes the geodesic distance between the two points (lat1, lon1) and (lat2,
	 * lon2)
	 * 
	 * @param lat1
	 *            a double, the latitude of the first point
	 * @param lon1
	 *            a double, the longitude of the first point
	 * @param lat2
	 *            double, the latitude of the second point
	 * @param lon2
	 *            double, the longitude of the second point
	 * @return a double, the curvilinear absciss s(A[lat1, lon1]B[lat2, lon2])
	 */
	static double geodesicDistance(double lat1, double lon1, double lat2, double lon2) {
		// --------------------------------------------------------------
		// Return the curvilinear absciss s(A[lat1, lon1]B[lat2, lon2])
		double d = 6367000.d * Math.sqrt(2.d
				- 2.d * Math.cos(Math.PI * lat1 / 180.d) * Math.cos(Math.PI * lat2 / 180.d)
						* Math.cos(Math.PI * (lon1 - lon2) / 180.d)
				- 2.d * Math.sin(Math.PI * lat1 / 180.d) * Math.sin(Math.PI * lat2 / 180.d));
		return (d);
	}

	/**
	 * Reads time non-dependant fields in NetCDF dataset
	 */
	void readConstantField() throws IOException {

		int[] origin = new int[] { jpo, ipo };
		int[] size = new int[] { ny, nx };
		Array arrLon, arrLat, arrMask_rho, arrMask_u, arrMask_v, arrH, arrZeta, arrPm, arrPn;
		Index index;

		StringBuffer list = new StringBuffer(strLon);
		list.append(", ");
		list.append(strLat);
		list.append(", ");
		list.append(strMask_rho);
		list.append(", ");
		// list.append(strMask_u);
		// list.append(", ");
		// list.append(strMask_v);
		// list.append(", ");
		list.append(strBathy);
		list.append(", ");
		list.append(strZeta);
		list.append(", ");
		list.append(strPm);
		list.append(", ");
		list.append(strPn);
		try {
			arrLon = ncIn.findVariable(strLon).read(origin, size);
			arrLat = ncIn.findVariable(strLat).read(origin, size);
			arrMask_rho = ncIn.findVariable(strMask_rho).read(origin, size);
			// arrMask_u =
			// ncIn.findVariable(strMask_u).read(origin, size);
			// arrMask_v =
			// ncIn.findVariable(strMask_v).read(origin, size);
			arrH = ncIn.findVariable(strBathy).read(origin, size);
			arrZeta = ncIn.findVariable(strZeta).read(new int[] { 0, jpo, ipo }, new int[] { 1, ny, nx }).reduce();
			arrPm = ncIn.findVariable(strPm).read(origin, size);
			arrPn = ncIn.findVariable(strPn).read(origin, size);

			if (arrLon.getElementType() == double.class) {
				lonRho = (double[][]) arrLon.copyToNDJavaArray();
				latRho = (double[][]) arrLat.copyToNDJavaArray();
			} else {
				lonRho = new double[ny][nx];
				latRho = new double[ny][nx];
				index = arrLon.getIndex();
				for (int j = 0; j < ny; j++) {
					for (int i = 0; i < nx; i++) {
						index.set(j, i);
						lonRho[j][i] = arrLon.getDouble(index);
						latRho[j][i] = arrLat.getDouble(index);
					}
				}
			}

			if (arrMask_rho.getElementType() != byte.class) {
				maskRho = new byte[ny][nx];
				index = arrMask_rho.getIndex();

				for (int j = 0; j < ny; j++) {
					for (int i = 0; i < nx; i++) {
						maskRho[j][i] = arrMask_rho.getByte(index.set(j, i));
					}
				}
			} else {
				maskRho = (byte[][]) arrMask_rho.copyToNDJavaArray();
			}
			/*
			 * if (arrMask_u.getElementType() != byte.class) { masku = new byte[ny][nx];
			 * index = arrMask_u.getIndex();
			 * 
			 * 
			 * 
			 * for (int j = 0; j < ny; j++) { for (int i = 0; i < nx; i++) { masku[j][i] =
			 * arrMask_u.getByte(index.set(j, i)); } } } else { masku = (byte[][])
			 * arrMask_u.copyToNDJavaArray(); }
			 * 
			 * if (arrMask_v.getElementType() != byte.class) { maskv = new byte[ny][nx];
			 * index = arrMask_v.getIndex();
			 * 
			 * 
			 * 
			 * for (int j = 0; j < ny; j++) { for (int i = 0; i < nx; i++) { maskv[j][i] =
			 * arrMask_v.getByte(index.set(j, i)); } } } else { maskv = (byte[][])
			 * arrMask_v.copyToNDJavaArray(); }
			 * 
			 */

			if (arrPm.getElementType() == double.class) {
				pm = (double[][]) arrPm.copyToNDJavaArray();
				pn = (double[][]) arrPn.copyToNDJavaArray();
			} else {
				pm = new double[ny][nx];
				pn = new double[ny][nx];
				index = arrPm.getIndex();
				for (int j = 0; j < ny; j++) {
					for (int i = 0; i < nx; i++) {
						index.set(j, i);
						pm[j][i] = arrPm.getDouble(index);
						pn[j][i] = arrPn.getDouble(index);
					}

				}

			}

			if (arrH.getElementType() == double.class) {
				hRho = (double[][]) arrH.copyToNDJavaArray();
			} else {
				hRho = new double[ny][nx];
				index = arrH.getIndex();
				for (int j = 0; j < ny; j++) {
					for (int i = 0; i < nx; i++) {
						hRho[j][i] = arrH.getDouble(index.set(j, i));
					}

				}

			}

			if (arrZeta.getElementType() == float.class) {
				zeta_tp0 = (float[][]) arrZeta.copyToNDJavaArray();
			} else {
				zeta_tp0 = new float[ny][nx];
				index = arrZeta.getIndex();
				for (int j = 0; j < ny; j++) {
					for (int i = 0; i < nx; i++) {
						zeta_tp0[j][i] = arrZeta.getFloat(index.set(j, i));
					}

				}
			}
			zeta_tp1 = zeta_tp0;
		} catch (IOException e) {
			throw new IOException("Problem (1) reading one of the fields " + list.toString() + " at location "
					+ ncIn.getLocation().toString() + " : " + e.getMessage());
		} catch (InvalidRangeException e) {
			throw new IOException("Problem (2) reading one of the fields " + list.toString() + " at location "
					+ ncIn.getLocation().toString() + " : " + e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException("Problem (3) reading one of the fields " + list.toString() + " at location "
					+ ncIn.getLocation().toString() + " : " + e.getMessage());
		}

	}

	/**
	 * Tests whether the file denoted by this location is a directory.
	 * 
	 * @param location
	 *            a String, the local path
	 * @return <code>true</code> if and only if the file denoted by this location
	 *         exists <em>and</em> is a directory; <code>false</code> otherwise
	 * @throws an
	 *             IOException if the file is not a valid directory.
	 */
	private boolean isDirectory(String location) throws IOException {

		File f = new File(location);
		if (!f.isDirectory()) {
			throw new IOException(location + " is not a valid directory.");
		}
		return f.isDirectory();
	}

	/**
	 * Computes the depth at sigma levels disregarding the free surface elevation.
	 */
	void getCstSigLevels() throws IOException {

		double thetas = 0, thetab = 0, hc = 0;
		double cff1, cff2;
		double[] sc_r = new double[nz];
		double[] Cs_r = new double[nz];
		double[] cff_r = new double[nz];
		double[] sc_w = new double[nz + 1];
		double[] Cs_w = new double[nz + 1];
		double[] cff_w = new double[nz + 1];

		// -----------------------------------------------------------
		// Read the Param in ncIn
		try {
			if (ncIn.findGlobalAttribute(strThetaS) == null) {
				System.out.println("ROMS Rutgers");
				thetas = ncIn.findVariable(strThetaS).readScalarDouble();
				thetab = ncIn.findVariable(strThetaB).readScalarDouble();
				hc = ncIn.findVariable(strHc).readScalarDouble();
			} else {
				System.out.println("ROMS UCLA");
				thetas = (ncIn.findGlobalAttribute(strThetaS).getNumericValue()).doubleValue();
				thetab = (ncIn.findGlobalAttribute(strThetaB).getNumericValue()).doubleValue();
				hc = (ncIn.findGlobalAttribute(strHc).getNumericValue()).doubleValue();
			}

		} catch (IOException e) {
			throw new IOException("Problem reading thetaS/thetaB/hc at location " + ncIn.getLocation().toString()
					+ " : " + e.getMessage());
		} catch (NullPointerException e) {
			throw new IOException("Problem reading thetaS/thetaB/hc at location " + ncIn.getLocation().toString()
					+ " : " + e.getMessage());
		}

		// -----------------------------------------------------------
		// Calculation of the Coeff
		cff1 = 1.d / sinh(thetas);
		cff2 = .5d / tanh(.5d * thetas);
		for (int k = nz; k-- > 0;) {
			sc_r[k] = ((double) (k - nz) + .5d) / (double) nz;
			Cs_r[k] = (1.d - thetab) * cff1 * sinh(thetas * sc_r[k])
					+ thetab * (cff2 * tanh((thetas * (sc_r[k] + .5d))) - .5d);
			cff_r[k] = hc * (sc_r[k] - Cs_r[k]);
		}

		for (int k = nz + 1; k-- > 0;) {
			sc_w[k] = (double) (k - nz) / (double) nz;
			Cs_w[k] = (1.d - thetab) * cff1 * sinh(thetas * sc_w[k])
					+ thetab * (cff2 * tanh((thetas * (sc_w[k] + .5d))) - .5d);
			cff_w[k] = hc * (sc_w[k] - Cs_w[k]);
		}

		sc_w[0] = -1.d;
		Cs_w[0] = -1.d;

		// ------------------------------------------------------------
		// Calculation of z_w , z_r
		double[][][] z_r_tmp = new double[nz][ny][nx];
		double[][][] z_w_tmp = new double[nz + 1][ny][nx];

		for (int i = nx; i-- > 0;) {
			for (int j = ny; j-- > 0;) {
				z_w_tmp[0][j][i] = -hRho[j][i];
				for (int k = nz; k-- > 0;) {
					z_r_tmp[k][j][i] = cff_r[k] + Cs_r[k] * hRho[j][i];
					z_w_tmp[k + 1][j][i] = cff_w[k + 1] + Cs_w[k + 1] * hRho[j][i];
				}
				z_w_tmp[nz][j][i] = 0.d;
			}
		}
		// z_rho_cst = new double[nz][ny][nx];
		// z_w_cst = new double[nz + 1][ny][nx];

		z_rho_cst = z_r_tmp;
		z_w_cst = z_w_tmp;

		z_w_tp0 = new double[nz + 1][ny][nx];
		z_w_tp1 = new double[nz + 1][ny][nx];

		// System.out.println("cst sig ok");

	}

	////////////////////////////////
	// Definition of proper methods
	////////////////////////////////
	/**
	 * Computes the Hyperbolic Sinus of x
	 */
	private static double sinh(double x) {
		return ((Math.exp(x) - Math.exp(-x)) / 2.d);
	}

	/**
	 * Computes the Hyperbolic Cosinus of x
	 */
	private static double cosh(double x) {
		return ((Math.exp(x) + Math.exp(-x)) / 2.d);
	}

	/**
	 * Computes the Hyperbolic Tangent of x
	 */
	private static double tanh(double x) {
		return (sinh(x) / cosh(x));
	}

	//////////
	// Getters
	//////////
	/**
	 * Gets the value of the NetCDF time variable just superior (or inferior for
	 * backward simulation) to the current time of the simulation.
	 * 
	 * @return a double, the time [second] of the NetCDF time variable strictly
	 *         superior (or inferior for backward simulation) to the current time of
	 *         the simulation.
	 */
	public static double getTimeTp1() {
		return time_tp1;
	}

	/**
	 * Gets the grid dimension in the XI-direction
	 * 
	 * @return an int, the grid dimension in the XI-direction (Zonal)
	 */
	public static int get_nx() {
		return nx;
	}

	/**
	 * Gets the grid dimension in the ETA-direction
	 * 
	 * @return an int, the grid dimension in the ETA-direction (Meridional)
	 */
	public static int get_ny() {
		return ny;
	}

	/**
	 * Gets the grid dimension in the vertical direction
	 * 
	 * @return an int, the grid dimension in the vertical direction
	 */
	public static int get_nz() {
		return nz;
	}

	/**
	 * Gets domain minimum latitude.
	 * 
	 * @return a double, the domain minimum latitude [north degree]
	 */
	public static double getLatMin() {
		return latMin;
	}

	/**
	 * Gets domain maximum latitude.
	 * 
	 * @return a double, the domain maximum latitude [north degree]
	 */
	public static double getLatMax() {
		return latMax;
	}

	/**
	 * Gets domain minimum longitude.
	 * 
	 * @return a double, the domain minimum longitude [east degree]
	 */
	public static double getLonMin() {
		return lonMin;
	}

	/**
	 * Gets domain maximum longitude.
	 * 
	 * @return a double, the domain maximum longitude [east degree]
	 */
	public static double getLonMax() {
		return lonMax;
	}

	/**
	 * Gets domain maximum depth.
	 * 
	 * @return a float, the domain maximum depth [meter]
	 */
	public static float getDepthMax() {
		return (float) depthMax;
	}

	/**
	 * Gets the latitude at (i, j) grid point.
	 * 
	 * @param i
	 *            an int, the i-ccordinate
	 * @param j
	 *            an int, the j-coordinate
	 * @return a double, the latitude [north degree] at (i, j) grid point.
	 */
	public static double getLat(int i, int j) {
		return latRho[j][i];
	}

	/**
	 * Gets the longitude at (i, j) grid point.
	 * 
	 * @param i
	 *            an int, the i-ccordinate
	 * @param j
	 *            an int, the j-coordinate
	 * @return a double, the longitude [east degree] at (i, j) grid point.
	 */
	public static double getLon(int i, int j) {
		return lonRho[j][i];
	}

	/**
	 * Gets the bathymetry at (i, j) grid point.
	 * 
	 * @param i
	 *            an int, the i-ccordinate
	 * @param j
	 *            an int, the j-coordinate
	 * @return a double, the bathymetry [meter] at (i, j) grid point if is in water,
	 *         return NaN otherwise.
	 */
	public static double getBathy(int i, int j) {

		if (isInWater(i, j)) {
			return hRho[j][i];
		}

		return Double.NaN;
	}

	// Nouvelle version de load_data : 13 Avril 2015, en utilisant un fichier ncml
	public static void load_data(int jour) throws IOException {
		int rank = jour; // DANS LE CAS OU L'ON A 1 OUTPOUT DE ROMS PAR JOUR
		// 1) COPIER LES DONNÉE PRécédantes "Tp1" dans "Tp0"
		setAllFieldsAtTime();
		// 2) LIRE ET COPIER LES DONNEES du rang suivant dans "Tp1"
		setAllFieldsTp1AtTime(rank);
	}

	// ANCIEN (Timothee... )

	public static void load_data(int jour, int year) throws IOException {
		String fileName;
		// CA PREND DU TEMPS DE CALCUL...

		// ICI ON CONSIDERE QUE :
		// - LE NOMBRE D'ENREGISTREMENT ROMS PAR MOIS EST CONSTANT POUR LES MOIS DE 1 A
		// 11 (nbTimeRecords);
		// - LE DERNIER MOIS PEUT EVENTUELEMENT AVOIR UN NOMBRE DIFFERENT
		// D'ENREGISTREMENT (nbTimeRecords)
		int month = 1 + ((int) jour / 30) % 12; // ON CONSIDERE QUE LES MOIS ONT 30 JOURS...
		int jourdumois = (int) jour % 30;
		if (jour == 360) {
			jourdumois = 30;
			month = 12;
		} // pour le cas ou on a 365 jours...

		int rank = jourdumois / dt_HyMo;
		// System.out.println("rank1 = " +rank);

		if (rank < nbTimeRecords) {
			// System.out.println("Load data : jour = " +jour+ " , jourdumois = "
			// +jourdumois+ " , month = " + month + " , rank " + rank + "...");
			fileName = get_filename(year, month);
			open(fileName);

			// 1) COPIER LES DONNÉE PRécédantes "Tp1" dans "Tp0"
			setAllFieldsAtTime();
		}

		// 2) LIRE ET COPIER LES DONNEES du rang suivant dans "Tp1"
		// (l'interpolation temporelle se fait entre Tp0 et Tp1, donc le jour doit être
		// entre les deux rangs)
		rank += 1;
		// System.out.println("rank2 = " +rank);

		if (rank == nbTimeRecords) { // {//nbTimeRecords - 1) {
			// System.out.println("rank > nbTimeRecords - 1 : , rank = " + rank);
			month = month + 1;
			if (month > 12) {
				month = 1;
				year = year + 1;
				// System.out.println(" -----> 222 XXXXXXXXXXXXXXXXXXXXXXXX rank = " + rank);
				// Modif 26 mai 2009 : dernier mois de l'année --> 1er moi de l'année SUIVANTE
				// System.out.println("1 year = Simulation.year + 1; year = " + year);
			}
			fileName = get_filename(year, month);
			rank = 0;
			open(fileName);
		}
		setAllFieldsTp1AtTime(rank);
	}

	public static double[] getFields_SaltTemp(double xRho, double yRho, double zRho, double frac) throws IOException {

		double[] pGrid = { xRho, yRho, zRho };
		double salt, temp, hbl;

		salt = getSalinity(pGrid, frac);
		temp = getTemperature(xRho, yRho, zRho, frac);
		/*
		 * if (temp < 10) { System.out.println("temp = " + temp); double[] CORRD =
		 * grid2Geo(xRho, yRho, zRho);
		 * System.out.println("double xRho, double yRho, double zRho, double frac =  " +
		 * xRho + " , " + yRho + " , " + zRho + " , " + frac);
		 * System.out.println("grid2Geo = " + CORRD[0] + " , " + CORRD[1] + " , " +
		 * CORRD[2]); System.out.println("getBathy --> BATHY = " + getBathy((int) xRho,
		 * (int) yRho)); }
		 */

		return (new double[] { temp, salt });
	}

	public static double[] getFields_uvw(double xRho, double yRho, double zRho, double frac, int dt)
			throws IOException {

		double[] pGrid = { xRho, yRho, zRho };
		// pour la mettre dans la partie advection:
		double[] uvw;
		if (!isOnEdge(xRho, yRho)) {
			uvw = get_uvw(pGrid, frac, dt);
			return (uvw);
		} else {
			System.out.println("CATASTROPHE ON THE EDGE");
			return (new double[] { 0, 0, 0 });
		}
	}

	public static double[] getFields_uv(double xRho, double yRho, double zRho, double frac, int dt) throws IOException {

		double[] pGrid = { xRho, yRho, zRho };
		// pour la mettre dans la partie advection:
		double[] uv;
		if (!isOnEdge(xRho, yRho)) {
			uv = get_uv(pGrid, frac, dt);
			return (uv);
		} else {
			System.out.println("CATASTROPHE ON THE EDGE");
			return (new double[] { 0, 0 });
		}
	}

	public static double[] get_uv(double[] pGrid, double frac, int dt) throws ArrayIndexOutOfBoundsException {
		// System.out.println("DANS get_uvw : w_tp1.length = " + w_tp1.length);

		double co, CO;
		int n = isCloseToCost(pGrid) ? 1 : 2;

		double x_chiant, du, dv, x_euler;

		// -----------------------------------------------------------
		// Interpolate the velocity, temperature and salinity fields
		// in the computational grid.

		double ix, jy, kz;
		ix = pGrid[0];
		jy = pGrid[1];
		kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

		// System.out.println("kz = " + kz + " nz = " + nz);

		du = 0.d;
		dv = 0.d;
		// x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
		// Timothee : (frac calculé dans Poisson.deplace)
		x_euler = frac;

		try {
			// ------------------------
			// Get du
			// kz = Math.min(kz, nz - 1.00001f);
			int i = (int) ix;
			int j = (int) jy;
			int k = (int) Math.round(kz);

			double dx = ix - (double) i;
			double dy = jy - (double) j;
			double dz = kz - (double) k;

			i = (int) Math.round(ix);
			k = (int) kz;
			dx = ix - (double) i;
			dz = kz - (double) k;
			CO = 0.d;

			CO = 0.d;
			for (int ii = 0; ii < 2; ii++) {
				for (int jj = 0; jj < n; jj++) {
					for (int kk = 0; kk < 2; kk++) {
						// if (isInWater(i + ii, j + jj)) {
						{
							co = Math.abs(
									(.5d - (double) ii - dx) * (1.d - (double) jj - dy) * (1.d - (double) kk - dz));
							CO += co;
							x_chiant = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1]
									+ x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
							du += .5d * x_chiant * co * (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j + jj][i + ii]);
						}

					}
				}
			}
			if (CO != 0) {
				du /= CO;
			}
			// System.out.println("du = " + du);

			// -------------------------
			// Get dv
			i = (int) ix;
			j = (int) Math.round(jy);
			dx = ix - (double) i;
			dy = jy - (double) j;
			CO = 0.d;
			for (int kk = 0; kk < 2; kk++) {
				for (int jj = 0; jj < 2; jj++) {
					for (int ii = 0; ii < n; ii++) {
						// if (isInWater(i + ii, j + jj)) {
						{
							co = Math.abs(
									(1.d - (double) ii - dx) * (.5d - (double) jj - dy) * (1.d - (double) kk - dz));
							CO += co;
							x_chiant = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i + ii]
									+ x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
							dv += .5d * x_chiant * co * (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j + jj][i + ii]);
						}

					}
				}
			}
			if (CO != 0) {
				dv /= CO;
			}
			// System.out.println("dv = " + dv);

		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Problem interpolating velocity fields : " + e.getMessage()
					+ " --> Pos : ix = " + ix + "  jy = " + jy + "  kz = " + kz);
		}

		// il faut vérifier que les vitesses restent possibles :
		// vitesse impossible si dv (nbre de mailles par seconde) est supérier à 1
		// (si dv ou du >1, la particule traverse une maille du modèle par seconde!!)

		if (du > 1) {
			System.err.println("! WARNING : CFL broken for u " + (float) du);
			System.out.println("ix,jy,kz : " + ix + " , " + jy + " , " + kz);
		}

		if (dv > 1) {
			System.err.println("! WARNING : CFL broken for v " + (float) dv);
			System.out.println("ix,jy,kz : " + ix + " , " + jy + " , " + kz);
		}

		// ---- Passage des nb maille/seconde au nb maille/ pas de temps (dt_advec) ---

		du *= dt * 60;// 3600;
		dv *= dt * 60; // 3600;

		return (new double[] { du, dv });
	}

	public static double[] get_PLANKTON_INTEG(double xRho, double yRho, double frac) throws

	ArrayIndexOutOfBoundsException {

		double co, CO, x, Diatoms, NanoPhyto, MicroZoo, MesoZoo;
		int n = isCloseToCost(xRho, yRho) ? 1 : 2;
		// -----------------------------------------------------------
		// Interpolate the hbl field
		// in the computational grid.
		int i = (int) xRho;
		int j = (int) yRho;
		double dx = xRho - (double) i;
		double dy = yRho - (double) j;

		// DIATOMS_INTEG :
		Diatoms = 0.d;
		CO = 0.d;
		for (int jj = 0; jj < n; jj++) {
			for (int ii = 0; ii < n; ii++) {
				{
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
					CO += co;
					x = 0.d;
					try {
						x = (1.d - frac) * Diatoms_integ_tp0[j + jj][i + ii] + frac * Diatoms_integ_tp1[j + jj][i + ii];
						Diatoms += x * co;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new ArrayIndexOutOfBoundsException(
								"Problem interpolating salinity field : " + e.getMessage());
					}
				}
			}
		}
		if (CO != 0) {
			Diatoms /= CO;
		}

		// NanoPhyto_INTEG :
		NanoPhyto = 0.d;
		CO = 0.d;
		for (int jj = 0; jj < n; jj++) {
			for (int ii = 0; ii < n; ii++) {
				{
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
					CO += co;
					x = 0.d;
					try {
						x = (1.d - frac) * NanoPhyto_integ_tp0[j + jj][i + ii]
								+ frac * NanoPhyto_integ_tp1[j + jj][i + ii];
						NanoPhyto += x * co;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new ArrayIndexOutOfBoundsException(
								"Problem interpolating salinity field : " + e.getMessage());
					}
				}
			}
		}
		if (CO != 0) {
			NanoPhyto /= CO;
		}

		// MicroZoo_INTEG :
		MicroZoo = 0.d;
		CO = 0.d;
		for (int jj = 0; jj < n; jj++) {
			for (int ii = 0; ii < n; ii++) {
				{
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
					CO += co;
					x = 0.d;
					try {
						x = (1.d - frac) * MicroZoo_integ_tp0[j + jj][i + ii]
								+ frac * MicroZoo_integ_tp1[j + jj][i + ii];
						MicroZoo += x * co;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new ArrayIndexOutOfBoundsException(
								"Problem interpolating salinity field : " + e.getMessage());
					}
				}
			}
		}
		if (CO != 0) {
			MicroZoo /= CO;
		}

		// MesoZoo_INTEG :
		MesoZoo = 0.d;
		CO = 0.d;
		for (int jj = 0; jj < n; jj++) {
			for (int ii = 0; ii < n; ii++) {
				{
					co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
					CO += co;
					x = 0.d;
					try {
						x = (1.d - frac) * MesoZoo_integ_tp0[j + jj][i + ii] + frac * MesoZoo_integ_tp1[j + jj][i + ii];
						MesoZoo += x * co;
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new ArrayIndexOutOfBoundsException(
								"Problem interpolating salinity field : " + e.getMessage());
					}
				}
			}
		}
		if (CO != 0) {
			MesoZoo /= CO;
		}

		return new double[] { Diatoms, NanoPhyto, MicroZoo, MesoZoo };
	}

	// ---------- End of class
}