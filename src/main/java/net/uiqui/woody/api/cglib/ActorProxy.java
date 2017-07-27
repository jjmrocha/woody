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
package net.uiqui.woody.api.cglib;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.InvocationHandler;
import net.uiqui.woody.annotations.Async;
import net.uiqui.woody.api.ActorMailbox;
import net.uiqui.woody.api.error.NotAvailableException;
import net.uiqui.woody.api.msg.CallMessage;
import net.uiqui.woody.api.msg.CastMessage;
import net.uiqui.woody.api.util.HashCode;

public class ActorProxy implements InvocationHandler {
	private final Map<Integer, CallType> methods = new HashMap<Integer, CallType>();
	private ActorMailbox mailbox = null;
	
	public ActorProxy(final Class<?> clazz, final ActorMailbox mailbox) {
		this.mailbox = mailbox;
		
		for (final Method method : clazz.getMethods()) {
			final Async async = method.getAnnotation(Async.class);
			
			if (async != null) {
				final Integer hashCode = HashCode.get(method);
				
				if (method.getReturnType() == Void.class) {
					methods.put(hashCode, CallType.CAST);
				} else {
					methods.put(hashCode, CallType.CALL);
				}
			}
		}
	}

	@Override
	public Object invoke(final Object obj, final Method method, final Object[] args) throws Throwable {
		final Integer hashCode = HashCode.get(method);
		final CallType callType = methods.get(hashCode);
		
		if (callType == null) {
			throw new NotAvailableException("The method " + method.getName() + " isn't exposed");
		}
		
		switch (callType) {
		case CAST:
			final CastMessage castMessage = new CastMessage(hashCode, args);
			mailbox.push(castMessage);
			break;
		case CALL:
			final CallMessage callMessage = new CallMessage(hashCode, args);
			mailbox.push(callMessage);
			return callMessage.get();
		}
		
		return null;
	}

	private static enum CallType {
		CAST,
		CALL
	}
}
