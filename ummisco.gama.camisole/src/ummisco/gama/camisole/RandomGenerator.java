package ummisco.gama.camisole;

import umontreal.ssj.randvar.NormalGen;
import umontreal.ssj.randvarmulti.DirichletGen;
import umontreal.ssj.rng.GenF2w32;
import umontreal.ssj.rng.RandomStream;

public abstract class RandomGenerator {
	private static int [] SEED_APSF=null;
	
	private static GenF2w32 RANDOM_STREAM_GENERATOR = null;

	public static void setSeed(int[] seed)
	{
		RandomGenerator.SEED_APSF = seed;
	}
	public static int [] generateSeed()
	{
		if(SEED_APSF != null)
			return SEED_APSF;
		
		int []sdd = new int[25] ;
		for(int i = 0; i<sdd.length;i++) {
			sdd[i] = (int)(Math.random()*1000)+1;
		}
		SEED_APSF = sdd;
		return SEED_APSF;
	}
	
	private static void generateRandomStream()
	{
		if(SEED_APSF == null)
			generateSeed();
		RANDOM_STREAM_GENERATOR = new GenF2w32();
		RANDOM_STREAM_GENERATOR.setSeed(SEED_APSF);
	}
	
	public static DirichletGen getDirichletGen()	{
		if(RANDOM_STREAM_GENERATOR==null)
			generateRandomStream();
		
		double [] alpha = {1,1,1,1};
		DirichletGen dir = new DirichletGen(RANDOM_STREAM_GENERATOR,alpha);
		return dir;
	}

	static NormalGen normalGen = null; 
	
	public static NormalGen getNormalGen(double mean, double sigma)	{
		if(RANDOM_STREAM_GENERATOR==null)
			generateRandomStream();
		return new NormalGen(RANDOM_STREAM_GENERATOR, 10,5);
	}
	
	
}
