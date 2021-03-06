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
import java.util.concurrent.atomic.AtomicBoolean;

import net.uiqui.woody.api.msg.CallMessage;
import net.uiqui.woody.api.msg.CastMessage;
import net.uiqui.woody.lib.Runner;

public class ActorMailbox {
	private final Queue<Object> queue = new LinkedBlockingQueue<Object>();
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	private ActorWrapper actor = null;

	public ActorMailbox(final ActorWrapper actor) {
		this.actor = actor;
	}

	public void push(final Object msg) {
		queue.offer(msg);

		if (tryToRun()) {
			Runner.run(new Runnable() {
				public void run() {
					do {
						try {
							do {
								final Object msg = queue.poll();

								if (msg != null) {
									if (msg instanceof CastMessage) {
										actor.handleCast((CastMessage) msg);									
									} else if (msg instanceof CallMessage) {
										actor.handleCall((CallMessage) msg);
									}
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
}
