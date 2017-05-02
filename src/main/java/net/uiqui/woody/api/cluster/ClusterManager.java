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

import java.util.ArrayList;
import java.util.List;

import org.jgroups.Address;

import net.uiqui.woody.Woody;
import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.cluster.msg.NodeList;
import net.uiqui.woody.api.util.TopicNames;

public class ClusterManager {
	private Address node = null;
	private List<Address> nodes = new ArrayList<Address>();
	
	public ClusterManager(final Address node) {
		this.node = node;
	}
	
	@CallHandler("node")
	public Address getNode() {
		return node;
	}
	
	@CallHandler("nodes")
	public List<Address> getNodes() {
		return nodes;
	}
	
	@CastHandler
	public void handleView(final NodeList nodeList) {
		for(Address newNode : nodeList.getNodes()) {
			if (!newNode.equals(node)) {
				if (!nodes.contains(newNode)) {
					nodes.add(newNode);
					Woody.publish(TopicNames.NODE_UP, newNode);
				}
			}
		}
		
		for (Address oldNode : nodes) {
			if (!nodeList.contains(oldNode)) {
				nodes.remove(oldNode);
				Woody.publish(TopicNames.NODE_DOWN, oldNode);
			}
		}
	}
}
