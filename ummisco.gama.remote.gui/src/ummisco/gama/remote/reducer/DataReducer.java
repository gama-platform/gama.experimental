package ummisco.gama.remote.reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import msi.gama.util.GamaList;
import msi.gama.util.GamaMap;

public class DataReducer {
	public static <T> ArrayList<T> castToList(final GamaList<T> mList) {
		return new ArrayList<T>(mList);
	}

	public static <K, V> Map<? extends K, ? extends V> castToMap(final GamaMap<K, V> mMap) {
		final Map<K, V> res = new HashMap<K, V>();
		mMap.forEach((k, v) -> res.put(k, v));
		return res;
	}
}
