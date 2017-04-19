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
package net.uiqui.woody;

import java.util.concurrent.Future;

import net.uiqui.woody.api.error.ActorNotFounfError;
import net.uiqui.woody.api.util.Ring;

/**
 * This class can be use to interact with a group of actors
 */
public class ActorGroup implements ActorRef {
	private final Ring<ActorRef> group = new Ring<ActorRef>();
	
	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#cast(java.lang.Object)
	 */
	public void cast(final Object msg) {
		final ActorRef actor = group.get();
		
		if (actor != null) {
			actor.cast(msg);
		} else {
			throw new ActorNotFounfError("No actor available on actor group to receive the message");
		}
	}

	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#call(java.lang.String, java.lang.Object)
	 */
	public Future<Object> call(final String operation, final Object payload) {
		final ActorRef actor = group.get();
		
		if (actor != null) {
			return actor.call(operation, payload);
		} else {
			throw new ActorNotFounfError("No actor available on actor group to process the RPC request");
		}
	}
	
	public void addMember(final ActorRef actor) {
		group.add(actor);
	}
	
	public void addMember(final Class<?> clazz) {
		final ActorRef actor = Woody.newActor(clazz);
		group.add(actor);
	}
	
	public void addMembers(final Class<?> clazz, final int size) {
		for (int i = 0; i < size; i++) {
			addMember(clazz);
		}
	}
}
