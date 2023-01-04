package across.gaml.extensions.webcam.types;

import com.github.sarxos.webcam.Webcam;

import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@type(name = "webcam", id = GamaWebcamType.id, wraps = { GamaWebcam.class }, concept = { IConcept.TYPE, "webcam" })
public class GamaWebcamType extends GamaType<GamaWebcam> {
	public final static int id = IType.AVAILABLE_TYPES + 4532623;

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
			
			Webcam.resetDriver();
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
