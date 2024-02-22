package gama.experimental.apsf.template;

import gama.experimental.apsf.spaces.Agglomerate;
import gama.experimental.apsf.spaces.OrganicMatter;
import gama.experimental.apsf.spaces.Particle;
import gama.experimental.apsf.spaces.SandParticle;
import gama.experimental.apsf.spaces.SoilLocation;
import gama.experimental.apsf.spaces.WhiteParticle;
import gama.experimental.camisole.RandomGenerator;

public class TechnoSoilTemplate extends Template {

	public final static String DEFAULT_NAME = "Soil template";

	private int ecartType = 2;
	private int esperance = 5;
	private Template[] Technosol;

	int gaussNbCells( double ecartType, double esperance)	{
		return Math.round((float)RandomGenerator.getNormalGen(esperance,ecartType).nextDouble());
	}


	void pushWhiteParticle(int x, int y, int z, int number) {
		float ll = 0;

		int total = 0;
		int distance = 0;
		while (total < number) {

			int i = x - distance;

			if (i < 0)
				i = 0;

			for (; i < size && i <= x + distance && total < number; i++) {

				int j = y - distance;
				if (j < 0)
					j = 0;
				for (; j < size && j <= y + distance && total < number; j++) {
					int k = z - distance;
					if (k < 0)
						k = 0;

					for (; k < size && k <= z + distance && total < number; k++) {
						if (this.getParticle(i, j, k) instanceof Agglomerate) {
							Particle pp = new WhiteParticle();
							pp.setLocation(new SoilLocation(i, j, k, 1,null));
							this.setTemplate(pp, i, j, k, ll);
							total++;
						}
					}
				}

			}
			distance++;
			if (distance == 5) // ????????
				number = 0;
		}
	}

	public void generateTemplateFinal(int scale) {
		double og = this.organic;
		// initialisation du template en le remplissant de fractal
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				// plusieur horizon
				for (int k = 0; k < size; k++) {
					Particle pp;
					pp = new Agglomerate(this);
					pp.setLocation(new SoilLocation(i, j, k, 0,null));
					this.setTemplate(pp, i, j, k, 0);
				}
		// d�pots de cavit�s...
		int nbCavite = (int) ((1 - solid - fractal - organic) * (size
				* size * size));
		int nbCaviteCree = 0;
		while (nbCaviteCree < nbCavite) {
			int nb = gaussNbCells( ecartType, esperance);
			System.out.println("gausse" + nb);

			// position al�atoire
			int i = (int) Math.round(Math.random() * size) % size;
			int j = (int) Math.round(Math.random() * size) % size;
			int k = (int) Math.round(Math.random() * (size)) % size;
			System.out.println("geusse******************************** : " + k);
			pushWhiteParticle(i, j, k, nb);
			nbCaviteCree += nb;
		}
		// d�pot de la matiere organique
		int nbOrganic = (int) ((organic) * (size * size * size));
		int nbOrganicCree = 0;
		while (nbOrganicCree < nbOrganic) {
			// position al�atoire
			int i = (int) Math.round(Math.random() * size) % size;
			int j = (int) Math.round(Math.random() * (size)) % size;
			int k = (int) Math.round(Math.random() * (size)) % size;
			System.out
					.println("organic******************************** : " + k);
			if (getParticle(i, j, k) instanceof Agglomerate) {
				Particle pp;
				pp = new OrganicMatter();
				pp.setLocation(new SoilLocation(i, j, k, scale,null));
				this.setTemplate(pp, i, j, k, 0);
				nbOrganicCree++;
			}
		}
		// d�pot des min�raux
		int nbSand = (int) ((solid) * (size * size * size));
		int nbSandCree = 0;
		while (nbSandCree < nbSand) {
			// position al�atoire
			int i = (int) Math.round(Math.random() * size) % size;
			int j = (int) Math.round(Math.random() * (size)) % size;
			int k = (int) Math.round(Math.random() * (size)) % size;
			System.out.println("solid******************************** : " + k);
			if (getParticle(i, j, k) instanceof Agglomerate) {
				Particle pp;
				pp = new SandParticle();
				pp.setLocation(new SoilLocation(i, j, k, scale,null));
				this.setTemplate(pp, i, j, k, 0);
				nbSandCree++;
			}
		}
	}

	/**/
	public TechnoSoilTemplate(String name, float solid, float fractal,
			float organic, int size, int ecartType, int esperance, int scale) {
		super(name, solid, fractal, organic, size);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(scale);
	}

	public TechnoSoilTemplate(String name, float solid, float fractal,
			float organic, int size, int ecartType, int esperance,
			Template sub, int scale) {
		super(name, solid, fractal, organic, size, sub);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(scale);
	}

	public TechnoSoilTemplate(String name, float solid, float fractal,
			float organic, int size, int ecartType, int esperance,
			Template[] sub, int scale) {
		super(name, solid, fractal, organic, size, sub);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.Technosol = sub;
		this.generateTemplateFinal(scale);
	}

	public TechnoSoilTemplate(float solid, float fractal, float organic,
			int size, int ecartType, int esperance, Template sub, int scale) {
		super(DEFAULT_NAME, solid, fractal, organic, size, sub);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(scale);
	}

	public TechnoSoilTemplate(float solid, float fractal, float organic,
			int size, int ecartType, int esperance, int scale) {
		super(DEFAULT_NAME, solid, fractal, organic, size);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.generateTemplateFinal(scale);
	}

	/**/
	public TechnoSoilTemplate() {
		super(DEFAULT_NAME, 0, 0, 0, 10);
		this.ecartType = 0;
		this.esperance = 0;
	}

	public TechnoSoilTemplate(Template[] technoSol, int ecartType,
			int esperance, int scale) {
		super("TechnoSol", 0, 0, 1, 10);
		this.ecartType = ecartType;
		this.esperance = esperance;
		this.Technosol = technoSol;
		this.generateTemplateFinal(scale);
	}

	public TechnoSoilTemplate(SoilTemplate soilTemplate[]) {
		super("", (float) soilTemplate[0].getSolid(), (float) soilTemplate[0]
				.getFractal(), (float) soilTemplate[0].getOrganic(),
				soilTemplate[0].getSize());
	}

	@Override
	public void generateTemplate() {
		// TODO Auto-generated method stub

	}
}
