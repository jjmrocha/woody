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
package net.uiqui.woody.api;

import java.io.Serializable;
import java.util.concurrent.Future;

import net.uiqui.woody.ActorRef;
import net.uiqui.woody.api.error.WoodyException;

public abstract class AbsActorRef implements ActorRef {
	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#syncCall(java.lang.String, java.io.Serializable)
	 */
	public Serializable syncCall(final String operation, final Serializable payload) {
		final Future<Serializable> future = call(operation, payload); 
		
		try {
			return future.get();
		} catch (Exception e) {
			throw new WoodyException("Error calling operation " + operation, e);
		}
	}

	/* (non-Javadoc)
	 * @see net.uiqui.woody.ActorRef#syncCall(java.lang.String)
	 */
	public Serializable syncCall(final String operation) {
		final Future<Serializable> future = call(operation); 
		
		try {
			return future.get();
		} catch (Exception e) {
			throw new WoodyException("Error calling operation " + operation, e);
		}
	}	
}
