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
package org.hyperledger.besu.ethereum.api.jsonrpc.internal.privacy.methods.priv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.enclave.Enclave;
import org.hyperledger.besu.enclave.types.PrivacyGroup;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequest;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.privacy.methods.EnclavePublicKeyProvider;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.core.PrivacyParameters;
import org.hyperledger.besu.ethereum.privacy.PrivacyController;

import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

public class PrivFindPrivacyGroupTest {
  private static final String ENCLAVE_PUBLIC_KEY = "A1aVtMxLCUHmBVHXoZzzBgPbW/wj5axDpW9X8l91SGo=";
  private static final List<String> ADDRESSES =
      List.of(
          "0xfe3b557e8fb62b89f4916b721be55ceb828dbd73",
          "0x627306090abab3a6e1400e9345bc60c78a8bef57");

  private final Enclave enclave = mock(Enclave.class);
  private final PrivacyParameters privacyParameters = mock(PrivacyParameters.class);
  private final PrivacyController privacyController = mock(PrivacyController.class);
  private final User user =
      new JWTUser(new JsonObject().put("privacyPublicKey", ENCLAVE_PUBLIC_KEY), "");
  private final EnclavePublicKeyProvider enclavePublicKeyProvider = (user) -> ENCLAVE_PUBLIC_KEY;

  private JsonRpcRequestContext request;
  private PrivacyGroup privacyGroup;

  @Before
  public void setUp() {
    when(privacyParameters.getEnclave()).thenReturn(enclave);
    when(privacyParameters.isEnabled()).thenReturn(true);
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest("1", "priv_deletePrivacyGroup", new Object[] {ADDRESSES}), user);
    privacyGroup = new PrivacyGroup();
    privacyGroup.setName("privacyGroup");
    privacyGroup.setDescription("privacyGroup desc");
    privacyGroup.setPrivacyGroupId("privacy group id");
    privacyGroup.setMembers(Lists.list("member1"));
  }

  @Test
  public void findsPrivacyGroupWithValidAddresses() {
    when(privacyController.findPrivacyGroup(ADDRESSES, ENCLAVE_PUBLIC_KEY))
        .thenReturn(new PrivacyGroup[] {privacyGroup});

    final PrivFindPrivacyGroup privFindPrivacyGroup =
        new PrivFindPrivacyGroup(privacyController, enclavePublicKeyProvider);

    final JsonRpcSuccessResponse response =
        (JsonRpcSuccessResponse) privFindPrivacyGroup.response(request);
    final PrivacyGroup[] result = (PrivacyGroup[]) response.getResult();
    assertThat(result).hasSize(1);
    assertThat(result[0]).isEqualToComparingFieldByField(privacyGroup);
    verify(privacyController).findPrivacyGroup(ADDRESSES, ENCLAVE_PUBLIC_KEY);
  }

  @Test
  public void failsWithFindPrivacyGroupErrorIfEnclaveFails() {
    when(privacyController.findPrivacyGroup(ADDRESSES, ENCLAVE_PUBLIC_KEY))
        .thenThrow(new IllegalStateException("some failure"));
    final PrivFindPrivacyGroup privFindPrivacyGroup =
        new PrivFindPrivacyGroup(privacyController, enclavePublicKeyProvider);

    final JsonRpcErrorResponse response =
        (JsonRpcErrorResponse) privFindPrivacyGroup.response(request);
    assertThat(response.getError()).isEqualTo(JsonRpcError.FIND_PRIVACY_GROUP_ERROR);
    verify(privacyController).findPrivacyGroup(ADDRESSES, ENCLAVE_PUBLIC_KEY);
  }
}
