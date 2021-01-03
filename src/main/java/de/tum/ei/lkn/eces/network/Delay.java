package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import org.jscience.physics.amount.Amount;
import org.json.JSONObject;

import javax.measure.quantity.Duration;

import static javax.measure.unit.SI.SECOND;

/**
 * Propagation delay of a link.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Delay extends Component {
	/**
	 * Delay in seconds.
	 */
	private double delay;

	/**
	 * Creates a new Delay Object.
	 * @param delay delay of the Object in second.
	 */
	public Delay(double delay) {
		super();
		this.delay = delay;
	}

	/**
	 * Creates a new Delay Object.
	 * @param delay delay of the Object.
	 */
	public Delay(Amount<Duration> delay) {
		this(0);
		setDelay(delay);
	}

	/**
	 * Creates a new Delay Object.
	 * @param delay String representation of the delay (value followed by a
	 *              space followed by the unit).
	 */
	public Delay(String delay) {
		this(0);
		setDelay(delay);
	}

	/**
	 * Gets the delay of the Object.
	 * @return delay value in seconds.
	 */
	public double getDelay() {
		return this.delay;
	}

	/**
	 * Gets the delay of the Object.
	 * @return delay as Amount Object.
	 */
	public Amount<Duration> getDelayAmount() {
		return Amount.valueOf(this.delay, SECOND);
	}

	/**
	 * Sets the delay of the Object.
	 * @param delay new delay.
	 */
	public void setDelay(double delay) {
		this.delay = delay;
	}

	/**
	 * Sets the delay of the Object.
	 * @param delay delay as Amount Object.
	 */
	public void setDelay(Amount<Duration> delay) {
		setDelay(delay.doubleValue(SECOND));
	}

	/**
	 * Sets the delay of the Object.
	 * @param delay String representation of the delay (value followed by a
	 *              space followed by the unit).
	 */
	public void setDelay(String delay) {
		setDelay(Amount.valueOf(delay).to(SECOND).doubleValue(SECOND));
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		obj.put("delay", this.delay * 1000); // ms
	    return obj;
	}
}
