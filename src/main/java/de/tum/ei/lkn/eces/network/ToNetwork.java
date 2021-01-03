package de.tum.ei.lkn.eces.network;

import de.tum.ei.lkn.eces.core.Component;
import de.tum.ei.lkn.eces.core.Entity;
import de.tum.ei.lkn.eces.core.annotations.ComponentBelongsTo;
import org.json.JSONObject;

/**
 * Component attached to the same Entity as a Component of the GraphSystem
 * and holding the Entity to which is attached the corresponding
 * NetworkingSystem Component.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
@ComponentBelongsTo(system = NetworkingSystem.class)
public class ToNetwork extends Component {
	/**
	 * Entity to which is attached the corresponding NetworkingSystem Component.
	 */
	public Entity networkEntity;

	/**
	 * Creates a new ToNetwork reference.
	 * @param networkEntity Entity to which is attached to corresponding
	 *                      NetworkingSystem Component.
	 */
	public ToNetwork(Entity networkEntity) {
		super();
		this.networkEntity = networkEntity;
	}

	/**
	 * Creates a new ToNetwork reference.
	 */
	public ToNetwork() {
		super();
	}

	/**
	 * Gets the Entity to which is attached the corresponding NetworkingSystem
	 * Component.
	 * @return the Entity.
	 */
	public Entity getNetworkEntity() {
		return networkEntity;
	}

	/**
	 * Sets the Entity to which is attached the corresponding NetworkingSystem
	 * Component.
	 * @param networkEntity new corresponding Entity.
	 */
	public void setNetworkEntity(Entity networkEntity) {
		this.networkEntity = networkEntity;
	}

	public JSONObject toJSONObject() {
		JSONObject result = new JSONObject();
		result.put("networkEntity", networkEntity.toJSONObject());
		return result;
	}
}