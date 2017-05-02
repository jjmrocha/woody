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

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.Woody;
import net.uiqui.woody.api.Event;
import net.uiqui.woody.api.cluster.msg.ClusterEvent;
import net.uiqui.woody.api.cluster.msg.NodeList;
import net.uiqui.woody.api.error.WoodyException;
import net.uiqui.woody.api.util.ActorNames;

public class Gateway extends ReceiverAdapter {
	private JChannel channel = null;
	private ActorRef topic = null;
	private ActorRef cluster = null;
	private ActorRef processor = null;
	private Address node = null;
	
	public Gateway() {
		try {
			channel = new JChannel();
			channel.setReceiver(this);
			channel.connect("Woody");
			node = channel.getAddress();
		} catch (Exception e) {
			throw new WoodyException(e);
		} 
		
		Woody.register(ActorNames.CLUSTER_MESSAGE_SENDER, new MessageSender(channel));
		topic = Woody.register(ActorNames.CLUSTER_TOPIC_MANAGER, new TopicManager(node));
		cluster = Woody.register(ActorNames.CLUSTER_NODE_MANAGER, new ClusterManager(node));
		processor = Woody.newActorGroup(ActorNames.CLUSTER_MESSAGE_PROCESSOR, MessageProcessor.class, Runtime.getRuntime().availableProcessors());
	}
	
	// Receiver implementation
	@Override
	public void viewAccepted(final View view) {
		if (cluster != null) {
			cluster.cast(new NodeList(view.getMembers()));
		}
	}

	@Override
	public void receive(final Message msg) {
		if (processor != null) {
			processor.cast(new MessageReceived(msg.src(), (Serializable) msg.getObject()));
		}
	}
	
	// Gateway methods
	public String getNodeName() {
		return node.toString();
	}
	
	public void route(final Event event) {
		topic.cast(new ClusterEvent(event.getTopic(), event.getPayload()));
	}
}
