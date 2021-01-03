package de.tum.ei.lkn.eces.network.util;

/**
 * Class representing an IP Address.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class IPAddress extends Address {
	/**
	 * Returns an IPAddress instance representing the specified address.
	 * @param address the String representation of the IP address to be parsed.
	 * @return an IPAddress instance representing the specified address.
	 * @throws IllegalArgumentException if the string cannot be parsed as an IP
	 *         address.
	 */
	public static IPAddress valueOf(String address) {
		if(IPAddressUtil.isIPv4LiteralAddress(address))
			return IPv4Address.valueOf(address);
		else if(IPAddressUtil.isIPv6LiteralAddress(address))
			return IPv6Address.valueOf(address);
		throw new IllegalArgumentException(address + " is not an IP address");
	}

	/**
	 * Returns an IPAddress instance representing the specified address.
	 * @param address the byte array to be parsed.
	 * @return an IPAddress instance representing the specified byte array.
	 * @throws IllegalArgumentException if the byte array cannot be parsed as an
	 *         IP address.
	 */
	public static IPAddress valueOf(byte[] address) {
		switch(address.length) {
			case IPv4Address.IPv4_ADDRESS_LENGTH:
				return IPv4Address.valueOf(address);
			case IPv6Address.IPv6_ADDRESS_LENGTH:
				return IPv6Address.valueOf(address);
			default:
				throw new IllegalArgumentException("The given byte array does not represent an IP address");
		}
	}
}
