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

import java.util.concurrent.ConcurrentHashMap;

import net.uiqui.woody.actor.Actor;
import net.uiqui.woody.error.NoPusherError;

public class Broker {
	private static final ConcurrentHashMap<Endpoint, Pusher<Object>> endpoints = new ConcurrentHashMap<Endpoint, Pusher<Object>>();
	private static final ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<String, Topic>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void register(final Endpoint endpoint, final Pusher pusher) {
		endpoints.put(endpoint, pusher);
	}

	public static void unregister(final Endpoint endpoint) {
		endpoints.remove(endpoint);
	}

	public static boolean isRegisted(final Endpoint endpoint) {
		return endpoints.containsKey(endpoint);
	}

	public static void register(final Topic topic) {
		topics.put(topic.getName(), topic);
	}
	
	public static void unregister(final Topic topic) {
		topics.remove(topic.getName());
		unregister(topic.endpoint());
	}
	
	public static Topic getTopic(String topicName) {
		Topic topic = topics.get(topicName);
		
		if (topic == null) {
			topic = createTopic(topicName);
		}
		
		return topic;
	}

	private static synchronized Topic createTopic(String topicName) {
		Topic topic = topics.get(topicName);
		
		if (topic != null) {
			return topic;
		}
		
		return new Topic(topicName);
	}
	
	@SuppressWarnings("rawtypes")
	public static void send(final Actor actor, final Object object) {
		send(actor.endpoint(), object);
	}	
	
	public static void send(final Topic topic, final Object object) {
		send(topic.endpoint(), object);
	}		
	
	public static void sendToTopic(final String topicName, final Object object) {
		Topic topic = getTopic(topicName);
		send(topic.endpoint(), object);
	}		

	public static void send(final Endpoint endpoint, final Object object) {
		final Pusher<Object> pusher = endpoints.get(endpoint);

		if (pusher != null) {
			pusher.push(object);
		} else {
			throw new NoPusherError(endpoint);
		}
	}
}
