/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.core;

import org.hyperledger.besu.plugin.data.Quantity;

import java.math.BigInteger;
import java.util.Objects;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

/** A particular quantity of Wei, the Ethereum currency. */
public final class Wei implements Quantity {

  public static final Wei ZERO = of(0);

  private final Bytes32 value;

  protected Wei(final Bytes32 bytes) {
    this.value = bytes;
  }

  private Wei(final long v) {
    this(UInt256.valueOf(v).toBytes());
  }

  private Wei(final BigInteger v) {
    this(UInt256.valueOf(v).toBytes());
  }

  private Wei(final String hexString) {
    this(Bytes32.fromHexStringLenient(hexString));
  }

  public static Wei of(final long value) {
    return new Wei(value);
  }

  public static Wei of(final BigInteger value) {
    return new Wei(value);
  }

  public static Wei of(final UInt256 value) {
    return new Wei(value.toBytes());
  }

  public static Wei wrap(final Bytes32 value) {
    return new Wei(value);
  }

  public static Wei fromHexString(final String str) {
    return new Wei(str);
  }

  public static Wei fromEth(final long eth) {
    return Wei.of(BigInteger.valueOf(eth).multiply(BigInteger.TEN.pow(18)));
  }

  public Bytes32 toBytes() {
    return value;
  }

  @Override
  public Number getValue() {
    return value.toUnsignedBigInteger();
  }

  @Override
  public byte[] getByteArray() {
    return value.toArray();
  }

  @Override
  public String getHexString() {
    return value.toHexString();
  }

  @Override
  public int size() {
    return value.size();
  }

  @Override
  public String toString() {
    return getHexString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Wei wei = (Wei) o;
    return Objects.equals(value, wei.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
