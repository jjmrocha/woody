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

public class Woody {
	private static final Registry registry = new Registry();

	@SuppressWarnings("unchecked")
	public static <T> T newActor(final Class<T> clazz) {
		if (ActorFactory.isSearchable(clazz)) {
			final String name = NameFactory.get();
			return (T) newActor(name, clazz);
		}
		
		return (T) ActorFactory.newActor(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T newActor(final Class<T> clazz, final int poolSize) {
		if (ActorFactory.isSearchable(clazz)) {
			final String name = NameFactory.get();
			return (T) newActor(name, clazz, poolSize);
		}
		
		return (T) ActorFactory.newActor(clazz, poolSize);
	}	

	@SuppressWarnings("unchecked")
	public static <T> T newActor(final String name, final Class<T> clazz) {
		final Object actor = ActorFactory.newActor(name, clazz);
		registerActor(name, actor);
		return (T) actor;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T newActor(final String name, final Class<T> clazz, final int poolSize) {
		final Object actor = ActorFactory.newActor(name, clazz, poolSize);
		registerActor(name, actor);
		return (T) actor;
	}	

	public static void register(final Object obj) {
		if (ActorFactory.isSearchable(obj)) {
			final String name = NameFactory.get();
			register(name, obj);
		}
		
		ActorFactory.newActor(obj);
	}
	
	public static void register(final String name, final Object obj) {
		final Object actor = ActorFactory.newActor(name, obj);
		registerActor(name, actor);
	}
	
	private static void registerActor(final String name, final Object actor) {
		if (registry.isRegistered(name)) {
			throw new AlreadyRegisteredException("The actor " + name + " is already registed");
		}
		
		registry.register(name, actor);
	}

	public static void unregister(final String name) {
		registry.unregister(name);
	}

	@SuppressWarnings("unchecked")
	public static <T> T findActor(final String name) {
		return (T) registry.findActor(name);
	}
}
