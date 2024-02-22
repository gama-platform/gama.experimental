package gama.experimental.camisole.test;

import gama.experimental.apsf.spaces.Apsf;
import umontreal.ssj.randvarmulti.DirichletGen;
import umontreal.ssj.rng.GenF2w32;

public class TestStochastic {

	public Apsf loadAndBuildSoil(final float[][] data) {
		/*
		 * Apsf res = new Apsf(20, 10); float[] upperScale = data[0];
		 * 
		 * SoilTemplate upT = new SoilTemplate(upperScale[2], upperScale[0], upperScale[1],Template.DEFAULT_SIZE, 2, 5);
		 * SoilTemplate temp = upT; for(int i = 1;i<data.length; i++) { upperScale = data[i]; temp = new
		 * SoilTemplate(upperScale[2], upperScale[0], upperScale[1],Template.DEFAULT_SIZE, 2, 5, temp); }
		 * res.defineTemplateTree(upT);
		 */
		return null;
	}

	public float[][] findBestParameterSet(final float[][] granu, final int nb) {
		final double[] alpha = new double[granu[0].length]; // {1,1,1,1};
		final double[] res = new double[granu[0].length];

		for (int i = 0; i < alpha.length; i++) {
			alpha[i] = 1;
			res[i] = 1;
		}

		final GenF2w32 rd = new GenF2w32();
		final DirichletGen dir = new DirichletGen(rd, alpha);
		for (int i = 0; i < nb; i++) {
			final float[][] selectedData = new float[granu.length][];
			for (int j = 0; j < granu.length; j++) {
				dir.nextPoint(res);
				selectedData[j] = new float[3];
				selectedData[j][0] = (float) res[0];
				selectedData[j][1] = (float) res[1];
				selectedData[j][2] = (float) res[2];
			}
			generatApsf(20, 10, selectedData);
		}

		return null;
	}

	public static Apsf generatApsf(final float dim, final int div, final float[][] characteristics) {
		return null;
		/*
		 * Apsf tmpApsf = new Apsf(20,10); SoilTemplate upTemplate = null; for(int j = characteristics.length-1;
		 * j>=0;j--) { float[] layer = characteristics[j]; SoilTemplate tmp = new
		 * SoilTemplate("h"+j,(float)layer[0],(float)layer[1],(float)layer[2],Template.DEFAULT_SIZE,5,10,upTemplate);
		 * upTemplate = tmp; } tmpApsf.defineTemplateTree(upTemplate); return tmpApsf;
		 */
	}

	public static void main(final String[] args) {
		/*
		 * Apsf app = new Apsf(20,10);
		 * 
		 * SoilTemplate tmp =new SoilTemplate("C3",0, 0.2f, 0.17f, 10, 1, 1); SoilTemplate tmp2 =new
		 * SoilTemplate("C2",0, 0.5f, 0, 10, 1, 1,tmp); SoilTemplate tmp3 =new SoilTemplate("C1",0, 0.5f, 0, 10, 1,
		 * 1,tmp2); app.defineTemplateTree(tmp3);
		 * 
		 * System.out.println(app.countOrganicMatterAtSizeScale(5));
		 * System.out.println(app.countOrganicMatterAtSizeScale(4));
		 * System.out.println(app.countOrganicMatterAtSizeScale(3));
		 * System.out.println(app.countOrganicMatterAtSizeScale(2));
		 * System.out.println(app.countOrganicMatterAtSizeScale(1));
		 * System.out.println(app.countOrganicMatterAtSizeScale(0));
		 */
	}

}
