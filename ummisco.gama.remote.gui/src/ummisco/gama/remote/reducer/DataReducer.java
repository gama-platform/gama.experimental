package ummisco.gama.remote.reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import msi.gama.util.IList;
import msi.gama.util.IMap;

public class DataReducer {
	public static <T> ArrayList<T> castToList(final IList<T> mList) {
		return new ArrayList<>(mList);
	}

	public static <K, V> Map<? extends K, ? extends V> castToMap(final IMap<K, V> mMap) {
		final Map<K, V> res = new HashMap<>();
		mMap.forEach((k, v) -> res.put(k, v));
		return res;
	}
}
