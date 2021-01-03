package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.util.EventCountTestSystem;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.network.exceptions.NetworkException;
import de.tum.ei.lkn.eces.network.mappers.*;
import de.tum.ei.lkn.eces.network.util.NetworkInterface;
import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import javax.measure.quantity.DataAmount;
import java.util.ArrayList;
import java.util.List;

import static javax.measure.unit.NonSI.BYTE;
import static javax.measure.unit.SI.SECOND;
import static org.junit.Assert.*;

/**
 * Test class for de.tum.ei.lkn.eces.network.NetworkingSystem.java.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NetworkingSystemTest {
	private Controller controller;
	private NetworkingSystem networkingSystem;
	private EventCountTestSystem countTestSystem;
	private GraphSystem graphSystem;

	@Before
	public void setUp() {
		controller = new Controller();
		graphSystem = new GraphSystem(controller);
		networkingSystem = new NetworkingSystem(controller, graphSystem);
		countTestSystem = new EventCountTestSystem(controller);
	}

	@Test
	public void testCreateNetwork() {
		Network network = networkingSystem.createNetwork();

		ToNetworkMapper toNetworkMapper = new ToNetworkMapper(controller);

		assertNotNull("A network should be created", network);
		assertTrue("ToNetwork component should be present at link level Node", toNetworkMapper.isIn(network.getLinkGraph().getEntity()));
		assertSame("ToNetwork component should have a reference to the NetworkNode", toNetworkMapper.get(network.getLinkGraph().getEntity()).getNetworkEntity(), network.getEntity());
		assertTrue("ToNetwork component should be present at queue level Node", toNetworkMapper.isIn(network.getLinkGraph().getEntity()));
		assertSame("ToNetwork component should have a reference to NetworkNode", toNetworkMapper.get(network.getLinkGraph().getEntity()).getNetworkEntity(), network.getEntity());
		assertTrue("The Network should be empty", network.getLinkGraph().getNodes().size() == 0
			&& network.getLinkGraph().getEdges().size() == 0
			&& network.getQueueGraph().getNodes().size() == 0
			&& network.getQueueGraph().getEdges().size() == 0
			&& network.getHosts().size() == 0);

		countTestSystem.doFullCheck(Graph.class, 2, 0, 0);
		countTestSystem.doFullCheck(Node.class, 0, 0, 0);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 0);

		countTestSystem.doFullCheck(Network.class, 1, 0, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 0);
		countTestSystem.doFullCheck(Link.class, 0, 0, 0);
		countTestSystem.doFullCheck(Host.class, 0, 0, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 2, 0, 0);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 0);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 0);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 0);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testDeleteNetwork() {
		Network network = networkingSystem.createNetwork();

		// Create a star topology.
		NetworkNode n = networkingSystem.createNode(network);
		NetworkNode[] nodes = new NetworkNode[10];
		for(int i = 0; i < nodes.length; i++) {
			nodes[i] = networkingSystem.createNode(network);
			networkingSystem.createLink(nodes[i], n,"1000 byte/s", "10 ms", "60000 byte");
			networkingSystem.createLink(n, nodes[i],"1000 byte/s", "10 ms", "60000 byte");
		}

		Host h = networkingSystem.createHost(network, "Test");

		NetworkNode node1 = networkingSystem.createNode(network);
		NetworkNode node2 = networkingSystem.createNode(network);

		NetworkNode hostNode = networkingSystem.addInterface(h, new NetworkInterface("eth0","00:00:00:00:00:01","125.2.2.1"));
		NetworkNode hostNode2 = networkingSystem.addInterface(h, new NetworkInterface("eth1","00:00:00:00:00:02","125.2.2.2"));

		networkingSystem.createLink(hostNode, node1, 1000, 0.01, 60000);
		networkingSystem.createLink(node1, hostNode, 1000, 0.01, 60000);
		networkingSystem.createLink(hostNode2, node2, 1000, 0.01, 60000);
		networkingSystem.createLink(node2, hostNode2, 1000, 0.01, 60000);

		countTestSystem.reset();

		networkingSystem.deleteNetwork(network);

		countTestSystem.doFullCheck(Network.class, 0, 3, 1);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 15);
		countTestSystem.doFullCheck(Link.class, 0, 0, 2 * nodes.length + 4);
		countTestSystem.doFullCheck(Host.class, 0, 2, 1);
		countTestSystem.doFullCheck(ToNetwork.class, 0, 0, (2 * nodes.length + 4 + nodes.length + 5) * 2);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 2 * nodes.length+ 4);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 2 * nodes.length+ 4 );
		countTestSystem.doFullCheck(Queue.class, 0, 0, 2 * nodes.length+ 4 );
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 2 * nodes.length+ 4);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.doFullCheck(Graph.class, 0, (2 * nodes.length + 4 + nodes.length + 5) * 2, 2);
		countTestSystem.doFullCheck(Node.class, 0, 8 * nodes.length + 16, (nodes.length + 5) * 2);
		countTestSystem.doFullCheck(Edge.class, 0, 0, (4 * nodes.length) + 8);

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testCreateNode() {
		Network network = networkingSystem.createNetwork();

		NetworkNode node = networkingSystem.createNode(network);

		ToNetworkMapper toNetworkMapper = new ToNetworkMapper(controller);

		assertNotNull("A NetworkNode should be created", node);
		assertTrue("ToNetwork component should be present at link level Node", toNetworkMapper.isIn(node.getLinkNode().getEntity()));
		assertSame("ToNetwork component should have a reference to NetworkNode", toNetworkMapper.get(node.getLinkNode().getEntity()).getNetworkEntity(), node.getEntity());
		assertTrue("ToNetwork component should be present at queue level Node", toNetworkMapper.isIn(node.getQueueNode().getEntity()));
		assertSame("ToNetwork component should have a reference to NetworkNode", toNetworkMapper.get(node.getQueueNode().getEntity()).getNetworkEntity(), node.getEntity());
		assertNotNull("A NetworkNode should be created", node);
		assertTrue("The Network should contain a NetworkNode", network.getLinkGraph().getNodes().size() == 1
				&& network.getLinkGraph().getNodes().contains(node.getLinkNode())
				&& network.getQueueGraph().getNodes().size() == 1
				&& network.getQueueGraph().getNodes().contains(node.getQueueNode()));
		assertEquals("The NetworkNode should have no name", 0, node.getName().compareTo(""));
		assertTrue("The Network should be empty", network.getLinkGraph().getEdges().size() == 0
				&& network.getQueueGraph().getEdges().size() == 0
				&& network.getHosts().size() == 0);

		countTestSystem.doFullCheck(Graph.class, 2, 2, 0);
		countTestSystem.doFullCheck(Node.class, 2, 0, 0);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 0);

		countTestSystem.doFullCheck(Network.class, 1, 0, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 1, 0, 0);
		countTestSystem.doFullCheck(Link.class, 0, 0, 0);
		countTestSystem.doFullCheck(Host.class, 0, 0, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 4, 0, 0);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 0);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 0);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 0);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testCreateNode1() {
		Network network = networkingSystem.createNetwork();
		NetworkNode node = networkingSystem.createNode(network,"Test");
		assertEquals("The NetworkNode should have name 'Test'", 0, node.getName().compareTo("Test"));
	}

	@Test
	public void testDeleteNode() {
		// Creating a star topology.
		Network network = networkingSystem.createNetwork();
		NetworkNode n = networkingSystem.createNode(network);
		NetworkNode[] nodes = new NetworkNode[10];
		for(int i = 0; i < nodes.length; i++) {
			nodes[i] = networkingSystem.createNode(network);
			networkingSystem.createLink(nodes[i],n,"1000 byte/s", "10 ms", "60000 byte");
			networkingSystem.createLink(n,nodes[i],"1000 byte/s", "10 ms", "60000 byte");
		}

		countTestSystem.reset();

		networkingSystem.deleteNode(n);

		countTestSystem.doFullCheck(Network.class, 0, 0, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 1);
		countTestSystem.doFullCheck(Link.class, 0, 0, 2 * nodes.length);
		countTestSystem.doFullCheck(Host.class, 0, 0, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 0, 0, 2 + (4 * nodes.length));

		countTestSystem.doFullCheck(Rate.class, 0, 0, 2 * nodes.length);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 2 * nodes.length);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 2 * nodes.length);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 2 * nodes.length);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.doFullCheck(Graph.class, 0, 2 + (4 * nodes.length), 0);
		countTestSystem.doFullCheck(Node.class, 0, (8 * nodes.length), 2);
		countTestSystem.doFullCheck(Edge.class, 0, 0, (4 * nodes.length));

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testCreateLink() {
		Network network = networkingSystem.createNetwork();

		RateMapper rateMapper = new RateMapper(controller);
		DelayMapper delayMapper = new DelayMapper(controller);
		QueueMapper queueMapper = new QueueMapper(controller);
		SchedulerMapper schedulerMapper = new SchedulerMapper(controller);
		ToNetworkMapper toNetworkMapper = new ToNetworkMapper(controller);

		for(int i = 0; i < 3; i++) {
			NetworkNode node1 = networkingSystem.createNode(network);
			NetworkNode node2 = networkingSystem.createNode(network);

			countTestSystem.reset();

			Link link = null;
			switch (i) {
				case 0:
					link = networkingSystem.createLink(node1, node2,"1000 byte/s", "10 ms", "60000 byte");
					break;
				case 1:
					link = networkingSystem.createLink(node1, node2, 1000, 0.01, 60000);
					break;
				case 2:
					link = networkingSystem.createLink(node1, node2, Amount.valueOf("1000 byte/s").to(Rate.BYTES_PER_SECOND)
							, Amount.valueOf("10 ms").to(SECOND)
							, Amount.valueOf("60000 byte").to(BYTE));
					break;
				default:
			}

			assertTrue("ToNetwork component should be present at link level Edge", toNetworkMapper.isIn(link.getLinkEdge().getEntity()));
			assertSame("ToNetwork component should have a reference to Link", toNetworkMapper.get(link.getLinkEdge().getEntity()).getNetworkEntity(), link.getEntity());
			for(Edge queueEdge: link.getQueueEdges()) {
				assertTrue("ToNetwork component should be present at queue level Edge", toNetworkMapper.isIn(queueEdge.getEntity()));
				assertSame("ToNetwork component should have a reference to Link", toNetworkMapper.get(queueEdge.getEntity()).getNetworkEntity(), link.getEntity());
			}

			assertEquals("Data rate should be 1000 byte/s", 1000, rateMapper.get(link.getLinkEdge().getEntity()).getRate(), 0.0);
			assertTrue("Delay should be 0.01 s but is " + delayMapper.get(link.getLinkEdge().getEntity()).getDelay(), Math.abs(delayMapper.get(link.getLinkEdge().getEntity()).getDelay() - 0.01) < 10E-10);
			assertEquals("There should be only one Queue", 1, link.getQueueEdges().length);
			assertEquals("The Queue should be 60000 bytes long", 60000, queueMapper.get(link.getQueueEdges()[0].getEntity()).getSize(), 0.0);
			assertEquals("The Scheduler should only contain one Queue", 1, schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues().length);
			assertSame("The Scheduler should contain the same queue as the queue level Edge", schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues()[0], queueMapper.get(link.getQueueEdges()[0].getEntity()));

			countTestSystem.doFullCheck(Graph.class, 0, 2, 0);
			countTestSystem.doFullCheck(Node.class, 0, 4, 0);
			countTestSystem.doFullCheck(Edge.class, 2, 0, 0);

			countTestSystem.doFullCheck(Network.class, 0, 0, 0);
			countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 0);
			countTestSystem.doFullCheck(Link.class, 1, 0, 0);
			countTestSystem.doFullCheck(Host.class, 0, 0, 0);
			countTestSystem.doFullCheck(ToNetwork.class, 2, 0, 0);

			countTestSystem.doFullCheck(Rate.class, 1, 0, 0);
			countTestSystem.doFullCheck(Delay.class, 1, 0, 0);
			countTestSystem.doFullCheck(Queue.class, 1, 0, 0);
			countTestSystem.doFullCheck(Scheduler.class, 1, 0, 0);
			countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
			countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

			countTestSystem.checkIfEmpty();
		}
	}

	@Test
	public void testCreateLinkWithPriorityScheduling() {
		Network network = networkingSystem.createNetwork();

		RateMapper rateMapper = new RateMapper(controller);
		DelayMapper delayMapper = new DelayMapper(controller);
		QueueMapper queueMapper = new QueueMapper(controller);
		PrioritySchedulerMapper schedulerMapper = new PrioritySchedulerMapper(controller);
		ToNetworkMapper toNetworkMapper = new ToNetworkMapper(controller);

		for(int i = 0; i < 3; i++) {
			NetworkNode node1 = networkingSystem.createNode(network);
			NetworkNode node2 = networkingSystem.createNode(network);

			countTestSystem.reset();

			Link link = null;
			switch (i) {
				case 0:
					String[] queues1 =  {"52000 byte", "60000 byte"};
					link = networkingSystem.createLinkWithPriorityScheduling(node1, node2,"1000 byte/s", "0.01 s",queues1);
					break;
				case 1:
					double[] queues2 =  {52000, 60000};
					link = networkingSystem.createLinkWithPriorityScheduling(node1, node2, 1000, 0.01, queues2);
					break;
				case 2:
					List<Amount<DataAmount>> queues3 = new ArrayList<>();
					queues3.add(Amount.valueOf("52000 byte").to(BYTE));
					queues3.add(Amount.valueOf("60000 byte").to(BYTE));
					link = networkingSystem.createLinkWithPriorityScheduling(node1, node2, Amount.valueOf("1000 byte/s").to(Rate.BYTES_PER_SECOND)
							, Amount.valueOf("10 ms").to(SECOND)
							, queues3);
					break;
				default:
			}

			assertTrue("ToNetwork component should be present at link level Edge", toNetworkMapper.isIn(link.getLinkEdge().getEntity()));
			assertSame("ToNetwork component should have a reference to Link", toNetworkMapper.get(link.getLinkEdge().getEntity()).getNetworkEntity(), link.getEntity());
			for(Edge queueEdge: link.getQueueEdges()) {
				assertTrue("ToNetwork component should be present at queue level Edge", toNetworkMapper.isIn(queueEdge.getEntity()));
				assertSame("ToNetwork component should have a reference to Link", toNetworkMapper.get(queueEdge.getEntity()).getNetworkEntity(), link.getEntity());
			}

			assertEquals("Data rate should be 1000 byte/s", 1000, rateMapper.get(link.getLinkEdge().getEntity()).getRate(), 0.0);
			assertTrue("Delay should be 0.01 s but is " + delayMapper.get(link.getLinkEdge().getEntity()).getDelay(), Math.abs(delayMapper.get(link.getLinkEdge().getEntity()).getDelay() - 0.01) < 10E-10);
			assertEquals("There should be only one Queue", 2, link.getQueueEdges().length);
			assertEquals("The Queue should be 52000 bytes long", 52000, queueMapper.get(link.getQueueEdges()[0].getEntity()).getSize(), 0.0);
			assertEquals("The Queue should be 60000 bytes long", 60000, queueMapper.get(link.getQueueEdges()[1].getEntity()).getSize(), 0.0);
			assertEquals("The Scheduler should contain two Queues", 2, schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues().length);
			assertSame("The Scheduler should contain the same Queue as the queue level Edge", schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues()[0], queueMapper.get(link.getQueueEdges()[0].getEntity()));
			assertSame("The Scheduler should contain the same Queue as the queue level Edge", schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues()[1], queueMapper.get(link.getQueueEdges()[1].getEntity()));

			countTestSystem.doFullCheck(Graph.class, 0, 3, 0);
			countTestSystem.doFullCheck(Node.class, 0, 6, 0);
			countTestSystem.doFullCheck(Edge.class, 3, 0, 0);

			countTestSystem.doFullCheck(Network.class, 0, 0, 0);
			countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 0);
			countTestSystem.doFullCheck(Link.class, 1, 0, 0);
			countTestSystem.doFullCheck(Host.class, 0, 0, 0);
			countTestSystem.doFullCheck(ToNetwork.class, 3, 0, 0);

			countTestSystem.doFullCheck(Rate.class, 1, 0, 0);
			countTestSystem.doFullCheck(Delay.class, 1, 0, 0);
			countTestSystem.doFullCheck(Queue.class, 2, 0, 0);
			countTestSystem.doFullCheck(Scheduler.class, 0, 0, 0);
			countTestSystem.doFullCheck(PriorityScheduler.class, 1, 0, 0);
			countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

			countTestSystem.checkIfEmpty();
		}
	}


	@Test
	public void testCreateLinkWithWFQScheduling() {
		Network network = networkingSystem.createNetwork();

		RateMapper rateMapper = new RateMapper(controller);
		DelayMapper delayMapper = new DelayMapper(controller);
		QueueMapper queueMapper = new QueueMapper(controller);
		WFQSchedulerMapper schedulerMapper = new WFQSchedulerMapper(controller);
		ToNetworkMapper toNetworkMapper = new ToNetworkMapper(controller);

		for(int i = 0; i < 3; i++) {
			NetworkNode node1 = networkingSystem.createNode(network);
			NetworkNode node2 = networkingSystem.createNode(network);

			countTestSystem.reset();

			Link link = null;
			switch (i) {
				case 0:
					String[] queues1 =  {"52000 byte", "60000 byte"};
					String[] weights1 =  {"1", "3"};
					link = networkingSystem.createLinkWithWFQScheduling(node1, node2,"1000 byte/s", "10 ms",queues1, weights1);
					break;
				case 1:
					double[] queues2 =  {52000, 60000};
					double[] weights2 =  {1, 3};
					link = networkingSystem.createLinkWithWFQScheduling(node1, node2, 1000, 0.01,queues2, weights2);
					break;
				case 2:
					List<Amount<DataAmount>> queues3 = new ArrayList<>();
					queues3.add(Amount.valueOf("52000 byte").to(BYTE));
					queues3.add(Amount.valueOf("60000 byte").to(BYTE));
					double[] weights3 =  {1, 3};
					link = networkingSystem.createLinkWithWFQScheduling(node1, node2, Amount.valueOf("1000 byte/s").to(Rate.BYTES_PER_SECOND)
							, Amount.valueOf("10 ms").to(SECOND)
							, queues3
							, weights3);
					break;
				default:
			}

			assertTrue("ToNetwork component should be present at link level Edge", toNetworkMapper.isIn(link.getLinkEdge().getEntity()));
			assertSame("ToNetwork component should have a reference to Link", toNetworkMapper.get(link.getLinkEdge().getEntity()).getNetworkEntity(), link.getEntity());
			for(Edge queueEdge: link.getQueueEdges()) {
				assertTrue("ToNetwork component should be present at queue level Edge", toNetworkMapper.isIn(queueEdge.getEntity()));
				assertSame("ToNetwork component should have a reference to Link", toNetworkMapper.get(queueEdge.getEntity()).getNetworkEntity(), link.getEntity());
			}

			assertEquals("Data rate should be 1000 byte/s", 1000, rateMapper.get(link.getLinkEdge().getEntity()).getRate(), 0.0);
			assertTrue("Delay should be 0.01 s but is " + delayMapper.get(link.getLinkEdge().getEntity()).getDelay() ,Math.abs(delayMapper.get(link.getLinkEdge().getEntity()).getDelay() - 0.01) < 10E-10);
			assertEquals("There should be only one Queue", 2, link.getQueueEdges().length);
			assertEquals("The Queue should have 52000 byte", 52000, queueMapper.get(link.getQueueEdges()[0].getEntity()).getSize(), 0.0);
			assertEquals("The Queue should have 60000 byte", 60000, queueMapper.get(link.getQueueEdges()[1].getEntity()).getSize(), 0.0);
			assertEquals("The Scheduler should contain two Queues", 2, schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues().length);
			assertSame("The Scheduler should contain the same Queue as the queue level Edge", schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues()[0], queueMapper.get(link.getQueueEdges()[0].getEntity()));
			assertSame("The Scheduler should contain the same Queue as the queue level Edge", schedulerMapper.get(link.getLinkEdge().getEntity()).getQueues()[1], queueMapper.get(link.getQueueEdges()[1].getEntity()));
			assertEquals("The Scheduler should be contain two weight values", 2, schedulerMapper.get(link.getLinkEdge().getEntity()).getWeights().length);
			assertEquals("The Scheduler's first Queue should have a weight of 1", 1, schedulerMapper.get(link.getLinkEdge().getEntity()).getWeights()[0], 0.0);
			assertEquals("The Scheduler's second Queue should have a weight of 3", 3, schedulerMapper.get(link.getLinkEdge().getEntity()).getWeights()[1], 0.0);

			countTestSystem.doFullCheck(Graph.class, 0, 3, 0);
			countTestSystem.doFullCheck(Node.class, 0, 6, 0);
			countTestSystem.doFullCheck(Edge.class, 3, 0, 0);

			countTestSystem.doFullCheck(Network.class, 0, 0, 0);
			countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 0);
			countTestSystem.doFullCheck(Link.class, 1, 0, 0);
			countTestSystem.doFullCheck(Host.class, 0, 0, 0);
			countTestSystem.doFullCheck(ToNetwork.class, 3, 0, 0);

			countTestSystem.doFullCheck(Rate.class, 1, 0, 0);
			countTestSystem.doFullCheck(Delay.class, 1, 0, 0);
			countTestSystem.doFullCheck(Queue.class, 2, 0, 0);
			countTestSystem.doFullCheck(Scheduler.class, 0, 0, 0);
			countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
			countTestSystem.doFullCheck(WFQScheduler.class, 1, 0, 0);

			countTestSystem.checkIfEmpty();
		}
	}


	@Test
	public void testDeleteLink() {
		Network network = networkingSystem.createNetwork();
		NetworkNode node1 = networkingSystem.createNode(network);
		NetworkNode node2 = networkingSystem.createNode(network);
		Link link = networkingSystem.createLink(node1,node2,"1000 byte/s", "10 ms", "60000 byte");

		countTestSystem.reset();

		networkingSystem.deleteLink(link);

		countTestSystem.doFullCheck(Graph.class, 0, 2, 0);
		countTestSystem.doFullCheck(Node.class, 0, 4, 0);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 2);

		countTestSystem.doFullCheck(Network.class, 0, 0, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 0);
		countTestSystem.doFullCheck(Link.class, 0, 0, 1);
		countTestSystem.doFullCheck(Host.class, 0, 0, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 0, 0, 2);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 1);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 1);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 1);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 1);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testCreateHost() {
		Network network = networkingSystem.createNetwork();

		countTestSystem.reset();

		Host h = networkingSystem.createHost(network);

		assertTrue("The Host should not have any downward connected interface", h.getDownwardsConnectedInterfaces().isEmpty());
		assertNull("The Host should not have any down link", h.getDownLink(null));
		assertEquals("The NetworkNode should have no name", 0, h.getName().compareTo(""));
		assertSame("The Host's Network is not properly set", h.getNetwork(), network);
		assertTrue("The Host should not have any upward connected interface", h.getUpwardsConnectedInterfaces().isEmpty());

		countTestSystem.doFullCheck(Graph.class, 0, 0, 0);
		countTestSystem.doFullCheck(Node.class, 0, 0, 0);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 0);

		countTestSystem.doFullCheck(Network.class, 0, 1, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 0);
		countTestSystem.doFullCheck(Link.class, 0, 0, 0);
		countTestSystem.doFullCheck(Host.class, 1, 0, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 0, 0, 0);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 0);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 0);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 0);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testCreateHost1() {
		Network network = networkingSystem.createNetwork();

		Host h = networkingSystem.createHost(network, "Test");

		assertEquals("The Host should have name 'Test'", 0, h.getName().compareTo("Test"));
	}

	@Test
	public void testDeleteHost() {
		Network network = networkingSystem.createNetwork();
		Host h = networkingSystem.createHost(network,"Test");
		NetworkNode node1 = networkingSystem.createNode(network);
		NetworkNode node2 = networkingSystem.createNode(network);
		NetworkNode hostNode = networkingSystem.addInterface(h, new NetworkInterface("eth0", "00:00:00:00:00:01", "125.2.2.1"));
		NetworkNode hostNode2 = networkingSystem.addInterface(h, new NetworkInterface("eth1", "00:00:00:00:00:02", "125.2.2.2"));
		networkingSystem.createLink(hostNode, node1, 1000, 0.01, 60000);
		networkingSystem.createLink(node1, hostNode, 1000, 0.01, 60000);
		networkingSystem.createLink(hostNode2, node2, 1000, 0.01, 60000);
		networkingSystem.createLink(node2, hostNode2, 1000, 0.01, 60000);

		countTestSystem.reset();
		networkingSystem.deleteHost(h);

		countTestSystem.doFullCheck(Network.class, 0, 3, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 2);
		countTestSystem.doFullCheck(Link.class, 0, 0, 4);
		countTestSystem.doFullCheck(Host.class, 0, 2, 1);
		countTestSystem.doFullCheck(ToNetwork.class, 0, 0, 12);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 4);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 4);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 4);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 4);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.doFullCheck(Graph.class, 0, 12, 0);
		countTestSystem.doFullCheck(Node.class, 0, 16, 4);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 8);

		countTestSystem.checkIfEmpty();
	}

	@Test
	public void testAddInterface() {
		Network network = networkingSystem.createNetwork();
		Host h = networkingSystem.createHost(network, "Test");
		NetworkNode node = networkingSystem.createNode(network);

		countTestSystem.reset();

		NetworkNode hostNode = networkingSystem.addInterface(h, new NetworkInterface("eth0", "00:00:00:00:00:01", "125.2.2.1"));

		countTestSystem.doFullCheck(Graph.class, 0, 2, 0);
		countTestSystem.doFullCheck(Node.class, 2, 0, 0);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 0);

		countTestSystem.doFullCheck(Network.class, 0, 1, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 1, 0, 0);
		countTestSystem.doFullCheck(Link.class, 0, 0, 0);
		countTestSystem.doFullCheck(Host.class, 0, 1, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 2, 0, 0);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 0);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 0);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 0);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.checkIfEmpty();

		networkingSystem.createLink(hostNode, node, 1000, 0.01, 60000);
		networkingSystem.createLink(node, hostNode, 1000, 0.01, 60000);


		try {
			networkingSystem.createLink(hostNode, node, 1000, 0.01, 60000);
			fail("System should throw an NetworkException because we try to connect an Interface twice");
		} catch (NetworkException e) {
			//
		}

		try {
			networkingSystem.createLink(node, hostNode, 1000, 0.01, 60000);
			fail("System should throw an NetworkException because we try to connect an Interface twice");
		} catch (NetworkException e) {
			//
		}

		NetworkNode hostNode2 = networkingSystem.addInterface(h, new NetworkInterface("eth2", "00:00:00:00:00:03", "125.2.2.5"));
		NetworkNode hostNode3 = networkingSystem.addInterface(h, new NetworkInterface("eth3", "00:00:00:00:00:33", "125.3.2.5"));
		try {
			networkingSystem.createLink(hostNode3, hostNode2, 1000, 0.01, 60000);
			fail("System should throw an NetworkException because we try to connect two interfaces together");
		} catch (NetworkException e) {
			//
		}

		try {
			networkingSystem.createLink(hostNode2, hostNode3, 1000, 0.01, 60000);
			fail("System should throw an NetworkException because we try to connect two interfaces together");
		} catch (NetworkException e) {
			//
		}

		try {
			networkingSystem.addInterface(h, new NetworkInterface("eth0", "00:00:00:00:00:08", "125.2.2.12"));
			fail("System should throw an NetworkException because we try to add an interface with the same name");
		} catch (NetworkException e) {
			//
		}

		controller.resetThreadLocal();
		try {
			networkingSystem.addInterface(h, new NetworkInterface("eth1", "00:00:00:00:00:03", "125.2.2.5"));
			fail("System should throw an NetworkException because we try to add an interface with the same MAC");
		} catch (NetworkException e) {
			//
		}

		controller.resetThreadLocal();
		try{
			networkingSystem.addInterface(h, new NetworkInterface("eth1", "00:00:00:00:00:07", "125.2.2.5"));
			fail("System should throw an NetworkException because we try to add an interface with the same IP");
		} catch (NetworkException e) {
			//
		}
		controller.resetThreadLocal();
	}

	@Test
	public void testDeleteInterface() {
		Network network = networkingSystem.createNetwork();
		Host h = networkingSystem.createHost(network, "Test");
		NetworkNode node = networkingSystem.createNode(network);
		NetworkNode hostNode = networkingSystem.addInterface(h, new NetworkInterface("eth0", "00:00:00:00:00:01", "125.2.2.1"));
		networkingSystem.createLink(hostNode, node, 1000, 0.01, 60000);
		networkingSystem.createLink(node, hostNode, 1000, 0.01, 60000);

		countTestSystem.reset();

		networkingSystem.deleteInterface(h, h.getInterfaces().toArray(new NetworkInterface[1])[0]);

		countTestSystem.doFullCheck(Network.class, 0, 1, 0);
		countTestSystem.doFullCheck(NetworkNode.class, 0, 0, 1);
		countTestSystem.doFullCheck(Link.class, 0, 0, 2);
		countTestSystem.doFullCheck(Host.class, 0, 1, 0);
		countTestSystem.doFullCheck(ToNetwork.class, 0, 0, 6);

		countTestSystem.doFullCheck(Rate.class, 0, 0, 2);
		countTestSystem.doFullCheck(Delay.class, 0, 0, 2);
		countTestSystem.doFullCheck(Queue.class, 0, 0, 2);
		countTestSystem.doFullCheck(Scheduler.class, 0, 0, 2);
		countTestSystem.doFullCheck(PriorityScheduler.class, 0, 0, 0);
		countTestSystem.doFullCheck(WFQScheduler.class, 0, 0, 0);

		countTestSystem.doFullCheck(Graph.class, 0, 6, 0);
		countTestSystem.doFullCheck(Node.class, 0, 8, 2);
		countTestSystem.doFullCheck(Edge.class, 0, 0, 4);

		countTestSystem.checkIfEmpty();
	}

	@Test(timeout = 1000)
	public void testIsAHost() {
		Network network = networkingSystem.createNetwork();
		NetworkNode node1 = networkingSystem.createNode(network);
		NetworkNode node2 = networkingSystem.createNode(network);

		Host h = networkingSystem.createHost(network, "Test");
		NetworkNode hostNode = networkingSystem.addInterface(h, new NetworkInterface("eth0", "00:00:00:00:00:01", "125.2.2.1"));
		NetworkNode hostNode2 = networkingSystem.addInterface(h, new NetworkInterface("eth2", "00:00:00:00:00:03", "125.2.2.5"));
		Host h2 = networkingSystem.createHost(network, "Test");
		NetworkNode hostNode3 = networkingSystem.addInterface(h2, new NetworkInterface("eth0", "00:00:00:00:00:05", "125.2.2.12"));

		assertTrue(networkingSystem.isAHost(network, hostNode.getQueueNode()));
		assertTrue(networkingSystem.isAHost(network, hostNode.getLinkNode()));
		assertTrue(networkingSystem.isAHost(network, hostNode2.getQueueNode()));
		assertTrue(networkingSystem.isAHost(network, hostNode2.getLinkNode()));
		assertTrue(networkingSystem.isAHost(network, hostNode3.getQueueNode()));
		assertTrue(networkingSystem.isAHost(network, hostNode3.getLinkNode()));

		assertFalse(networkingSystem.isAHost(network, node1.getQueueNode()));
		assertFalse(networkingSystem.isAHost(network, node1.getLinkNode()));
		assertFalse(networkingSystem.isAHost(network, node2.getQueueNode()));
		assertFalse(networkingSystem.isAHost(network, node2.getLinkNode()));

		// check if the given node is crap
		assertFalse(networkingSystem.isAHost(network, networkingSystem.createNode(network).getLinkNode()));
		assertFalse(networkingSystem.isAHost(network, networkingSystem.createNode(network).getQueueNode()));
		assertFalse(networkingSystem.isAHost(network, networkingSystem.createNode(networkingSystem.createNetwork()).getLinkNode()));
		assertFalse(networkingSystem.isAHost(network, networkingSystem.createNode(networkingSystem.createNetwork()).getQueueNode()));
		assertFalse(networkingSystem.isAHost(network, graphSystem.createNode(graphSystem.createGraph())));

		// check if the given network is crap
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), hostNode.getQueueNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), hostNode.getLinkNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), hostNode2.getQueueNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), hostNode2.getLinkNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), hostNode3.getQueueNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), hostNode3.getLinkNode()));

		// check if both are crap
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), networkingSystem.createNode(network).getLinkNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), networkingSystem.createNode(network).getQueueNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), networkingSystem.createNode(networkingSystem.createNetwork()).getLinkNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), networkingSystem.createNode(networkingSystem.createNetwork()).getQueueNode()));
		assertFalse(networkingSystem.isAHost(networkingSystem.createNetwork(), graphSystem.createNode(graphSystem.createGraph())));
	}
}
