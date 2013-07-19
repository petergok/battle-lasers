package com.pianist.battlelasers;

import java.util.ArrayList;
import java.util.List;

/**
 * The Pool Object describes a pool of elements that can easily be accessed
 * 
 * @author Apress Beginning Android Games 
 * @version June, 2011
 */
public class Pool <T> {
	public interface PoolObjectFactory<T> {
		public T createObject();
	}

	private final List<T> freeObjects;
	private final PoolObjectFactory<T> factory;
	private final int maxSize;

	public Pool(PoolObjectFactory<T> factory, int maxSize) {
		this.factory = factory;
		this.maxSize = maxSize;
		this.freeObjects = new ArrayList<T>(maxSize);
	}

	public T newObject() {
		T object = null;
		
		if (freeObjects.isEmpty())
			object = factory.createObject();
		else
			object = freeObjects.remove(freeObjects.size() - 1);
		
		return object;
	}
	
	public void free(T object) {
		if (freeObjects.size() < maxSize)
			freeObjects.add(object);
	}
}
