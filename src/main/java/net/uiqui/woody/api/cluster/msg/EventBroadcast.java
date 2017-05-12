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

public class EventBroadcast implements Serializable {
	private static final long serialVersionUID = 8537313324456412898L;
	
	private String topic = null;
	private Serializable payload = null;
	
	public EventBroadcast(final String topic, final Serializable payload) {
		this.topic = topic;
		this.payload = payload;
	}

	public String getTopic() {
		return topic;
	}

	public Serializable getPayload() {
		return payload;
	}
}
