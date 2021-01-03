package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import org.jscience.physics.amount.Amount;
import org.json.JSONObject;

import javax.measure.quantity.DataAmount;

import static javax.measure.unit.NonSI.BYTE;

/**
 * Queue at the output link of a Node.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Queue extends Component {
	/**
	 * Size of the Queue in bytes.
	 */
	private double size;

	/**
	 * Scheduler managing the Queue.
	 */
	private Scheduler scheduler;

	/**
	 * Creates a new Queue.
	 * @param size size of the new Queue in bytes.
	 * @param scheduler Scheduler managing the Queue.
	 */
	public Queue(double size, Scheduler scheduler) {
		super();
		this.size = size;
		this.scheduler = scheduler;
	}

	/**
	 * Creates a new Queue.
	 * @param size size of the new Queue as an Amount Object.
	 * @param scheduler Scheduler managing the Queue.
	 */
	public Queue(Amount<DataAmount> size, Scheduler scheduler) {
		this(0, scheduler);
		setSize(size);
	}

	/**
	 * Creates a new Queue.
	 * @param size String representation of the size (value followed by a space
	 *             followed by the unit).
	 * @param scheduler Scheduler managing the Queue.
	 */
	public Queue(String size, Scheduler scheduler) {
		this(0, scheduler);
		setSize(size);
	}

	/**
	 * Creates a new Queue.
	 * @param size size of the Queue.
	 */
	public Queue(double size) {
		this(size, null);
	}

	/**
	 * Creates a new Queue.
	 * @param size size of the new Queue as an Amount Object.
	 */
	public Queue(Amount<DataAmount> size) {
		this(size, null);
	}

	/**
	 * Creates a new Queue.
	 * @param size String representation of the size (value followed by a space
	 *             followed by the unit).
	 */
	public Queue(String size) {
		this(size, null);
	}

	/**
	 * Gets the Scheduler managing the Queue.
	 * @return Scheduler or null if no Scheduler is managing the Queue yet.
	 */
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	/**
	 * Sets the Scheduler managing the Queue.
	 * @param scheduler the Scheduler managing the Queue.
	 */
	protected void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Gets the size of the Queue.
	 * @return Queue size in bytes.
	 */
	public double getSize() {
		return this.size;
	}

	/**
	 * Gets the size of the Queue.
	 * @return Queue size as an Amount Object.
	 */
	public Amount<DataAmount> getSizeAmount() {
		return Amount.valueOf(this.size, BYTE);
	}

	/**
	 * Sets the size of the Queue.
	 * @param size new Queue size.
	 */
	private void setSize(double size) {
		this.size = size;
	}

	/**
	 * Sets the size of the Queue.
	 * @param length new Queue size as an Amount Object.
	 */
	private void setSize(Amount<DataAmount> length) {
		this.size = length.doubleValue(BYTE);
	}

	/**
	 * Sets the size of the Queue.
	 * @param length String representation of the size (value followed by a
	 *               space followed by the unit).
	 */
	private void setSize(String length) {
		this.size = Amount.valueOf(length).to(BYTE).doubleValue(BYTE);
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		if(Double.isInfinite(this.size))
			obj.put("size", "infinite");
		else
			obj.put("size", this.size); // bytes
	    return obj;
	}
}
