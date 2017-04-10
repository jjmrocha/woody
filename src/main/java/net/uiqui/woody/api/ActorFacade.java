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

import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.ActorRef;
import net.uiqui.woody.annotations.Actor;
import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.annotations.Self;

public class ActorFacade extends DynamicInvoker {
	private boolean searchable = false;
	
	public ActorFacade(final String name, final Object actor) {
		super(actor);

		for (Method method : actor.getClass().getMethods()) {
			if (method.getParameterTypes().length == 1) {
				final CastHandler cast = method.getAnnotation(CastHandler.class);

				if (cast != null) {
					addTypeInvoker(method.getParameterTypes()[0], method);
				}

				final Subscription subscription = method.getAnnotation(Subscription.class);

				if (subscription != null && subscription.value() != null) {
					addTypeInvoker(subscription.value(), method.getParameterTypes()[0], method);
					searchable = true;
				}

				final CallHandler call = method.getAnnotation(CallHandler.class);

				if (call != null && call.value() != null && method.getReturnType() != Void.class) {
					addTypeInvoker(call.value(), method.getParameterTypes()[0], method);
				}
			}
		}

		try {
			for (Field field : actor.getClass().getDeclaredFields()) {
				final Class<?> type = field.getType();
				
				final Self self = field.getAnnotation(Self.class);

				if (self != null && type.equals(String.class)) {
					field.setAccessible(true);
					field.set(actor, name);
					searchable = true;
				}
				
				final Actor actorRef = field.getAnnotation(Actor.class);

				if (actorRef != null && actorRef.value() != null && type.equals(ActorRef.class)) {
					field.setAccessible(true);
					field.set(actor, new LazyActorRef(actorRef.value()));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error accessing actor fields", e);
		}
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void onMessage(final Object msg) {
		if (msg instanceof Call) {
			handleCall((Call) msg);
		} else if (msg instanceof Event) {
			onEvent((Event) msg);
		} else {
			invoke(msg);
		}
	}

	private void onEvent(final Event event) {
		invoke(event.getTopic(), event.getPayload());
	}

	private void handleCall(final Call request) {
		if (request.tryRun()) {
			try {
				final Object reply = invoke(request.getOperation(), request.getPayload());
				request.setResult(reply);
			} catch (Throwable cause) {
				request.setException(cause);
			}
		}
	}
}
