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
package net.uiqui.woody.api.cluster.msg;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import net.uiqui.woody.api.cluster.Node;

public class SendRequest implements Serializable {
	private static final long serialVersionUID = 1579420424182265154L;
	
	private Set<Node> nodes = new TreeSet<Node>();
	private Serializable payload = null;
	
	public SendRequest(final Serializable payload) {
		this.payload = payload;
	}
	
	public SendRequest(final Node node, final Serializable payload) {
		this.nodes.add(node);
		this.payload = payload;
	}
	
	public SendRequest(final Collection<Node> nodes, final Serializable payload) {
		this.nodes.addAll(nodes);
		this.payload = payload;
	}

	public Collection<Node> getNodes() {
		return nodes;
	}

	public Serializable getPayload() {
		return payload;
	}
}
