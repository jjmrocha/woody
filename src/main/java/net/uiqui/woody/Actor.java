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
import net.uiqui.woody.api.DynamicInvoker;
import net.uiqui.woody.api.RpcRequest;
import net.uiqui.woody.api.error.CallTimeoutException;
import net.uiqui.woody.api.error.WoodyException;

public abstract class Actor extends DynamicInvoker {
	private String name = null;
	
	public Actor(final String name) throws WoodyException {
		super();
		this.name = name;
		Broker.register(name, this);
		setup();
	}

	public Actor() throws WoodyException {
		super();
		this.name = Broker.register(this);
		setup();
	}
	
	private void setup() {
		for (Method method : this.getClass().getMethods()) {
			final CallHandler handler = method.getAnnotation(CallHandler.class);
			
			if (handler != null && method.getParameterTypes().length == 1 && method.getReturnType() != Void.class) {
				addInvoker(method.getParameterTypes()[0], method);
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void send(final Object msg) {
		Broker.send(name, msg);
	}
	
	public <T> T call(final Object msg) throws CallTimeoutException {
		return Broker.call(name, msg);
	}
	
	public <T> T call(final Object msg, final long timeout) throws CallTimeoutException {
		return Broker.call(name, msg, timeout);
	}	
	
	@MessageHandler
	public void handleCall(final RpcRequest request) {
		final Object reply = invoke(request.getPayload());
		Broker.send(request.getReplyTo(), reply);
	}
}
