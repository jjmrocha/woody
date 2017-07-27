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

import net.uiqui.woody.api.ActorFactory;
import net.uiqui.woody.api.Registry;
import net.uiqui.woody.api.error.AlreadyRegisteredException;
import net.uiqui.woody.lib.NameFactory;

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
	@SuppressWarnings("unchecked")
	public static <T> T newActor(final Class<T> clazz) {
		if (ActorFactory.isSearchable(clazz)) {
			final String name = NameFactory.get();
			return (T) newActor(name, clazz);
		}
		
		return (T) ActorFactory.newActor(clazz);
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
	@SuppressWarnings("unchecked")
	public static <T> T newActor(final String name, final Class<T> clazz) {
		final Object actor = ActorFactory.newActor(name, clazz);
		registerActor(name, actor);
		return (T) actor;
	}

	/**
	 * Register one object as an actor
	 *
	 * @param actor
	 *            the actor instance
	 * @return the reference for the actor
	 */
	@SuppressWarnings("unchecked")
	public static <T> T register(final Object obj) {
		if (ActorFactory.isSearchable(obj)) {
			final String name = NameFactory.get();
			return (T) register(name, obj);
		}
		
		return (T) ActorFactory.newActor(obj);
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
	@SuppressWarnings("unchecked")
	public static <T> T register(final String name, final Object obj) {
		final Object actor = ActorFactory.newActor(name, obj);
		registerActor(name, actor);
		return (T) actor;
	}
	
	private static void registerActor(final String name, final Object actor) {
		if (registry.isRegistered(name)) {
			throw new AlreadyRegisteredException("The actor " + name + " is already registed");
		}
		
		registry.register(name, actor);
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
	@SuppressWarnings("unchecked")
	public static <T> T findActor(final String name) {
		return (T) registry.findActor(name);
	}
}
