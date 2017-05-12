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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.annotations.Actor;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.api.cluster.Node;
import net.uiqui.woody.api.cluster.msg.CastMessage;
import net.uiqui.woody.api.cluster.msg.EventBroadcast;
import net.uiqui.woody.api.cluster.msg.MessageReceived;
import net.uiqui.woody.api.cluster.msg.SendRequest;
import net.uiqui.woody.api.util.ActorNames;
import net.uiqui.woody.api.util.FutureResult;
import net.uiqui.woody.api.util.TopicNames;

public class TopicManager extends AbstractNodeBasedActor {
	private final Set<String> topics = new TreeSet<String>();
	private final Map<String, Set<Node>> subscriptions = new HashMap<String, Set<Node>>();

	@Actor(ActorNames.CLUSTER_MESSAGE_SENDER) private ActorRef sender = null;
	@Actor(ActorNames.CLUSTER_NODE_MANAGER) private ActorRef cluster = null;

	public TopicManager(final FutureResult<Node> selfFuture) {
		super(selfFuture);
	}

	@Subscription(TopicNames.NEW_TOPIC)
	public void handleNewTopic(final String topicName) {
		topics.add(topicName);
		final Collection<Node> nodes = cluster.syncCall("nodes");
		
		if (!nodes.isEmpty()) {
			final RoutingRequest request = new RoutingRequest(topicName);
			final CastMessage message = new CastMessage(ActorNames.CLUSTER_TOPIC_MANAGER, request);
			sender.cast(new SendRequest(nodes, message));
		}
	}

	@Subscription(TopicNames.NODE_UP)
	public void handleNodeUp(final Node node) {
		if (!topics.isEmpty()) {
			final RoutingRequest request = new RoutingRequest(topics);
			final CastMessage message = new CastMessage(ActorNames.CLUSTER_TOPIC_MANAGER, request);
			sender.cast(new SendRequest(node, message));
		}
	}

	@Subscription(TopicNames.NODE_DOWN)
	public void handleNodeDown(final Node node) {
		for (Set<Node> nodes : subscriptions.values()) {
			nodes.remove(node);
		}
	}

	@CastHandler
	public void handleEventBroadcast(final EventBroadcast event) {
		final Set<Node> nodes = subscriptions.get(event.getTopic());

		if (nodes != null && !nodes.isEmpty()) {
			sender.cast(new SendRequest(nodes, event));
		}
	}

	@CastHandler
	public void handleMessageReceived(final MessageReceived msg) {
		final CastMessage castMessage = (CastMessage) msg.getPayload();
		
		if (castMessage.getPayload() instanceof RoutingRequest) {
			handleRoutingRequest(msg.getFrom(), (RoutingRequest) castMessage.getPayload());
		}
	}
	
	public void handleRoutingRequest(final Node from, RoutingRequest request) {		
		if (!from.equals(self())) {
			for (String topic : request.topics) {				
				Set<Node> nodes = subscriptions.get(topic);

				if (nodes != null) {
					nodes.add(from);
				} else {
					nodes = new TreeSet<Node>();
					subscriptions.put(topic, nodes);
				}
			}
		}
	}	

	private static class RoutingRequest implements Serializable {
		private static final long serialVersionUID = 8763758729434416916L;

		private List<String> topics = new ArrayList<String>();

		public RoutingRequest(final String topic) {
			this.topics.add(topic);
		}

		public RoutingRequest(final Collection<String> topics) {
			this.topics.addAll(topics);
		}
	}
}
