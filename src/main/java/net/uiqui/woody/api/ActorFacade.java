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

import java.lang.reflect.Method;

import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.annotations.MessageHandler;

public class ActorFacade extends Dynamic {
	public ActorFacade(final Object actor) {
		super(actor);
		
		for (Method method : actor.getClass().getMethods()) {
			if (method.getParameterTypes().length == 1) {
				final MessageHandler handler = method.getAnnotation(MessageHandler.class);
				
				if (handler != null) {
					addTypeInvoker(method.getParameterTypes()[0], method);
				}
				
				final Subscription subscription = method.getAnnotation(Subscription.class);
				
				if (subscription != null && subscription.value() != null) {
					addTypeInvoker(subscription.value(), method.getParameterTypes()[0], method);
				}					
			}		
		}
	}
	
	public void onMessage(final Object msg) {
		if (msg instanceof Event) {
			onEvent((Event) msg);
		} else {
			invoke(msg);
		}
	}
	
	private void onEvent(final Event event) {
		invoke(event.getTopic(), event.getPayload());
	}
}
