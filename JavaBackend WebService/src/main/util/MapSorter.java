package main.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapSorter
{
	
//Constructor
	
	private MapSorter(){}
	
	
	/**
	 * Sorts a Map by values, in descending order
	 * @param map: the map to sort
	 * @return the sorted (Linked)Map
	 */
    public static <K,V extends Comparable<V>> Map<K,V> sortDescending(Map<K,V> map)
    {
        List<Map.Entry<K,V>> list = new LinkedList<Map.Entry<K,V>>(map.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<K,V>>()
        {
            public int compare(Map.Entry<K,V> o1,Map.Entry<K,V> o2)
            {
                return -o1.getValue().compareTo(o2.getValue());
            }
        } );

        Map<K,V> result = new LinkedHashMap<K,V>();
        for (Map.Entry<K,V> entry : list)
        {
            result.put(entry.getKey(),entry.getValue());
        }
        return result;
    }
}
