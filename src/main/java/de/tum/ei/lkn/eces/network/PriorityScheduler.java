package de.tum.ei.lkn.eces.network;

/**
 * Strict Priority Scheduler.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Creates a new priority scheduler.
	 * @param queues array of Queues managed by the Scheduler, sorted by
	 *               increasing order of priority (0th element has highest
	 *               priority).
	 */
	public PriorityScheduler(Queue[] queues) {
		super(queues);
	}
}