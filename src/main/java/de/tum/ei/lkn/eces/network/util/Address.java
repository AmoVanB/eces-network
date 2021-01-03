package de.tum.ei.lkn.eces.network.util;

import java.util.Arrays;

/**
 * Class representing a Networking Address.
 *
 * @author Jochen Guck
 * @author Amaury Van Bemten
 */
public abstract class Address {
	/**
	 * The Address consists of bytes.
	 */
	protected byte[] address;

	/**
	 * Returns the length of the Address.
	 * @return the length of the Address.
	 */
	public int getLength() {
		return address.length;
	}

	/**
	 * Returns the value of the Address as a byte array.
	 * @return the Address as a byte array.
	 */
	public byte[] toBytes() {
		return Arrays.copyOf(address, address.length);
	}

	@Override
	public boolean equals(Object o) {
		if(o == this)
			return true;

		if(!(o instanceof Address))
			return false;

		Address other = (Address) o;
		return Arrays.equals(this.address, other.address);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.address);
	}
}