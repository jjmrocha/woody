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

import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.annotations.MessageHandler;
import net.uiqui.woody.api.ActorMailbox;
import net.uiqui.woody.api.ActorFacade;
import net.uiqui.woody.api.Exchange;
import net.uiqui.woody.api.Mailbox;
import net.uiqui.woody.api.CallMailbox;
import net.uiqui.woody.api.CallRequest;
import net.uiqui.woody.api.Event;
import net.uiqui.woody.api.error.AlreadyRegisteredException;
import net.uiqui.woody.api.error.CallTimeoutException;
import net.uiqui.woody.api.error.InvalidActorException;
import net.uiqui.woody.api.error.NotRegisteredError;
import net.uiqui.woody.api.error.WoodyException;
import net.uiqui.woody.factory.ReferenceFactory;

/**
 * The Class Broker.
 */
public class Broker {
	private static final long DEFAULT_TIMEOUT = 5000;
	
	private static final ConcurrentHashMap<String, Mailbox> mailboxes = new ConcurrentHashMap<String, Mailbox>();
	private static final ConcurrentHashMap<String, Exchange> topics = new ConcurrentHashMap<String, Exchange>();

	/**
	 * Register.
	 *
	 * @param actor the actor
	 * @return the string
	 * @throws WoodyException the woody exception
	 */
	public static String register(final Object actor) throws WoodyException {
		final String name = ReferenceFactory.get();
		register(name, actor);
		return name;
	}

	/**
	 * Register.
	 *
	 * @param name the name
	 * @param actor the actor
	 * @throws WoodyException the woody exception
	 */
	public static void register(final String name, final Object actor) throws WoodyException {
		if (isValidActor(actor)) {
			if (!isRegisted(name)) {
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
	 * Unregister.
	 *
	 * @param name the name
	 */
	public static void unregister(final String name) {
		mailboxes.remove(name);
	}

	/**
	 * Checks if is registed.
	 *
	 * @param name the name
	 * @return true, if is registed
	 */
	public static boolean isRegisted(final String name) {
		return mailboxes.containsKey(name);
	}

	/**
	 * Send.
	 *
	 * @param name the name
	 * @param msg the msg
	 */
	public static void send(final String name, final Object msg) {
		final Mailbox mailbox = mailboxes.get(name);

		if (mailbox != null) {
			mailbox.deliver(msg);
		} else {
			throw new NotRegisteredError(name);
		}
	}

	/**
	 * Publish.
	 *
	 * @param topic the topic
	 * @param payload the payload
	 */
	public static void publish(final String topic, final Object payload) {
		final Exchange exchange = topics.get(topic);

		if (exchange != null) {
			exchange.route(new Event(topic, payload));
		}
	}
	
	/**
	 * Call.
	 *
	 * @param <T> the generic type
	 * @param serverName the server name
	 * @param operation the operation
	 * @param payload the payload
	 * @return the t
	 * @throws CallTimeoutException the call timeout exception
	 */
	public static <T> T call(final String serverName, final String operation, final Object payload) throws CallTimeoutException {
		return call(serverName, operation, payload, DEFAULT_TIMEOUT);
	}
	
	/**
	 * Call.
	 *
	 * @param <T> the generic type
	 * @param serverName the server name
	 * @param operation the operation
	 * @param payload the payload
	 * @param timeout the timeout
	 * @return the t
	 * @throws CallTimeoutException the call timeout exception
	 */
	public static <T> T call(final String serverName, final String operation, final Object payload, final long timeout) throws CallTimeoutException {
		final Mailbox serverMailbox = mailboxes.get(serverName);

		if (serverMailbox != null) {
			final String callMailboxName = ReferenceFactory.get();
			final CallMailbox callMailbox = new CallMailbox();
			final CallRequest request = new CallRequest(operation, payload, callMailboxName);
			
			try {
				mailboxes.put(callMailboxName, callMailbox);
				serverMailbox.deliver(request);
				return callMailbox.receiveReply(timeout);
			} finally {
				unregister(callMailboxName);
			}
		} else {
			throw new NotRegisteredError(serverName);
		}
	}

	private static boolean isValidActor(final Object actor) {
		for (Method method : actor.getClass().getMethods()) {
			final MessageHandler handler = method.getAnnotation(MessageHandler.class);
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
