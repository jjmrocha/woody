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
package net.uiqui.woody.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Runner {
	private static final boolean USE_DAEMON_THREADS = useDeamonThreads();

	protected static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		public Thread newThread(final Runnable command) {
			final Thread thread = new Thread(command);
			thread.setDaemon(USE_DAEMON_THREADS);
			return thread;
		}
	};

	private static final Executor THREAD_POOL = newThreadPoolExecutor();
	private static final Executor THREAD_QUEUE = Executors.newSingleThreadExecutor(THREAD_FACTORY);

	public static void run(final Runnable command) {
		THREAD_POOL.execute(command);
	}

	public static void start(final Runnable command) {
		final Thread thread = THREAD_FACTORY.newThread(command);
		thread.start();
	}

	public static void queue(final Runnable command) {
		THREAD_QUEUE.execute(command);
	}

	public static void sleep(final long delay, final TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(delay));
		} catch (InterruptedException e) {
		}
	}

	public static void sleep(final long delay) {
		sleep(delay, TimeUnit.MILLISECONDS);
	}	

	private static boolean useDeamonThreads() {
		final String propValue = System.getProperty("woody.use.daemon.threads");

		if (propValue != null) {
			return Boolean.valueOf(propValue);
		}

		return true;
	}

	private static int maxPoolSize() {
		final String propValue = System.getProperty("woody.max.pool.size");

		if (propValue != null) {
			return Integer.valueOf(propValue);
		}

		return Runtime.getRuntime().availableProcessors() * 50;
	}

	private static ThreadPoolExecutor newThreadPoolExecutor() {
		final int maxPoolSize = maxPoolSize();
		final int availableProcessors = Runtime.getRuntime().availableProcessors();
		final int corePoolSize = availableProcessors < maxPoolSize ? maxPoolSize : availableProcessors;
		final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS, queue, THREAD_FACTORY);
	}
}
