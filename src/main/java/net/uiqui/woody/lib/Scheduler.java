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

public class Scheduler {
	private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, Runner.THREAD_FACTORY);

	public static ScheduledFuture<?> scheduleAfter(final long delay, final Runnable command) {
		return EXECUTOR_SERVICE.schedule(command, delay, TimeUnit.MILLISECONDS);
	}

	public static ScheduledFuture<?> scheduleInterval(final long interval, final Runnable command) {
		return EXECUTOR_SERVICE.scheduleAtFixedRate(command, interval, interval, TimeUnit.MILLISECONDS);
	}

}
