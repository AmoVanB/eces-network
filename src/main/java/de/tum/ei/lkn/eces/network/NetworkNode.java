package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import de.tum.ei.lkn.eces.graph.Node;
import org.json.JSONObject;

/**
 * Class representing a Node in a Network.
 * A Node in a Network consists of a Node in the link-level Graph of the Network
 * and of a Node in the queue-level Graph of the Network.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class NetworkNode extends Component {
	/**
	 * Name of the Node.
	 */
	private String name;

	/**
	 * Link-level Graph Node corresponding to the NetworkNode.
	 */
	private Node linkNode;

	/**
	 * Queue-level Graph Node corresponding to the NetworkNode.
	 */
	private Node queueNode;

	/**
	 * Network to which the Node belongs.
	 */
	private Network network;

	/**
	 * Creates a new NetworkNode.
	 * @param name Name of the Node.
	 * @param linkNode Node in the link-level Graph of the Network.
	 * @param queueNode Node in the queue-level Graph of the Network.
	 * @param network Network containing the Link.
	 */
	protected NetworkNode(String name, Node linkNode, Node queueNode, Network network) {
		this.name = name;
		this.linkNode = linkNode;
		this.queueNode = queueNode;
		this.network = network;
	}

	/**
	 * Gets the link-level Node corresponding to the NetworkNode.
	 * @return link-level Node.
	 */
	public Node getLinkNode() {
		return linkNode;
	}

	/**
	 * Gets the link-level Node corresponding to the NetworkNode.
	 * @return link-level Node.
	 */
	public Node getQueueNode() {
		return queueNode;
	}

	/**
	 * Gets the name of the Node.
	 * @return Name of the Node.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the Network containing the Node.
	 * @return Network containing the Nofde.
	 */
	public Network getNetwork() {
		return network;
	}

	public JSONObject toJSONObject() {
		JSONObject result = new JSONObject();
		if(this.getNetwork().getHostNodesToHost().get(this) != null) {
			// this node is a host
			result.put("type", "interface");
		}
		else {
			result.put("type", "switch");
		}

		return result;
	}
}