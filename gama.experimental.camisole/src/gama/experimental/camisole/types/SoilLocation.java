package gama.experimental.camisole.types;

import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.gaml.types.IType;

@vars({
	@variable(name="x", type=IType.INT),
	@variable(name="y", type=IType.INT),
	@variable(name="z", type=IType.INT),
	@variable(name="scale", type=IType.INT),
})
public class SoilLocation {
	public final static int Id = 100;
	private int x;
	private int y;
	private int z;
	private int scale;
	
	public SoilLocation(int x,int y,int z, int scale)
	{
		this.scale = scale;
		this.x = x;
		this.y = y;
		this.z =z;
	}

	@getter(ISoilLocationType.X)
	public int getX() {
		return x;
	}

	@setter(ISoilLocationType.X)
	public void setX(int x) {
		this.x = x;
	}

	@getter(ISoilLocationType.Y)
	public int getY() {
		return y;
	}

	@setter(ISoilLocationType.Y)
	public void setY(int y) {
		this.y = y;
	}

	@getter(ISoilLocationType.Z)
	public int getZ() {
		return z;
	}

	@setter(ISoilLocationType.Z)
	public void setZ(int z) {
		this.z = z;
	}

	@getter(ISoilLocationType.SCALE)
	public int getScale() {
		return scale;
	}

	@setter(ISoilLocationType.SCALE)
	public void setScale(int scale) {
		this.scale = scale;
	}
	
	
}
