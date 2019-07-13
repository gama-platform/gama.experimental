package ummisco.gama.camisole;

import java.util.ArrayList;

import msi.gama.metamodel.agent.IAgent;
import ummisco.gama.apsf.spaces.Apsf;
import ummisco.gama.apsf.template.SoilTemplate;
import umontreal.ssj.randvarmulti.DirichletGen;
import umontreal.ssj.rng.GenF2w32;

public class SoilFactory {

	private final ArrayList<InputData> mineralInputs;
	private final ArrayList<InputData> organicMatterInputs;
	private final ArrayList<GranulometricScale> scales;
	private float bulkDensity;
	private double cubeSize;
	private int cubeDividing;

	private static String GENERIC_SCALE_NAME = "C";

	public SoilFactory() {
		this.bulkDensity = 0;
		mineralInputs = new ArrayList<>();
		organicMatterInputs = new ArrayList<>();
		scales = new ArrayList<>();
	}

	public void addMineralGranulometricScale(final double mineral, final double min, final double max) {
		final InputData tmp = new InputData(mineral, min, max);
		this.mineralInputs.add(tmp);
	}

	public void addOMGranulometricScale(final double om, final double min, final double max) {
		final InputData tmp = new InputData(om, min, max);
		this.organicMatterInputs.add(tmp);
	}

	private double getLowBoundary() {
		double res = Double.MAX_VALUE;
		for (final InputData dte : mineralInputs) {
			res = Math.min(dte.maxBoundary, res);
		}
		for (final InputData dte : mineralInputs) {
			res = Math.min(dte.maxBoundary, res);
		}

		return res;
	}

	private int countDefaultNumberOfScale(final double cubeWidth, final int dividing) {
		final double minSize = getLowBoundary();
		int cScale = 0;
		double cSize = cubeWidth;
		do {
			cSize = cSize / dividing;
			cScale++;
		} while (cSize > minSize);
		return cScale + 1;
	}

	private void generateGranulometricScale(final int nbScale) {
		double max = cubeSize;
		for (int i = 0; i < nbScale; i++) {
			final double min = nbScale == i + 1 ? 0 : max / cubeDividing;
			final GranulometricScale tmp = new GranulometricScale(GENERIC_SCALE_NAME + i, min, max, i);
			this.scales.add(tmp);
			System.out.println("creation de  " + tmp.getName() + " " + min + " " + max);
			max = min;
		}
	}

	private float sumScaleRates(final ArrayList<InputData> data, final double min, final double max,
			final double round) {
		double res = 0;
		for (final InputData dts : data) {
			res += dts.getValueBetween(min, max, round);
		}
		return (float) res;
	}

	private void compileGranulometricScale(final int nbScale) {
		generateGranulometricScale(nbScale);
		for (final GranulometricScale s : scales) {
			final double min = s.getMinBoundary();
			final double max = s.getMaxBoundary() > this.cubeSize ? this.cubeSize : s.getMaxBoundary();
			final double weight = Math.pow(this.cubeSize, 3) * this.bulkDensity;
			final double roundScale = Math.pow(this.cubeDividing, s.getScale());

			// weight - totalomWeight;
			final double sum = sumScaleRates(this.mineralInputs, min, max, roundScale) * (weight / 1000)
					/ SoilConstant.MINERAL_MATTER_DA;
			final double sum2 = sumScaleRates(this.organicMatterInputs, min, max, roundScale) * (weight / 1000)
					* SoilConstant.ORGANIC_MATTER_CONCENTRATION / SoilConstant.ORGANIC_MATTER_DA;

			s.setVolume((float) sum, (float) sum2);
		}
	}

	private DirichletGen[] configureRandomLaw() {
		final double[] levelRate = new double[this.scales.size()];
		this.scales.size();
		double total_om = 0;
		double total_sand = 0;
		this.scales.size();
		final double[][] alphaValues = new double[this.scales.size()][];
		int i = 0;
		final double soilVolume = Math.pow(this.cubeSize, 3);
		for (final GranulometricScale s : this.scales) {
			total_om += s.getOrganicMatterVolume();
			total_sand += s.getMineralVolume();
			levelRate[i] = (s.getMineralVolume() + s.getOrganicMatterVolume()) / soilVolume;
			System.out.println(" level rate " + levelRate[i]);
			i = i + 1;
		}
		i = 0;
		for (final GranulometricScale s : this.scales) {
			final double sum = s.getMineralVolume() + s.getOrganicMatterVolume();
			final double sa = s.getMineralVolume() / total_sand;
			final double omA = s.getOrganicMatterVolume() / total_om;
			final double f = 1 - levelRate[i];

			// double sum2 = omA+sa+ f + 2*0.001 ;

			alphaValues[i] = new double[4];
			alphaValues[i][0] = Math.max(sa, 0.4); // sRate[i]; //(sa+0.001)*0.9; ///sum2;
			alphaValues[i][1] = f; /// sum2;
			alphaValues[i][2] = Math.max(omA, 0.4); // (omA+0.001)*0.9; ///sum2;
			alphaValues[i][3] = 1;

			System.out.println("alpha " + sum + " " + alphaValues[i][0] + " " + alphaValues[i][1] + " "
					+ alphaValues[i][2] + " " + alphaValues[i][3]);
			i++;
		}
		final DirichletGen[] gen = new DirichletGen[this.scales.size()];
		for (i = 0; i < this.scales.size(); i++) {
			gen[i] = configureRandomLaw((float) alphaValues[i][0], (float) alphaValues[i][2], (float) alphaValues[i][1],
					(float) alphaValues[i][3]);
		}

		return gen;

	}

	private DirichletGen configureRandomLaw(final float solid, final float om, final float fractal, final float porus) {
		System.out.println("trr  \t" + solid + "\t" + fractal + "\t" + om + "\t" + porus);
		final double[] alpha = { solid, fractal, om, porus }; // {0.3,1,0.3,0.1};
		final int[] sdd = new int[25];
		for (int i = 0; i < sdd.length; i++) {
			sdd[i] = (int) (Math.random() * 1000) + 1;
		}
		final GenF2w32 rd = new GenF2w32();
		rd.setSeed(sdd);
		final DirichletGen dir = new DirichletGen(rd, alpha);
		return dir;
	}

	private float[][] generateParameter() {
		final int nbScale = this.scales.size();
		final double[] res = { 1, 1, 1, 1 };
		final float[][] selectedData = new float[nbScale][];
		for (int j = 0; j < nbScale; j++) {
			final DirichletGen gen = generators[j];
			gen.nextPoint(res);
			final GranulometricScale g = getGranulometricData(j);
			selectedData[j] = new float[3];
			if (j == 0) {
				selectedData[j][0] = (float) (g.getMineralVolume() / Math.pow(this.cubeSize, 3));
				selectedData[j][1] = (float) res[1];
				selectedData[j][2] = (float) (g.getOrganicMatterVolume() / Math.pow(this.cubeSize, 3));

			} else {
				selectedData[j][0] = g.getMineralVolume() == 0 ? 0 : (float) res[0];
				selectedData[j][1] = (float) res[1];
				selectedData[j][2] = g.getOrganicMatterVolume() == 0 ? 0 : (float) res[2];
			}
		}
		return selectedData;

	}

	public Apsf generatApsf(final float[][] characteristics, final IAgent agt) {
		final Apsf tmpApsf = new Apsf(this.cubeSize, this.cubeDividing, agt);
		SoilTemplate upTemplate = null;
		// System.out.println("\n");
		for (int j = characteristics.length - 1; j >= 0; j--) {
			final float[] layer = characteristics[j];
			final SoilTemplate tmp = new SoilTemplate(GENERIC_SCALE_NAME + j, (float) layer[0], (float) layer[1],
					(float) layer[2], this.cubeDividing, 5, 10, upTemplate);
			upTemplate = tmp;
		}
		// System.out.println("\n");

		tmpApsf.defineTemplateTree(upTemplate);
		return tmpApsf;
	}

	public GranulometricScale getGranulometricData(final int scale) {
		for (final GranulometricScale a : scales) {
			if (a.getScale() == scale) { return a; }
		}
		return null;
	}

	public float evaluateRMS(final Apsf apsf) {
		final int nbScale = this.scales.size();
		double rms = 0;

		final double soilSize = Math.pow(this.cubeSize, 3);
		// System.out.println("\n\n");
		for (int i = 0; i < nbScale; i++) {
			final GranulometricScale dte = getGranulometricData(i);
			final double sand = apsf.countSandMatterAtSizeScale(i) * soilSize;
			final double om = apsf.countOrganicMatterAtSizeScale(i) * soilSize;

			// System.out.println("OM matter " + dte.getOrganicMatterVolume()+" " + om);

			final double sdelta = dte.getMineralVolume() - sand;
			final double oMdelta = dte.getOrganicMatterVolume() - om;
			rms += sdelta * sdelta + oMdelta * oMdelta;
		}
		// System.out.println("\n\n");

		return (float) Math.sqrt(rms);
	}

	private static DirichletGen[] generators = null;

	private Apsf buildApsf(final int nbIter, final IAgent agt) {
		Apsf res = null;
		float rms = Float.MAX_VALUE;
		generators = configureRandomLaw();
		for (int i = 0; i < nbIter; i++) {
			final float[][] param = generateParameter();
			final Apsf tmpApsf = generatApsf(param, agt);
			final float tmp = evaluateRMS(tmpApsf);
			if (tmp < rms) {
				rms = tmp;
				res = tmpApsf;
			}
		}
		final int nbScale = this.scales.size();
		for (int i = 0; i < nbScale; i++) {
			final GranulometricScale dte = getGranulometricData(i);

			System.out.println("solid delta\t" + i + "\t\t" + dte.getMineralVolume() + "\t\t"
					+ res.countSandMatterAtSizeScale(i) * 20 * 20 * 20);
			System.out.println("oM delta\t" + i + "\t\t" + dte.getOrganicMatterVolume() + "\t\t"
					+ res.countOrganicMatterAtSizeScale(i) * 20 * 20 * 20);
		}

		System.out.println(
				"bstRMSE " + rms + "  " + res.getAPSF().getTemplate().getOrganicMatterTotalVolumicMass(0, res));

		final double omW = res.countTotalOrganicMatter() * Math.pow(this.cubeSize, 3) * SoilConstant.ORGANIC_MATTER_DA;
		final double mmW = res.countTotalSandMatter() * Math.pow(this.cubeSize, 3) * SoilConstant.MINERAL_MATTER_DA;

		final double weight = omW + mmW;

		System.out.println("total om " + omW);
		System.out.println("total mineral " + mmW);
		System.out.println("total Soil " + weight + " " + weight / Math.pow(this.cubeSize, 3));
		res.getAPSF().initMatters(omW, mmW);
		return res;
	}

	public Apsf compileAndBuild() {
		return compileAndBuild(1.38f, 20, 10, 20, null);
	}

	public Apsf compileAndBuild(final float bulkDensity, final double cubeWidth, final int dividing, final int nbIter,
			final IAgent agt) {
		this.bulkDensity = bulkDensity;
		this.cubeSize = cubeWidth;
		this.cubeDividing = dividing;
		// Apsf res = new Apsf(cubeWidth,dividing);
		final int nbScale = countDefaultNumberOfScale(cubeWidth, dividing);
		compileGranulometricScale(nbScale);
		return buildApsf(nbIter, agt);
	}

}
