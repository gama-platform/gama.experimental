/*******************************************************************************************************
 *
 * GamaDeque.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.util.deque;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import msi.gama.common.util.StringUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.IMap;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.types.GamaIntegerType;
import msi.gaml.types.GamaMatrixType;
import msi.gaml.types.IContainerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * Deque used for Queue and Stack types
 * 
 * @author Jean-François Erdelyi
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GamaDeque<T> extends ArrayDeque<T> implements IContainer<Integer, Object> {

	// ############################################
	// Attributs

	/**
	 * The serializable class does not declare a static final serialVersionUID field
	 * of type long
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * GAMA type
	 */
	protected IContainerType<?> type;

	// ############################################
	// Constructors

	/**
	 * Default constructor
	 */
	public GamaDeque(IContainerType<?> contentsType) {
		super();
		type = contentsType;
	}

	/**
	 * Constructor with values and type (List)
	 */
	public GamaDeque(IContainerType<?> contentsType, List<T> values) {
		super();
		type = contentsType;
		addAll(values);
	}

	/**
	 * Constructor with values and type (array)
	 */
	public GamaDeque(IContainerType<?> contentsType, T[] values) {
		super();
		type = contentsType;
		addAll(values);
	}

	/**
	 * Constructor with values and type (Collection)
	 */
	public GamaDeque(IContainerType<?> contentsType, Collection<T> values) {
		super();
		type = contentsType;
		addAll(values);
	}

	/**
	 * Copy constructor
	 */
	public GamaDeque(GamaDeque<T> gq) {
		super(gq.clone());
		type = gq.getGamlType();
	}

	// ############################################
	// Methods

	/**
	 * Add all values (List)
	 */
	private void addAll(List<T> values) {
		for (T v : values) {
			add(v);
		}
	}

	/**
	 * Add all values (array)
	 */
	private void addAll(T[] values) {
		for (T v : values) {
			add(v);
		}
	}

	/**
	 * Build value
	 */
	public T buildValue(final IScope scope, final Object object) {
		final IType<?> ct = getGamlType().getContentType();
		return (T) ct.cast(scope, object, null, false);
	}

	/**
	 * Build values
	 */
	public GamaDeque<T> buildValues(final IScope scope, final IContainer<?, ?> objects) {
		return (GamaDeque<T>) getGamlType().cast(scope, objects, null, false);
	}

	/**
	 * Build index
	 */
	public Integer buildIndex(final IScope scope, final Object object) {
		return GamaIntegerType.staticCast(scope, object, null, false);
	}

	// ############################################
	// Override: methods

	/**
	 * To string
	 */
	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return serialize(false);
	}

	/**
	 * Serialization (like a list)
	 */
	@Override
	public String serialize(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder(size() * 10);
		Object[] values = toArray();

		sb.append('[');
		for (int i = 0; i < size(); i++) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append(StringUtils.toGaml(values[i], includingBuiltIn));
		}
		sb.append(']');

		return sb.toString();
	}

	/**
	 * Clone data
	 */
	@Override
	public GamaDeque<T> copy(IScope scope) throws GamaRuntimeException {
		return new GamaDeque<T>(this);
	}

	/**
	 * Get GAMA type
	 */
	@Override
	public IContainerType<?> getGamlType() {
		return type;
	}

	/**
	 * Return true if the value exists in the collection
	 */
	@Override
	public boolean contains(IScope scope, Object o) throws GamaRuntimeException {
		return contains(o);
	}

	/**
	 * Check if the "key" exists (index here)
	 */
	@Override
	public boolean containsKey(IScope scope, Object o) throws GamaRuntimeException {
		if (o instanceof Integer) {
			final Integer i = (Integer) o;
			return i >= 0 && i < this.size();
		}
		return false;
	}

	/**
	 * Get first value
	 */
	@Override
	public T firstValue(IScope scope) throws GamaRuntimeException {
		return getFirst();
	}

	/**
	 * Get last value
	 */
	@Override
	public T lastValue(IScope scope) throws GamaRuntimeException {
		return getLast();
	}

	/**
	 * Get random value
	 */
	@Override
	public T anyValue(IScope scope) {
		final int i = scope.getRandom().between(0, 1);
		return i == 0 ? getFirst() : getLast();
	}

	/**
	 * Get the number of values
	 */
	@Override
	public int length(IScope scope) {
		return size();
	}

	/**
	 * True if the collection is empty
	 */
	@Override
	public boolean isEmpty(IScope scope) {
		return isEmpty();
	}

	// ############################################
	// Override: methods about values

	/**
	 * Get the list of values (is "equivalent" to toArray())
	 */
	@Override
	public IList<Object> listValue(IScope scope, IType contentType, boolean copy) {
		// return GamaListFactory.wrap(contentType, toArray());
		ArrayList<? extends T> list = (ArrayList<? extends T>) Arrays.asList(toArray());
		return (IList<Object>) GamaListFactory.wrap(contentType, list);
	}

	/**
	 * Return iterable collection
	 */
	@Override
	public Iterable<? extends T> iterable(IScope scope) {
		return (Iterable<? extends T>) listValue(scope, Types.NO_TYPE, false);
	}

	/**
	 * Reverse the collection: useless ?
	 */
	@Override
	public IContainer<?, ?> reverse(IScope scope) throws GamaRuntimeException {
		ArrayList<T> list = (ArrayList<T>) Arrays.asList(toArray());
		Collections.reverse(list);
		return new GamaDeque(type, list);
	}

	/**
	 * Return a map from values: deque does not allow this
	 */
	@Override
	public <D, C> IMap<C, D> mapValue(IScope scope, IType<C> keyType, IType<D> contentType, boolean copy) {
		final IMap result = GamaMapFactory.create(keyType, contentType);
		result.setAllValues(scope, toArray());
		return result;
	}

	/**
	 * Return matrix from values
	 */
	@Override
	public IMatrix<?> matrixValue(IScope scope, IType<?> contentType, boolean copy) {
		return matrixValue(scope, contentType, null, copy);
	}

	/**
	 * Return matrix from values with prefered size
	 */
	@Override
	public IMatrix<?> matrixValue(IScope scope, IType<?> contentType, GamaPoint size, boolean copy) {
		return GamaMatrixType.from(scope, listValue(scope, contentType, copy), contentType, size);

	}
}
