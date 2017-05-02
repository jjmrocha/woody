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
package net.uiqui.woody.api.cluster;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.cluster.msg.CastMessage;
import net.uiqui.woody.api.cluster.msg.ClusterEvent;

public class MessageProcessor {
	@CastHandler
	public void handleMessage(final MessageReceived msg) {
		if (msg.getPayload() instanceof ClusterEvent) {
			handleEvent((ClusterEvent) msg.getPayload());
		} else if (msg.getPayload() instanceof CastMessage) {
			handleCast((CastMessage) msg.getPayload());
		}
	}
	
	private void handleCast(final CastMessage msg) {
		final ActorRef actorRef = Woody.getActorRef(msg.getName());
		
		if (actorRef != null) {
			actorRef.cast(msg.getPayload());
		}
	}

	public void handleEvent(final ClusterEvent event) {
		Woody.publish(event.getTopic(), event.getPayload());
	}
}
