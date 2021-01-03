package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.MapperSpace;
import de.tum.ei.lkn.eces.core.RootSystem;
import de.tum.ei.lkn.eces.graph.Edge;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.GraphSystem;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.graph.mappers.EdgeMapper;
import de.tum.ei.lkn.eces.graph.mappers.NodeMapper;
import de.tum.ei.lkn.eces.network.exceptions.NetworkException;
import de.tum.ei.lkn.eces.network.mappers.*;
import de.tum.ei.lkn.eces.network.util.NetworkInterface;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.DataRate;
import javax.measure.quantity.Duration;
import java.util.LinkedList;
import java.util.List;

import static javax.measure.unit.NonSI.BYTE;
import static javax.measure.unit.SI.SECOND;

/**
 * System handling a Network.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NetworkingSystem extends RootSystem {
	/**
	 * Graph System used.
	 */
	private GraphSystem graphSystem;

	// Mappers
	private NetworkMapper networkMapper;
	private NetworkNodeMapper networkNodeMapper;
	private LinkMapper linkMapper;
	private HostMapper hostMapper;
	private ToNetworkMapper toNetworkMapper;
	private DelayMapper delayMapper;
	private QueueMapper queueMapper;
	private RateMapper rateMapper;
	private SchedulerMapper schedulerMapper;
	private NodeMapper nodeMapper;
	private EdgeMapper edgeMapper;

	/**
	 * Creates a new NetworkingSystem.
	 * @param controller Controller responsible for the NetworkingSystem.
	 */
	public NetworkingSystem(Controller controller) {
		this(controller, new GraphSystem(controller));
	}

	/**
	 * Creates a new NetworkingSystem.
	 * @param controller Controller responsible for the NetworkingSystem.
	 * @param graphSystem Underlying GraphSystem used by the NetworkingSystem.
	 */
	public NetworkingSystem(Controller controller, GraphSystem graphSystem) {
		super(controller);
		this.graphSystem = graphSystem;
		this.networkMapper = new NetworkMapper(controller);
		this.toNetworkMapper = new ToNetworkMapper(controller);
		this.delayMapper = new DelayMapper(controller);
		this.queueMapper = new QueueMapper(controller);
		this.rateMapper = new RateMapper(controller);
		this.schedulerMapper = new SchedulerMapper(controller);
		this.linkMapper = new LinkMapper(controller);
		this.hostMapper = new HostMapper(controller);
		this.networkNodeMapper = new NetworkNodeMapper(controller);
		this.nodeMapper = new NodeMapper(controller);
		this.edgeMapper = new EdgeMapper(controller);
	}

	/**
	 * Creates a new Network as a Component of a new Entity.
	 * @return the new Network.
	 */
	public Network createNetwork() {
		try(MapperSpace ms = controller.startMapperSpace()) {
			// Create Graphs.
			Graph linkGraph = graphSystem.createGraph();
			Graph queueGraph = graphSystem.createGraph();

			// Create Network.
			Entity networkEntity = controller.createEntity();
			Network network = new Network(linkGraph, queueGraph);
			networkMapper.attachComponent(networkEntity, network);

			// Attach references.
			ToNetwork linkGraphToNetwork = new ToNetwork();
			ToNetwork queueGraphToNetwork = new ToNetwork();
			linkGraphToNetwork.setNetworkEntity(networkEntity);
			queueGraphToNetwork.setNetworkEntity(networkEntity);
			toNetworkMapper.attachComponent(linkGraph, linkGraphToNetwork);
			toNetworkMapper.attachComponent(queueGraph, queueGraphToNetwork);

			logger.info(network + " creation triggered.");
			return network;
		}
	}

	/**
	 * Deletes a Network, all its Components and the underlying Graphs.
	 * @param network Network to delete.
	 */
	public void deleteNetwork(Network network) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			networkMapper.acquireReadLock(network);

			// Deleting all the Hosts of the Network.
			for(Host host : network.getHosts())
					deleteHostWithoutGraph(host);

			/* Deleting all the Links of the Network which are not connected to
			 * a Host because this has already been done with the Host deletion
			 * step here above. */
			for(Edge edge : network.getLinkGraph().getEdges()) {
				edgeMapper.acquireReadLock(edge);
				Node graphSrc = edge.getSource();
				Node graphDst = edge.getDestination();
				nodeMapper.acquireReadLock(graphSrc);
				nodeMapper.acquireReadLock(graphDst);
				NetworkNode src = networkNodeMapper.get(toNetworkMapper.get(graphSrc.getEntity()).getNetworkEntity());
				NetworkNode dst = networkNodeMapper.get(toNetworkMapper.get(graphDst.getEntity()).getNetworkEntity());
				Link link = linkMapper.get(toNetworkMapper.get(edge.getEntity()).getNetworkEntity());

				if(!network.getHostNodesToHost().containsKey(src) && !network.getHostNodesToHost().containsKey(dst))
					deleteLinkWithoutGraph(link);
			}

			/* Deleting all the Nodes of the Network which are not Hosts
			 * NetworkInterfaces because this has already been done with the
			 * Host deletion step here above. */
			for(Node graphNode : network.getLinkGraph().getNodes()) {
				ToNetwork toNetwork = toNetworkMapper.get(graphNode.getEntity());
				NetworkNode node = networkNodeMapper.get(toNetwork.getNetworkEntity());

				if(!network.getHostNodesToHost().containsKey(node)) {
					// Remove toNetwork Components of the Node.
					toNetworkMapper.detachComponent(node.getLinkNode());
					toNetworkMapper.detachComponent(node.getQueueNode());

					// Remove Node from Network.
					networkNodeMapper.detachComponent(node);
				}
			}

			// Deleting the underlying Graphs.
			graphSystem.deleteGraph(network.getLinkGraph());
			graphSystem.deleteGraph(network.getQueueGraph());

			networkMapper.detachComponent(network);

			logger.info(network + " deletion trigerred.");
		}
	}

	/**
	 * Deletes a Network, all its Components and the underlying Graphs.
	 * @param entity Entity containing the Network to be deleted.
	 */
	public void deleteNetwork(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteNetwork(networkMapper.get(entity));
		}
	}

	/**
	 * Creates a Node in a Network.
	 * @param network Network in which to create the Node.
	 * @param name name of the Node.
	 * @return the created NetworkingNode.
	 */
	public NetworkNode createNode(Network network, String name) {
		NetworkNode node;

		try(MapperSpace ms = controller.startMapperSpace()) {
			// Create Graph nodes equivalent.
			networkMapper.acquireReadLock(network);
			Graph linkGraph = network.getLinkGraph();
			Graph queueGraph = network.getQueueGraph();
			Node linkNode = graphSystem.createNode(linkGraph, name);
			Node queueNode = graphSystem.createNode(queueGraph, name);

			// Create NetworkNode.
			Entity nodeEntity = controller.createEntity();
			node = new NetworkNode(name, linkNode, queueNode, network);
			networkNodeMapper.attachComponent(nodeEntity, node);

			// Attaching the toNetwork Components.
			ToNetwork linkLevelToNetwork = new ToNetwork();
			ToNetwork queueLevelToNetwork = new ToNetwork();
			linkLevelToNetwork.setNetworkEntity(nodeEntity);
			queueLevelToNetwork.setNetworkEntity(nodeEntity);
			toNetworkMapper.attachComponent(linkNode, linkLevelToNetwork);
			toNetworkMapper.attachComponent(queueNode, queueLevelToNetwork);

			logger.info(node + " creation in " + network + " triggered.");
		}

		return node;
	}

	/**
	 * Creates a Node in a Network.
	 * @param network Network in which to create the Node.
	 * @return the created NetworkingNode.
	 */
	public NetworkNode createNode(Network network) {
		return createNode(network, "");
	}

	/**
	 * Deletes a Node and all links connected to it. This method cannot be
	 * called on a Node belonging to a Host's NetworkInterface. To delete
	 * such a Node, use deleteInterface or deleteHost methods.
	 * @param node NetworkNode to remove.
	 * @throws NetworkException if one tries to delete a Node belonging to a
	 * Host NetworkInterface. This should be done using the deleteInterface()
	 * method.
	 */
	public void deleteNode(NetworkNode node) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteNodeWithoutGraph(node);
			graphSystem.deleteNode(node.getQueueNode());
			graphSystem.deleteNode(node.getLinkNode());

			logger.info(node + " deletion from " + node.getNetwork() + " triggered.");
		}
	}

	/**
	 * Deletes a Node and all links connected to it. This method cannot be
	 * called on a Node belonging to a Host's NetworkInterface. To delete
	 * such a Node, use deleteInterface or deleteHost methods.
	 * @param node NetworkNode to remove.
	 * @throws NetworkException if one tries to delete a Node belonging to a
	 * Host NetworkInterface. This should be done using the deleteInterface()
	 * method.
	 */
	private void deleteNodeWithoutGraph(NetworkNode node) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			nodeMapper.acquireReadLock(node);
			Network network = node.getNetwork();
			networkNodeMapper.acquireReadLock(network);

			// Do not delete Nodes belonging to a Host.
			if(network.getHostNodesToHost().containsKey(node))
				throw new NetworkException("It is forbidden to delete a Node belonging to a Host NetworkInterface (" + node + " belongs to " + network.getHostNodesToHost().get(node) + "). This must be done using the deleteInterface() method.");

			deleteAnyNodeWithoutGraph(node);
		}
	}

	/**
	 * Deletes a Node and all links connected to it. This method can be called
	 * on any Node.
	 * @param node NetworkNode to remove.
	 */
	private void deleteAnyNode(NetworkNode node) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteAnyNodeWithoutGraph(node);

			// Remove queue- and link-level Nodes.
			graphSystem.deleteNode(node.getLinkNode());
			graphSystem.deleteNode(node.getQueueNode());

			logger.info(node + " deletion from " + node.getNetwork() + " triggered.");
		}
	}

	/**
	 * Deletes a Node and all links connected to it. This method can be called
	 * on any Node.
	 * No operation is done on the underlying Graph elements.
	 * @param node NetworkNode to remove.
	 */
	private void deleteAnyNodeWithoutGraph(NetworkNode node) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			networkNodeMapper.acquireReadLock(node);
			Network network = node.getNetwork();
			networkMapper.acquireReadLock(network);

			// Getting link-edges connected to the Node.
			List<Edge> edges = new LinkedList<>(node.getLinkNode().getOutgoingConnections());
			edges.addAll(node.getLinkNode().getIncomingConnections());

			// Remove all the Links connected to the Node.
			for(Edge edge : edges) {
				ToNetwork toNetwork = toNetworkMapper.get(edge.getEntity());
				Link link = linkMapper.get(toNetwork.getNetworkEntity());
				deleteLinkWithoutGraph(link);
			}

			// Remove toNetwork Components of the Node.
			toNetworkMapper.detachComponent(node.getLinkNode());
			toNetworkMapper.detachComponent(node.getQueueNode());

			// Remove Node from Network.
			networkNodeMapper.detachComponent(node);
		}
	}

	/**
	 * Deletes a Node and all links connected to it.
	 * deleteInterface or deleteHost methods.
	 * @param entity Entity containing the NetworkNode to remove.
	 * @throws NetworkException if one tries to delete a Node belonging to a
	 * Host NetworkInterface. This should be done using the deleteInterface()
	 * method.
	 */
	public void deleteNode(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteNode(networkNodeMapper.get(entity));
		}
	}

	/**
	 * Creates a Link with a single Queue.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link.
	 * @param delay Delay of the Link.
	 * @param queueSize Size of the Queue.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLink(NetworkNode srcNode, NetworkNode dstNode, Amount<DataRate> rate, Amount<Duration> delay, Amount<DataAmount> queueSize) {
		Queue queue = new Queue(queueSize.doubleValue(BYTE));
		Queue[] queues = {queue};
		Scheduler scheduler = new Scheduler(queues);
		return createLinkWithScheduler(srcNode, dstNode, rate, delay, scheduler);
	}

	/**
	 * Creates a Link with a single Queue.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate String representation of the rate of the Link (value followed
	 *             by a space followed by 'byte/s' or 'bit/s').
	 * @param delay String representation of the delay of the Link (value
	 *              followed by a space followed by the unit).
	 * @param queueSize String representation of the length of the Queue (value
	 *                  followed by a space followed by the unit).
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLink(NetworkNode srcNode, NetworkNode dstNode, String rate, String delay, String queueSize) {
		return createLink(srcNode, dstNode, Amount.valueOf(rate).to(Rate.BYTES_PER_SECOND), Amount.valueOf(delay).to(SECOND), Amount.valueOf(queueSize).to(BYTE));
	}

	/**
	 * Creates a Link with a single Queue.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link in bytes/s.
	 * @param delay Delay of the Link in seconds.
	 * @param queueSize Size of each Queue (List must be of size numQueues) in
	 *                   bytes.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLink(NetworkNode srcNode, NetworkNode dstNode, double rate, double delay, double queueSize) {
		return createLink(srcNode, dstNode, Amount.valueOf(rate, Rate.BYTES_PER_SECOND), Amount.valueOf(delay, SECOND), Amount.valueOf(queueSize, BYTE));
	}

	/**
	 * Creates a Link with a priority scheduling policy.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link in bytes/s.
	 * @param delay Delay of the Link in seconds.
	 * @param queueSizes Size of each Queue in bytes. The size of the array
	 *                   corresponds to the number of Queues to add.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLinkWithPriorityScheduling(NetworkNode srcNode, NetworkNode dstNode, double rate, double delay, double[] queueSizes) {
		List<Amount<DataAmount>> queueSizesList = new LinkedList<>();
		for(double queueSize : queueSizes)
			queueSizesList.add(Amount.valueOf(queueSize, BYTE));
		return createLinkWithPriorityScheduling(srcNode, dstNode, Amount.valueOf(rate, Rate.BYTES_PER_SECOND), Amount.valueOf(delay, SECOND), queueSizesList);
	}

	/**
	 * Creates a Link with a priority scheduling policy.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link.
	 * @param delay Delay of the Link.
	 * @param queueSizes Size of each Queue (List must be of size numQueues).
	 *                   The size of the List corresponds to the number of
	 *                   Queues to add.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLinkWithPriorityScheduling(NetworkNode srcNode, NetworkNode dstNode, Amount<DataRate> rate, Amount<Duration> delay, List<Amount<DataAmount>> queueSizes) {
		if(queueSizes.size() < 1)
			throw new NetworkException("Impossible to create a link with less than one queue");

		// Creating Scheduler and Queues.
		Queue[] queues = new Queue[queueSizes.size()];
		for(int i = 0; i < queues.length; i++)
			queues[i] = new Queue(queueSizes.get(i));
		PriorityScheduler scheduler = new PriorityScheduler(queues);

		return createLinkWithScheduler(srcNode, dstNode, rate, delay, scheduler);
	}

	/**
	 * Creates a Link with a priority scheduling policy.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate String representation of the rate of the Link (value followed
	 *             by a space followed by 'byte/s' or 'bit/s').
	 * @param delay String representation of the delay of the Link (value
	 *              followed by a space followed by the unit).
	 * @param queueSizes String representation of the length of each Queue
	 *                   (value followed by a space followed by the unit). The
	 *                   List size corresponds to the number of Queues to add.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLinkWithPriorityScheduling(NetworkNode srcNode, NetworkNode dstNode, String rate, String delay, String[] queueSizes) {
		List<Amount<DataAmount>> queueSizesList = new LinkedList<>();
		for(String queueSize : queueSizes)
			queueSizesList.add(Amount.valueOf(queueSize).to(BYTE));
		return createLinkWithPriorityScheduling(srcNode, dstNode, Amount.valueOf(rate).to(Rate.BYTES_PER_SECOND), Amount.valueOf(delay).to(SECOND), queueSizesList);
	}

	/**
	 * Creates a Link with a WFQ scheduling policy.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link in bytes/s.
	 * @param delay Delay of the Link in seconds.
	 * @param queueSizes Size of each Queue in bytes. The size of the array
	 *                   corresponds to the number of Queues to add.
	 * @param weights Weights to assign to each Queue. The size of the array
	 *                must be the same as the size of queueSizes).
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLinkWithWFQScheduling(NetworkNode srcNode, NetworkNode dstNode, double rate, double delay, double[] queueSizes, double[] weights) {
		List<Amount<DataAmount>> queueSizesList = new LinkedList<>();
		for(double queueSize : queueSizes)
			queueSizesList.add(Amount.valueOf(queueSize, BYTE));
		return createLinkWithWFQScheduling(srcNode, dstNode, Amount.valueOf(rate, Rate.BYTES_PER_SECOND), Amount.valueOf(delay, SECOND), queueSizesList, weights);
	}

	/**
	 * Creates a Link with a WFQ scheduling policy.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link.
	 * @param delay Delay of the Link.
	 * @param queueSizes Size of each Queue (List must be of size numQueues).
	 *                   The size of the List corresponds to the number of
	 *                   Queues to add.
	 * @param weights Weights to assign to each Queue. The size of the array
	 *                must be the same as the size of queueSizes).
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLinkWithWFQScheduling(NetworkNode srcNode, NetworkNode dstNode, Amount<DataRate> rate, Amount<Duration> delay, List<Amount<DataAmount>> queueSizes, double[] weights) {
		if(queueSizes.size() != weights.length)
			throw new NetworkException("The array of queue sizes must be of same size as the array of weights");
		if(queueSizes.size() < 1)
			throw new NetworkException("Impossible to create a link with less than one queue");

		// Creating Scheduler and Queues.
		Queue[] queues = new Queue[queueSizes.size()];
		for(int i = 0; i < queues.length; i++)
			queues[i] = new Queue(queueSizes.get(i));
		WFQScheduler scheduler = new WFQScheduler(queues, weights);

		return createLinkWithScheduler(srcNode, dstNode, rate, delay, scheduler);
	}

	/**
	 * Creates a Link with a priority scheduling policy.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate String representation of the rate of the Link (value followed
	 *             by a space followed by 'byte/s' or 'bit/s').
	 * @param delay String representation of the delay of the Link (value
	 *              followed by a space followed by the unit).
	 * @param queueSizes String representation of the length of each Queue
	 *                   (value followed by a space followed by the unit). The
	 *                   List size corresponds to the number of Queues to add.
	 * @param weights Weights to assign to each Queue. The size of the array
	 *                must be the same as the size of queueSizes.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	public Link createLinkWithWFQScheduling(NetworkNode srcNode, NetworkNode dstNode, String rate, String delay, String[] queueSizes, String[] weights) {
		List<Amount<DataAmount>> queueSizesList = new LinkedList<>();
		for(String queueSize : queueSizes)
			queueSizesList.add(Amount.valueOf(queueSize).to(BYTE));

		double[] weightsDouble = new double[weights.length];
		for(int i = 0; i < weights.length; i++)
			weightsDouble[i] = Double.parseDouble(weights[i]);

		return createLinkWithWFQScheduling(srcNode, dstNode, Amount.valueOf(rate).to(Rate.BYTES_PER_SECOND), Amount.valueOf(delay).to(SECOND), queueSizesList, weightsDouble);
	}

	/**
	 * Helper method creating a Link with a specific Scheduler.
	 * @param srcNode Source of the Link.
	 * @param dstNode Destination of the Link.
	 * @param rate Rate of the Link.
	 * @param delay Delay of the Link.
	 * @param scheduler Scheduler managing the Queues to be added.
	 * @return The newly created Link.
	 * @throws NetworkException if we try to create a Link on a NetworkInterface
	 * of a host that already has a Link in this direction or if we try to
	 * create a Link between two NetworkInterface.
	 */
	private Link createLinkWithScheduler(NetworkNode srcNode, NetworkNode dstNode, Amount<DataRate> rate, Amount<Duration> delay, Scheduler scheduler) {
		Link link;

		try(MapperSpace ms = controller.startMapperSpace()) {
			// Checking Nodes belong to the same Network.
			networkNodeMapper.acquireReadLock(srcNode);
			networkNodeMapper.acquireReadLock(dstNode);
			if(srcNode.getNetwork() != dstNode.getNetwork())
				throw new NetworkException("Impossible to create a Link between two Nodes of different Networks (" + srcNode + " belongs to " + srcNode.getNetwork() + " while " + dstNode + " belongs to " + dstNode.getNetwork());
			Network network = srcNode.getNetwork();

			/* We throw a NetworkException if the user tries to create a Link on
			 * an interface that already has a Link in this direction. */
			networkMapper.acquireReadLock(network);
			if(network.getHostNodesToHost().containsKey(srcNode)) {
				/* The source node is a NetworkInterface, we check that it
				 * has no uplink yet. */
				Node linkNode = srcNode.getLinkNode();
				nodeMapper.acquireReadLock(linkNode);
				if(srcNode.getLinkNode().getOutgoingConnections().size() > 0)
					throw new NetworkException(srcNode + " is an interface of " + network.getHostNodesToHost().get(srcNode) + " which has already an uplink");
			}

			if(network.getHostNodesToHost().containsKey(dstNode)) {
				/* The destination node is a NetworkInterface, we check that it
				 * has no downlink yet. */
				Node linkNode = dstNode.getLinkNode();
				nodeMapper.acquireReadLock(linkNode);
				if(dstNode.getLinkNode().getIncomingConnections().size() > 0)
					throw new NetworkException(dstNode + " is an interface of " + network.getHostNodesToHost().get(dstNode) + " which has already a downlink");
			}

			/* We throw a NetworkException if the user tries to create a Link
			 * between two NetworkInterfaces. */
			if(network.getHostNodesToHost().containsKey(srcNode) && network.getHostNodesToHost().containsKey(dstNode))
				throw new NetworkException("It is not allowed to create a Link between two host interfaces");

			// Getting link- and queue-level Graphs and Nodes.
			networkMapper.acquireReadLock(network);
			Node linkSrcNode = srcNode.getLinkNode();
			Node queueSrcNode = srcNode.getQueueNode();
			Node linkDstNode = dstNode.getLinkNode();
			Node queueDstNode = dstNode.getQueueNode();

			// Creating link-level Edge and adding Rate, Delay and Scheduler to it.
			Edge linkEdge;
			if(linkSrcNode.getName().compareTo("") != 0 && linkDstNode.getName().compareTo("") != 0)
				linkEdge = graphSystem.createEdge(linkSrcNode, linkDstNode, linkSrcNode.getName() + "->" + linkDstNode.getName());
			else
				linkEdge = graphSystem.createEdge(linkSrcNode, linkDstNode);
			rateMapper.attachComponent(linkEdge, new Rate(rate));
			delayMapper.attachComponent(linkEdge, new Delay(delay));
			schedulerMapper.attachComponent(linkEdge, scheduler);

			// Creating queue-level Edges and adding Queues to them.
			schedulerMapper.acquireReadLock(scheduler);
			Queue[] queues = scheduler.getQueues();
			Edge[] queueEdges = new Edge[queues.length];
			for(int i = 0; i < queueEdges.length; i++) {
				if(queueSrcNode.getName().compareTo("") != 0 && queueDstNode.getName().compareTo("") != 0)
					queueEdges[i] = graphSystem.createEdge(queueSrcNode, queueDstNode, queueSrcNode.getName() + "->" + queueDstNode.getName() + "#" + i);
				else
					queueEdges[i] = graphSystem.createEdge(queueSrcNode, queueDstNode);
				queueMapper.attachComponent(queueEdges[i], queues[i]);
			}

			// Creating Link.
			if(srcNode.getName().compareTo("") != 0 && dstNode.getName().compareTo("") != 0)
				link = new Link(linkEdge, queueEdges, network, srcNode.getName() + "->" + dstNode.getName());
			else
				link = new Link(linkEdge, queueEdges, network);
			Entity linkEntity = controller.createEntity();
			linkMapper.attachComponent(linkEntity, link);

			// Creating reference from link- queue- level Edges to Link.
			ToNetwork linkEdgeToNetwork = new ToNetwork();
			linkEdgeToNetwork.setNetworkEntity(linkEntity);
			toNetworkMapper.attachComponent(linkEdge, linkEdgeToNetwork);

			for(int i = 0; i < queues.length; i++) {
				ToNetwork queueEdgeToNetwork = new ToNetwork();
				queueEdgeToNetwork.setNetworkEntity(linkEntity);
				toNetworkMapper.attachComponent(queueEdges[i], queueEdgeToNetwork);
			}

			logger.info(link + " (" + srcNode + " -> " + dstNode + " - " + scheduler + " - " + scheduler.getQueues().length + " queues) creation in " + network + " triggered.");
		}

		return link;
	}

	/**
	 * Deletes a Link.
	 * @param entity Entity to which the Link is attached.
	 */
	public void deleteLink(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteLink(linkMapper.get(entity));
		}
	}

	/**
	 * Deletes a Link.
	 * @param link Link to delete.
	 */
	public void deleteLink(Link link) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteLinkWithoutGraph(link);

			// Deleting the underlying Graph part of the Link.
			linkMapper.acquireReadLock(link);

			for(Edge edge : link.getQueueEdges())
				graphSystem.deleteEdge(edge);
			graphSystem.deleteEdge(link.getLinkEdge());

			logger.info(link + " deletion from " + link.getNetwork() + " triggered.");
		}
	}

	/**
	 * Deletes a Link.
	 * No operation is done on the underlying Graph elements.
	 * @param link Link to delete.
	 */
	private void deleteLinkWithoutGraph(Link link) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			linkMapper.acquireReadLock(link);
			Edge[] queueEdges = link.getQueueEdges();
			Edge linkEdge = link.getLinkEdge();

			// Remove the toNetwork and Queues from queue-level Edges and then delete them.
			for(Edge edge : queueEdges) {
				toNetworkMapper.detachComponent(edge);
				queueMapper.detachComponent(edge);
			}

			// Remove the toNetwork, Scheduler, Delay and Rate from the link-level Edge and then delete it.
			toNetworkMapper.detachComponent(linkEdge);
			schedulerMapper.detachComponent(linkEdge);
			delayMapper.detachComponent(linkEdge);
			rateMapper.detachComponent(linkEdge);

			// Destroying link.
			linkMapper.detachComponent(link);
		}
	}

	/**
	 * Creates a Host in a Network.
	 * @param network Network in which the Host must be created.
	 * @param name Name of the Host.
	 * @return Newly created Host.
	 */
	public Host createHost(Network network, String name) {
		Host host = new Host(network, name);
		try(MapperSpace ms = controller.startMapperSpace()) {
			hostMapper.attachComponent(controller.createEntity(), host);
			networkMapper.updateComponent(network, ()->network.addHost(host));

			logger.info(host + " creation in " + network + " triggered.");
		}

		return host;
	}

	public Host createHost(Network network) {
		return createHost(network, "");
	}

	/**
	 * Deletes a Host and all of its Interfaces (this means all the NetworkNode
	 * representing it and all the Links connecting it to the Network).
	 * @param host Host to delete.
	 */
	public void deleteHost(Host host) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteHostWithoutGraph(host);
			hostMapper.acquireReadLock(host);

			// Delete the underlying Graph Nodes.
			for(NetworkInterface ifc : host.getInterfaces()) {
				NetworkNode node = host.getNetworkNode(ifc);
				networkNodeMapper.acquireReadLock(node);
				graphSystem.deleteNode(node.getLinkNode());
				graphSystem.deleteNode(node.getQueueNode());
			}

			logger.info(host + " deletion from " + host.getNetwork() + " triggered.");
		}
	}

	/**
	 * Deletes a Host and all of its Interfaces (this means all the NetworkNode
	 * representing it and all the Links connecting it to the Network).
	 * No operation is done on the underlying Graph.
	 * @param host Host to delete.
	 */
	private void deleteHostWithoutGraph(Host host) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			hostMapper.acquireReadLock(host);
			Network network = host.getNetwork();

			// Deleting all the interfaces of the Host.
			for(NetworkInterface ifc : host.getInterfaces())
				deleteInterfaceWithoutGraph(host, ifc);

			networkMapper.updateComponent(network, ()->network.removeHost(host));
			hostMapper.detachComponent(host);

			logger.info(host + " deletion from " + network + " triggered.");
		}
	}

	/**
	 * Deletes a Host and all of its Interfaces (this means all the NetworkNode
	 * and Link representing it).
	 * @param entity Entity to which the Host is attached.
	 */
	public void deleteHost(Entity entity) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteHost(hostMapper.get(entity));
		}
	}

	/**
	 * Adds an interface to a Host.
	 * @param host Host to which an interface must be added.
	 * @param ifc NetworkInterface to add.
	 */
	public NetworkNode addInterface(Host host, NetworkInterface ifc) {
		NetworkNode ifcNode;
		try(MapperSpace ms = controller.startMapperSpace()) {
			// Creating the Node representing the Interface.
			hostMapper.acquireReadLock(host);
			Network network = host.getNetwork();
			ifcNode = createNode(network, host.getName() + ":" + ifc.getName());

			hostMapper.updateComponent(host, ()->host.addInterface(ifc, ifcNode));
			networkMapper.updateComponent(network, ()->network.addInterface(host, ifc, ifcNode));

			logger.info("Addition of " + ifc + " (" + ifcNode + ") to " + host + " triggered.");
		}

		return ifcNode;
	}

	/**
	 * Deletes an interface from a Host. The NetworkNode representing the
	 * interface and the Links connecting it to the network are also deleted.
	 * @param host Host.
	 * @param ifc Interface from the Host that has to be removed.
	 */
	public void deleteInterface(Host host, NetworkInterface ifc) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			deleteInterfaceWithoutGraph(host, ifc);

			// Deleting the Graph Nodes.
			hostMapper.acquireReadLock(host);
			NetworkNode node = host.getNetworkNode(ifc);
			networkNodeMapper.acquireReadLock(node);
			graphSystem.deleteNode(node.getLinkNode());
			graphSystem.deleteNode(node.getQueueNode());

			logger.info("Deletion of " + ifc + " (" + node + ") from " + host + " triggered.");
		}
	}

	/**
	 * Check if a graph node corresponds to a host in a network.
	 * @param network given Network.
	 * @param node given graph Node (queue- or link-level).
	 * @return true if the graph Node corresponds to a Host in the network
	 *         (more precisely, if it corresponds to the Interface of a Host),
	 *         false otherwise.
	 */
	public boolean isAHost(Network network, Node node) {
		// check that there is a mapping to a network
		if(!toNetworkMapper.isIn(node.getEntity()))
			return false;

		// check that this mapping leads to a network node
		Entity networkEntity = toNetworkMapper.get(node.getEntity()).getNetworkEntity();
		if (!networkNodeMapper.isIn(networkEntity))
			return false;

		// check that the network node is in the given network
		NetworkNode networkNode = networkNodeMapper.get(networkEntity);
		if (networkNode.getNetwork() != network)
			return false;

		// check that the network node is an interface of the network
		return network.getHostNodesToHost().containsKey(networkNode);
	}

	/**
	 * Deletes an interface from a Host. The NetworkNode representing the
	 * interface and the Links connecting it to the network are also deleted.
	 * No operation is done on the underlying Graph.
	 * @param host Host.
	 * @param ifc Interface from the Host that has to be removed.
	 */
	private void deleteInterfaceWithoutGraph(Host host, NetworkInterface ifc) {
		try(MapperSpace ms = controller.startMapperSpace()) {
			hostMapper.acquireReadLock(host);
			Network network = host.getNetwork();
			NetworkNode ifcNode = host.getNetworkNode(ifc);

			// Update associated data structures.
			hostMapper.updateComponent(host, ()->host.removeInterface(ifc));
			networkMapper.updateComponent(network, ()->network.removeInterface(ifc));

			// Delete the Node representing the interface.
			deleteAnyNodeWithoutGraph(ifcNode);
		}
	}
}
