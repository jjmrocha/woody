/*
 * Woody - core
 * 
 * Copyright (C) 2014 Joaquim Rocha <jrocha@gmailbox.org>
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
package net.uiqui.woody;

import java.io.Serializable;

import net.uiqui.woody.error.InvalidEndpointException;

public class Endpoint implements Serializable {
	private static final long serialVersionUID = -7406089919193703525L;
	
	private static final String WOODY_TYPE = "w:";
	private static final String ACTOR_BASE = "a";
	private static final String TOPIC_BASE = "t";
	
	private String url = null;
	private transient int hashCode = 0;
	
	private Endpoint(final String address) {
		this.url = generateUrl(address);
	}
	
	@Override
	public String toString() {
		return url;
	}

	private String generateUrl(final String address) {
		final StringBuilder buffer = new StringBuilder();
		
		buffer.append(WOODY_TYPE);
		buffer.append(address);
		
		return buffer.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		
		if (! (o instanceof Endpoint)) {
			return false;
		}
		
		final Endpoint endpoint = (Endpoint) o;
		
		return url.equals(endpoint.url);
	}
	
	@Override
	public int hashCode() {
		if (hashCode == 0) {
			hashCode = url.hashCode();
		}
		
		return hashCode;
	}
	
	public static Endpoint getEndpoint(final String address) {
		return new Endpoint(address);
	}	
	
	public static Endpoint getEndpointForTopic(final String topicName) {
		return new Endpoint(buildName(TOPIC_BASE, topicName));
	}	
	
	public static Endpoint getEndpointForActor(final String actorName) {
		return new Endpoint(buildName(ACTOR_BASE, actorName));
	}	
	
	private static String buildName(final String base, final String name) {
		final StringBuilder buffer = new StringBuilder();
		
		buffer.append(base);
		buffer.append("/");
		buffer.append(name);
		
		return buffer.toString();
	}
	
	public static Endpoint parse(final String url) throws InvalidEndpointException {		
		if (!url.startsWith(WOODY_TYPE)) {
			throw new InvalidEndpointException(url);
		}
		
		final int startAddressPos = WOODY_TYPE.length();
		
		final String address = url.substring(startAddressPos);
		
		return new Endpoint(address);
	}	
}
