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

public class Ring<T> {
	private Node<T> pointer = null;
	private Node<T> first = null;
	private Node<T> last = null;

	public synchronized void add(final T value) {
		if (pointer == null) {
			pointer = new Node<T>(value, null);
			pointer.next = pointer;
			first = pointer;
			last = pointer;
		} else {
			final Node<T> previousLast = last;
			last = new Node<T>(value, first);
			previousLast.next = last;
		}
	}

	public synchronized T next() {
		if (pointer == null) {
			return null;
		}

		pointer = pointer.next;
		return pointer.value;
	}

	private static class Node<E> {
		public final E value;
		public Node<E> next;

		public Node(final E value, final Node<E> next) {
			this.value = value;
			this.next = next;
		}
	}
}