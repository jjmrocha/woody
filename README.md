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
    <version>2.3.0</version>
</dependency>
```

 
## API

### Actor

Any class can be used as an actor, the only requirement is to use one of the annotations **CastHandler**, **CallHandler** or **Subscription** on one of its methods.

To be able to receive messages asynchronously the actor must use the **CastHandler** annotation to mark the method to process the message. 
The actor must implement different methods for each type of message it can receive, e.g. If the actor can receive string and integer messages, it must provide two methods:

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

**NOTE: You may want to consider the addition of a catch all method (that receives Object instances) to prevent the crash of your actor when you receive a message of an unsupported data type.**

```java
public class Actor1 {
	// Self annotation, can be used to obtain the actor's name on runtime
	@Self
	private String name = null;
	
	@CastHandler
	public void handleCast(String msg) {
		System.out.println("[" + name + "] Received: " + msg);
	}
}

// Register actor
ActorRef a1 = Woody.register("a1", new Actor1());

// Send a message to actor
a1.cast("Hello actor!");
```


### Topic
Topics can be used to deliver events to many actors (subscribers of the topic).

To be able to receive events asynchronously the actor must subscribe one or more topics using the **Subscription** annotation to mark the method to process the event. 
The actor must implement different methods for each type of event:

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

**NOTE: You may want to consider the addition of a catch all method (that receives Object instances) to prevent the crash of your actor when you receive an event of an unsupported data type.**


```java
public class Actor2 {
	@Self
	private String name = null;
	
	@Subscription("ping")
	public void ping(Integer msg) {
		System.out.println("[" + name + "] ping: " + msg);
	}
}

// Every time we create/register an actor, a new instance is created
Woody.newActor(Actor2.class); // Just create a new actor
Woody.newActor(Actor2.class); // Just create another actor
Woody.register(new Actor2()); // And a third actor is registered

for (int i = 0; i < 5; i ++) {
	// The event is published to a topic
	Woody.publish("ping", i);
	Runner.sleep(1, TimeUnit.SECONDS);
}
```


### RPC
The RPC mechanism is implemented by sending messages, the caller send a message to the actor, the actor computes a response and the caller receives it using a Future object.

To be able to receive RPC calls the actor must provide one or more methods mark with the **CallHandler** annotation to mark the method to process the RPC call.

```java
public class Actor3 {
	// We can use the Actor annotation to request the actor reference to be injected 
	@Actor("a1")
	private ActorRef a1 = null;
	
	private int counter = 0;
	
	@CallHandler("increment")
	public Integer inc(Integer value) {
		counter += value;
		
		// We can send a message/call to another actor
		a1.cast("Current value is " + counter);
		
		return counter;
	}
}

// We can create an actor instance and registration in one operation 
Woody.newActor("a3", Actor3.class);

//... somewhere else in our code ...

// We can also obtain a reference to an actor
ActorRef a3 = Woody.getActorRef("a3");

Future<Object> currentValue = a3.call("increment", 1);

// ... do stuff ...

System.out.println("Added 1 and now the value is " + currentValue.get());

// We can specify the maximum time the computation can take to completed
try {
	// We can specify the maxim time the computation can take to completed
	System.out.println("Added 5 and now the value is " + a3.call("increment", 5).get(10, TimeUnit.MILLISECONDS));
} catch (TimeoutException e) {
	System.err.println("Computation took to long, we received a timeout");
}
```


## License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
