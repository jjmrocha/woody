package net.uiqui.woody.api.cluster.msg;

import java.io.Serializable;

public class CastMessage implements Serializable {
	private static final long serialVersionUID = -683709447509648496L;
	
	private String name = null;
	private Serializable payload = null;
	
	public CastMessage(final String name, final Serializable payload) {
		this.name = name;
		this.payload = payload;
	}

	public String getName() {
		return name;
	}

	public Serializable getPayload() {
		return payload;
	}
}
