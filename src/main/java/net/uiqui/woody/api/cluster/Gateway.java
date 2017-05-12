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
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;
import net.uiqui.woody.api.Event;
import net.uiqui.woody.api.cluster.actor.ClusterManager;
import net.uiqui.woody.api.cluster.actor.MessageProcessor;
import net.uiqui.woody.api.cluster.actor.MessageSender;
import net.uiqui.woody.api.cluster.actor.TopicManager;
import net.uiqui.woody.api.cluster.msg.ClusterUpdate;
import net.uiqui.woody.api.cluster.msg.EventBroadcast;
import net.uiqui.woody.api.cluster.msg.MessageReceived;
import net.uiqui.woody.api.error.WoodyException;
import net.uiqui.woody.api.util.ActorNames;
import net.uiqui.woody.api.util.FutureResult;

public class Gateway extends ReceiverAdapter {
	private JChannel channel = null;
	private ActorRef topic = null;
	private ActorRef cluster = null;
	private ActorRef processor = null;
	private Node self = null;
	private List<Node> clusterNodes = null;
	
	public Gateway() {		
		final FutureResult<Node> selfFuture = new FutureResult<Node>();
		topic = Woody.register(ActorNames.CLUSTER_TOPIC_MANAGER, new TopicManager(selfFuture));
		cluster = Woody.register(ActorNames.CLUSTER_NODE_MANAGER, new ClusterManager(selfFuture));
		processor = Woody.newActorGroup(ActorNames.CLUSTER_MESSAGE_PROCESSOR, MessageProcessor.class, Runtime.getRuntime().availableProcessors());
		
		try {
			channel = new JChannel();
			Woody.register(ActorNames.CLUSTER_MESSAGE_SENDER, new MessageSender(channel));
			
			channel.setReceiver(this);
			channel.connect("Woody");
			
			self = Node.build(channel.getAddress());
			selfFuture.setResult(self);
		} catch (Exception e) {
			throw new WoodyException(e);
		} 
		
		// In case we are not the first node to start
		if (clusterNodes != null) {
			cluster.cast(new ClusterUpdate(clusterNodes));
		}
	}
	
	// Receiver implementation
	@Override
	public void viewAccepted(final View view) {
		clusterNodes = Node.build(view.getMembers());
		
		if (cluster != null) {
			cluster.cast(new ClusterUpdate(clusterNodes));
		}
	}

	@Override
	public void receive(final Message msg) {
		if (processor != null) {
			final Node from = Node.build(msg.src());
			
			if (from != null && !from.equals(self)) {
				processor.cast(new MessageReceived(from, (Serializable) msg.getObject()));
			}
		}
	}
	
	// Gateway methods
	public String getNodeName() {
		return self.getName();
	}
	
	public void route(final Event event) {
		topic.cast(new EventBroadcast(event.getTopic(), event.getPayload()));
	}
}
