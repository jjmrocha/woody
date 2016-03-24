/*
 * Woody - Basic Actor model implementation
 * 
 * Copyright (C) 2016 Joaquim Rocha <jrocha@gmailbox.org>
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
package net.uiqui.woody.rpc.impl;

import java.io.Serializable;

import net.uiqui.woody.Endpoint;

public class RPCMessage<E> implements Serializable {
	private static final long serialVersionUID = -1741152421937631613L;
	
	private E value = null;
	private Endpoint replyTo = null;
	
	public RPCMessage(final Endpoint replyTo, final E value) {
		this.replyTo = replyTo;
		this.value = value;
	}

	public Endpoint replyTo() {
		return replyTo;
	}

	public E value() {
		return value;
	}
}
