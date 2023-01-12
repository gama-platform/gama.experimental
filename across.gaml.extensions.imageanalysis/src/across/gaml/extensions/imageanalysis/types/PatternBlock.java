package across.gaml.extensions.imageanalysis.types;

import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.GamaMatrixType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

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
		matrix = (GamaIntMatrix) GamaMatrixType.with(scope, init, cols, rows);
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

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return id + ":"+matrix.serialize(includingBuiltIn);
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

	
}
