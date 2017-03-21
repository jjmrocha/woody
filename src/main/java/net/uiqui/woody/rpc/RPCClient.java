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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.uiqui.woody.Broker;
import net.uiqui.woody.Endpoint;
import net.uiqui.woody.Pusher;
import net.uiqui.woody.error.RPCTimeoutException;
import net.uiqui.woody.rpc.impl.RPCMessage;

public class RPCClient<R, E> {
	private static final long TIMEOUT = 5000;
	
	private Endpoint endpoint = null;
	
	public RPCClient(final Endpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public RPCClient(final String rpcName) {
		this(Endpoint.getEndpointForActor(rpcName));
	}

	public E call(final R message) throws RPCTimeoutException {
		return call(message, TIMEOUT);
	}

	public E call(final R message, final long timeout) throws RPCTimeoutException {
		final Endpoint replyTo = Endpoint.getEndpointForRPC();
		
		try {
			final Semaphore semaphore = new Semaphore(0);
			final LocalPusher<E> pusher = new LocalPusher<E>(semaphore);
			Broker.register(replyTo, pusher);
			
			final RPCMessage<R> request = new RPCMessage<R>(replyTo, message);
			Broker.send(endpoint, request);
			
			if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
				return pusher.value();
			} else {
				throw new RPCTimeoutException(endpoint);
			}
		} catch (InterruptedException e) {
		} finally {
			Broker.unregister(replyTo);
		}
		
		return null;
	}

	private static class LocalPusher<E> implements Pusher<E> {
		private E returnValue = null;
		private Semaphore semaphore = null;
		
		public LocalPusher(final Semaphore semaphore) {
			this.semaphore = semaphore;
		}

		public E value() {
			return returnValue;
		}

		@Override
		public void push(final E value) {
			returnValue = value;
			semaphore.release();
		}
	}
}
