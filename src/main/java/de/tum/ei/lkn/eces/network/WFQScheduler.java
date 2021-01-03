package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.network.exceptions.NetworkException;

/**
 * Weighted Fair Queuing (WFQ) scheduler.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class WFQScheduler extends Scheduler {
	/**
	 * Weights of all the Queues.
	 */
	private double[] weights;

	/**
	 * Sum of all the weights.
	 */
	private double sumOfWeights;

	public WFQScheduler(Queue[] queues, double[] weights) {
		super(queues);
		setWeights(weights);
	}

	/**
	 * Gets the weights.
	 * @return the weights.
	 */
	public double[] getWeights() {
		return weights;
	}

	/**
	 * Sets the weights.
	 * @param weights the weights to set.
	 */
	public void setWeights(double[] weights) {
		if(queues.length != weights.length)
			throw new NetworkException("The number of weights of a WFQ scheduler must be equal to the number of queues");
		this.weights = weights;
		sumOfWeights = 0;
		for(double z : weights)
			sumOfWeights += z;
	}

	/**
	 * Gets the normalized weights
	 * @return the normalized weights (so that the sum makes 1).
	 */
	public double[] getNormalizedWeights() {
		double[] normalizedWeights = new double[weights.length];
		for(int i = 0; i < weights.length; i++)
			normalizedWeights[i] = weights[i]/sumOfWeights;

		return normalizedWeights;
	}
}