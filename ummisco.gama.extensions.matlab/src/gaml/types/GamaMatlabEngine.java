package gaml.types;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

@vars({
//	@variable(name = "attributes", type = IType.LIST, of = IType.STRING, doc = {@doc("Returns the list of attribute names") }),
})
public class GamaMatlabEngine implements IValue {

	MatlabEngine eng;
	
	public GamaMatlabEngine(MatlabEngine _eng) {
		eng = _eng;
	}
	
	public MatlabEngine getEngine() {
		return eng;
	}
	
	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	public void disconnect(IScope scope, boolean async) throws GamaRuntimeException {
		try {
			if(async) {
				eng.disconnectAsync();
			} else {
				eng.disconnect();
			}
		} catch (EngineException e) {
		    throw GamaRuntimeException.error("Disconnection Failed", scope);
		}		
	}

//	@getter("generation_algo")
//	public String getGenerationAlgorithm() {
//		return generationAlgorithm;
//	}

}

