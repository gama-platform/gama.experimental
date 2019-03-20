package ummisco.gama.camisole.types;

import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@type(name = ISoilLocationType.TYPE_NAME, id = SoilLocation.Id, kind = ISymbolKind.Variable.REGULAR, wraps = { SoilLocation.class })
public class SoilLocationType extends GamaType<SoilLocation> {

	@Override
	public boolean canCastToConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SoilLocation cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		if(obj instanceof SoilLocation){
				return new SoilLocation(((SoilLocation) obj).getX(), ((SoilLocation) obj).getY(),((SoilLocation) obj).getZ(),((SoilLocation) obj).getScale());
		}// TODO Auto-generated method stub
		if(obj instanceof GamaList )
		{
			IType mtype = ((GamaList)obj).getGamlType();
			if(mtype.getKeyType() == GamaType.INT )
			{
				GamaList<Integer> tt = (GamaList<Integer>) obj;
				
			}
			
			
		//	if(((GamaList)obj).)
		}
			
		return null;
	}

	@Override
	public SoilLocation getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

}
