package miat.gama.extension.qrcode.type;

import com.github.sarxos.webcam.Webcam;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.shape.GamaPoint;
//import miat.gaml.extensions.argumentation.types.GamaArgument;
//import miat.gaml.extensions.argumentation.types.GamaArgumentType;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.test;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.GamaMatrixType;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@type(name = "webcam", id = GamaWebcamType.id, wraps = { GamaWebcam.class }, concept = { IConcept.TYPE, "webcam" })
public class GamaWebcamType extends GamaType<GamaWebcam> {
//	public final static int id = IType.AVAILABLE_TYPES + 175769875;
	public final static int id = IType.AVAILABLE_TYPES + 4532623;

	@Override
	public GamaWebcam getDefault() {
		GamaWebcam webcam = new GamaWebcam(0);
		webcam.setWebcam(Webcam.getDefault());
		return webcam;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public GamaWebcam cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		// TODO Auto-generated method stub
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
			return webcam;
		}
		return null;
	}

}
