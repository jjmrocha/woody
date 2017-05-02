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

import java.io.Serializable;
import java.util.concurrent.Future;

import net.uiqui.woody.api.AbsActorRef;
import net.uiqui.woody.api.error.ActorNotFounfError;
import net.uiqui.woody.api.util.Ring;

/**
 * This class can be use to interact with a group of actors
 */
public class ActorGroup extends AbsActorRef {
	private final Ring<ActorRef> group = new Ring<ActorRef>();
	
	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#cast(java.io.Serializable)
	 */
	public void cast(final Serializable msg) {
		final ActorRef actor = group.next();
		
		if (actor != null) {
			actor.cast(msg);
		} else {
			throw new ActorNotFounfError("No actor available on actor group to receive the message");
		}
	}

	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#call(java.lang.String, java.io.Serializable)
	 */
	public Future<Serializable> call(final String operation, final Serializable payload) {
		final ActorRef actor = group.next();
		
		if (actor != null) {
			return actor.call(operation, payload);
		} else {
			throw new ActorNotFounfError("No actor available on actor group to process the RPC request");
		}
	}
	
	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#call(java.lang.String)
	 */
	public Future<Serializable> call(final String operation) {
		final ActorRef actor = group.next();
		
		if (actor != null) {
			return actor.call(operation);
		} else {
			throw new ActorNotFounfError("No actor available on actor group to process the RPC request");
		}
	}
	
	/**
	 * Adds a new actor to the group
	 * 
	 * @param actor the actor to be added
	 */
	public void addMember(final ActorRef actor) {
		group.add(actor);
	}
	
	/**
	 * Adds a new actor to the group
	 * 
	 * @param clazz the class for the actor creation
	 */
	public void addMember(final Class<?> clazz) {
		final ActorRef actor = Woody.newActor(clazz);
		addMember(actor);
	}
	
	/**
	 * Adds some actors to the group
	 * 
	 * @param clazz the class for the actor creation
	 * @param size the number of actor to create
	 */
	public void addMembers(final Class<?> clazz, final int size) {
		for (int i = 0; i < size; i++) {
			addMember(clazz);
		}
	}
}
