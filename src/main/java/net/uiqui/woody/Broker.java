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

import net.uiqui.woody.annotations.EventSubscription;
import net.uiqui.woody.annotations.MessageHandler;
import net.uiqui.woody.api.ActorMailbox;
import net.uiqui.woody.api.AlreadyRegisteredException;
import net.uiqui.woody.api.Exchange;
import net.uiqui.woody.api.InvalidActorException;
import net.uiqui.woody.api.NotRegisteredError;
import net.uiqui.woody.api.WoodyException;
import net.uiqui.woody.util.ReferenceFactory;

public class Broker {
	private static final ConcurrentHashMap<String, ActorMailbox> actors = new ConcurrentHashMap<String, ActorMailbox>();
	private static final ConcurrentHashMap<Class<?>, Exchange> exchanges = new ConcurrentHashMap<Class<?>, Exchange>();
	
	public static String register(final Object actor) throws WoodyException {
		final String name = ReferenceFactory.get(); 
		register(name, actor);
		return name;
	}
	
	public static void register(final String name, final Object actor) throws WoodyException {
		if (isValidActor(actor)) {
			if (!isRegisted(name)) {
				actors.putIfAbsent(name, new ActorMailbox(actor));
				handleSubscriptions(name, actor);
			} else {
				throw new AlreadyRegisteredException("The actor " + name + " is already registed");
			}
		} else {
			throw new InvalidActorException("Class " + actor.getClass().getName() + " is not a valid actor");
		}
	}

	public static void unregister(final String name) {
		final ActorMailbox mailbox = actors.remove(name);

		if (mailbox != null) {
			mailbox.close();
		}
	}

	public static boolean isRegisted(final String name) {
		return actors.containsKey(name);
	}
	
	public static void send(final String name, final Object msg) {
		final ActorMailbox mailbox = actors.get(name);
		
		if (mailbox != null) {
			mailbox.send(msg);
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
