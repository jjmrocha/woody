woody - Basic actor model implementation
========================================

 Maven dependency:
 
 ```
<dependency>
    <groupId>net.uiqui</groupId>
    <artifactId>woody</artifactId>
    <version>1.0.0</version>
</dependency>
 ```
 
 Example:
 
 ```
Actor<String> actor = new Actor<String>() {
	public void handle(String msg) {
		System.out.println("Received: " + msg);
	}
};

Broker.send(actor.endpoint(), "Hello");
 ```
