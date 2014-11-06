/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2014 Joaquim Rocha <jrocha@gmailbox.org>
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
package net.uiqui.woody.util;

public class Ring<E> {
	private Node<E> current = null;
	
	public synchronized void add(final E value) {
		if (current == null) {
			current = new Node<E>(value, null);
			current.next = current;
		} else {
			Node<E> next = current.next;
			current.next = new Node<E>(value, next);
		}
	}
	
	public synchronized E get() {
		E value = current.value;
		current = current.next;
		return value;
	}

	private class Node<Type> {
		public final Type value;
		public Node<Type> next;

		public Node(final Type value, final Node<Type> next) {
			this.value = value;
			this.next = next;
		}
	}
}
