package ummisco.gama.camisole.test;

import ummisco.gama.apsf.spaces.Apsf;
import ummisco.gama.apsf.spaces.Particle;
import ummisco.gama.apsf.spaces.SoilLocation;
import ummisco.gama.camisole.SoilFactory;

public class TestSoilData {

	public static void main(final String[] args) {
		final SoilFactory soil = new SoilFactory();
		soil.addMineralGranulometricScale(0, 2, Double.MAX_VALUE);
		soil.addMineralGranulometricScale(164.9f, 0.2f, 2);
		soil.addMineralGranulometricScale(092.5f, 0.02f, 0.05f);
		soil.addMineralGranulometricScale(086.5f, 0.02f, 0.05f);
		soil.addMineralGranulometricScale(232.1f, 0.002f, 0.02f);
		soil.addMineralGranulometricScale(423.9f, 0, 0.002f);

		soil.addOMGranulometricScale(0.5f, 2, Double.MAX_VALUE);
		soil.addOMGranulometricScale(0.04f, 0.2f, 2);
		soil.addOMGranulometricScale(2.03f, 0.05f, 0.2f);
		soil.addOMGranulometricScale(28.13f, 0, 0.05f);
		final Apsf f = soil.compileAndBuild();
		f.getAPSF().getTemplate().getAllSubTemplate();

		final SoilLocation s = new SoilLocation(41, 26, 89, 3, f);

		final Particle p = f.getParticleAtLocation(null, s);

		// Particle p = f.getOneParticleWithCharacteristics(null,md.get(md.size()-1),7,IParticle.WHITE_PARTICLE);

		System.out.println("3 " + p.getTemplateName() + " " + p.getLocation());

	}

}
