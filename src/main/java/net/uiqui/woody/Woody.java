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
import java.util.concurrent.Future;

import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.api.ActorFacade;
import net.uiqui.woody.api.ActorMailbox;
import net.uiqui.woody.api.CallRequest;
import net.uiqui.woody.api.Event;
import net.uiqui.woody.api.Exchange;
import net.uiqui.woody.api.Mailbox;
import net.uiqui.woody.api.error.AlreadyRegisteredException;
import net.uiqui.woody.api.error.InvalidActorException;
import net.uiqui.woody.api.error.NotRegisteredError;
import net.uiqui.woody.api.error.WoodyException;
import net.uiqui.woody.lib.NameFactory;

/**
 * The Class Woody is responsible for the main features, mainly: Actor and topic registration/subscription and
 * support for sending messages, publish events and perform rpc calls 
 */
public class Woody {
	private static final ConcurrentHashMap<String, Mailbox> mailboxes = new ConcurrentHashMap<String, Mailbox>();
	private static final ConcurrentHashMap<String, Exchange> topics = new ConcurrentHashMap<String, Exchange>();

	/**
	 * Register one object as an actor
	 * The object's class must extend Actor class or use one the CastHandler or Subscription 
	 * annotation on one of its methods
	 *
	 * @param actor the actor instance
	 * @return the actor's name
	 * @throws WoodyException thrown when an error occurred during actor's registering 
	 */
	public static String register(final Object actor) throws WoodyException {
		final String name = NameFactory.get();
		register(name, actor);
		return name;
	}

	/**
	 * Register one object as an actor
	 * The object's class must extend Actor class or use one the CastHandler or Subscription 
	 * annotation on one of its methods
	 *
	 * @param name the actor's name
	 * @param actor the actor instance
	 * @throws WoodyException thrown when an error occurred during actor's registering 
	 */
	public static void register(final String name, final Object actor) throws WoodyException {
		if (isValidActor(actor)) {
			if (!isRegistered(name)) {
				final ActorFacade wrapper = new ActorFacade(actor);
				final Mailbox mailbox = new ActorMailbox(wrapper);
				mailboxes.putIfAbsent(name, mailbox);
				registerSubscriptions(name, actor);
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
	 * Send a message asynchronously to an actor, 
	 * the message will be delivered to a method marked with
	 * the CastHandler annotation  
	 *
	 * @param name the actor's name
	 * @param msg Message to send asynchronously
	 */
	public static void cast(final String name, final Object msg) {
		final Mailbox mailbox = mailboxes.get(name);

		if (mailbox != null) {
			mailbox.deliver(msg);
		} else {
			throw new NotRegisteredError(name);
		}
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
			exchange.route(new Event(topic, payload));
		}
	}
	
	/**
	 * Invokes asynchronously one of the methods marked with the CallHandler
	 * annotation for the operation, of the actor identified by serverName
	 *
	 * @param serverName the actor's name
	 * @param operation name of the operation to invoke
	 * @param payload call's argument
	 * @return a future that will contain the call output
	 */
	public static Future<Object> call(final String serverName, final String operation, final Object payload) {
		final CallRequest request = new CallRequest(operation, payload);
		cast(serverName, request);
		return request;
	}

	private static boolean isValidActor(final Object actor) {
		for (Method method : actor.getClass().getMethods()) {
			final CastHandler handler = method.getAnnotation(CastHandler.class);
			final Subscription subscription = method.getAnnotation(Subscription.class);

			if ((handler != null || (subscription != null && subscription.value() != null)) && method.getParameterTypes().length == 1) {
				return true;
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
