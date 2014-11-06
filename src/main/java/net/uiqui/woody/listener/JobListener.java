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
package net.uiqui.woody.listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JobListener extends ListenerPusher<Runnable> {
	
	public JobListener() {
		this(new ConcurrentLinkedQueue<Runnable>());	
	}
	
	public JobListener(final int queueMaxSize) {
		this(new LinkedBlockingQueue<Runnable>(queueMaxSize));	
	}
	
	public JobListener(final Queue<Runnable> queue) {
		super(getListener(), queue);		
	}	

	private static Listener<Runnable> getListener() {
		return new Listener<Runnable>() {
			public void onMessage(final Runnable task) {
				task.run();
			}			
		};
	}
}
