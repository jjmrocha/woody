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
package net.uiqui.woody;

import net.uiqui.woody.api.error.WoodyException;

public abstract class Actor {
	private String name = null;
	
	public Actor(final String name) throws WoodyException {
		this.name = name;
		Broker.register(name, this);
	}
	
	public Actor() throws WoodyException {
		this.name = Broker.register(this);
	}
	
	public String getName() {
		return name;
	}
	
	public void send(final Object msg) {
		Broker.send(name, msg);
	}
}
