package ummisco.gama.camisole;

public class GranulometricScale {

	private String name;
	private double solid;
	private double organicMatter;
	private double minBoundary;
	private double maxBoundary;
	private int scale;
	
	GranulometricScale(String name, double mn, double mx, int scale) {
		super();
		this.name = name;
		this.minBoundary = mn;
		this.maxBoundary = mx;
		this.scale = scale;
	}
	
	public String getName() {
		return name;
	}
	public double getMineralVolume() {
		return solid;
	}
	public double getOrganicMatterVolume() {
		return organicMatter;
	}

	public float getScale() {
		return scale;
	}

	
	public void setVolume(float mineral, float om)
	{
		this.organicMatter=om;
		this.solid = mineral;
		System.out.println("Volume " + om+" " +mineral);
		
	}
	
	public double getMinBoundary() {
		return minBoundary;
	}

	public double getMaxBoundary() {
		return maxBoundary;
	}
	
}
