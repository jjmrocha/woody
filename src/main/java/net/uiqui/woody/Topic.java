/*
 * Woody - core
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
package net.uiqui.woody;

import java.util.ArrayList;
import java.util.List;

import net.uiqui.woody.actor.Actor;
import net.uiqui.woody.error.NoPusherError;
import net.uiqui.woody.listener.Listener;
import net.uiqui.woody.listener.ListenerPusher;

public class Topic implements Listener<Object> {
	private final List<Endpoint> subscribers = new ArrayList<Endpoint>();
	private final ListenerPusher<Object> queue = new ListenerPusher<Object>(this);
	
	private Endpoint endpoint = null;
	private String name = null;
	
	public Topic(final String topicName) {
		this.name = topicName;

		endpoint = Endpoint.getEndpointForTopic(topicName);
		Broker.register(this);
		Broker.register(endpoint, queue);
	}
	
	public String getName() {
		return name;
	}
	
	public Endpoint endpoint() {
		return endpoint;
	}
	
	public void close() {
		Broker.unregister(this);
		queue.stop();
	}	

	@SuppressWarnings("rawtypes")
	public void subscribe(final Actor actor) {
		subscribe(actor.endpoint());
	}
	
	public void subscribe(final Topic topic) {
		subscribe(topic.endpoint());
	}
	
	public void subscribe(final Endpoint endpoint) {
		subscribers.add(endpoint);
	}	

	public void unsubscribe(final Endpoint endpoint) {
		subscribers.remove(endpoint);
	}

	public void onMessage(Object msg) {
		List<Endpoint> errors = new ArrayList<Endpoint>();
		
		for (Endpoint endpoint: subscribers) {
			try {
				Broker.send(endpoint, msg);
			} catch (NoPusherError e) {
				errors.add(endpoint);
			}
		}
		
		if (!errors.isEmpty()) {
			for (Endpoint endpoint: errors) {
				unsubscribe(endpoint);
			}
		}
	}
}
