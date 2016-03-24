/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2016 Joaquim Rocha <jrocha@gmailbox.org>
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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DeamonPool {
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		@Override
		public Thread newThread(final Runnable r) {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			return thread;
		}
	};
	
	public static Executor newFixedDaemonPool(final int maxSize) {
		return Executors.newFixedThreadPool(maxSize, THREAD_FACTORY);
	}
	
	public static Executor newCachedDaemonPool() {
		return Executors.newCachedThreadPool(THREAD_FACTORY);
	}	
	
	public static Executor newSingleDaemonExecutor() {
		return Executors.newSingleThreadExecutor(THREAD_FACTORY);
	}	
}
