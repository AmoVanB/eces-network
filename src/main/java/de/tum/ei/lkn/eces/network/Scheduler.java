package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Scheduler at at an output link of a Node.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Scheduler extends Component {
	/**
	 * Queues managed by the Scheduler.
	 */
	protected Queue[] queues;

	/**
	 * Creates a new Scheduler.
	 * @param queues queues to be managed by the Scheduler.
	 */
	public Scheduler(Queue[] queues) {
		super();
		this.queues = queues;

		for(Queue queue : queues)
			queue.setScheduler(this);
	}

	/**
	 * Gets the Queues managed by the Scheduler.
	 * @return array of Queues.
	 */
	public Queue[] getQueues() {
		return queues;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();

		JSONArray queuesArray = new JSONArray();
		for(Queue queue : queues)
			queuesArray.put(queue.toJSONObject());

		obj.put("queues", queuesArray);
		return obj;
	}
}