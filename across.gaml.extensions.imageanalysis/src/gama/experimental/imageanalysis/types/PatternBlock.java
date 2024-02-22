package gama.experimental.imageanalysis.types;

import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.core.util.matrix.GamaIntMatrix;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaMatrixType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@vars({ @variable(name = "id", type = IType.STRING), 
		@variable(name = "val", type = IType.MATRIX)})
public class PatternBlock implements IValue {
	private String id;
	private GamaIntMatrix matrix;	

	public PatternBlock(String typeName) {
		id = typeName;
	}
	public PatternBlock(IScope scope, String typeName, int cols, int rows, IExpression init, boolean parallel) {
		id = typeName;
		matrix = (GamaIntMatrix) GamaMatrixType.with(scope, init, cols, rows, parallel);
	}

	@getter ("id")
	public String getType() {
		return id;
	}
	
	
	public void setType(String type) {
		this.id = type;
	}
	
	public void setVal(GamaIntMatrix arr) {
		matrix = arr;
	}
	
	
	@getter ("val")
	public GamaIntMatrix getMatrix() {
		return matrix;
	}


	public void setMatrix(GamaIntMatrix matrix) {
		this.matrix = matrix;
	}



	@Override
	public IType<?> getGamlType() {
		return Types.get(PatternBlockType.id);
	}
	
	
	@Override
	public String toString() {
		return serialize(true);
	}

	public String serialize(final boolean includingBuiltIn) {
		return null;
//		return id + ":"+matrix.serialize(includingBuiltIn);
	}
	
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return id + ":"+matrix.stringValue(scope);
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		PatternBlock p = new PatternBlock(this.id);
		p.setMatrix((GamaIntMatrix) getMatrix().copy(scope));
		return p;
	}
	@Override
	public JsonValue serializeToJson(Json json) {
		// TODO Auto-generated method stub
		return matrix.serializeToJson(json);
	}

	
}
