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
package net.uiqui.woody.lib;

import net.uiqui.woody.api.error.InvalidActorException;

public class ActorFactory {
	public static Object newActor(final Class<?> clazz) throws InvalidActorException {
		try {
			return clazz.newInstance();
		} catch (final Exception e) {
			throw new InvalidActorException("Error creating instance of " + clazz.getName(), e);
		}
	}
}
