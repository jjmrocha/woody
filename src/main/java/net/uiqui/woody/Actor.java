package net.uiqui.woody;

import net.uiqui.woody.api.WoodyException;

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
