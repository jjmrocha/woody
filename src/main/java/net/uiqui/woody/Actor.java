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

import java.lang.reflect.Method;

import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.MessageHandler;
import net.uiqui.woody.api.Dynamic;
import net.uiqui.woody.api.CallRequest;
import net.uiqui.woody.api.error.CallTimeoutException;
import net.uiqui.woody.api.error.NotRegisteredError;
import net.uiqui.woody.api.error.WoodyException;

/**
 * The Class Actor.
 */
public abstract class Actor extends Dynamic {
	private String name = null;
	
	/**
	 * Instantiates a new actor.
	 *
	 * @param name the name
	 * @throws WoodyException the woody exception
	 */
	public Actor(final String name) throws WoodyException {
		super();
		this.name = name;
		Broker.register(name, this);
		setup();
	}

	/**
	 * Instantiates a new actor.
	 *
	 * @throws WoodyException the woody exception
	 */
	public Actor() throws WoodyException {
		super();
		this.name = Broker.register(this);
		setup();
	}
	
	private void setup() {
		for (Method method : this.getClass().getMethods()) {
			final CallHandler handler = method.getAnnotation(CallHandler.class);
			
			if (handler != null && handler.value() != null && method.getParameterTypes().length == 1 && method.getReturnType() != Void.class) {
				addTypeInvoker(handler.value(), method.getParameterTypes()[0], method);
			}
		}
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Send.
	 *
	 * @param msg the msg
	 */
	public void send(final Object msg) {
		Broker.send(name, msg);
	}
	
	/**
	 * Call.
	 *
	 * @param <T> the generic type
	 * @param operation the operation
	 * @param msg the msg
	 * @return the t
	 * @throws CallTimeoutException the call timeout exception
	 */
	public <T> T call(final String operation, final Object msg) throws CallTimeoutException {
		return Broker.call(name, operation, msg);
	}
	
	/**
	 * Call.
	 *
	 * @param <T> the generic type
	 * @param operation the operation
	 * @param msg the msg
	 * @param timeout the timeout
	 * @return the t
	 * @throws CallTimeoutException the call timeout exception
	 */
	public <T> T call(final String operation, final Object msg, final long timeout) throws CallTimeoutException {
		return Broker.call(name, operation, msg, timeout);
	}	
	
	/**
	 * Close.
	 */
	public void close() {
		Broker.unregister(name);
	}
	
	/**
	 * Handle call.
	 *
	 * @param request the request
	 */
	@MessageHandler
	public void handleCall(final CallRequest request) {
		final Object reply = invoke(request.getOperation(), request.getPayload());
		
		try {
			Broker.send(request.getReplyTo(), reply);
		} catch(NotRegisteredError e) {
		}
	}
}
