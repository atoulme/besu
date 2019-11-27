/*
 *
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
 *
 */
package org.hyperledger.besu.ethereum.api.query;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableList;

import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.TopicsDeserializer;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Log;
import org.hyperledger.besu.ethereum.core.LogTopic;
import org.hyperledger.besu.ethereum.core.LogsBloomFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;
import org.apache.tuweni.bytes.Bytes;

public class LogsQuery {

  private final List<Address> addresses;
  private final List<List<LogTopic>> topics;
  private final List<LogsBloomFilter> addressBlooms;
  private final List<List<LogsBloomFilter>> topicsBlooms;

  @JsonCreator
  public LogsQuery(
      @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("address")
          final List<Address> addresses,
      @JsonDeserialize(using = TopicsDeserializer.class) @JsonProperty("topics")
          final List<List<LogTopic>> topics) {
    this.addresses = addresses != null ? addresses : emptyList();
    this.topics = topics != null ? topics : emptyList();
    this.addressBlooms =
        this.addresses.stream()
            .map(a -> LogsBloomFilter.computeBytes(a))
            .collect(toUnmodifiableList());
    this.topicsBlooms =
        this.topics.stream()
            .map(
                subTopics ->
                    subTopics.stream()
                        .filter(Objects::nonNull)
                        .map(LogsBloomFilter::computeBytes)
                        .collect(Collectors.toList()))
            .collect(toUnmodifiableList());
  }

  public boolean couldMatch(final LogsBloomFilter bloom) {
    return (addressBlooms.isEmpty() || addressBlooms.stream().anyMatch(bloom::couldContain))
        && (topicsBlooms.isEmpty()
            || topicsBlooms.stream()
                .allMatch(
                    topics -> topics.isEmpty() || topics.stream().anyMatch(bloom::couldContain)));
  }

  public boolean matches(final Log log) {
    return matchesAddresses(log.getLogger()) && matchesTopics(log.getTopics());
  }

  private boolean matchesAddresses(final Address address) {
    return addresses.isEmpty() || addresses.contains(address);
  }

  private boolean matchesTopics(final List<LogTopic> topics) {
    return this.topics.isEmpty()
        || (topics.size() >= this.topics.size()
            && IntStream.range(0, this.topics.size())
                .allMatch(i -> matchesTopic(topics.get(i), this.topics.get(i))));
  }

  private boolean matchesTopic(final Bytes topic, final List<LogTopic> matchCriteria) {
    return matchCriteria.contains(null) || matchCriteria.contains(topic);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final LogsQuery logsQuery = (LogsQuery) o;
    return Objects.equals(addresses, logsQuery.addresses)
        && Objects.equals(topics, logsQuery.topics);
  }

  @Override
  public String toString() {
    return String.format(
        "%s{addresses=%s, topics=%s", getClass().getSimpleName(), addresses, topics);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addresses, topics);
  }

  public static class Builder {
    private final List<Address> queryAddresses = Lists.newArrayList();
    private final List<List<LogTopic>> queryTopics = Lists.newArrayList();

    public Builder address(final Address address) {
      if (address != null) {
        queryAddresses.add(address);
      }
      return this;
    }

    public Builder addresses(final Address... addresses) {
      if (addresses != null && addresses.length > 0) {
        queryAddresses.addAll(Arrays.asList(addresses));
      }
      return this;
    }

    public Builder addresses(final List<Address> addresses) {
      if (addresses != null && !addresses.isEmpty()) {
        queryAddresses.addAll(addresses);
      }
      return this;
    }

    public Builder topics(final List<List<LogTopic>> topics) {
      if (topics != null && !topics.isEmpty()) {
        queryTopics.addAll(topics);
      }
      return this;
    }

    public LogsQuery build() {
      return new LogsQuery(queryAddresses, queryTopics);
    }
  }
}
