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
package net.uiqui.woody.api.util;

public class TopicNames {
	public static final String NEW_TOPIC = "woody.topic.creation";
	public static final String NODE_UP = "woody.node.up";
	public static final String NODE_DOWN = "woody.node.down";
	
	private static final String [] INTERNAL_TOPICS = {
		NEW_TOPIC,
		NODE_UP,
		NODE_DOWN
	};
	
	public static boolean isInternalTopic(final String name) {
		for (int i = 0; i < INTERNAL_TOPICS.length; i++) {
			if (INTERNAL_TOPICS[i].equals(name)) {
				return true;
			}
		}
		
		return false;
	}
}
