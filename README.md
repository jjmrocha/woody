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
    <version>2.4.0</version>
</dependency>
```

 
## API

### Actor

Any class can be used as an actor, the only requirement the use one of the annotation **Async** on one of its methods.

```java
public class Storage {
	private final Map<String, Object> keyMap = new HashMap<String, Object>();
	
	@Self private String name = null;
	
	@Async
	public void put(final String key, final Object value) {
		debug("put(" + key + ", " + value + ")");
		keyMap.put(key, value);
	}
	
	@Async
	public Object get(final String key) {
		debug("get(" + key + ")");
		return keyMap.get(key);
	}
	
	@Async
	public void delete(final String key) {
		debug("delete(" + key + ")");
		keyMap.remove(key);
	}

	private void debug(final String msg) {
		System.out.println("[" + name + "] " + msg);
	}
}
```

All methods marked with the **Async** annotation will be invoked asynchronously.
The annotation **Self** injects the actor name on the annotated field.
 

### Actor creation and registration

Woody provides methods to create actor instances and register already created objects as actors.

```java
// Create an anonymous actor
Storage storage = Woody.newActor(Storage.class);

// Create an anonymous actor pool (multiples actor instances)
int poolSize = 10;
Storage storage = Woody.newActor(Storage.class, poolSize);

// Create and register an actor
Storage storage = Woody.newActor("storage", Storage.class);

// Create and register an actor pool (multiples actor instances)
int poolSize = 10;
Storage storage = Woody.newActor("storage", Storage.class, poolSize); 
// NOTE: this example doesn't make any sense, because each actor as its own instance variables

// Register an object as an anonymous actor
Woody.register(new Storage());
// NOTE: The only way to contact an anonymous actor, registered this way, is the actor passing is automatic generated name to another actor

// Register an object as actor
Woody.register("storage", new Storage());
```


#### Obtaining actor instances

Woody provides 3 ways to obtain an actor instance.

```java
// Obtain an actor instance during actor creation
Storage storage = Woody.newActor(Storage.class);

// Obtain an actor instance searching for its registration name
Storage storage = Woody.findActor("storage");

// Let Woody inject the actor instance dependency
@Actor("storage") private Storage storage = null;
```


### Invoke actor methods

Use the actor instance like any other object, but remember, you are only allowed to invoke the method marked with **Async** annotation.   

```java
storage.put("a", 1);
storage.put("b", 2);

System.out.println(storage.get("a"));
System.out.println(storage.get("b"));

storage.delete("b");

System.out.println(storage.get("a"));
System.out.println(storage.get("b"));
```

## License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
