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
package net.uiqui.woody.api.cluster.actor;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;
import net.uiqui.woody.annotations.Actor;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.cluster.Node;
import net.uiqui.woody.api.cluster.msg.CastMessage;
import net.uiqui.woody.api.cluster.msg.EventBroadcast;
import net.uiqui.woody.api.cluster.msg.MessageReceived;
import net.uiqui.woody.api.util.ActorNames;

public class MessageProcessor {
	@Actor(ActorNames.CLUSTER_TOPIC_MANAGER) private ActorRef topic = null;
	
	@CastHandler
	public void handleMessage(final MessageReceived msg) {
		if (msg.getPayload() instanceof EventBroadcast) {
			handleEvent((EventBroadcast) msg.getPayload());
		} else {
			if (msg.getPayload() instanceof CastMessage) {
				final CastMessage castMessage = (CastMessage) msg.getPayload();
				
				if (ActorNames.CLUSTER_TOPIC_MANAGER.equals(castMessage.getName())) {
					topic.cast(msg);
				} else {
					handleCast(msg.getFrom(), castMessage);
				}
			}
		}
	}
	
	private void handleCast(final Node node, final CastMessage msg) {
		final ActorRef actorRef = Woody.getActorRef(msg.getName());
		
		if (actorRef != null) {
			actorRef.cast(msg.getPayload());
		}
	}

	public void handleEvent(final EventBroadcast event) {
		Woody.publishLocally(event.getTopic(), event.getPayload());
	}
}
