package gama.experimental.camisole.types;

import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@type (
		name = ISoilLocationType.TYPE_NAME,
		id = SoilLocation.Id,
		kind = ISymbolKind.Variable.REGULAR,
		wraps = { SoilLocation.class })
public class SoilLocationType extends GamaType<SoilLocation> {

	@Override
	public boolean canCastToConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public SoilLocation cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof SoilLocation) {
			return new SoilLocation(((SoilLocation) obj).getX(), ((SoilLocation) obj).getY(),
					((SoilLocation) obj).getZ(), ((SoilLocation) obj).getScale());
		} // TODO Auto-generated method stub
		if (obj instanceof IList) {
			final IType<?> mtype = ((IList<?>) obj).getGamlType();
			if (mtype.getKeyType() == Types.INT) {
				final IList<Integer> tt = (IList<Integer>) obj;

			}

			// if(((GamaList)obj).)
		}

		return null;
	}

	@Override
	public SoilLocation getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

}
