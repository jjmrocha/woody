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

import net.uiqui.woody.annotations.EventSubscription;
import net.uiqui.woody.annotations.MessageHandler;

public class ActorFacade extends Dynamic {
	public ActorFacade(final Object actor) {
		super(actor);
		
		for (Method method : actor.getClass().getMethods()) {
			final MessageHandler handler = method.getAnnotation(MessageHandler.class);
			final EventSubscription subscription = method.getAnnotation(EventSubscription.class);
			
			if ((handler != null || subscription != null) && method.getParameterTypes().length == 1) {
				addTypeInvoker(method.getParameterTypes()[0], method);
			}
		}
	}
	
	public void onMessage(final Object msg) {
		invoke(msg);
	}
}
