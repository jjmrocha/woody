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

public class DynamicInvoker {
	private final Map<Class<?>, Method> methods = new HashMap<Class<?>, Method>();
	private Object actor = null;
	
	public DynamicInvoker(final Object actor) {
		this.actor = actor;
	}
	
	public DynamicInvoker() {
		this.actor = this;
	}

	public void addInvoker(final Class<?> type, final Method method) {
		methods.put(type, method);
		method.setAccessible(true);
	}

	public Object invoke(final Object msg) {
		final Method method = getMethod(msg);
		
		if (method != null) {
			try {
				return method.invoke(actor, msg);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Error invoking method " + method.getName() + " on class " + actor.getClass().getName() + " with parameter of type " + msg.getClass().getName(), e);
			}
		}
		
		return null;
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
