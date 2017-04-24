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

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.api.util.Empty;
import net.uiqui.woody.lib.Runner;

public class ActorMailbox implements ActorRef {
	private final Queue<Object> queue = new LinkedBlockingQueue<Object>();
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	private ActorFacade actor = null;

	public ActorMailbox(final ActorFacade actor) {
		this.actor = actor;
	}

	public void cast(final Serializable msg) {
		queue.offer(msg);

		if (tryToRun()) {
			Runner.spawn(new Runnable() {
				public void run() {
					do {
						try {
							do {
								final Object msg = queue.poll();

								if (msg != null) {
									actor.onMessage(msg);
								}
							} while (queue.size() > 0);
						} finally {
							running.set(false);
						}
					} while (queue.size() > 0 && tryToRun());
				}
			});
		}
	}

	private boolean tryToRun() {
		return running.compareAndSet(false, true);
	}

	public Future<Serializable> call(final String operation, final Serializable payload) {
		final Call request = new Call(operation, payload);
		cast(request);
		return request;
	}
	
	public Future<Serializable> call(final String operation) {
		return call(operation, Empty.VALUE);
	}
}
