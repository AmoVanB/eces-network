package de.tum.ei.lkn.eces.network.mappers;

import de.tum.ei.lkn.eces.core.Controller;
import de.tum.ei.lkn.eces.core.Mapper;
import de.tum.ei.lkn.eces.network.WFQScheduler;

/**
 * Mapper for WFQScheduler components.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class WFQSchedulerMapper extends Mapper<WFQScheduler> {
	public WFQSchedulerMapper(Controller controller) {
		super(controller);
	}
}
