/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2014-17 Joaquim Rocha <jrocha@gmailbox.org>
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
package net.uiqui.woody.api.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.uiqui.woody.api.Listener;
import net.uiqui.woody.api.Pusher;
import net.uiqui.woody.util.DeamonFactory;

public final class ListenerQueue<E> implements Pusher<E> {
	private final BlockingQueue<E> queue = new LinkedBlockingQueue<E>();
	private boolean running = true;

	public ListenerQueue(final Listener<E> listener) {
		DeamonFactory.run(new Runnable() {
			@Override
			public void run() {
				E msg = null;
						
				while (isRunning()) {
					try {
						if (msg != null) {
							listener.onMessage(msg);
						}
						
						msg = queue.poll(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}
	
	public void push(final E msg) {
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
