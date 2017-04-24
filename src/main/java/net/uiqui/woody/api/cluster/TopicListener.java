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

import java.util.ArrayList;
import java.util.List;

import net.uiqui.woody.annotations.CallHandler;
import net.uiqui.woody.annotations.Subscription;
import net.uiqui.woody.api.util.TopicNames;

public class TopicListener {
	private final List<String> topicList = new ArrayList<String>();
	private Gateway gateway = null;
	
	public TopicListener(final Gateway gateway) {
		this.gateway = gateway;
	}
	
	@Subscription(TopicNames.NEW_TOPIC)
	public void register(final String topicName) {
		topicList.add(topicName);
		gateway.requestTopic(topicName);
	}
	
	@CallHandler("topics")
	public List<String> getList() {
		return topicList;
	}
}
