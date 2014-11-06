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

import java.util.concurrent.ThreadFactory;

public class ThreadFactoryUtil implements ThreadFactory {
	private static final int NO_PRIORITY = -1;
	
	private boolean daemon = false;
	private int priority = NO_PRIORITY;
	private String name = null;
	private long count = 0;
	
	private ThreadFactoryUtil(final String name, final boolean daemon, final int priority) {
		this.name = name;
		this.daemon = daemon;
		this.priority = priority;
	}

	@Override
	public Thread newThread(final Runnable target) {
		Thread thread = new Thread(target);
		
		if (name != null) {
			thread.setName(name());
		}
		
		thread.setDaemon(daemon);
		
		if (priority != NO_PRIORITY) {
			thread.setPriority(priority);
		}
		
		return thread;
	}

	private String name() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(name);
		builder.append("-");
		builder.append(count++);
		
		return builder.toString();
	}

	public static ThreadFactory daemonFactory() {
		return daemonFactory(null, NO_PRIORITY);
	}
	
	public static ThreadFactory daemonFactory(final String name) {
		return daemonFactory(name, NO_PRIORITY);
	}
	
	public static ThreadFactory daemonFactory(final int priority) {
		return daemonFactory(null, priority);
	}	
	
	public static ThreadFactory daemonFactory(final String name, final int priority) {
		return new ThreadFactoryUtil(name, true, priority);
	}	
	
	public static ThreadFactory threadFactory() {
		return threadFactory(null, NO_PRIORITY);
	}
	
	public static ThreadFactory threadFactory(final String name) {
		return threadFactory(name, NO_PRIORITY);
	}
	
	public static ThreadFactory threadFactory(final int priority) {
		return threadFactory(null, priority);
	}	
	
	public static ThreadFactory threadFactory(final String name, final int priority) {
		return new ThreadFactoryUtil(name, false, priority);
	}	
}
