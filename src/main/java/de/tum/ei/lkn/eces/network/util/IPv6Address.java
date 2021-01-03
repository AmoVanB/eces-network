package de.tum.ei.lkn.eces.network.util;

import java.util.Arrays;

/**
 * Class representing an IPv4 Address.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class IPv6Address extends IPAddress {
	/**
	 * Length of an IPv6 address in bytes.
	 */
	final static protected int IPv6_ADDRESS_LENGTH = 16;

	/**
	 * Creates an IPv6Address from a byte array.
	 * @param address byte array representing the address to be created.
	 * @throws IllegalArgumentException if the string cannot be parsed as an
	 *         IPv6 address.
	 */
	public IPv6Address(byte[] address) {
		if(address.length != IPv6_ADDRESS_LENGTH)
			throw new IllegalArgumentException("The byte array size does not correspond to an IPv6 address");

		this.address = Arrays.copyOf(address, IPv6_ADDRESS_LENGTH);
	}

	/**
	 * Returns an IPv6Address instance representing the specified address.
	 * @param address the String representation of the IPv6 address to be
	 *                parsed.
	 * @return an IPv6 address instance representing the specified address.
	 * @throws IllegalArgumentException if the string cannot be parsed as an
	 *         IPv6 address.
	 */
	public static IPv6Address valueOf(String address) {
		if(!IPAddressUtil.isIPv6LiteralAddress(address))
			throw new IllegalArgumentException(address + " does not correspond to an IPv6 address");

		return new IPv6Address(IPAddressUtil.textToNumericFormatV6(address));
	}

	/**
	 * Returns an IPv6Address instance representing the specified address.
	 * @param address the byte array to be parsed.
	 * @return an IPv6Address instance representing the specified byte array.
	 * @throws IllegalArgumentException if the byte array cannot be parsed as an
	 *         IPv6 address (is not of size IPv6_ADDRESS_LENGTH).
	 */
	public static IPv6Address valueOf(byte[] address) {
		if(address.length != IPv6_ADDRESS_LENGTH)
			throw new IllegalArgumentException("The given byte array does not represent an IPv4 address");

		return new IPv6Address(address);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(39);
		for(int i = 0; i < (IPv6_ADDRESS_LENGTH / 2); i++) {
			sb.append(Integer.toHexString(((address[i<<1]<<8) & 0xff00) | (address[(i<<1)+1] & 0xff)));

			if(i < (IPv6_ADDRESS_LENGTH / 2) -1 )
				sb.append(":");
		}
		return sb.toString();
	}
}
