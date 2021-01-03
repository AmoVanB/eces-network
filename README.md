# Network

This project implements a network library on top of the [graph](https://github.com/AmoVanB/eces-graph) library of the [ECES](https://github.com/AmoVanB/eces-core) framework.
The module defines a system (`NetworkingSystem.java`) in the context of the core ECES framework.

The module uses the graph module to represent networks as two graphs: 

- A *link-level graph* corresponding to the physical topology of the network.
- A *queue-level graph*, where each link in the physical graph correponds to *n* edges, where *n* corresponds to the number of queues at this link.

## Usage

The project can be downloaded from maven central using:
```xml
<dependency>
  <groupId>de.tum.ei.lkn.eces</groupId>
  <artifactId>network</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

### Networks, nodes and links

The system is able to create, modify and delete networks (`Network.java`) consisting of links (`Link.java`) and nodes (`NetworkNode.java`). This is done using the `createNetwork()`, `deleteNetwork()`, `createNode()`, `deleteNode()`, `createLink()` and `deleteLink()` methods. The `create*()` methods will return the created component (network, node or link) and their respective public methods can then be used to get information on the corresponding component (queue-level edge corresponding to a link, link-level graph corresponding to a network, queue-level node corresponding to a network node, etc.).

### Delay and rate

The networking system attaches a delay (`Delay.java`) and a rate (`Rate.java`) object to the entity to which a link-level edge is attached. These components represent the propagation delay and the rate of the physical link.

### Schedulers

The networking system attaches a scheduler (`Scheduler.java`) to the entity to which a link-level edge is attached. We define two different types of schedulers:

- *Priority schedulers* (`PriorityScheduler.java`, which can be created with the `createLinkWithPriorityScheduling()` method of the networking system).
- *Weighted Fair Queueing (WFQ) schedulers* (`WFQScheduler.java`, which can be created with the `createLinkWithWFQScheduling()` method of the networking system).

A scheduler contains a series of queues (`Queue.java`) which are attached to the entity of the corresponding queue-level edge. A queue is defined by its size.

Per default, the `createLink()` method creates a link with a priority scheduler consisting of a single queue.

### To network

The networking system attaches a *to network* (`ToNetwork.java`) component to the entity of each graph node and edge (both link- and queue-level). These components point to the entity of the corresponding network component.

### Hosts and interfaces

The networking system also allows to create and delete hosts (`Host.java`). This is done using the `createHost()` and `deleteHost()` methods. A host corresponds to a list of network interfaces (`NetworkInterface.java`). An interface can be added to and removed from a host using the `addInterface()` and `deleteInterface()` methods from the networking system. The `addInterface()` methods returns a networking node instance representing the interface in the network. The interface can then be connected anywhere in the network using the `createLink*()` methods. Note that an interface can only have one outgoing and one incoming link.


## Examples

```java
Network network = networkingSystem.createNetwork();

// Create a star topology
NetworkNode n = networkingSystem.createNode(network);
NetworkNode[] nodes = new NetworkNode[10];
for(int i = 0; i < nodes.length; i++) {
    nodes[i] = networkingSystem.createNode(network);
    networkingSystem.createLink(nodes[i], n, "1000 byte/s", "10 ms", "60000 byte");
    networkingSystem.createLink(n, nodes[i], "1000 byte/s", "10 ms", "60000 byte");
}

Host h = networkingSystem.createHost(network, "host name");

NetworkNode node1 = networkingSystem.createNode(network);
NetworkNode node2 = networkingSystem.createNode(network);

NetworkNode hostNode = networkingSystem.addInterface(h, new NetworkInterface("eth0","00:00:00:00:00:01","125.2.2.1"));
NetworkNode hostNode2 = networkingSystem.addInterface(h, new NetworkInterface("eth1","00:00:00:00:00:02","125.2.2.2"));

networkingSystem.createLink(hostNode, node1, 1000, 0.01, 60000);
networkingSystem.createLink(node1, hostNode, 1000, 0.01, 60000);
networkingSystem.createLink(hostNode2, node2, 1000, 0.01, 60000);
networkingSystem.createLink(node2, hostNode2, 1000, 0.01, 60000);
```

See [tests](src/test) for other simple examples.

See other ECES repositories using this network library (e.g., the [southbound interface](https://github.com/AmoVanB/eces-sbi) project and the [tenant manager](https://github.com/AmoVanB/eces-tenant-manager)) for more detailed/advanced examples.
