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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.uiqui.woody.util.DeamonFactory;

public class ActorQueue {
	private final BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	private boolean running = true;
	private ActorWrapper actorWrapper = null;
	
	public ActorQueue(final Object actor) {
		this.actorWrapper = new ActorWrapper(actor);
		
		DeamonFactory.run(new Runnable() {
			@Override
			public void run() {
				Object msg = null;
						
				while (isRunning()) {
					try {
						if (msg != null) {
							actorWrapper.onMessage(msg);
						}
						
						msg = queue.poll(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}

	public void push(final Object msg) {
		if (isRunning()) {
			queue.offer(msg);
		}
	}

	public synchronized void stop() {
		if (running) {
			running = false;
			queue.clear();
		}
	}

	private synchronized boolean isRunning() {
		return running;
	}
}
