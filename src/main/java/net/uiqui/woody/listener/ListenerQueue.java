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
package net.uiqui.woody.listener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.uiqui.woody.Pusher;

public final class ListenerQueue<E> implements Runnable, Pusher<E> {
	private final BlockingQueue<E> queue = new LinkedBlockingQueue<E>();
	private Listener<E> listener = null;
	private boolean running = true;

	public ListenerQueue(final Listener<E> listener) {
		this.listener = listener;

		final Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	public void run() {
		while (isRunning()) {
			try {
				E msg = queue.take();

				if (msg != null) {
					listener.onMessage(msg);
				}
			} catch (InterruptedException e) {
			}
		}
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
