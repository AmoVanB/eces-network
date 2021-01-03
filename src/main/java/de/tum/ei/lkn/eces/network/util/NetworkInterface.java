package de.tum.ei.lkn.eces.network.util;

/**
 * Class representing a Network Interface.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class NetworkInterface {
	/**
	 * Name of the interface.
	 */
	private String name;

	/**
	 * IP address of the interface ('0.0.0.0' if not set).
	 */
	private IPAddress ipAddress;

	/**
	 * MAC address of the interface.
	 */
	private MACAddress macAddress;

	/**
	 * Creates a new interface.
	 * @param name name of the interface.
	 * @param macAddress MAC address of the interface.
	 * @param ipAddress IP address of the interface.
	 */
	public NetworkInterface(String name, MACAddress macAddress, IPAddress ipAddress) {
		this.ipAddress = ipAddress;
		this.name = name;
		this.macAddress = macAddress;
	}

	/**
	 * Creates a new interface with no IP address defined.
	 * @param name name of the interface.
	 * @param macAddress MAC address of the interface.
	 */
	public NetworkInterface(String name, MACAddress macAddress) {
		this(name, macAddress, IPAddress.valueOf("0.0.0.0"));
	}

	/**
	 * Creates a new interface.
	 * @param name name of the interface.
	 * @param macAddress MAC address of the interface.
	 * @param ipAddress IP address of the interface.
	 */
	public NetworkInterface(String name, String macAddress, String ipAddress) {
		this(name, MACAddress.valueOf(macAddress), IPv4Address.valueOf(ipAddress));
	}
	/**
	 * Creates a new interface with no IP address defined.
	 * @param name name of the interface.
	 * @param macAddress MAC Address of the interface.
	 */
	public NetworkInterface(String name, String macAddress) {
		this(name, macAddress, "0.0.0.0");
	}

	/**
	 * Gets the name of the interface.
	 * @return name of the interface.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the IP address of the interface.
	 * @return IP address of the interface.
	 */
	public IPAddress getIPAddress() {
		return ipAddress;
	}

	/**
	 * Gets the MAC address of the interface.
	 * @return the MAC address of the interface.
	 */
	public MACAddress getMACAddress() {
		return macAddress;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}
}