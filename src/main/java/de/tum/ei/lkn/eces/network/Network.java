package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.graph.Graph;
import de.tum.ei.lkn.eces.graph.Node;
import de.tum.ei.lkn.eces.network.exceptions.NetworkException;
import de.tum.ei.lkn.eces.network.util.IPAddress;
import de.tum.ei.lkn.eces.network.util.MACAddress;
import de.tum.ei.lkn.eces.network.util.NetworkInterface;

import java.util.*;

/**
 * A network is represented by two graphs:
 * - The link-level Graph and
 * - the queue-level Graph.
 *
 * To the link-level Graph Edge's Entity are attached the Rate, Delay and
 * Scheduler of the link.
 *
 * To the queue-level graph Edge's Entity is attached the Queue corresponding
 * to the queue-level Edge.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Network extends Component {
	/**
	 * Link-level Graph of the Network.
	 */
	private final Graph linkGraph;

	/**
	 * Queue-level Graph of the Network.
	 */
	private final Graph queueGraph;

	/**
	 * MAC addresses of Nodes in the Network.
	 */
	private final Map<MACAddress, NetworkNode> macAddresses;

	/**
	 * IP addresses of Nodes in the Network.
	 */
	private final Map<IPAddress, NetworkNode> ipAddresses;

	/**
	 * Map of NetworkNodes to their corresponding Host if the Node actually
	 * belongs to a Host.
	 */
	private final Map<NetworkNode, Host> hostNodesToHost;

	/**
	 * Sets of the Nodes in the Network.
	 */
	private final Set<Host> hosts;

	public Network(Graph linkGraph, Graph queueGraph) {
		super();
		this.hostNodesToHost = new HashMap<>();
		this.macAddresses = new HashMap<>();
		this.ipAddresses = new HashMap<>();
		this.hosts = new HashSet<>();
		this.linkGraph = linkGraph;
		this.queueGraph = queueGraph;
	}

	/**
	 * Gets the queue-level Graph of the Network.
	 * @return the queue-level Graph.
	 */
	public Graph getQueueGraph() {
		return queueGraph;
	}

	/**
	 * Gets the link-level Graph of the Network.
	 * @return the link-level Graph.
	 */
	public Graph getLinkGraph() {
		return linkGraph;
	}

	/**
	 * Gets the identifier of this Network. The ID is the ID of the Entity
	 * holding this Node. This is unique among all Nodes since an Entity can
	 * only carry one Node.
	 * @return the ID.
	 */
	public long getId() {
		return this.getEntity().getId();
	}

	/**
	 * Gets a Set of the Hosts in the Network.
	 * @return Set of Hosts.
	 */
	public Collection<Host> getHosts() {
		return Collections.unmodifiableSet(hosts);
	}

	/**
	 * Adds a Host the Network. The Host is assumed to have no NetworkInterfaces
	 * yet.
	 * @param host Host to add.
	 */
	protected void addHost(Host host) {
		hosts.add(host);
	}

	/**
	 * Removes a Host from the Network. The Host is assumed to have no
	 * NetworkInterfaces anymore.
	 * @param host Host to remove.
	 */
	protected void removeHost(Host host) {
		hosts.remove(host);
	}

	/**
	 * Adds an interface in the Network.
	 * @param host Host to which the interface belongs.
	 * @param ifc Object representing the Interface to add.
	 * @param node NetworkNode representing the Interface to add in the Network.
	 */
	protected void addInterface(Host host, NetworkInterface ifc, NetworkNode node) {
		IPAddress ip = ifc.getIPAddress();
		MACAddress mac = ifc.getMACAddress();

		// IP checks.
		if(ip.equals(IPAddress.valueOf("0.0.0.0")))
			return; // We do not add the undefined IP address.
		if(ipAddresses.containsKey(ip))
			throw new NetworkException("A given IP address can only be present once in a network (" + ip + " already present)");

		// MAC checks.
		if(macAddresses.containsKey(mac))
			throw new NetworkException("A given MAC address can only be present once in a network (" + mac + " already present)");

		ipAddresses.put(ip, node);
		macAddresses.put(mac, node);
		hostNodesToHost.put(node, host);
	}

	/**
	 * Removes an Interface from the Network.
	 * @param ifc Interface to remove.
	 */
	protected void removeInterface(NetworkInterface ifc) {
		NetworkNode node = ipAddresses.get(ifc.getIPAddress());
		if(node == null)
			return;
		ipAddresses.remove(ifc.getIPAddress());
		macAddresses.remove(ifc.getMACAddress());
		hostNodesToHost.remove(node);
	}

	/**
	 * Gets the Map from NetworkNodes to the Host of which one of the Interface
	 * is represented by this NetworkNode.
	 * @return The Map.
	 */
	protected Map<NetworkNode, Host> getHostNodesToHost() {
		return Collections.unmodifiableMap(hostNodesToHost);
	}
}