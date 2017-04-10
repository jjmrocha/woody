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

import java.util.concurrent.Future;

/**
 * This interface allows the interaction with an actor  
 */
public interface ActorRef {		
	/**
	 * Send a message asynchronously to the actor instance 
	 * the message will be delivered to a method marked with
	 * the CastHandler annotation  
	 *
	 * @param msg message to send asynchronously
	 */
	public void cast(final Object msg);
	
	/**
	 * Invokes asynchronously one of the methods marked with the CallHandler
	 * annotation for the operation.
	 *
	 * @param operation name of the operation to invoke
	 * @param payload call's argument
	 * @return the method's return value
	 */
	public Future<Object> call(final String operation, final Object payload);
}
