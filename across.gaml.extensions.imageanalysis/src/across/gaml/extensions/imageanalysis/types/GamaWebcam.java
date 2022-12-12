package across.gaml.extensions.imageanalysis.types;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import msi.gama.common.interfaces.IValue;
import msi.gama.common.util.FileUtils;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars({ @variable(name = "id", type = IType.INT)})
public class GamaWebcam implements IValue {

	private Webcam webcam;
	private Integer id;
	
	
	
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


	public boolean saveImageAsFile(IScope scope, String file_path, int width, int height) {
		
		if (webcam == null) {
			return false;
		}
		Dimension dim = new Dimension(width, height);
		if (!webcam.getViewSize().equals(dim)) {
			webcam.close();
			boolean nonStandard = true;
			for (int i = 0; i < webcam.getViewSizes().length; i++) {
				if (webcam.getViewSizes()[i].equals(dim)) {
					nonStandard = false;
					break;
				}
			}
			if (nonStandard) {
				Dimension[] nonStandardResolutions = new Dimension[] {dim};
				webcam.setCustomViewSizes(nonStandardResolutions);
			}
			webcam.setViewSize(dim);
	
			webcam.getLock().disable();
			webcam.open();
		}
		BufferedImage bim = (BufferedImage) webcam.getImage(); 

		if (file_path != null && !file_path.isBlank()) {
			String path_gen = FileUtils.constructAbsoluteFilePath(scope, file_path, false);
			File outputfile = new File(path_gen);
	    	try {
				ImageIO.write(bim, "jpg", outputfile);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return false;
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
