/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2014 Joaquim Rocha <jrocha@gmailbox.org>
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
package net.uiqui.woody.listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.uiqui.woody.Pusher;
import net.uiqui.woody.util.Semaphore;

public class ListenerPusher<E> implements Runnable, Pusher<E> {
	private Queue<E> queue = null;
	private boolean running = true;
	private Listener<E> listener = null;
	private final Semaphore semaphore = new Semaphore();

	public ListenerPusher(final Listener<E> listener) {
		this(listener, new ConcurrentLinkedQueue<E>());
	}

	public ListenerPusher(final Listener<E> listener, final Queue<E> queue) {
		this.queue = queue;
		this.listener = listener;

		final Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	public void run() {
		while (isRunning()) {
			if (!queue.isEmpty()) {
				E msg = queue.poll();

				if (msg != null) {
					listener.onMessage(msg);
				}
			} else {
				semaphore.stop();
			}
		}
	}

	public boolean push(final E msg) {
		if (isRunning()) {
			final boolean done = queue.offer(msg);

			if (done && semaphore.status() == Semaphore.RED) {
				semaphore.go();
			}

			return done;
		} else {
			return false;
		}
	}

	public synchronized void stop() {
		if (running) {
			running = false;
			queue.clear();

			if (semaphore.status() == Semaphore.RED) {
				semaphore.go();
			}
		}
	}

	private synchronized boolean isRunning() {
		return running;
	}
}
