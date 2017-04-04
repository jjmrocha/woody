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

/**
 * This class is responsible for execution of tasks using thread pools
 * 
 * The behavior of this class can be customized using the system properties:
 * 
 * <pre>
 * woody.use.daemon.threads The threads used by Woody are all daemon? (default value is true)
 * woody.max.pool.size      Max number of threads to allow on the pool (default value is 50 threads for each processor core)
 * </pre>
 */
public class Runner {
	private static final boolean USE_DAEMON_THREADS = useDeamonThreads();

	protected static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		@Override
		public Thread newThread(final Runnable command) {
			final Thread thread = new Thread(command);
			thread.setDaemon(USE_DAEMON_THREADS);
			return thread;
		}
	};

	private static final Executor THREAD_POOL = newThreadPoolExecutor();
	private static final Executor THREAD_QUEUE = Executors.newSingleThreadExecutor(THREAD_FACTORY);

	/**
	 * Executes a task using a CachedThreadPool
	 *
	 * @param command
	 *            the command to execute
	 */
	public static void spawn(final Runnable command) {
		THREAD_POOL.execute(command);
	}

	/**
	 * Run a that in a new tread
	 *
	 * @param command
	 *            the command to execute
	 */
	public static void run(final Runnable command) {
		final Thread thread = THREAD_FACTORY.newThread(command);
		thread.start();
	}

	/**
	 * Executes a task using a SingleThreadExecutor
	 *
	 * @param command
	 *            the command to execute
	 */
	public static void queue(final Runnable command) {
		THREAD_QUEUE.execute(command);
	}

	/**
	 * Causes the currently executing thread to sleep
	 *
	 * @param delay
	 *            the sleep period
	 * @param unit
	 *            the time unit of the delay argument
	 */
	public static void sleep(final long delay, final TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(delay));
		} catch (InterruptedException e) {
		}
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
