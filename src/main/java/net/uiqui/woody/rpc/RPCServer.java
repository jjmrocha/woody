/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2016 Joaquim Rocha <jrocha@gmailbox.org>
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
package net.uiqui.woody.rpc;

import java.util.concurrent.Executor;

import net.uiqui.woody.Broker;
import net.uiqui.woody.actor.SupportingActor;
import net.uiqui.woody.error.NoPusherError;
import net.uiqui.woody.rpc.impl.RPCMessage;

public abstract class RPCServer<R, E> extends SupportingActor<RPCMessage<R>> {
	public RPCServer(Executor threadPool) {
		super(threadPool);
	}
	
	public RPCServer(String rpcName, Executor threadPool) {
		super(rpcName, threadPool);
	}

	@Override
	public void handle(final RPCMessage<R> msg) {
		final E response = process(msg.value());
		
		try {
			Broker.send(msg.replyTo(), response);
		} catch (NoPusherError e) {
		}
	}

	public abstract E process(final R value);
}
