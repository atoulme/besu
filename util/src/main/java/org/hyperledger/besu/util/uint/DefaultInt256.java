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
package org.hyperledger.besu.util.uint;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes32;

/**
 * Default implementation of a {@link Int256}.
 *
 * <p>Note that this class is not meant to be exposed outside of this package. Use {@link Int256}
 * static methods to build {@link Int256} values instead.
 */
class DefaultInt256 implements Int256 {

  private final Bytes32 value;

  DefaultInt256(final Bytes32 bytes) {
    checkArgument(
        bytes.size() == SIZE,
        "Invalid value for a UInt256: expecting %s bytes but got %s",
        SIZE,
        bytes.size());
    this.value = bytes;
  }

  @Override
  public Bytes32 getBytes() {
    return value;
  }

  // Note meant to be used directly, use Int256.MINUS_ONE instead
  static DefaultInt256 minusOne() {
    final MutableBytes32 v = MutableBytes32.create();
    v.fill((byte) 0xFF);
    return new DefaultInt256(v);
  }

  @Override
  public Int256 dividedBy(final Int256 value) {
    return new DefaultInt256(
        Bytes32.wrap(
            getBytes().toBigInteger().divide(value.getBytes().toBigInteger()).toByteArray()));
  }

  @Override
  public Int256 mod(final Int256 value) {
    return new DefaultInt256(
        Bytes32.wrap(getBytes().toBigInteger().mod(value.getBytes().toBigInteger()).toByteArray()));
  }

  @Override
  public int compareTo(final Int256 other) {
    return getBytes().toBigInteger().compareTo(other.getBytes().toBigInteger());
  }
}
