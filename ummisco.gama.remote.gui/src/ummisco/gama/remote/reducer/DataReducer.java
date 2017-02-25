package ummisco.gama.remote.reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import msi.gama.util.GamaList;
import msi.gama.util.GamaMap;

public class DataReducer {
	public static <T> ArrayList<T> castToList(GamaList<T> mList)
	{
		return  new ArrayList<T>(mList);
	}
	public static <K,V> Map<K,V> castToMap(GamaMap< K, V> mMap)
	{
		Map<K,V> res =  new HashMap<K,V>();
		res.putAll((Map<K,V>)mMap);
		return res;
	}
}
