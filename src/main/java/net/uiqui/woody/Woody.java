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

import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.api.ActorFacade;
import net.uiqui.woody.api.ActorMailbox;
import net.uiqui.woody.api.Event;
import net.uiqui.woody.api.Exchange;
import net.uiqui.woody.api.error.AlreadyRegisteredException;
import net.uiqui.woody.api.error.InvalidActorException;
import net.uiqui.woody.api.error.WoodyException;
import net.uiqui.woody.lib.NameFactory;
import net.uiqui.woody.lib.Runner;

/**
 * The Class Woody is responsible for the main features, mainly: Actor and topic registration/subscription and
 * support for sending messages, publish events and perform rpc calls 
 */
public class Woody {
	private static final ConcurrentHashMap<String, ActorRef> mailboxes = new ConcurrentHashMap<String, ActorRef>();
	private static final ConcurrentHashMap<String, Exchange> topics = new ConcurrentHashMap<String, Exchange>();

	/**
	 * Register one object as an actor
	 * The object's class must extend Actor class or use one the CastHandler or Subscription 
	 * annotation on one of its methods
	 *
	 * @param actor the actor instance
	 * @return the reference for the actor
	 * @throws WoodyException thrown when an error occurred during actor's registering 
	 */
	public static ActorRef register(final Object actor) throws WoodyException {
		final String name = NameFactory.get();
		return register(name, actor);
	}

	/**
	 * Register one object as an actor
	 * The object's class must extend Actor class or use one the CastHandler or Subscription 
	 * annotation on one of its methods
	 *
	 * @param name the actor's name
	 * @param actor the actor instance
	 * @return the reference for the actor
	 * @throws WoodyException thrown when an error occurred during actor's registering 
	 */
	public static ActorRef register(final String name, final Object actor) throws WoodyException {
		if (isValidActor(actor)) {
			if (!isRegistered(name)) {
				final ActorFacade wrapper = new ActorFacade(actor);
				final ActorRef actorRef = new ActorMailbox(name, wrapper);
				mailboxes.putIfAbsent(name, actorRef);
				registerSubscriptions(name, actor);
				
				return actorRef;
			} else {
				throw new AlreadyRegisteredException("The actor " + name + " is already registed");
			}
		} else {
			throw new InvalidActorException("Class " + actor.getClass().getName() + " is not a valid actor");
		}
	}

	/**
	 * Unregister the actor
	 *
	 * @param name the actor's name
	 */
	public static void unregister(final String name) {
		mailboxes.remove(name);
	}

	/**
	 * Checks if exists an actor registered with the name
	 *
	 * @param name the name to check
	 * @return true, if is name is registered
	 */
	public static boolean isRegistered(final String name) {
		return mailboxes.containsKey(name);
	}
	
	/**
	 * Return a reference for the actor
	 *
	 * @param name the name of the actor
	 * @return the actor's reference
	 */
	public static ActorRef getActorRef(final String name) {
		return mailboxes.get(name);
	}

	/**
	 * Publish a event to a topic,
	 * all actors subscribing the topic will receive the message
	 * (an actor subscribes a topic, by annotating a method with the Subscription annotation)  
	 *
	 * @param topic the topic's name
	 * @param payload the event to deliver to all subscribers
	 */
	public static void publish(final String topic, final Object payload) {
		final Exchange exchange = topics.get(topic);

		if (exchange != null) {
			Runner.queue(new Runnable() {
				@Override
				public void run() {
					exchange.route(new Event(topic, payload));
				}
			});
		}
	}

	private static boolean isValidActor(final Object actor) {
		for (Method method : actor.getClass().getMethods()) {
			if (method.getParameterTypes().length == 1) {
				final CastHandler cast = method.getAnnotation(CastHandler.class);
				
				if (cast != null) {
					return true;
				}
				
				final Subscription subscription = method.getAnnotation(Subscription.class);
				
				if (subscription != null && subscription.value() != null) {
					return true;
				}		
				
				final CallHandler call = method.getAnnotation(CallHandler.class);
				
				if (call != null && call.value() != null && method.getReturnType() != Void.class) {
					return true;
				}
			}
		}

		return false;
	}

	private static void registerSubscriptions(final String name, final Object actor) {
		for (Method method : actor.getClass().getMethods()) {
			final Subscription subscription = method.getAnnotation(Subscription.class);

			if (subscription != null && subscription.value() != null && method.getParameterTypes().length == 1) {
				subscribe(subscription.value(), name);
			}
		}
	}

	private static void subscribe(final String topic, final String subscriber) {
		final Exchange exchange = topics.putIfAbsent(topic, new Exchange(subscriber));

		if (exchange != null) {
			exchange.bind(subscriber);
		}
	}
}
