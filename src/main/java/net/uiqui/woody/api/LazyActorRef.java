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
package net.uiqui.woody.api;

import java.io.Serializable;
import java.util.concurrent.Future;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;
import net.uiqui.woody.api.error.ActorNotFounfError;

public class LazyActorRef implements ActorRef {
	private String name = null;
	private ActorRef actor = null;
	
	public LazyActorRef(final String name) {
		this.name = name;
	}

	public void cast(final Serializable msg) {
		final ActorRef ref = getActorRef();
		ref.cast(msg);
	}

	public Future<Serializable> call(final String operation, final Serializable payload) {
		final ActorRef ref = getActorRef();
		return ref.call(operation, payload);
	}
	
	public Future<Serializable> call(final String operation) {
		final ActorRef ref = getActorRef();
		return ref.call(operation);
	}	

	private ActorRef getActorRef() {
		if (actor == null) {
			lazyLoad();
		}
		
		return actor;
	}

	private synchronized void lazyLoad() {
		if (actor != null) {
			return;
		}
		
		actor = Woody.getActorRef(name);
		
		if (actor == null) {
			throw new ActorNotFounfError("Actor " + name + " is not registered!");
		}
	}

}
