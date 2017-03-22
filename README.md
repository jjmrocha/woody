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
    <version>1.2.0</version>
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

 ```
 
 
### Topic

 ```java
// When we create a Topic it becomes automatically registered
Topic errorTopic = new Topic("error");

// If a topic don't exists the broker creates one, if exists the broker returns the Topic instance
Topic eventTopic = Broker.getTopic("event");

// An Actor can subscribe and topic
errorTopic.subscribe(bot);

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

##License
[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)