package ummisco.gama.apsf.spaces;

import ummisco.gama.apsf.template.SoilTemplate;

public class TechnoSoilHorizon {
	private SoilTemplate horizon;
	private int depth;
	
	public TechnoSoilHorizon(SoilTemplate horizon,int depth){
		this.horizon = horizon;
		this.depth = depth;
	}
	
	public void setHorizon(SoilTemplate horizon) {
		this.horizon = horizon;
	}
	public SoilTemplate getHorizon() {
		return horizon;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public int getDepth() {
		return depth;
	}
	
}
