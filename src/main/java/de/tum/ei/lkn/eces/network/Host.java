package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.network.exceptions.NetworkException;
import de.tum.ei.lkn.eces.network.util.NetworkInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class representing a Host in a Network.
 * A Host is represented by a series of NetworkInterfaces. Each NetworkInterface
 * is represented in the Network by a NetworkNode. Once the NetworkInterface is
 * connected to the Network, it is then also represented by up to two links:
 * an up- and a down-link.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Host extends Component {
	/**
	 * Name of the Host.
	 */
	private final String name;

	/**
	 * Network in which the Host is.
	 */
	private final Network network;

	/**
	 * NetworkNodes of all the NetworkInterfaces.
	 */
	private Map<NetworkInterface, NetworkNode> interfacesNodes;

	/**
	 * Uplinks of all the upwards connected NetworkInterfaces.
	 */
	private Map<NetworkInterface, Link> interfacesUpLinks;

	/**
	 * Downlinks of all the downwards connected NetworkInterfaces.
	 */
	private Map<NetworkInterface, Link> interfacesDownLinks;

	/**
	 * Set of the names of the NetworkInterfaces.
	 */
	private Set<String> interfaceNames;


	public Host(Network network, String name) {
		this.network = network;
		this.name = name;
		this.interfacesNodes = new HashMap<>();
		this.interfacesUpLinks = new HashMap<>();
		this.interfacesDownLinks = new HashMap<>();
		this.interfaceNames = new HashSet<>();
	}

	/**
	 * Gets all the NetworkInterfaces of the Host.
	 * @return all the NetworkInterfaces of the Host.
	 */
	public Set<NetworkInterface> getInterfaces() {
		return interfacesNodes.keySet();
	}

	/**
	 * Gets all the NetworkInterfaces that have an upwards connection to the
	 * Network, meaning that the interface can upload data in the network.
	 * @return The set of upwards-connected NetworkInterfaces.
	 */
	public Set<NetworkInterface> getUpwardsConnectedInterfaces() {
		return interfacesUpLinks.keySet();
	}

	/**
	 * Gets all the NetworkInterfaces that have a downwards connection to the
	 * Network, meaning that the interface can download data from the network.
	 * @return The set of downwards-connected NetworkInterfaces.
	 */
	public Set<NetworkInterface> getDownwardsConnectedInterfaces() {
		return interfacesDownLinks.keySet();
	}

	/**
	 * Gets the name of the Host.
	 * @return name of the Host.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the network in which the Host is.
	 * @return Network in which the Host is.
	 */
	public Network getNetwork() {
		return network;
	}

	/**
	 * Gets the NetworkNode representing a NetworkInterface of the Host.
	 * @param networkInterface a NetworkInterface of the Host.
	 * @return The NetworkNode representing the given NetworkInterface or null
	 *         if the NetworkInterface does not belong to the Host.
	 */
	public NetworkNode getNetworkNode(NetworkInterface networkInterface) {
		return interfacesNodes.get(networkInterface);
	}

	/**
	 * Gets the upwards Link connecting a NetworkInterface of the Host to the
	 * Network.
	 * @param networkInterface a NetworkInterface of the Host.
	 * @return The Link connecting the NetworkInterface to the Network or null
	 *         if the NetworkInterface does not belong to the Host or if this
	 *         NetworkInterface has no uplink yet.
	 */
	public Link getUpLink(NetworkInterface networkInterface) {
		return interfacesUpLinks.get(networkInterface);
	}

	/**
	 * Gets the downwards Link connecting a NetworkInterface of the Host to the
	 * Network.
	 * @param networkInterface a NetworkInterface of the Host.
	 * @return The Link connecting the NetworkInterface to the Network or null
	 *         if the NetworkInterface does not belong to the Host or if this
	 *         NetworkInterface has no downlink yet.
	 */
	public Link getDownLink(NetworkInterface networkInterface) {
		return interfacesDownLinks.get(networkInterface);
	}

	/**
	 * Adds a NetworkInterface to the Host.
	 * @param networkInterface NetworkInterface to add.
	 * @param node Node representing the NetworkInterface in the Network.
	 * @throws NetworkException if we try to add an interface which has the same
	 * name as an existing interface on the host.
	 */
	protected void addInterface(NetworkInterface networkInterface, NetworkNode node) {
		if(interfaceNames.contains(networkInterface.getName()))
			throw new NetworkException("A host cannot have two interfaces with the same name");
		interfacesNodes.put(networkInterface, node);
		interfaceNames.add(networkInterface.getName());
	}

	/**
	 * Connects a given NetworkInterface of the Host to the Network.
	 * Nothing is done if the link has the NetworkNode of the NetworkInterface
	 * neither as source nor as destination.
	 * @param networkInterface Interface to connect.
	 * @param link Link to be used for connection.
	 */
	protected void connectInterface(NetworkInterface networkInterface, Link link) {
		if(link.getLinkEdge().getSource() == getNetworkNode(networkInterface).getLinkNode())
			interfacesUpLinks.put(networkInterface, link);
		else if(link.getLinkEdge().getDestination() == getNetworkNode(networkInterface).getLinkNode())
			interfacesDownLinks.put(networkInterface, link);
	}

	/**
	 * Removes a NetworkInterface from the Host.
	 * @param networkInterface NetworkInterface to remove.
	 */
	protected void removeInterface(NetworkInterface networkInterface) {
		interfacesNodes.remove(networkInterface);
		interfacesUpLinks.remove(networkInterface);
		interfacesDownLinks.remove(networkInterface);
		interfaceNames.remove(networkInterface.getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}