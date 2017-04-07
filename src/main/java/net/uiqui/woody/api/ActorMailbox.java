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

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.lib.Runner;

public class ActorMailbox implements ActorRef {
	private final Queue<Object> queue = new LinkedBlockingQueue<Object>();
	private final Semaphore singletonController = new Semaphore(1);
	
	private String name = null;
	private ActorFacade actor = null;

	public ActorMailbox(final String name, final ActorFacade actor) {
		this.name = name;
		this.actor = actor;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void cast(final Object msg) {
		queue.offer(msg);

		if (singletonController.tryAcquire()) {
			Runner.spawn(new Runnable() {
				@Override
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
							singletonController.release();
						}
					} while (queue.size() > 0 && singletonController.tryAcquire());
				}
			});
		}
	}

	@Override
	public Future<Object> call(final String operation, final Object payload) {
		final Call request = new Call(operation, payload);
		cast(request);
		return request;
	}
}
