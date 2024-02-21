package gama.experimental.webcam.types;

import java.awt.Dimension;

import com.github.sarxos.webcam.Webcam;

import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@vars({ @variable(name = "id", type = IType.INT),@variable(name = "resolutions", type = IType.LIST) })
public class GamaWebcam implements IValue {

	private WebcamCustom webcam;
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
	
	public WebcamCustom getWebcam() {
		return webcam;
	}

	public void setWebcam(WebcamCustom webcam) {
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

	@Override
	public JsonValue serializeToJson(Json json) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
