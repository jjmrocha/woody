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
package net.uiqui.woody.api.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DynamicInvoker {
	private static final String DEFAULT_KEY = "$$";
	private final Map<KeyType, Method> methods = new HashMap<KeyType, Method>();
	private Object target = null;
	
	public DynamicInvoker(final Object target) {
		this.target = target;
	}
	
	public DynamicInvoker() {
		this.target = this;
	}

	public void addTypeInvoker(final Class<?> type, final Method method) {
		addTypeInvoker(DEFAULT_KEY, type, method);
	}
	
	public void addTypeInvoker(final String key, final Class<?> type, final Method method) {
		methods.put(new KeyType(key, type), method);
		method.setAccessible(true);
	}

	public Object invoke(final String key, final Object param) {
		final Method method = getMethod(key, param);
		
		if (method != null) {
			try {
				if (param.equals(Empty.VALUE)) {
					return method.invoke(target);
				} else {
					return method.invoke(target, param);
				}
			} catch (Exception e) {
				throw new RuntimeException("Error invoking method " + method.getName() + " on class " + target.getClass().getName() + " with parameter of type " + param.getClass().getName(), e);
			}
		} else {
			throw new RuntimeException("Method not found on class " + target.getClass().getName() + " for parameter of type " + param.getClass().getName() + (key.equals(DEFAULT_KEY)?"": " and idenfified as '" + key + "'"));
		}
	}
	
	public Object invoke(final Object param) {
		return invoke(DEFAULT_KEY, param);
	}

	private Method getMethod(final String key, final Object param) {
		final Class<?> type = param.getClass();
		final KeyType keyType = new KeyType(key, type);
		final Method method = methods.get(keyType);
		
		if (method != null) {
			return method;
		}
		
		for (Map.Entry<KeyType, Method> entry : methods.entrySet()) {
			final KeyType entryKeyType = entry.getKey();
			
			if (entryKeyType.key.equals(key) && entryKeyType.type.isAssignableFrom(type)) {
				methods.put(keyType, entry.getValue());
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	private static final class KeyType {
		private String key = null;
		private Class<?> type = null;
		
		public KeyType(final String key, final Class<?> type) {
			this.key = key;
			this.type = type;
		}

		@Override
		public int hashCode() {
			int result = 31 + key.hashCode();
			return 31 * result + type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			
			if (obj instanceof KeyType) {
				final KeyType other = (KeyType) obj;
				
				if (!key.equals(other.key)) {
					return false;
				}
				
				if (!type.getName().equals(other.type.getName())) {
					return false;
				}
				
				return true;
			}
			
			return false;
		}
	}
}
