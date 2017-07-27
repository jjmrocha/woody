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
import java.util.HashMap;
import java.util.Map;

import net.uiqui.woody.annotations.Actor;
import net.uiqui.woody.annotations.Async;
import net.uiqui.woody.annotations.Self;
import net.uiqui.woody.api.msg.CallMessage;
import net.uiqui.woody.api.msg.CastMessage;
import net.uiqui.woody.api.util.HashCode;

public class ActorWrapper {
	private final Map<Integer, Method> methods = new HashMap<Integer, Method>();
	private Object target = null;
	
	public ActorWrapper(final String name, final Object actor) {
		this.target = actor;
		
		for (final Method method : actor.getClass().getMethods()) {
			final Async async = method.getAnnotation(Async.class);
			
			if (async != null) {
				final Integer hashCode = HashCode.get(method);
				methods.put(hashCode, method);
			}
		}

		try {
			for (final Field field : actor.getClass().getDeclaredFields()) {
				final Class<?> type = field.getType();
				final Self self = field.getAnnotation(Self.class);

				if (self != null && type.equals(String.class)) {
					field.setAccessible(true);
					field.set(actor, name);
				}
				
				final Actor actorRef = field.getAnnotation(Actor.class);

				if (actorRef != null && actorRef.value() != null) {
					final Object lazyActor = ActorFactory.newLazyActor(actorRef.value(), type);
					field.setAccessible(true);
					field.set(actor, lazyActor);
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException("Error accessing actor fields", e);
		}
	}
	
	public void handleCast(final CastMessage msg) {
		invoke(msg.getMethod(), msg.getArgs());
	}

	public void handleCall(final CallMessage request) {
		if (request.tryRun()) {
			try {
				final Object reply = invoke(request.getMethod(), request.getArgs());
				request.setResult(reply);
			} catch (final Throwable cause) {
				request.setException(cause);
			}
		}
	}
	
	private Object invoke(final Integer methodHashCode, final Object[] args) {
		final Method method = methods.get(methodHashCode);
		
		if (method != null) {
			try {
				return method.invoke(target, args);
			} catch (final Exception e) {
				throw new RuntimeException("Error invoking method " + method.getName() + " on class " + target.getClass().getName(), e);
			}
		} else {
			throw new RuntimeException("Method not found on class " + target.getClass().getName() + " with hashCode " + methodHashCode);
		}
	}
}
