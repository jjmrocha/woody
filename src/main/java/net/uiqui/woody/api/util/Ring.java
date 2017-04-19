/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2017 Joaquim Rocha <jrocha@gmailbox.org>
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.uiqui.woody.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Ring<T> {
	private final List<T> storage = new ArrayList<T>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Index index = new Index();
	
	public boolean add(final T element) {
		lock.writeLock().lock();
		
		try {
			return storage.add(element);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public boolean remove(final T element) {
		lock.writeLock().lock();
		
		try {
			return storage.remove(element);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public T get() {
		lock.readLock().lock();
		
		try {
			final int size = storage.size();
			
			if (size == 0) {
				return null;
			}

			return storage.get(index.next(size));
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private static class Index {
		private int value = 0;
		
		public synchronized int next(final int max) {
			if (value >= max) {
				value = 0;
			}
			
			return value++;
		}
	}
}
