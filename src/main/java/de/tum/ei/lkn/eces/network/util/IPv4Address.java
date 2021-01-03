package de.tum.ei.lkn.eces.network.util;

import java.util.Arrays;

/**
 * Class representing an IPv4 Address.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class IPv4Address extends IPAddress {
	/**
	 * Length of an IPv4 address in bytes.
	 */
	static final protected int IPv4_ADDRESS_LENGTH = 4;

	/**
	 * Creates an IPv4Address from a byte array.
	 * @param address byte array representing the address to be created.
	 * @throws IllegalArgumentException if the string cannot be parsed as an
	 *         IPv4 address.
	 */
	public IPv4Address(byte[] address) {
		if(address.length != IPv4_ADDRESS_LENGTH)
			throw new IllegalArgumentException("The byte array size does not correspond to an IPv4 address");

		this.address = Arrays.copyOf(address, IPv4_ADDRESS_LENGTH);
	}

	/**
	 * Returns an IPv4Address instance representing the specified address.
	 * @param address the String representation of the IPv4 address to be
	 *                parsed.
	 * @return an IPv4 address instance representing the specified address.
	 * @throws IllegalArgumentException if the string cannot be parsed as an
	 *         IPv4 address.
	 */
	public static IPv4Address valueOf(String address) {
		if(!IPAddressUtil.isIPv4LiteralAddress(address))
			throw new IllegalArgumentException(address + " does not correspond to an IPv4 address");

		return new IPv4Address(IPAddressUtil.textToNumericFormatV4(address));
	}

	/**
	 * Returns an IPv4Address instance representing the specified address.
	 * @param address the byte array to be parsed.
	 * @return an IPv4Address instance representing the specified byte array.
	 * @throws IllegalArgumentException if the byte array cannot be parsed as an
	 *         IPv4 address (is not of size IPv4_ADDRESS_LENGTH).
	 */
	public static IPv4Address valueOf(byte[] address) {
		if(address.length != IPv4_ADDRESS_LENGTH)
			throw new IllegalArgumentException("The given byte array does not represent an IPv4 address");

		return new IPv4Address(address);
	}

	@Override
	public String toString() {
		return (address[0] & 0xff) + "." + (address[1] & 0xff) + "." + (address[2] & 0xff) + "." + (address[3] & 0xff);
	}
}
