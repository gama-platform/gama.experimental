package gama.experimental.imageanalysis.types;

import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

@vars({ @variable(name = "type", type = PatternBlockType.id), 
		@variable(name = "shape", type = IType.GEOMETRY)})
public class PhysicalBlock implements IValue {
	private PatternBlock pattern;
	private IShape shape;

	public PhysicalBlock() {
	}
	

	public PhysicalBlock(IScope scope, String typeName, int cols, int rows, IExpression init, IShape geom, boolean parallel) {
		pattern = new PatternBlock(scope, typeName, cols, rows, init, parallel);
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

	public String serialize(final boolean includingBuiltIn) {
		return (pattern == null ? "": pattern.serialize(includingBuiltIn)) + ":" + (shape == null ? "" : shape.serialize(includingBuiltIn)) ;
	}

	@Override
	public JsonValue serializeToJson(Json json) {
		return (pattern == null ? null: pattern.serializeToJson(json)) + ":" + (shape == null ? null : shape.serializeToJson(json)) ;
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
