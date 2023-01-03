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
//	public final static int id = IType.AVAILABLE_TYPES + 175769875;
	public final static int id = IType.AVAILABLE_TYPES + 4532623;

	@Override
	public GamaWebcam getDefault() {
		GamaWebcam webcam = new GamaWebcam(0);
		webcam.setWebcam(Webcam.getDefault());
		if (webcam.getWebcam() != null) {
			if (webcam.getWebcam().getLock() != null)
				webcam.getWebcam().getLock().disable();
			webcam.getWebcam().close();
			webcam.getWebcam().open();
		}
		return webcam;
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
			webcam.setWebcam(Webcam.getWebcams().get(id));
			if (webcam.getWebcam() != null) {
				if (webcam.getWebcam().getLock() != null)
					webcam.getWebcam().getLock().disable();
				webcam.getWebcam().close();
				webcam.getWebcam().open();
			}
			return webcam;
		}
		return null;
	}

}
