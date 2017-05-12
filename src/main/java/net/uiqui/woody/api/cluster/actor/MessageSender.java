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

import org.jgroups.Address;
import org.jgroups.JChannel;

import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.cluster.Node;
import net.uiqui.woody.api.cluster.msg.SendRequest;

public class MessageSender {
	private JChannel channel = null;
	
	public MessageSender(final JChannel channel) {
		this.channel = channel;
	}
	
	@CastHandler
	public void handleMessage(final SendRequest request) throws Exception {
		if (request.getNodes().isEmpty()) {
			send(null, request.getPayload());
		} else {
			for (Node node : request.getNodes()) {
				send(node.getAddress(), request.getPayload());
			}
		}
	}

	private void send(final Address address, final Serializable payload) throws Exception {
		channel.send(address, payload);
	}
}
