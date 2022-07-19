package across.gaml.extensions.imageanalysis.operators;

import msi.gama.common.util.RandomUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.GamaMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.types.IType;
import one.util.streamex.StreamEx;

@vars({ @variable(name = "type", type = IType.STRING), 
		@variable(name = "val", type = IType.MATRIX)})
public class Lego extends GamaMatrix  {

	protected Lego(int cols, int rows, IType contentsType) {
		super(cols, rows, contentsType);
		// TODO Auto-generated constructor stub
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
		type = type;
	}
	
	public void setVal(GamaIntMatrix arr) {
		val = arr;
	}

	@Override
	public double[] getFieldData(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(IScope scope, int col, int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(IScope scope, int col, int row, Object obj) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StreamEx stream(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shuffleWith(RandomUtils randomAgent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IMatrix copy(IScope scope, GamaPoint preferredSize, boolean copy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable iterable(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getNthElement(Integer index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(IScope scope, int col, int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setNthElement(IScope scope, int index, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IList _listValue(IScope scope, IType contentsType, boolean cast) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IMatrix _matrixValue(IScope scope, GamaPoint size, IType type, boolean copy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void _clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean _removeFirst(IScope scope, Object value) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean _removeAll(IScope scope, IContainer value) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void _putAll(IScope scope, Object value) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected IMatrix _reverse(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean _isEmpty(IScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean _contains(IScope scope, Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Integer _length(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object _last(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object _first(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}
}
