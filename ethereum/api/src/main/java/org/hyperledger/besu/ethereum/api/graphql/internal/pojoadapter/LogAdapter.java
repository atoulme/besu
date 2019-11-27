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
package org.hyperledger.besu.ethereum.api.graphql.internal.pojoadapter;

import org.hyperledger.besu.ethereum.api.query.BlockchainQueries;
import org.hyperledger.besu.ethereum.api.query.TransactionWithMetadata;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.LogTopic;
import org.hyperledger.besu.ethereum.core.LogWithMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import graphql.schema.DataFetchingEnvironment;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

@SuppressWarnings("unused") // reflected by GraphQL
public class LogAdapter extends AdapterBase {
  private final LogWithMetadata logWithMetadata;

  public LogAdapter(final LogWithMetadata logWithMetadata) {
    this.logWithMetadata = logWithMetadata;
  }

  public Optional<Integer> getIndex() {
    return Optional.of(logWithMetadata.getLogIndex());
  }

  public List<Bytes32> getTopics() {
    final List<LogTopic> topics = logWithMetadata.getTopics();
    final List<Bytes32> result = new ArrayList<>();
    for (final Bytes topic : topics) {
      result.add(Bytes32.leftPad(topic));
    }
    return result;
  }

  public Optional<Bytes> getData() {
    return Optional.of(Bytes.wrap(logWithMetadata.getData().getByteArray()));
  }

  public Optional<TransactionAdapter> getTransaction(final DataFetchingEnvironment environment) {
    final BlockchainQueries query = getBlockchainQueries(environment);
    final Hash hash = logWithMetadata.getTransactionHash();
    final Optional<TransactionWithMetadata> tran = query.transactionByHash(hash);
    return tran.map(TransactionAdapter::new);
  }

  public Optional<AccountAdapter> getAccount(final DataFetchingEnvironment environment) {
    final BlockchainQueries query = getBlockchainQueries(environment);
    long blockNumber = logWithMetadata.getBlockNumber();
    final Long bn = environment.getArgument("block");
    if (bn != null) {
      blockNumber = bn;
    }

    return query
        .getWorldState(blockNumber)
        .map(ws -> new AccountAdapter(ws.get(logWithMetadata.getLogger())));
  }
}
