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
import java.util.concurrent.Future;

import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.CallRequest;
import net.uiqui.woody.api.DynamicInvoker;
import net.uiqui.woody.api.error.WoodyException;

/**
 * The Class Actor provides the auto registration mechanics and is mandatory for RPC support.
 */
public abstract class Actor extends DynamicInvoker {
	private String name = null;
	
	/**
	 * Instantiates a new actor.
	 *
	 * @param name the actor registration name
	 * @throws WoodyException thrown when an error occurred during actor's registering 
	 */
	public Actor(final String name) throws WoodyException {
		super();
		this.name = name;
		Woody.register(name, this);
		setup();
	}

	/**
	 * Instantiates a new actor.
	 *
	 * @throws WoodyException thrown when an error occurred during actor's registering 
	 */
	public Actor() throws WoodyException {
		super();
		this.name = Woody.register(this);
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
	 * Returns the actor's name
	 *
	 * @return the actor's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Send a message asynchronously to the actor instance 
	 * the message will be delivered to a method marked with
	 * the CastHandler annotation  
	 *
	 * @param msg message to send asynchronously
	 */
	public void cast(final Object msg) {
		Woody.cast(name, msg);
	}
	
	/**
	 * Invokes asynchronously one of the methods marked with the CallHandler
	 * annotation for the operation.
	 *
	 * @param operation name of the operation to invoke
	 * @param payload call's argument
	 * @return the method's return value
	 */
	public Future<Object> call(final String operation, final Object payload) {
		return Woody.call(name, operation, payload);
	}
	
	/**
	 * Unregister the actor
	 */
	public void close() {
		Woody.unregister(name);
	}
	
	@CastHandler
	public void handleCall(final CallRequest request) {
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
