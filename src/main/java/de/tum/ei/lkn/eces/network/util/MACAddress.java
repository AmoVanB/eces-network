package de.tum.ei.lkn.eces.network.util;

import java.util.Arrays;

/**
 * The class representing MAC address.
 *
 * @author Sho Shimizu (from net.floodlightcontroller.util.MACAddress)
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public class MACAddress extends Address {
	/**
	 * Length of a MAC address in bytes.
	 */
	final static protected int MAC_ADDRESS_LENGTH = 6;

	/**
	 * Creates a MACAddress from a byte array.
	 * @param address byte array representing the address to be created.
	 * @throws IllegalArgumentException if the string cannot be parsed as a
	 *         MAC address.
	 */
	public MACAddress(byte[] address) {
		if(address.length != MAC_ADDRESS_LENGTH)
			throw new IllegalArgumentException("The byte array size does not correspond to a MAC address");

		this.address = Arrays.copyOf(address, MAC_ADDRESS_LENGTH);
	}

	/**
	 * Returns a MAC address instance representing the value of the specified
	 * address.
	 * @param address the String representation of the MAC Address to be parsed.
	 * @return a MACAddress instance representing the value of the specified
	 *         address.
	 * @throws IllegalArgumentException if the string cannot be parsed as a
	 *         MAC address.
	 */
	public static MACAddress valueOf(String address) {
		String[] elements = address.split(":");
		if(elements.length != MAC_ADDRESS_LENGTH)
			throw new IllegalArgumentException("Specified MAC Address must contain 12 hex digits separated pairwise by colons.");

		byte[] addressInBytes = new byte[MAC_ADDRESS_LENGTH];
		for(int i = 0; i < MAC_ADDRESS_LENGTH; i++) {
			String element = elements[i];
			addressInBytes[i] = (byte) Integer.parseInt(element, 16);
		}

		return new MACAddress(addressInBytes);
	}

	/**
	 * Returns a MAC address instance representing the specified byte array.
	 * @param address the byte array to be parsed.
	 * @return a MAC address instance representing the specified byte array.
	 * @throws IllegalArgumentException if the byte array cannot be parsed as a
	 *         MAC address.
	 */
	public static MACAddress valueOf(byte[] address) {
		if(address.length != MAC_ADDRESS_LENGTH)
			throw new IllegalArgumentException("The given byte array does not represent a MAC address");

		return new MACAddress(address);
	}

	/**
	 * Tells if the MAC address is the broadcast address or not.
	 * @return true if the MAC address is the broadcast address.
	 */
	public boolean isBroadcast() {
		for(byte b : address)
			if(b != -1) // checks if equal to 0xff
				return false;

		return true;
	}

	/**
	 * Tells if the MAC address is the multicast address.
	 * @return true if the MAC address is the multicast address.
	 */
	public boolean isMulticast() {
		if(isBroadcast()) {
			return false;
		}
		return (address[0] & 0x01) != 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(byte b: address) {
			if(builder.length() > 0)
				builder.append(":");

			builder.append(String.format("%02X", b & 0xFF));
		}

		return builder.toString();
	}
}