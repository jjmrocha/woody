woody 
=====


##Introduction

  Basic actor model implementation in JAVA.
  
  
##Instalation

Maven dependency:
 
 ```xml
<dependency>
    <groupId>net.uiqui</groupId>
    <artifactId>woody</artifactId>
    <version>1.1.0</version>
</dependency>
 ```
 
 
##API

### Actor
 
 ```java
// An actor receives messages sent to the actor address automatically
Actor<String> actorStr = new Actor<String>() {
	@Override
	public void handle(String msg) {
		System.out.println("Message received: " + msg);
	}
};

// For concurrent processing we can use the SupportingActor 
// that processes messages using and Executor
Executor threadPool = DeamonPool.newCachedDaemonPool();

Actor<Record> server = new SupportingActor<Record>(threadPool) {
	@Override
	public void handle(Record msg) {
		// Do some work
	}
};

// The LeadingActor implementation runs autonomous code by implementing the Runnable interface 
Actor<Event> bot = new LeadingActor<Event>() {
	@Override
	public void run() {
		while(true) {
			// Do some work
		}
	}

	@Override
	public void handle(Event msg) {
		// Process message
	}
};
 ```
 
 
### Topic

 ```java
// When we create a Topic it becomes automatically registered
Topic errorTopic = new Topic("error");

// If a topic don't exists the broker creates one, if exists the broker returns the Topic instance
Topic eventTopic = Broker.getTopic("event");

// An Actor can subscribe and topic
errorTopic.subscribe(actorStr);

// A Topic can also subscribe a Topic 
errorTopic.subscribe(eventTopic);
 ```
 

### Broker & Sending Messages

 ```java
// Send messages to an actor
Broker.send(bot, new Event());

// Send messages to a topic
Broker.send(eventTopic, new Event());
Broker.sendToTopic("error", new Event());

// Send messages using actor/topic endpoint
Endpoint serverEndpoint = Endpoint.getEndpointForActor("serverName");
Broker.send(serverEndpoint, new Event());

Broker.send(actorStr.endpoint(), "Test"); 

Broker.send(errorTopic.endpoint(), new Event()); 
 ```
 
 
### RPC

 ```java
// RPC Server
RPCServer<MathRequest, Double> calculator = new RPCServer<MathRequest, Double>("mathServer", threadPool){
	@Override
	public Double process(MathRequest request) {
		return request.compute();
	}
};

// RPC Client
RPCClient<MathRequest, Double> rpcClient = new RPCClient<MathRequest, Double>("mathServer");
Double result = rpcClient.call(new MathRequest("add", 1, 2));
 ```


##License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)