/*
 * Woody - Basic Actor model implementation
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
package net.uiqui.woody.actor;

import java.util.concurrent.Executor;

import net.uiqui.woody.util.ReferenceUtil;

public abstract class SupportingActor<E> extends Actor<E> {
	private Executor threadPool = null;

	public SupportingActor(final Executor threadPool) {
		this(ReferenceUtil.getReference(), threadPool);
	}
	
	public SupportingActor(final String actorName, final Executor threadPool) {
		super(actorName);
		this.threadPool = threadPool;
	}
	
	public void onMessage(final E msg) {
		threadPool.execute(new Runnable() {
			public void run() {
				handle(msg);
			}
		});
	}
}
