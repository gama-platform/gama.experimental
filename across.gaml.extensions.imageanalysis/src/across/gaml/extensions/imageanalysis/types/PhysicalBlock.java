package across.gaml.extensions.imageanalysis.types;

import msi.gama.common.interfaces.IValue;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@vars({ @variable(name = "type", type = PatternBlockType.id), 
		@variable(name = "shape", type = IType.GEOMETRY)})
public class PhysicalBlock implements IValue {
	private PatternBlock pattern;
	private IShape shape;

	public PhysicalBlock() {
	}
	

	public PhysicalBlock(IScope scope, String typeName, int cols, int rows, IExpression init, IShape geom) {
		pattern = new PatternBlock(scope, typeName, cols, rows, init);
		shape = geom;
	}

		
	
	@getter ("type")
	public PatternBlock getPattern() {
		return pattern;
	}




	public void setPattern(PatternBlock pattern) {
		this.pattern = pattern;
	}



	@getter ("shape")
	public IShape getShape() {
		return shape;
	}


	public void setShape(IShape shape) {
		this.shape = shape;
	}


	@Override
	public IType<?> getGamlType() {
		return Types.get(PhysicalBlockType.id);
	}
	
	
	@Override
	public String toString() {
		return serialize(true);
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return (pattern == null ? "": pattern.serialize(includingBuiltIn)) + ":" + (shape == null ? "" : shape.serialize(includingBuiltIn)) ;
	}
	
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return (pattern == null ? "": pattern.stringValue(scope)) + ":" + (shape == null ? "" : shape.stringValue(scope));
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		PhysicalBlock p = new PhysicalBlock();
		p.setPattern((PatternBlock) pattern.copy(scope));
		p.setShape(shape.copy(scope));
		return p;
	}

	
}
