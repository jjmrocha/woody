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
package net.uiqui.woody;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import net.uiqui.woody.annotations.Actor;
import net.uiqui.woody.annotations.EventSubscription;
import net.uiqui.woody.annotations.MessageHandler;
import net.uiqui.woody.api.AlreadyRegisteredException;
import net.uiqui.woody.api.Exchange;
import net.uiqui.woody.api.InvalidActorException;
import net.uiqui.woody.api.ActorQueue;
import net.uiqui.woody.api.NotRegisteredError;
import net.uiqui.woody.api.WoodyException;
import net.uiqui.woody.util.ReferenceFactory;

public class Broker {
	private static final ConcurrentHashMap<String, ActorQueue> actors = new ConcurrentHashMap<String, ActorQueue>();
	private static final ConcurrentHashMap<Class<?>, Exchange> exchanges = new ConcurrentHashMap<Class<?>, Exchange>();
	
	public static String register(final Object actor) throws WoodyException {
		if (isValidActor(actor)) {
			final String name = getActorName(actor);
			
			if (!isRegisted(name)) {
				actors.putIfAbsent(name, new ActorQueue(actor));
				handleSubscriptions(name, actor);
				
				return name;
			} else {
				throw new AlreadyRegisteredException("The actor " + name + " is already registed");
			}
		} else {
			throw new InvalidActorException("Class " + actor.getClass().getName() + " is not a valid actor");
		}
	}

	public static void unregister(final String name) {
		final ActorQueue listenerQueue = actors.remove(name);

		if (listenerQueue != null) {
			listenerQueue.stop();
		}
	}

	public static boolean isRegisted(final String name) {
		return actors.containsKey(name);
	}
	
	public static void send(final String name, final Object msg) {
		final ActorQueue queue = actors.get(name);
		
		if (queue != null) {
			queue.push(msg);
		} else {
			throw new NotRegisteredError(name);
		}
	}
	
	public static void publish(final Object event) {
		final Exchange exchange = exchanges.get(event.getClass());
		
		if (exchange != null) {
			exchange.route(event);
		}
	}	

	private static String getActorName(final Object actor) {
		final Actor actorAnnotation = actor.getClass().getAnnotation(Actor.class);
		
		if (actorAnnotation != null && actorAnnotation.value() != null) {
			return actorAnnotation.value();
		}
		
		return ReferenceFactory.get();
	}

	private static boolean isValidActor(final Object actor) {		
		for (Method method : actor.getClass().getMethods()) {
			final MessageHandler handler = method.getAnnotation(MessageHandler.class);
			final EventSubscription subscription = method.getAnnotation(EventSubscription.class);
			
			if ((handler != null || subscription != null) && method.getParameterTypes().length == 1) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void handleSubscriptions(final String name, final Object actor) {
		for (Method method : actor.getClass().getMethods()) {
			final EventSubscription subscription = method.getAnnotation(EventSubscription.class);
			
			if (subscription != null && method.getParameterTypes().length == 1) {
				subscribe(method.getParameterTypes()[0], name);
			}
		}
	}

	private static void subscribe(final Class<?> eventType, final String name) {
		final Exchange exchange = exchanges.putIfAbsent(eventType, new Exchange(name));
		
		if (exchange != null) {
			exchange.bind(name);
		}
	}	
}
