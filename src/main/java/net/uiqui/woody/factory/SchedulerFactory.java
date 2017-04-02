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
package net.uiqui.woody.factory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.uiqui.woody.Broker;

/**
 * A factory for creating Scheduler objects.
 */
public class SchedulerFactory {
	private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, DeamonFactory.THREAD_FACTORY);
	
	/**
	 * Schedule after.
	 *
	 * @param delay the delay
	 * @param command the command
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> scheduleAfter(final long delay, final Runnable command) {
		return EXECUTOR_SERVICE.schedule(command, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedule interval.
	 *
	 * @param delay the delay
	 * @param command the command
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> scheduleInterval(final long delay, final Runnable command) {
		return EXECUTOR_SERVICE.scheduleAtFixedRate(command, delay, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Send after.
	 *
	 * @param delay the delay
	 * @param name the name
	 * @param msg the msg
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> sendAfter(final long delay, final String name, final Object msg) {
		return scheduleAfter(delay, new Runnable() {
			@Override
			public void run() {
				Broker.send(name, msg);
			}
		});
	}
	
	/**
	 * Send interval.
	 *
	 * @param delay the delay
	 * @param name the name
	 * @param msg the msg
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> sendInterval(final long delay, final String name, final Object msg) {
		return scheduleInterval(delay, new Runnable() {
			@Override
			public void run() {
				Broker.send(name, msg);
			}
		});
	}
}
