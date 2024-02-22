package gama.experimental.netcdf.file;

//import java.awt.*;
import java.io.*;
import static java.lang.Double.*;

//import evolution.DebLayer;
//import evolution.Kinesis;
import java.util.Random;

public class Poissons {
	// ----------------------------------------------------------------------------

	public static int compteur_advec;
	// ****************************************
	public double x, y, z, temp, salt, phyto, lat, lon, lyaponov, grad_temp;
	public double T_opt, sigma_t;

	public float Body_length, depth, bathyActuelle;
	public long id;
	public int age;
	int compteur_phyto, ss, Nb_strates;
	public boolean living, isRecruited, isRetained_on_shelf, isRecruitedGREG, isRecruitedGREGdens, isdead_predation,
			isdead_temp, first_spawn;
	// Variables de mémoire : (enrgistrée une fois au début pour chaque poisson)
	// 1 - environnement natal (ou initial)
	public double lat_init, lon_init, depth_init, profThermo_natal, DistVoisin_init, DistVoisin_finale, x_init, y_init,
			z_init, temperature_natal, salinite_natal, bathy_init, phyto_init, egg_density;
	// Variables de test recrutement : (enrgistrée une fois à la fin pour chaque
	// poisson)
	double chla_fin, Boufe_avant, SST;
	// 3 - tolerances environnementales, spatiales et temporelles (hereditaire)
	public double tolerance_temperature, tolerance_salinite, tolerance_HBL, sigma_t_swim;
	public int rayon_exploration_temporel, rayon_exploration_spatial;

	public int day_init, year_init, zone;
	int nbre_voisins_ini, nbre_voisins, tolerance_batymetrie;
	double[] uv, TS, plankton;
	int[] Tolerances_spatiotemps;
	double[] Tolerances_TS;
	boolean[][][] strates;
	// Variables de super individus :
	double poids;
	// Pour enregistrer les temperatures max et min rencontree par chaque
	// individu (6 octobre 2011):
	float tempmax, tempmin;
	public double bouffe;
	double bouffe_max_0, bouffe_max_1, bouffe_max_2, bouffe_max_3;
	// bouffe_max_0 : memoir de la plus forte concentration de plankton[0] vue
	// ArrayList positions_OK;
	// private final List<Position_ponte>

	public double Q, Qold; // Habitat quality
	double CarCapa; // Habitat Carrying Capacity
	float biomasse_locale_autres_SI, Biomasse_SI;

	// Densite dans la zone
	public double S; // Nombre d'individu que représente ce super-individu
	double weight, weight_old; // Poids en grammes
	public double[] V_kinesis, V_kinesis_old; // composante x,y,z de la nage donne par la kinesis en m/s
	public double dx_kinesis_swim_km, dy_kinesis_swim_km, dz_swim_m;// composante x,y,z de la nage donne par la kinesis
																	// en km
	public double dx_advec_km, dy_advec_km;

	public double f_reponse_fonctionnel;
	static float dist_max_par_dt, dist_parcourue_km;
	int compteur_standstill; // pour enregistrer le nombre de fois d'affiler que l'indiv est collé à la côte
	// --> si > 100 fois on considere l'indiv comme perdu (échoué)
	public long Nb_eggs_spawned_dt, Nb_SI_spawned_dt;
	public double M_DDkinesis;
	public String Cause_de_la_mort;

	// ----------------------------------------------------------------------------
	public Poissons(int jour) {

		// S = Simulation.nbeggs_max_per_SI; // Nbre dd'individus que représente le
		// super indiv.
		ss = 0;
		boolean outZones = true;
		while (outZones) {

			int bat_max = Dataset_EVOL.bathy_max;

			poids = -999;

			// if ((x < Population.x_min) || (y < Population.y_min)) {
			// System.out.println(" x = " + x + " y = " + y + " depth = " + depth);
			// }

			int ix = (int) x;
			int iy = (int) y;
			double xi = x;
			double yi = y;
			depth = (float) -(Dataset_EVOL.prof_ponte_min
					+ Math.random() * (Dataset_EVOL.prof_ponte_max - Dataset_EVOL.prof_ponte_min));

			if (ss > 10000) {
				System.out.println(" depth = " + depth + " ;x= " + x + " ;y= " + y);
				System.out.println("(!Dataset_EVOL.isInWater(x, y))  = " + (!Dataset_EVOL.isInWater(x, y)));
				System.out.println("(x > (Dataset_EVOL.nx - 3.0f)) = " + (x > (Dataset_EVOL.nx - 3.0f)));
				System.out.println("(Dataset_EVOL.isCloseToCost(x, y)) = " + (Dataset_EVOL.isCloseToCost(x, y)));
				System.out.println("(depth < getDepthBottom(x, y)) = " + (depth < getDepthBottom(x, y)));
				System.out.println("depth = " + depth);
				System.out.println("getDepthBottom(x, y) = " + getDepthBottom(x, y));
				System.out.println("(Dataset_EVOL.getBathy(ix, iy) > Dataset_EVOL.bathy_max) = "
						+ (Dataset_EVOL.getBathy(ix, iy) > Dataset_EVOL.bathy_max));
				System.out.println("(Dataset_EVOL.getBathy(ix, iy) = " + Dataset_EVOL.getBathy(ix, iy));
				System.out.println("(Dataset_EVOL.bathy_max = " + Dataset_EVOL.bathy_max);

				System.out.println(" x = " + x + " y = " + y + " depth = " + depth);
				System.exit(0);
			}
		}

		// if (Simulation.TEST_KINESIS) {
		// // % cap blanc : 21.5N, 17.5W --> i=78, j= 140
		// x = 90;
		// y = 180;
		// }

		init_tolerances();
		init(jour);

	}

	// ----------------------------------------------------------------------------
	public Poissons(double fx, double fy, double fdepth, int jour, double radius, int[] Heritage_tolerances_spatiotemps,
			double[] Heritage_tolerances_TS) {

		ss = 0;
		double p = 2;/// Simulation.vertical_patchiness_rad; // rayon sur la profondeur = +-2m
		boolean outZones = true;
		boolean Densite_trop_importante = false;
		while (outZones) {
			double r, t;
			x = -1.f;
			y = -1.f;

			// ajout 30 septembre

			// int ix = (int) x;
			// int iy = (int) y;
			depth = (float) (fdepth + 2 * p * (Math.random() - 0.5f)); // shift sur la profondeur de ponde
			// Securite por pas qu'elles pondent en l'air :
			bathyActuelle = (float) getDepthBottom(x, y);

			if (depth > -0.5) {
				depth = -1;
			}
			if (depth < bathyActuelle) {
				depth = (bathyActuelle + Math.abs(bathyActuelle / 2));
			}
			int ix = (int) x;
			int iy = (int) y;

			outZones = ((!Dataset_EVOL.isInWater(ix, iy)) || (x > (Dataset_EVOL.nx - 2.0f)) || (x < 2.0f)
					|| (y > (Dataset_EVOL.ny - 3.0f)) || (y < 2.0f)) || // ajout 30 septembre
					(Dataset_EVOL.isCloseToCost(x, y)) || (depth < getDepthBottom(x, y));
			ss++;
			if (ss > 7000) {
				System.out.println(" impossible (2) ");
				System.out.println(" depth = " + depth + " ;x= " + x + " ;y= " + y);
				System.out.println("(!Dataset_EVOL.isInWater(x, y))  = " + (!Dataset_EVOL.isInWater(x, y)));
				System.out.println("(x > (Dataset_EVOL.nx - 3.0f)) = " + (x > (Dataset_EVOL.nx - 3.0f)));
				System.out.println("(Dataset_EVOL.isCloseToCost(x, y)) = " + (Dataset_EVOL.isCloseToCost(x, y)));
				System.out.println("(depth < getDepthBottom(x, y)) = " + (depth < getDepthBottom(x, y)));
				System.out.println("depth = " + depth);
				System.out.println("getDepthBottom(x, y) = " + getDepthBottom(x, y));
				System.out.println("Densite_trop_importante : " + Densite_trop_importante);
				x = fx;
				y = fy;
				z = 0;// Dataset_EVOL.depth2z(x, y, fdepth - Simulation.decalage_zeta);
				System.exit(0);
				outZones = false;
			}
		}
		z = 0;// Dataset_EVOL.depth2z(x, y, depth - Simulation.decalage_zeta);

		// ON MEMORISE L'ENVIRONNEMENT DE NAISSANCE :
		day_init = jour;

		// HERITAGE DES TOLERANCES :
		// Memoire des tolerances des parents, avec une mutation aleatoire :
		double alea;
		int i = 2;
		double R_alea;

		// Rayon d'exploration spatial :----------------------------------------

		// ----------------------------------------------------------------------
		// Rayon d'exploration temporel :

		// ----------------------------------------------------------------------
		// Tolérance aux variations de Bathymetrie :

		// ----------------------------------------------------------------------

		// ----------------------------------------------------------------------

		// ----------------------------------------------------------------------
		// Tolérance aux variation de HBL : mutation de + ou - 5m

		// ----------------------------------------------------------------------

		// On remplit les tableaux "Tolerances_spatiotemps" et "Tolerances_TS"
		// avec les nouvelles valeurs "mutées"
		Tolerances_spatiotemps = new int[] { rayon_exploration_spatial, rayon_exploration_temporel,
				tolerance_batymetrie };
		Tolerances_TS = new double[] { tolerance_temperature, tolerance_salinite, tolerance_HBL, sigma_t_swim };
		// id = Population.compteur_id_poissons;

	}

	// ----------------------------------------------------------------------------
	public Poissons(double fx, double fy, double fdepth, int jour, double radius, double Nbeggs) {

		S = Nbeggs;

		ss = 0; // compteur pour la boucle while ci-dessous
		double p = 2;// Simulation.vertical_patchiness_rad; // rayon sur la profondeur = +-2m
		double r, t;
		boolean outZones = true;
		boolean Densite_trop_importante = false;
		while (outZones) {
			// x = -1.f;
			// y = -1.f;
			t = Math.random() * 2.0f * Math.PI; // direction du shift
			r = Math.random() * radius; // radius = rayon de patchiness (en nbre de mailles)
			x = fx + r * Math.cos(t); // distance horiz x
			y = fy + r * Math.sin(t); // distance horiz y

			/*
			 * if ((x < Population.x_min) || // ajout 30 septembre; 5 Nov: modif
			 * "Population.sponge_grid" (x > Population.x_max) || (y < Population.y_min) ||
			 * // ajout 30 septembre (y > Population.y_max)) {
			 * 
			 * System.out.println(" PONTE HORS DOMAINE"); System.out.println("x_min = " +
			 * Population.x_min + " ; x_max = " + Population.x_max + "  -> x = " + x);
			 * System.out.println("y_min = " + Population.y_min + " ; y_max = " +
			 * Population.y_max + "  -> y = " + y); }
			 *
			 */
			// ajout 30 septembre
			// int ix = (int) x;
			// int iy = (int) y;
			depth = (float) (fdepth + 2 * p * (Math.random() - 0.5f)); // shift sur la profondeur de ponde
			// Securite por pas qu'elles pondent en l'air :
			bathyActuelle = (float) getDepthBottom(x, y);
			if (depth > -0.5) {
				depth = -1;
			}
			if (depth < bathyActuelle) {
				depth = (float) (bathyActuelle + Math.abs(bathyActuelle / 2));
			}
			int ix = (int) x;
			int iy = (int) y;

			outZones = ((!Dataset_EVOL.isInWater(ix, iy)) || (x > (Dataset_EVOL.nx - 2.0f)) || (x < 2.0f)
					|| (y > (Dataset_EVOL.ny - 3.0f)) || (y < 2.0f)) || // ajout 30 septembre
					(Dataset_EVOL.isCloseToCost(x, y)) || (depth < getDepthBottom(x, y));
			ss++;
			if (ss > 7000) {
				System.out.println(" impossible (3) ");
				System.out.println(" depth = " + depth + " ;x= " + x + " ;y= " + y);
				System.out.println("(!Dataset_EVOL.isInWater(x, y))  = " + (!Dataset_EVOL.isInWater(x, y)));
				System.out.println("(x > (Dataset_EVOL.nx - 3.0f)) = " + (x > (Dataset_EVOL.nx - 3.0f)));
				System.out.println("(Dataset_EVOL.isCloseToCost(x, y)) = " + (Dataset_EVOL.isCloseToCost(x, y)));
				System.out.println("(depth < getDepthBottom(x, y)) = " + (depth < getDepthBottom(x, y)));
				System.out.println("depth = " + depth);
				System.out.println("getDepthBottom(x, y) = " + getDepthBottom(x, y));

				System.out.println("Densite_trop_importante : " + Densite_trop_importante);
				System.out.println("Point de départ fx = " + fx + " , fy = " + fy + " fdepth = " + fdepth);
				System.exit(0);

				x = fx;
				y = fy;
				z = 0;// Dataset_EVOL.depth2z(x, y, fdepth - Simulation.decalage_zeta);
				outZones = false;
			}
		}
		z = 0;// Dataset_EVOL.depth2z(x, y, depth - Simulation.decalage_zeta);

		// ON MEMORISE L'ENVIRONNEMENT DE NAISSANCE :
		init(jour);
		// = Population.compteur_id_poissons;

		// System.out.println("ID poisson pondu = " + id);
	}
	// --------------------------------------------------------------------------

	void init_tolerances() {
		rayon_exploration_spatial = 1;/// Math.round(Math.round(Simulation.rayon_exploration_spatial_max *
										/// Math.random()));
		rayon_exploration_temporel = 1;/// Math.round(Math.round(Simulation.rayon_exploration_temporel_max *
										/// Math.random())) + 1;
		tolerance_batymetrie = 1;// Math.round(Math.round(Simulation.Range_tolerance_batymetrie *
									// Math.random()));

		tolerance_temperature = 1;// Simulation.Range_tolerance_temperature * Math.random();
		sigma_t_swim = tolerance_temperature;

		tolerance_salinite = 1;// Simulation.Range_tolerance_salinite * Math.random();
		tolerance_HBL = 1;// Simulation.Range_tolerance_HBL * Math.random();

		// stockage dans "Tolerance" (pour n'avoir qu'une variable à passer aux
		// heritiers)
		/*
		 * Tolerances[0] = rayon_exploration_spatial; Tolerances[1] =
		 * rayon_exploration_temporel; Tolerances[2] = tolerance_temperature;
		 * Tolerances[3] = tolerance_salinite; Tolerances[4] = tolerance_batymetrie;
		 * Tolerances[5] = tolerance_HBL;
		 */
		Tolerances_spatiotemps = new int[] { rayon_exploration_spatial, rayon_exploration_temporel,
				tolerance_batymetrie };
		Tolerances_TS = new double[] { tolerance_temperature, tolerance_salinite, tolerance_HBL };
	}

	// -----------------------------------------------------------------------------
	void init(int jour) {

		first_spawn = true;
		// ---------------------------

		day_init = jour;
		year_init = 2010;// Simulation.year;
		living = true;
		isRecruited = false;
		isdead_temp = false;
		age = 0;
		Body_length = 0.1f; // cm
		weight = 0.01; // g

		compteur_standstill = 0;

		// (float) util.GrowthModel.LENGTH_INIT; // taille de l'oeuf
		// localisation initiale :
		lat_init = lat;
		lon_init = lon;
		depth_init = depth;
		dist_parcourue_km = 0;

		x_init = x;
		y_init = y;
		z_init = z;

		lyaponov = -999;

		// Pour enregistrement des temp max et min rencontrée (pour BH, 6 octobre 2011)
		tempmax = -999;
		tempmin = 999;

		DistVoisin_init = 999999999; // ( initialement mise a l'infinie, puis est calculee si on active le module
										// greg)
		DistVoisin_finale = 999999999;
		compteur_phyto = 0;

		// Super indiv -------------
		S = 1;// Simulation.nbeggs_max_per_SI; // Nbre dd'individus que représente le super
				// indiv.
		// Calibrer ce nombre tq pour une mortalite naturelle on arrive a des bancs
		// adulte de ~ 1 tonne
		//
		Q = 0; // init habitat quality
		V_kinesis_old = new double[] { 0, 0, 0 }; // composante x,y,z de la nage donne par la kinesis
		V_kinesis = new double[] { 0, 0, 0 }; // composante x,y,z de la nage donne par la kinesis

		Cause_de_la_mort = "0";
	}

	/*
	 * //
	 * -----------------------------------------------------------------------------
	 * void deplace_ichthyoplankton() { //System.out.println("NEW DEPLACE ... jj = "
	 * + jj + "****************************"); int it_debut, it_fin; it_debut =
	 * (int) Population.nb_jours_ecoule_entre_roms_records * 24 / (int)
	 * Simulation.dt_advec; it_fin = (int)
	 * (Population.nb_jours_ecoule_entre_roms_records + 1) * 24 / (int)
	 * Simulation.dt_advec; // *****************************************************
	 * for (int it = it_debut; it < it_fin; it++) {
	 * 
	 * double frac = it * Simulation.dt; try { // LECTURE/INTERPOLATION DES CHAMPS
	 * DE VITESSE U, V et W : uvw = Dataset_EVOL.getFields_uvw(x, y, z, frac); //
	 * LECTURE/INTERPOLATION DES CHAMPS TEMPERATURE ET SALINITE : TS =
	 * Dataset_EVOL.getFields_SaltTemp(x, y, z, frac); // LECTURE/INTERPOLATION DES
	 * CHAMPS PLANKTON ET OXYGENE : if (Simulation.flagGrowth) { //||
	 * Simulation.flagSwim_O2) { // rétablir si lecture O2 Pisces necessaire pour
	 * swim (retablir dans Poissons et dans Dataset_EVOL) double[] pGrid = {x, y,
	 * z}; //plankton = Dataset_EVOL.getPlankton_PISCES(pGrid, frac); plankton =
	 * Dataset_EVOL.getPlankton_PISCES(pGrid, frac); bouffe = plankton[1];
	 * 
	 * CarCapa = CarCapa + CarCapa_locale_corr_f(bouffe, Simulation.dt_advec);
	 * 
	 * }
	 * 
	 * // LECTURE/INTERPOLATION DES CHAMPS DE DEPLACEMENT DU A LA TURBULENCE
	 * SOUS-MAILLE if (Simulation.flagTurbulence) { // INTRODUIRE ICI LE MODULE
	 * TURBULENCE DE ICHTHYOP }
	 * 
	 * temp = TS[0]; salt = TS[1];
	 * 
	 * // MODIF DU 28 juin 2013 : on ne fait pas le deplacement si celui-ci nous
	 * amene a terre : if (Dataset_EVOL.isInWater(x + uvw[0], y + uvw[1])) { x = x +
	 * uvw[0]; y = y + uvw[1]; z = z + uvw[2]; }
	 * 
	 * if ((Simulation.flagBuoy) || (Simulation.flagSwim_O2) ||
	 * Simulation.flagSwim_random) { depth = (float) Dataset_EVOL.z2depth(x, y, z);
	 * depth = (float) (depth + Simulation.decalage_zeta); // Pour corriger le
	 * souscis avec le run preindus }
	 * 
	 * bathyActuelle = getDepthBottom(x, y); //Dataset_EVOL.getDepth(x, y, 0);
	 * 
	 * ///////////// ----- E X I T O F D O M A I N
	 * ---------------------------------- auBord(); if (!living) { break; }
	 * 
	 * ///////////// ----- G R O W T H
	 * ------------------------------------------------ if (Simulation.flagGrowth) {
	 * //double Diatoms = plankton[0]; //double NanoPhyto = plankton[1]; //double
	 * MicroZoo = plankton[2]; //double MesoZoo = plankton[3]; //length =
	 * util.GrowthModel.grow(length, temp, Diatoms, NanoPhyto, MicroZoo, MesoZoo);
	 * 
	 * DEB.execute(temp, bouffe, (double) Simulation.dt_advec / 24);
	 * 
	 * /* System.out.println("Diatoms = " + Diatoms);
	 * System.out.println("NanoPhyto = " + NanoPhyto);
	 * System.out.println("MicroZoo = " + MicroZoo); System.out.println("MesoZoo = "
	 * + MesoZoo);
	 * 
	 * }
	 * 
	 * ///////////// ----- S W I M ( D V M )
	 * ------------------------------------------ // MIGRATION VERTICALE (DVM
	 * SURFACE - Oxycline) : if (Simulation.flagSwim_random) { if
	 * (!Simulation.flagGrowth && (age > Simulation.egg_duration)) { // ||
	 * Simulation.flagGrowth && (util.GrowthModel.getStage(length) == 2)) { // EN
	 * L'ABSENCE DE DONNEE PLUS PRECISE : depth = (float) (-Math.random() *
	 * Simulation.Swim_lowerlimit); }
	 * 
	 * } else if (Simulation.flagSwim_O2) { if (!Simulation.flagGrowth && (age >
	 * Simulation.egg_duration)) { // || Simulation.flagGrowth &&
	 * (util.GrowthModel.getStage(length) == 2)) {
	 * 
	 * // En lisant la prof de l'oxycline 1ml.L-1 dans les données : double prof_Oxy
	 * = GetOxyclinDepth_BIDOUILLE.getOxyclin_depth(x, y); // Pour les cas ou la
	 * couche 1ml.L-1 n'est pas définie : //prof_Oxy = Math.max(Math.max(prof_Oxy,
	 * bathyActuelle), -100); // prof max de migration = 100m (si bathy et oxycline
	 * plus profonde) //System.out.println("Max = prof_Oxy = " + prof_Oxy +
	 * "  ; bathyActuelle" + bathyActuelle); depth = (float) (Math.random() *
	 * Math.max(prof_Oxy, -50)); // (prof_Oxy est négatif) } } } catch (IOException
	 * e) { System.out.println("youstone on a un probleme : " + e.getMessage()); }
	 * 
	 * // DEBUT Correction sur les valeurs aberrantes de depth :
	 * ---------------------- ss = 0; boolean refaire_prof = false; while ((depth <
	 * bathyActuelle + 0.1) || (depth > -0.1)) { refaire_prof = true; ss++;
	 * 
	 * if (depth > -0.1) { depth = (float) (-0.1 - Math.random() * 3); } else if
	 * (depth < bathyActuelle + 1) { depth = (float) (bathyActuelle + 2 +
	 * Math.random() * 3); }
	 * 
	 * if (ss > 100) { System.out.println(" impossible  (3) - bathyActuelle = " +
	 * bathyActuelle); System.out.println(" x = " + x + " y = " + y + " depth = " +
	 * depth); System.exit(0); } } /// FIN de correction sur les valeurs aberrantes
	 * de depth ---------------------- // Passage de la profondeur en coordonnées
	 * Grille ****************************** if ((Simulation.flagBuoy) ||
	 * (Simulation.flagSwim_O2) || refaire_prof) { depth = (float) (depth -
	 * Simulation.decalage_zeta); // Pour corriger le souscis avec le run preindusF
	 * z = Dataset_EVOL.depth2z(x, y, depth); } positGeog3D(); // Passage des
	 * coordonées grille aux coordonnée lon lat depth } }
	 */
	// -----------------------------------------------------------------------------
	void positGeog2D() {
		double[] po = Dataset_EVOL.grid2Geo(x, y);
		lat = po[0];
		lon = po[1];
		// depth = po[2];
	}
	// -----------------------------------------------------------------------------

	void positGeog3D() {
		double[] po = Dataset_EVOL.grid2Geo(x, y, z);

		if ((lat == po[0]) && (lon == po[1])) {
			// compteur_standstill = compteur_standstill + 1;
			// System.out.println("compteur_standstill + 1 = " + compteur_standstill);
		}
		lat = po[0];
		lon = po[1];
		depth = (float) po[2];
	}
	// -----------------------------------------------------------------------------
	// Donne la profondeur (bathy) au point xRho, yRho.

	double getDepthBottom(double xRho, double yRho) {
		double h = Dataset_EVOL.getDepth(xRho, yRho, 0);
		return (h);
	}

	// -----------------------------------------------------------------------------
	void auBord() {

		if (compteur_standstill > 24) {
			System.out.println("MORT en STAND STILL-------------------------");
			living = false;
			Cause_de_la_mort = "Astandstill";

		}
		if ((x > Dataset_EVOL.nx - 3.0f) || (x < 3.0f)) {
			living = false;
			Cause_de_la_mort = "Baubord";
			System.out.println("MORT AU BORD EST OU OUEST-------------------------");
		}

		if ((y > Dataset_EVOL.ny - 3.0f) || (y < 3.0f)) {
			living = false;
			Cause_de_la_mort = "Baubord";
			System.out.println("MORT AU BORD NORD OU SUD -------------------------");
		}

		// TEST SUR LA PROFONDEUR :
		// 1 - Poisson dans la colonne d'eau (entre fond et surface)??
		if ((depth < bathyActuelle) || (depth > 0)) {
			living = false;
			Cause_de_la_mort = "Caufond_enlair";
			// System.out.println("bathyActuelle = " + bathyActuelle + " ; depth = " + depth
			// + " -> MORT AU BORD");
			System.out.println("problem avec la profondeur de ce poisson. depth = " + depth);
			// System.out.println("bathyActuelle 2 = " + bathyActuelle);
		}
		// 2 - Pronfondeur limite de la grille hydro?

		// AUTRES TEST :
		if (depth > 0) {
			living = false;
			Cause_de_la_mort = "Denlair";
			System.out.println("depth = " + depth + "MORT EN L'AIR");
		}

		if (!Dataset_EVOL.isInWater(x, y)) {
			System.out.println("MORT A TERRE");
			living = false;
			Cause_de_la_mort = "Eaterre";
		}

		if (Dataset_EVOL.isOnEdge(x, y)) {
			living = false;
			Cause_de_la_mort = "FisOnEdge";
			System.out.println("Mort ON EDGE");
		}

		if (Dataset_EVOL.isCloseToCost(x, y)) {
			living = false;
			Cause_de_la_mort = "ICloseToCost";
			System.out.println("Mort CLOSE TO COAST");
		}
		// TEST DEBUG :
		// if (living == false) {
		// System.out.println("Mort sur les bord de la grille : x = " + x + " , y = " +
		// y + "bathyActuelle = " + bathyActuelle);
		// }
	}

	// --------------------------------------------------------------------------
	void stepSerial(int jour) {
		// System.out.println("Age = " + age + " jours ; Body_length = " +
		// (float)Math.round(Body_length*10)/10 + " cm ; Poids = " +
		// Math.round(this.weight) + " g ; Effectif = " + Math.round(S) + " DEB.E_H = "
		// + DEB.E_H);
		// System.out.println(" Wet_weigth_Reserve = " +
		// Math.round(DEB.W_E/(1-(float)DEB.c_w)) + " g ; Wet_weigth_structure " +
		// Math.round(DEB.W_V/(1-(float)DEB.c_w)) + " g ; GONADE_ETENDUE = " +
		// Math.round(DEB.W_ER/(1-(float)DEB.c_w)) + " g ");
		// System.out.println("adult_ready_to_spawn = " +
		// this.DEB.adult_ready_to_spawn);
		// Indiv dans la zone consideree?
		// sinon on les y remet!

	}

	// ------------------------------------------------------------------------------
	// POSITIONS POTENTIELLES DE PONTE :
	public void init_strates() {

	}

	public void erase_strates() {
		strates = null;

		// Autre solution :
		/*
		 * public void add(Position_ponte p) throws IOException {
		 * this.positions_OK.add(p); }
		 * 
		 * public void remove_positions() { this.positions_OK.clear(); }
		 */
	}

	void migration_ponte(int jour, int dt) {
		// System.out.println("MIGRATION VERS ENVIRONNEMENT DE PONTE ");

		// 1 - Determiner l'env_ok le plus proche (zone de ponte potentielle la +
		// proche)
		// a) le même jour, dans un rayon de 10 km
		// en fait on cherche par resolution_temp donc le rayon = 10*resolution_temp
		int deplacement_max_par_jour_km = 10;
		int strate_temp = (int) Math.floor((jour));

		float deplacement_max_dt = (float) deplacement_max_par_jour_km * ((float) dt / 24);
		// au moment "strate_temp" on cherche i et j tq
		// strates[strate_temp][i][j]==true
		// && (i-Po.x)^2 + (j-Po.j)^2 < rayon_accessible_km*resolution_spatiale

		// 2 - nager dans cette direction SAUF si obstacle (côte)

		// ACODER -!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! <---------------!!!
	}

	int nbre_au_hazard_entre_0_et_x(int x) {
		Random rand = new Random(); // constructeur
		int i = rand.nextInt(x + 1); // génération
		return (i);
	}

}
