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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import net.uiqui.woody.factory.DeamonFactory;

public class ActorMailbox implements Mailbox {
	private final Queue<Object> queue = new LinkedBlockingQueue<Object>();
	private final Semaphore singletonController = new Semaphore(1);
	private ActorFacade actor = null;

	public ActorMailbox(final ActorFacade actor) {
		this.actor = actor;
	}

	@Override
	public void deliver(final Object msg) {
		queue.offer(msg);

		if (singletonController.tryAcquire()) {
			DeamonFactory.spawn(new Runnable() {
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
}
