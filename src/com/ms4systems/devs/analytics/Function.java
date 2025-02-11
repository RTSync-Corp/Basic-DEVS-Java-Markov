package com.ms4systems.devs.analytics;

import java.io.File;
import java.io.Serializable;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;

// Custom library code
//ID:LIB:0
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

//ENDID
// End custom library code
@SuppressWarnings("unused")
public class Function {

	protected Hashtable h;
	protected int size;
	protected String name;

	public Function() {
		this("Function");
	}

	public Function(String name) {
		this.name = name;
	}

	public Function(boolean b) {
		h = new Hashtable();
		size = 0;
	}

	public Function(String nameOfFunction, boolean b) {
		name = nameOfFunction;
		h = new Hashtable();
		size = 0;
	}

	public Function(Hashtable hs) {
		h = hs;
		size = hs.size();
	}

	public Hashtable<String, Double> getHashtable() {
		return h;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public Object get(Object element) {
		return h.get(element);
	}

	public synchronized void put(Object key, Object val) {
		h.put(key, val);
	}

	public HashSet<String> keySet() {
		return new HashSet(h.keySet());
	}

	public HashSet valueSet() {
		return new HashSet(h.values());
	}

	public String toString() {
		return name + " " + h.toString();
	}

	public void print() {
		System.out.println(toString());
	}

	// public synchronized boolean equals(Object o) {
	// if (o == this)
	// return true;
	// Class cl = getClass();
	// if (!cl.isInstance (o))return false;
	// Relation t = (Relation) o;
	// if (t.size() != size())
	// return false;
	//
	// Set keyset = keySet();
	// Set tset = t.keySet();
	// if (!keyset.equals(tset))return false;
	//
	// Iterator it = keyset.iterator();
	// while (it.hasNext()) {
	// Object key = it.next();
	// Set valueset = getSet(key);
	// Set tvalueset = t.getSet(key);
	// if (!valueset.equals(tvalueset))
	// return false;
	// }
	// return true;
	// }

	// End custom function definitions
	public static void main(String[] args) {

	}
}
