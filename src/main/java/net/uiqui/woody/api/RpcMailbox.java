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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.uiqui.woody.api.error.CallTimeoutException;

public class RpcMailbox implements Mailbox {
	private final Semaphore semaphore = new Semaphore(0);
	private Object value = null;
	
	@Override
	public void deliver(final Object reply) {
		value = reply;
		semaphore.release();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T receiveReply(final long timeout) throws CallTimeoutException {
		try {
			if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
				return (T) value;
			} else {
				throw new CallTimeoutException();
			}
		} catch (InterruptedException e) {
			return null;
		}
	}
}
