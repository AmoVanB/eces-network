package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import org.jscience.physics.amount.Amount;
import org.json.JSONObject;

import javax.measure.quantity.DataRate;
import javax.measure.unit.Unit;

/**
 * Rate of a link.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class Rate extends Component {
	/**
	 * byte/s unit.
	 */
	public final static Unit<DataRate> BYTES_PER_SECOND = DataRate.UNIT.times(8);

	/**
	 * Rate in bytes/s.
	 */
	private double rate;

	/**
	 * Create a new Rate Object with a given rate.
	 * @param rate rate of the new Object.
	 */
	public Rate(double rate) {
		super();
		this.rate = rate;
	}

	/**
	 * Create a new Rate Object with a given rate.
	 * @param rate rate of the new Object.
	 */
	public Rate(Amount<DataRate> rate) {
		this(0);
		setRate(rate);
	}

	/**
	 * Creates a new Rate Object with a given rate.
	 * @param rate String representation of the rate (value followed by a space
	 *             followed by 'byte/s' or 'bit/s').
	 */
	public Rate(String rate) {
		this(0);
		setRate(rate);
	}

	/**
	 * Gets the rate of the Rate Object.
	 * @return rate in bytes/s.
	 */
	public double getRate() {
		return this.rate;
	}

	/**
	 * Gets the rate of the Rate Object.
	 * @return rate as an Amount Object.
	 */
	public Amount<DataRate> getRateAmount() {
		return Amount.valueOf(this.rate, BYTES_PER_SECOND);
	}

	/**
	 * Sets the rate of the Rate Object.
	 * @param rate new rate in bytes/s.
	 */
	private void setRate(double rate) {
		this.rate = rate;
	}

	/**
	 * Sets the rate of the Rate Object.
	 * @param rate new rate as an Amount Object.
	 */
	private void setRate(Amount<DataRate> rate) {
		this.rate = rate.doubleValue(BYTES_PER_SECOND);
	}

	/**
	 * Sets the rate of the Rate Object.
	 * @param rate String representation of the rate (value followed by a
	 *             space followed by 'byte/s' or 'bit/s').
	 */
	private void setRate(String rate) {
		this.rate = Amount.valueOf(rate).to(BYTES_PER_SECOND).doubleValue(BYTES_PER_SECOND);
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = super.toJSONObject();
		if(Double.isInfinite(this.rate))
			obj.put("rate", "infinite");
		else
			obj.put("rate", this.rate / 1000 * 8); // Kbps
	    return obj;
	}
}
