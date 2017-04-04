# WOODY 

*Woody is a basic implementation of the actor model in JAVA*


> The actor model in computer science is a mathematical model of concurrent computation that treats "actors" as the universal 
> primitives of concurrent computation. In response to a message that it receives, an actor can: make local decisions, create 
> more actors, send more messages, and determine how to respond to the next message received. Actors may modify private state, 
> but can only affect each other through messages (avoiding the need for any locks).

*Wikipedia*


## Introduction

![Woody Woodpecker](https://upload.wikimedia.org/wikipedia/en/3/3f/Woody_Woodpecker.png)

This implementation was heavily influenced by Erlang programming language.

The main characteristics of this implementation are:
* All communication (message sending, event delivery and RPC calls) with an actor are perform a asynchronously
* An actor will receive the messages sequentially and in the same order the messages were sent
* The messages are deliver almost immediately, if the actor is busy processing a previous message, the new message is queue
* Woody uses a thread pool for processing messages, this way the system only has as many threads running as needed. 


## Instalation

Maven dependency:
 
```xml
<dependency>
    <groupId>net.uiqui</groupId>
    <artifactId>woody</artifactId>
    <version>2.0.1</version>
</dependency>
```

 
## API

### Actor

Any class can be used as an actor, the only requirement is to use one of the annotations CastHandler or Subscription on one of its methods.

To be able to receive messages asynchronously the actor must use the CastHandler annotation to mark the method to process the message. 
The actor must implement diferente methods for each type of message it can receive, e.g. If the actor can receive string and integer messages, it must provide two methods:

```java
@CastHandler
public void handleString(String msg) {
	System.out.println("Received STR: " + msg);
}

@CastHandler
public void handleInt(Integer msg) {
	System.out.println("Received INT: " + msg);
}
```


##### Pojo Actor Example

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


##### Actor Class Example

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
Topics can be used to deliver events to many actors (subscribers of the topic).

To be able to receive events asynchronously the actor must subscribe one or more topics using the Subscription annotation to mark the method to process the event. 
The actor must implement diferente methods for each type of event:

```java
@Subscription("ping")
public void ping(Integer msg) {
	System.out.println("ping: " + msg);
}

@Subscription("echo")
public Integer echo(Integer event) {
	return event;
}

@Subscription("echo")
public String echo(String event) {
	return event;
}
```


##### Pojo Actor Example

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


##### Actor Class Example

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
The RPC mechanism is implemented by sending messages, the caller send a message to the actor, the actor computes a response a returns it by sending a message to the caller.

To be able to receive RPC calls the actor must extend the class **net.uiqui.woody.Actor**, and provide one or more methods mark with the CallHandler annotation to mark the method to process the RPC call.

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
	// If the call take more than 5000 milliseconds (the default value) 
	// the caller will receive a CallTimeoutException
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
