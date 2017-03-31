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

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.uiqui.woody.Broker;
import net.uiqui.woody.api.error.NotRegisteredError;

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
	
	public void route(final Object msg) {
		for (String name: subscribers) {
			try {
				Broker.send(name, msg);
			} catch (NotRegisteredError e) {
				subscribers.remove(name);
			}
		}
	}
}
