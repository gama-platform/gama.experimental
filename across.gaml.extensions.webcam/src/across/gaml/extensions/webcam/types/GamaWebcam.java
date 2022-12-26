package across.gaml.extensions.webcam.types;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import msi.gama.common.interfaces.IValue;
import msi.gama.common.util.FileUtils;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaPair;
import msi.gama.util.IList;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars({ @variable(name = "id", type = IType.INT),@variable(name = "resolutions", type = IType.LIST) })
public class GamaWebcam implements IValue {

	private Webcam webcam;
	private Integer id;
	
	
	@getter ("resolutions")
	public IList<GamaPair<Integer, Integer>> getResolutions() {
		Dimension[] dims =  webcam.getViewSizes();
		IList<GamaPair<Integer, Integer>> resolutions = GamaListFactory.create();
		for (Dimension d : dims) {
			resolutions.add(new GamaPair<Integer, Integer>(d.width, d.height, Types.INT, Types.INT));
		}
		return resolutions;
	}
	
	public Webcam getWebcam() {
		return webcam;
	}

	public void setWebcam(Webcam webcam) {
		this.webcam = webcam;
	}

	protected GamaWebcam(int id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	} 
	
	
	public void setType(Integer id) {
		this.id = id;
	}
	

	@Override
	public IType<?> getGamlType() {
		return Types.get(GamaWebcamType.id);
	}
	
	
	@Override
	public String toString() {
		return serialize(true);
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return ""+id;
	}
	
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "" +id;
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		return null;
	}

	
}
