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
import java.util.List;

import org.jgroups.Address;

public class Node implements Serializable, Comparable<Node> {
	private static final long serialVersionUID = 8188085731074283014L;
	
	private Address address = null;
	
	public Node(final Address address) {
		this.address = address;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public String getName() {
		return address.toString();
	}
	
	@Override
	public int compareTo(Node o) {
		return address.compareTo(o.address);
	}
	
	@Override
	public int hashCode() {
		return 31 + address.hashCode();
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof Node)) {
			return false;
		}
		
		final Node other = (Node) obj;
		
		return address.equals(other.address);
	}
	
	public static Node build(final Address address) {
		if (address == null) {
			return null;
		}
		
		return new Node(address);
	}
	
	public static List<Node> build(final List<Address> addresses) {
		final List<Node> list = new ArrayList<Node>();
		
		if (addresses != null) {
			for (Address address : addresses) {
				list.add(build(address));
			}
		}
		
		return list;
	}
}
