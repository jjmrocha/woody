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
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.DynamicInvoker;
import net.uiqui.woody.api.CallRequest;
import net.uiqui.woody.api.error.CallTimeoutException;
import net.uiqui.woody.api.error.NotRegisteredError;
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
	 * @throws CallTimeoutException thrown if the call took more time than the default timeout threshold
	 */
	public <T> T call(final String operation, final Object payload) throws CallTimeoutException {
		return Woody.call(name, operation, payload);
	}
	
	/**
	 * Invokes asynchronously one of the methods marked with the CallHandler
	 * annotation for the operation.
	 *
	 * @param operation name of the operation to invoke
	 * @param payload call's argument
	 * @param timeout timeout threshold in milliseconds
	 * @return the method's return value
	 * @throws CallTimeoutException thrown if the call took more time than the timeout threshold
	 */
	public <T> T call(final String operation, final Object payload, final long timeout) throws CallTimeoutException {
		return Woody.call(name, operation, payload, timeout);
	}	
	
	/**
	 * Unregister the actor
	 */
	public void close() {
		Woody.unregister(name);
	}
	
	@CastHandler
	public void handleCall(final CallRequest request) {
		final Object reply = invoke(request.getOperation(), request.getPayload());
		
		try {
			Woody.cast(request.getReplyTo(), reply);
		} catch(NotRegisteredError e) {
		}
	}
}
