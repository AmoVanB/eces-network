package de.tum.ei.lkn.eces.network.mappers;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.network.PriorityScheduler;

/**
 * Mapper for PriorityScheduler components.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class PrioritySchedulerMapper extends Mapper<PriorityScheduler> {
	public PrioritySchedulerMapper(Controller controller) {
		super(controller);
	}
}
