package gama.experimental.camisole;

public class InputData {
	double minBoundary= 0.0f;
	double maxBoundary= Double.MAX_VALUE;
	double value;
	
	InputData(double v, double mn, double mx)
	{
		this.minBoundary = mn;
		this.maxBoundary = mx;
		this.value = v;
	}
	
	double getValueBetween(double mn, double mx, double round)
	{
		double mxx = maxBoundary==Double.MAX_VALUE ? mx:maxBoundary;
		double xx = Math.round(round*mxx)/round;
		double yy = Math.round(round*minBoundary)/round;
		if(mxx<=mn||mx<=yy)
			return 0;
		double up = Math.min(mx, mxx);
		double bt = Math.max(mn, yy);
		return value /(xx-yy) * (up-bt);
	}
}
