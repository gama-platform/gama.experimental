package gama.experimental.webcam.types;

import java.util.Locale;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;

import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

@type(name = "webcam", id = GamaWebcamType.id, wraps = { GamaWebcam.class }, concept = { IConcept.TYPE, "webcam" })
public class GamaWebcamType extends GamaType<GamaWebcam> {
	public final static int id = IType.AVAILABLE_TYPES + 4532623;

	static {
		final String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((os.contains("mac")) || (os.contains("darwin"))) 
		     Webcam.setDriver(new NativeDriver());
		else 
			Webcam.setDriver( new WebcamDefaultDriver());
	}
	
	@Override
	public GamaWebcam getDefault() {
		return null;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public GamaWebcam cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if(obj instanceof GamaWebcam) {
			return (GamaWebcam) obj;
		}
		if(obj instanceof Integer) {
			int id = (Integer) obj;
			id = Math.min(Webcam.getWebcams().size() -1, id);
			if (id == -1) {
				return null;
			}
			
			GamaWebcam webcam = new GamaWebcam(id);
			WebcamCustom c = new WebcamCustom(Webcam.getWebcams().get(id).getDevice());
			if (c.getLock().isLocked()) {
				c.getLock().disable();
			}
			if (c.getDevice().isOpen()) {
				c.getDevice().close();
			}
			if (c.isOpen()) {
				c.close();
			}
			webcam.setWebcam(c);
			
			
			return webcam;
		}
		return null;
	}

}
