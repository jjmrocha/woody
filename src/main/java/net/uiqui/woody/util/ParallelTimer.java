/*
 * Woody - core
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
package net.uiqui.woody.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class ParallelTimer {
	private Timer timer = null;
	private Executor executor = null;
	
	public ParallelTimer(final Executor executor) {
		this.timer = new Timer(true);
		this.executor = executor;
	}
	
	public void schedule(final Runnable task, final long delay) {
		timer.schedule(timerTask(task), delay);
	}

	public void schedule(final Runnable task, final Date time) {
		timer.schedule(timerTask(task), time);
	}

	public void schedule(final Runnable task, final long delay, final long period) {
		timer.schedule(timerTask(task), delay, period);
	}

	public void schedule(final Runnable task, final Date firstTime, final long period) {
		timer.schedule(timerTask(task), firstTime, period);
	}

	public void scheduleAtFixedRate(final Runnable task, final long delay, final long period) {
		timer.scheduleAtFixedRate(timerTask(task), delay, period);
	}

	public void scheduleAtFixedRate(final Runnable task, final Date firstTime, final long period) {
		timer.scheduleAtFixedRate(timerTask(task), firstTime, period);
	}	
	
	private TimerTask timerTask(final Runnable runnable) {
		return new TimerTask() {
			public void run() {
				executor.execute(runnable);
			}
		};
	}
	
	public void cancel() {
		timer.cancel();
	}
}