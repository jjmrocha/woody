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
package net.uiqui.woody.api;

import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;

public class Exchange {
	private final Deque<String> subscribers = new ConcurrentLinkedDeque<String>();

	public Exchange(final String name) {
		bind(name);
	}

	public void bind(final String name) {
		if (!subscribers.contains(name)) {
			subscribers.add(name);
		}
	}
	
	public void unbind(final String name) {
		subscribers.remove(name);
	}	

	public void route(final Serializable msg) {
		for (final String name : subscribers) {
			final ActorRef actorRef = Woody.getActorRef(name);

			if (actorRef != null) {
				actorRef.cast(msg);
			} else {
				unbind(name);
			}
		}
	}

	public boolean isEmpty() {
		return subscribers.isEmpty();
	}
}
