/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2014-16 Joaquim Rocha <jrocha@gmailbox.org>
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

import net.uiqui.woody.util.ReferenceUtil;

public abstract class LeadingActor<E> extends Actor<E> implements Runnable {
	private String actorName = null;
	
	public LeadingActor() {
		this(ReferenceUtil.getReference());
	}

	public LeadingActor(final String actorName) {
		super(actorName);
		
		this.actorName = actorName;
	}

	public void start() {
		Thread thread = new Thread(this, actorName);
		thread.start();
	}
}
