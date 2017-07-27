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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.uiqui.woody.annotations.Async;
import net.uiqui.woody.annotations.Self;
import net.uiqui.woody.api.cglib.ActorPool;
import net.uiqui.woody.api.cglib.ActorProxy;
import net.uiqui.woody.api.cglib.LazyActor;
import net.uiqui.woody.api.error.InvalidActorException;
import net.uiqui.woody.api.util.Ring;

public class ActorFactory {
	public static Object newActor(final String name, final Object obj) {
		final Class<?> type = obj.getClass();
		
		if (!isValidActor(type)) {
			throw new InvalidActorException("Class " + type.getName() + " is not a valid actor");
		}
		
		final ActorWrapper wrapper = new ActorWrapper(name, obj);
		final ActorMailbox mailbox = new ActorMailbox(wrapper);
		final ActorProxy proxy = new ActorProxy(type, mailbox);
		
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallback(proxy);
		return enhancer.create();
	}

	public static Object newActor(final Object obj) {
		return newActor(null, obj);
	}
	
	public static Object newActor(final Class<?> type) throws InvalidActorException {
		return newActor(null, type);
	}
	
	public static Object newActor(final String name, final Class<?> type) throws InvalidActorException {
		try {
			final Object obj = type.newInstance();
			return newActor(name, obj);
		} catch (final Exception e) {
			throw new InvalidActorException("Error creating instance of " + type.getName(), e);
		}
	}
	
	public static Object newActor(final Class<?> type, final int poolSize) throws InvalidActorException {
		return newActor(null, type, poolSize);
	}
	
	public static Object newActor(final String name, final Class<?> type, final int poolSize) throws InvalidActorException {
		final Ring<Object> pool = new Ring<Object>();
		
		for (int i = 0; i < poolSize; i++) {
			final Object actor = newActor(name, type);
			pool.add(actor);
		}
		
		final ActorPool actorPool = new ActorPool(pool);
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallback(actorPool);
		return enhancer.create();
	}
	
	public static Object newLazyActor(final String name, final Class<?> type) {
		final LazyActor lazyActor = new LazyActor(name);
		final Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallback(lazyActor);
		return enhancer.create();
	}	
	
	public static boolean isValidActor(final Class<?> clazz) {
		for (final Method method : clazz.getMethods()) {
			final Async async = method.getAnnotation(Async.class);
			
			if (async != null) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isValidActor(final Object obj) {
		return isValidActor(obj.getClass());
	}
	
	public static boolean isSearchable(final Class<?> clazz) {
		for (final Field field : clazz.getDeclaredFields()) {
			final Class<?> type = field.getType();
			final Self self = field.getAnnotation(Self.class);

			if (self != null && type.equals(String.class)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isSearchable(final Object obj) {
		return isSearchable(obj.getClass());
	}
}
