package de.tum.ei.lkn.eces.network.mappers;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.network.Scheduler;

/**
 * Mapper for Scheduler components.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class SchedulerMapper extends Mapper<Scheduler> {
	public SchedulerMapper(Controller controller) {
		super(controller);
	}
}
