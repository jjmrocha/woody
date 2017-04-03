# WOODY 

## Introduction

Woody is basic implementation of the actor model implementation.
This implementation is heavily influenced by Erlang.


## Instalation

Maven dependency:
 
```xml
	<dependency>
	    <groupId>net.uiqui</groupId>
	    <artifactId>woody</artifactId>
	    <version>2.0.0</version>
	</dependency>
```

 
## API

### Actor
> The actor model in computer science is a mathematical model of concurrent computation that treats "actors" as the universal 
> primitives of concurrent computation. In response to a message that it receives, an actor can: make local decisions, create 
> more actors, send more messages, and determine how to respond to the next message received. Actors may modify private state, 
> but can only affect each other through messages (avoiding the need for any locks).
*Wikipedia*


##### Pojo Actor

```java
	// Any class can be an actor
	public class PojoActor1 {
		@CastHandler
		public void handleCast(String msg) {
			System.out.println("Received: " + msg);
		}
	}
	
	// Register actor
	Woody.register("pojo1", new PojoActor1());
	
	// Send a message to actor
	Woody.cast("pojo1", "Hello pojo!");
```


##### Actor Class

```java
	// Extending the Actor class the actor is automatically registered
	Actor actor1 = new Actor() {
		@CastHandler
		public void handleCast(String msg) {
			System.out.println("[" + getName() + "] Received: " + msg);
		}
	};
	
	// We can use the actor method cast to send an asynchronously message
	actor1.cast("Hello actor {1}");
	
	// But we can still send a message using Woody
	Woody.cast(actor1.getName(), "Hello actor {2}");
```

 
### Topic
Topics can be use to deliver events to many actors (subscribers of the topic).


##### Pojo Actor

```java
	public class PojoActor2 {
		@Subscription("ping")
		public void ping(Integer msg) {
			System.out.println("ping: " + msg);
		}
	}
	
	// When we register and actor without providing a name for the actor
	// the register returns the generated name 
	String actorName = Woody.register(new PojoActor2());
	
	for (int i = 0; i < 5; i ++) {
		// The event is published to a topic
		Woody.publish("ping", i);
		Runner.sleep(1, TimeUnit.SECONDS);
	}
```


##### Actor Class

```java
	// We can specify the actor's name on the constructor 
	Actor actor2 = new Actor("actor2") {
		@Subscription("ping")
		public void event(Integer msg) {
			System.out.println("[" + getName() + "] event: " + msg);
		}			
	};
	
	for (int i = 0; i < 5; i ++) {
		// The event is published to a topic
		Woody.publish("ping", i);
		Runner.sleep(1, TimeUnit.SECONDS);
	}
```


### RPC
The RPC mechanism is implemented by sending messages.
The caller send a message to the actor, the actor computes a response a returns it by sending a message to the caller.

```java
	Actor actor3 = new Actor("calculator") {
		@CallHandler("add")
		public Integer add(Parameters param) {
			return param.getA() + param.getB();
		}
		
		@CallHandler("multiply")
		public Integer multiply(Parameters param) {
			return param.getA() * param.getB();
		}
	};
	
	try {
		// If the call take more than 5000 milliseconds (the default value) the caller will receive a CallTimeoutException
		Integer sum = actor3.call("add", new Parameters(2, 3));
		System.out.println(sum);
		
		// We can specify a timeout for the call
		Integer product = Woody.call("calculator", "multiply", new Parameters(2, 3), 10);
		System.out.println(product);
	} catch (CallTimeoutException e) {
		System.err.println("Computation took to long, we received a timeout");
	}
```


## License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)