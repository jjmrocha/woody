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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.uiqui.woody.Woody;
import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.CastHandler;
import net.uiqui.woody.api.cluster.Node;
import net.uiqui.woody.api.cluster.msg.ClusterUpdate;
import net.uiqui.woody.api.util.FutureResult;
import net.uiqui.woody.api.util.TopicNames;

public class ClusterManager extends AbstractNodeBasedActor {
	private Set<Node> nodes = new TreeSet<Node>();
	
	public ClusterManager(final FutureResult<Node> selfFuture) {
		super(selfFuture);
	}
	
	@CallHandler("node")
	public Node getNode() {
		return self();
	}
	
	@CallHandler("nodes")
	public Collection<Node> getNodes() {
		return nodes;
	}
	
	@CastHandler
	public void handleClusterUpdate(final ClusterUpdate cluster) {
		final List<Node> toRemove = new ArrayList<Node>();
		
		for (Node oldNode : nodes) {
			if (!cluster.contains(oldNode)) {
				toRemove.add(oldNode);
				Woody.publish(TopicNames.NODE_DOWN, oldNode);
			}
		}
		
		nodes.removeAll(toRemove);
		
		for(Node newNode : cluster.getNodes()) {
			if (!newNode.equals(self())) {
				if (!nodes.contains(newNode)) {
					nodes.add(newNode);
					Woody.publish(TopicNames.NODE_UP, newNode);
				}
			}
		}
	}
}
