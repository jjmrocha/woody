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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureResult<T> implements Future<T> {
	private static final int STATE_WAITING = 0;
	private static final int STATE_RUNNING = 1;
	private static final int STATE_DONE = 2;
	private static final int STATE_CANCELED = 3;
	
	// Reply
	private T result = null;
	private Throwable cause = null;
	
	// Future
	private final Semaphore clientParking = new Semaphore(0);
	private volatile int state = STATE_WAITING;

	// Future implementation
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {		
		return tryChangeState(STATE_CANCELED, null, null);
	}

	@Override
	public boolean isCancelled() {
		return state == STATE_CANCELED;
	}

	@Override
	public boolean isDone() {
		return state == STATE_DONE;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (state < STATE_DONE) {
			clientParking.acquire();
		}
		
		return report();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException {
		if (state >= STATE_DONE || clientParking.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
			return report();
		} else {
			throw new TimeoutException();
		}
	}
	
	private T report() throws ExecutionException {
		if (state == STATE_CANCELED) {
			throw new CancellationException();
		}
		
		if (cause != null) {
			throw new ExecutionException(cause);
		}
		
		return result;
	}
	
	// Request management
	public void setResult(final T result) {
		tryChangeState(STATE_DONE, result, null);
	}
	
	public void setException(final Throwable cause) {
		tryChangeState(STATE_DONE, null, cause);
	}
	
	public boolean tryRun() {
		return tryChangeState(STATE_RUNNING, null, null);
	}
	
	private synchronized boolean tryChangeState(final int newState, final T result, final Throwable cause) {
		if (state == STATE_CANCELED || state == STATE_DONE) {
			return false;
		}
		
		if (state == STATE_RUNNING && newState != STATE_DONE) {
			return false;
		}
		
		if (newState == STATE_DONE) {
			this.result = result;
			this.cause = cause;			
			clientParking.release();
		}
		
		this.state = newState;
		return true;
	}
}
