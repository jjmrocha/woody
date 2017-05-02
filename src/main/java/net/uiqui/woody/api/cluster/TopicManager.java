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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgroups.Address;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.annotations.Actor;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.api.cluster.msg.CastMessage;
import net.uiqui.woody.api.cluster.msg.ClusterEvent;
import net.uiqui.woody.api.util.ActorNames;
import net.uiqui.woody.api.util.TopicNames;

public class TopicManager {
	private final Set<String> topics = new TreeSet<String>();
	private final Map<String, Set<Address>> subscriptions = new HashMap<String, Set<Address>>();

	private Address node = null;

	@Actor(ActorNames.CLUSTER_MESSAGE_SENDER) private ActorRef sender = null;
	@Actor(ActorNames.CLUSTER_NODE_MANAGER) private ActorRef cluster = null;

	public TopicManager(final Address node) {
		this.node = node;
	}

	@Subscription(TopicNames.NEW_TOPIC)
	public void handleNewTopic(final String topicName) {
		topics.add(topicName);

		final RoutingRequest request = new RoutingRequest(topicName, node);
		final CastMessage message = new CastMessage(ActorNames.CLUSTER_TOPIC_MANAGER, request);
		@SuppressWarnings("unchecked") 
		final Collection<Address> nodes = (Collection<Address>) cluster.syncCall("nodes");
		sender.cast(new SendRequest(nodes, message));
	}

	@Subscription(TopicNames.NODE_UP)
	public void handleNodeUp(final Address nodeAddress) {
		final RoutingRequest request = new RoutingRequest(topics, node);
		final CastMessage message = new CastMessage(ActorNames.CLUSTER_TOPIC_MANAGER, request);
		sender.cast(new SendRequest(nodeAddress, message));
	}

	@Subscription(TopicNames.NODE_DOWN)
	public void handleNodeDown(final Address nodeAddress) {
		for (Set<Address> addresses : subscriptions.values()) {
			addresses.remove(nodeAddress);
		}
	}

	@CastHandler
	public void handleClusterEvent(final ClusterEvent event) {
		final Set<Address> addresses = subscriptions.get(event.getTopic());

		if (addresses != null) {
			sender.cast(new SendRequest(addresses, event));
		}
	}

	@CastHandler
	public void handleRoutingRequest(final RoutingRequest request) {
		if (!request.node.equals(node)) {
			for (String topic : request.topics) {
				Set<Address> addresses = subscriptions.get(topic);

				if (addresses != null) {
					addresses.add(request.node);
				} else {
					addresses = new TreeSet<Address>();
					subscriptions.put(topic, addresses);
				}
			}
		}
	}

	private static class RoutingRequest implements Serializable {
		private static final long serialVersionUID = 8763758729434416916L;

		private List<String> topics = new ArrayList<String>();
		private Address node = null;

		public RoutingRequest(final String topic, final Address node) {
			this.topics.add(topic);
			this.node = node;
		}

		public RoutingRequest(final Collection<String> topics, final Address node) {
			this.topics.addAll(topics);
			this.node = node;
		}
	}
}
