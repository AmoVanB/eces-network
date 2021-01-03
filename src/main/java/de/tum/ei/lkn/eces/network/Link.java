package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.graph.Edge;

/**
 * Class representing a Link in a Network.
 * A Link consists of an Edge in the link-level Graph of the Network and of an
 * array of Edges in the queue-level Graph of the Network.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Link extends Component {
	/**
	 * Physical Edge corresponding to the Link in the link-level Graph.
	 */
	private Edge linkEdge;

	/**
	 * Queue-level Edges corresponding to the Link.
	 */
	private Edge[] queueEdges;

	/**
	 * Network to which the Node belongs.
	 */
	private Network network;

	/**
	 * Link name.
	 */
	private String name;

	/**
	 * Creates a new Link.
	 * @param linkEdge link-level Edge of the Link.
	 * @param queueEdges queue-level Edges of the Link.
	 * @param network Network containing the Link.
	 */
	public Link(Edge linkEdge, Edge[] queueEdges, Network network) {
		this(linkEdge, queueEdges, network, "");
	}

	/**
	 * Creates a new Link.
	 * @param linkEdge link-level Edge of the Link.
	 * @param queueEdges queue-level Edges of the Link.
	 * @param network Network containing the Link.
	 * @param name Name.
	 */
	public Link(Edge linkEdge, Edge[] queueEdges, Network network, String name) {
		this.linkEdge = linkEdge;
		this.queueEdges = queueEdges;
		this.network = network;
		this.name = name;
	}

	/**
	 * Gets the link-level Edge corresponding to the Link.
	 * @return the link-level Edge.
	 */
	public Edge getLinkEdge() {
		return linkEdge;
	}

	/**
	 * Gets the queue-level Edge corresponding to the Edge.
	 * @return the link-level Edge.
	 */
	public Edge[] getQueueEdges() {
		return queueEdges;
	}

	/**
	 * Gets the name of the Link.
	 * @return Edge name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the Network containing the Link.
	 * @return Network containing the Link.
	 */
	public Network getNetwork() {
		return network;
	}

	public String toString() {
		if(this.getName().compareTo("") == 0)
			return super.toString();
		else
			return this.getName();
	}
}