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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.uiqui.woody.annotations.EventSubscription;
import net.uiqui.woody.annotations.MessageHandler;

public class ActorWrapper {
	private final Map<Class<?>, Method> methods = new HashMap<Class<?>, Method>();
	private String name = null;
	private Object actor = null;
	
	public ActorWrapper(final String name, Object actor) throws IllegalArgumentException, IllegalAccessException {
		this.name = name;
		this.actor = actor;
		
		for (Method method : actor.getClass().getMethods()) {
			final MessageHandler handler = method.getAnnotation(MessageHandler.class);
			final EventSubscription subscription = method.getAnnotation(EventSubscription.class);
			
			if ((handler != null || subscription != null) && method.getParameterTypes().length == 1) {
				methods.put(method.getParameterTypes()[0], method);
				method.setAccessible(true);
			}
		}
	}
	
	public void onMessage(final Object msg) {
		final Method method = getMethod(msg);
		
		if (method != null) {
			try {
				method.invoke(actor, msg);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Error invoking method " + method.getName() + " on class " + actor.getClass().getName() + " (actor '" + name + "') with parameter of type " + msg.getClass().getName(), e);
			}
		}
	}

	private Method getMethod(final Object msg) {
		final Class<?> type = msg.getClass();
		final Method method = methods.get(type);
		
		if (method != null) {
			return method;
		}
		
		for (Map.Entry<Class<?>, Method> entry : methods.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				methods.put(type, entry.getValue());
				return entry.getValue();
			}
		}
		
		return null;
	}
}
