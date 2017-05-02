package net.uiqui.woody.api.cluster.msg;

import java.io.Serializable;

public class ClusterEvent implements Serializable {
	private static final long serialVersionUID = 8537313324456412898L;
	
	private String topic = null;
	private Serializable payload = null;
	
	public ClusterEvent(final String topic, final Serializable payload) {
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
