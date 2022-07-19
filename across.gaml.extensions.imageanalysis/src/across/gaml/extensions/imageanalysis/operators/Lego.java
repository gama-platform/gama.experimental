package across.gaml.extensions.imageanalysis.operators;

import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.GamaMatrixType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars({ @variable(name = "type", type = IType.STRING), 
		@variable(name = "val", type = IType.MATRIX)})
public class Lego implements IValue {

	protected Lego(int cols, int rows) {
		val = new GamaIntMatrix(cols,rows);
	}

	public Lego(IScope scope, String typeName, int x, int y, IExpression init) {
		type = typeName;
		val = (GamaIntMatrix) GamaMatrixType.with(scope, init, x, y);
	}

	private String type;
	private GamaIntMatrix val;
	
	public String getType() {
		return type;
	}
	
	public GamaIntMatrix getVal(){
		return val;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setVal(GamaIntMatrix arr) {
		val = arr;
	}
	
	@Override
	public IType<?> getGamlType() {
		return Types.get(LegoType.id);
	}
	
	
	@Override
	public String toString() {
		return serialize(true);
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return type + ":"+val.serialize(includingBuiltIn);
	}
	
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return type + ":"+val.stringValue(scope);
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
