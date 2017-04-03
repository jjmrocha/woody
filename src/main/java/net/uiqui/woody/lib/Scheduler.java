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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.uiqui.woody.Woody;

/**
 * Runs a scheduler for execution of tasks and sending messages 
 */
public class Scheduler {
	private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, Runner.THREAD_FACTORY);
	
	/**
	 * Schedule a task for execution after specific delay in milliseconds.
	 *
	 * @param delay the delay in milliseconds
	 * @param command the command
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> scheduleAfter(final long delay, final Runnable command) {
		return EXECUTOR_SERVICE.schedule(command, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedule a task for periodic execution
	 *
	 * @param interval the execution interval in milliseconds
	 * @param command the command
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> scheduleInterval(final long interval, final Runnable command) {
		return EXECUTOR_SERVICE.scheduleAtFixedRate(command, interval, interval, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Send a message to an actor after a specific delay in milliseconds.
	 *
	 * @param delay the delay in milliseconds
	 * @param name the actor's name
	 * @param msg the message to send
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> sendAfter(final long delay, final String name, final Object msg) {
		return scheduleAfter(delay, new Runnable() {
			@Override
			public void run() {
				Woody.cast(name, msg);
			}
		});
	}
	
	/**
	 * Send a periodic message to an actor.
	 *
	 * @param interval the interval in milliseconds
	 * @param name the actor's name
	 * @param msg the message to send
	 * @return the scheduled future
	 */
	public static ScheduledFuture<?> sendInterval(final long interval, final String name, final Object msg) {
		return scheduleInterval(interval, new Runnable() {
			@Override
			public void run() {
				Woody.cast(name, msg);
			}
		});
	}
}
