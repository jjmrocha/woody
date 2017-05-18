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

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;
import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.api.error.InvalidActorException;
import net.uiqui.woody.api.util.TopicNames;
import net.uiqui.woody.lib.NameFactory;

public class Registry {
	private final ConcurrentHashMap<String, ActorRef> mailboxes = new ConcurrentHashMap<String, ActorRef>();
	private final ConcurrentHashMap<String, Exchange> topics = new ConcurrentHashMap<String, Exchange>();
	
	public ActorRef register(final String name, final Object actor) {
		return register(name, true, actor);
	}
	
	public ActorRef register(final Object actor) {
		final String name = NameFactory.get();
		return register(name, false, actor);
	}
	
	private ActorRef register(final String name, final boolean register, final Object actor) {
		if (isValidActor(actor)) {
			final ActorFacade facade = new ActorFacade(name, actor);
			final ActorRef actorRef = new ActorMailbox(facade);

			if (register || facade.isSearchable()) {
				registerActor(name, actorRef);
			}

			registerSubscriptions(name, actor);

			return actorRef;
		} else {
			throw new InvalidActorException("Class " + actor.getClass().getName() + " is not a valid actor");
		}
	}
	
	public void unregister(final String name) {
		mailboxes.remove(name);
	}
	
	public boolean isRegistered(final String name) {
		return mailboxes.containsKey(name);
	}
	
	public ActorRef findActor(final String name) {
		return mailboxes.get(name);
	}
	
	public Exchange findTopic(final String name) {
		return topics.get(name);
	}

	public void registerActor(final String name, final ActorRef actorRef) {
		mailboxes.putIfAbsent(name, actorRef);
	}

	private boolean isValidActor(final Object actor) {
		for (final Method method : actor.getClass().getMethods()) {
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
			} else if (method.getParameterTypes().length == 0) {
				final CallHandler call = method.getAnnotation(CallHandler.class);

				if (call != null && call.value() != null && method.getReturnType() != Void.class) {
					return true;
				}
			}
		}

		return false;
	}

	private void registerSubscriptions(final String name, final Object actor) {
		for (final Method method : actor.getClass().getMethods()) {
			final Subscription subscription = method.getAnnotation(Subscription.class);

			if (subscription != null && subscription.value() != null && method.getParameterTypes().length == 1) {
				subscribe(subscription.value(), name);
			}
		}
	}
	
	private void subscribe(final String topic, final String actorName) {
		final Exchange exchange = topics.putIfAbsent(topic, new Exchange(actorName));

		if (exchange != null) {
			exchange.bind(actorName);
		} else if (!TopicNames.isInternalTopic(topic)) {
			Woody.publish(TopicNames.NEW_TOPIC, topic);
		}
	}	
}
