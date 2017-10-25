package main.match;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author Renas
 *
 */
public class AnnotationSet implements Collection<AnnotationCandidate>{
	private Vector<AnnotationCandidate> list;
	private HashSet<AnnotationCandidate> set;

	
	public AnnotationSet() {
		list = new Vector<AnnotationCandidate>();
		set = new HashSet<AnnotationCandidate>();
	}
	
	@Override
	public boolean add(AnnotationCandidate a) {
		if(set.add(a)) {
			list.add(a);
			return true;
		}
		else {
			AnnotationCandidate b = list.get(list.indexOf(a));
			if(a.compareTo(b) > 0) {
				b.setWeight(a.getWeight());
			}
			return false;
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends AnnotationCandidate> c) {
		boolean check = false;
		for (AnnotationCandidate a : c) {
			check = add(a) || check;
		}
		return check;
	}
	
	public void sort() {
		Collections.sort(list, new Comparator<AnnotationCandidate>() {
			public int compare(AnnotationCandidate a1, AnnotationCandidate a2) {
				return a2.compareTo(a1);
			}
		});
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<AnnotationCandidate> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		if(set.remove(o)) {
			list.remove(o);
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean check = false;
		for(Object a : c) {
			check = remove(a) || check;
		}
		return check;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		list.retainAll(c);
		return set.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
		set.clear();
	}
	
	public Vector<AnnotationCandidate> getList() {
		return list;
	}
	
	
	/**
	 * Converts a list into a JSON string
	 * @param list: list equal to the AnnotationSet that we will use to convert it to a JSOn String
	 * @return a JSON string after converting it from a list
	 */
	public String createJSON(Vector<AnnotationCandidate> list) {
		String json = new Gson().toJson(list);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(json);
		String prettyJsonString = gson.toJson(je);
		return prettyJsonString;
	}
}
