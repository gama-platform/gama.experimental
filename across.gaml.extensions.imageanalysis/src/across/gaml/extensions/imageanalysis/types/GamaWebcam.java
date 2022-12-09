package across.gaml.extensions.imageanalysis.types;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
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


	public boolean saveImageAsFile(String file_path) {
		File outputfile = new File(file_path);
		try {
			ImageIO.write(webcam.getImage(), "jpg", outputfile);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
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
