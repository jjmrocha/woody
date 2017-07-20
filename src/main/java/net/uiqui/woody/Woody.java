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

import net.uiqui.woody.api.Event;
import net.uiqui.woody.api.Exchange;
import net.uiqui.woody.api.Registry;
import net.uiqui.woody.api.error.AlreadyRegisteredException;
import net.uiqui.woody.lib.ActorFactory;
import net.uiqui.woody.lib.Runner;

/**
 * The Class Woody is responsible for the main features, mainly: Actor and topic
 * registration/subscription and support for sending messages, publish events
 * and perform rpc calls
 */
public class Woody {
	private static final Registry registry = new Registry();

	/**
	 * Creates a new instance of an actor
	 * 
	 * @param clazz
	 *            actor's class
	 * @return the reference for the actor
	 */
	public static ActorRef newActor(final Class<?> clazz) {
		final Object actor = ActorFactory.newActor(clazz);
		return register(actor);
	}

	/**
	 * Creates a new instance of an actor, and register the instance with the
	 * supplied name
	 * 
	 * @param name
	 *            the actor's name
	 * @param clazz
	 *            actor's class
	 * @return the reference for the actor
	 */
	public static ActorRef newActor(final String name, final Class<?> clazz) {
		final Object actor = ActorFactory.newActor(clazz);
		return register(name, actor);
	}

	/**
	 * Create and registers a new actor group
	 * 
	 * @return the new actor group
	 */
	public static ActorGroup newActorGroup() {
		return new ActorGroup();
	}

	/**
	 * Create a new actor group
	 * 
	 * @param name the name for the actor group registration
	 * @return the new actor group
	 */
	public static ActorGroup newActorGroup(final String name) {
		if (!registry.isRegistered(name)) {
			final ActorGroup actorGroup = newActorGroup();
			registry.registerActor(name, actorGroup);

			return actorGroup;
		} else {
			throw new AlreadyRegisteredException("The actor " + name + " is already registed");
		}
	}
	
	/**
	 * Create and registers a new actor group
	 * 
	 * @param clazz the class for the actor creation
	 * @param size the number of actor to create
	 * @return the new actor group
	 */
	public static ActorGroup newActorGroup(final Class<?> clazz, final int size) {
		final ActorGroup actorGroup = newActorGroup();
		actorGroup.addMembers(clazz, size);
		return actorGroup;
	}

	/**
	 * Create a new actor group
	 * 
	 * @param name the name for the actor group registration
	 * @param clazz the class for the actor creation
	 * @param size the number of actor to create 
	 * @return the new actor group
	 */
	public static ActorGroup newActorGroup(final String name, final Class<?> clazz, final int size) {
		final ActorGroup actorGroup = newActorGroup(name);
		actorGroup.addMembers(clazz, size);
		return actorGroup;
	}	

	/**
	 * Register one object as an actor
	 *
	 * @param actor
	 *            the actor instance
	 * @return the reference for the actor
	 */
	public static ActorRef register(final Object actor) {
		return registry.register(actor);
	}

	/**
	 * Register one object as an actor
	 *
	 * @param name
	 *            the actor's name
	 * @param actor
	 *            the actor instance
	 * @return the reference for the actor
	 */
	public static ActorRef register(final String name, final Object actor) {
		if (!registry.isRegistered(name)) {
			return registry.register(name, actor);
		} else {
			throw new AlreadyRegisteredException("The actor " + name + " is already registed");
		}
	}

	/**
	 * Unregister the actor
	 *
	 * @param name
	 *            the actor's name
	 */
	public static void unregister(final String name) {
		registry.unregister(name);
	}

	/**
	 * Return a reference for the actor
	 *
	 * @param name
	 *            the name of the actor
	 * @return the actor's reference
	 */
	public static ActorRef getActorRef(final String name) {
		return registry.findActor(name);
	}
	
	/**
	 * Publish a event to a topic, all actors subscribing the topic will receive
	 * the message (an actor subscribes a topic, by annotating a method with the
	 * Subscription annotation)
	 *
	 * @param topic
	 *            the topic's name
	 * @param payload
	 *            the event to deliver to all subscribers
	 */
	public static void publish(final String topic, final Object payload) {
		final Event event = new Event(topic, payload);
		
		Runner.queue(new Runnable() {
			public void run() {
				final Exchange exchange = registry.findTopic(event.getTopic());

				if (exchange != null) {
					exchange.route(event);
				}
			}
		});
	}	
}
