/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.data.v2.stub;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.BatchingCallSettings;
import com.google.api.gax.rpc.ServerStreamingCallSettings;
import com.google.api.gax.rpc.StatusCode.Code;
import com.google.api.gax.rpc.UnaryCallSettings;
import com.google.bigtable.admin.v2.InstanceName;
import com.google.cloud.bigtable.data.v2.models.ConditionalRowMutation;
import com.google.cloud.bigtable.data.v2.models.KeyOffset;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.threeten.bp.Duration;

@RunWith(JUnit4.class)
public class EnhancedBigtableStubSettingsTest {
  @Test
  public void instanceNameIsRequiredTest() {
    EnhancedBigtableStubSettings.Builder builder = EnhancedBigtableStubSettings.newBuilder();

    Throwable error = null;
    try {
      builder.build();
    } catch (Throwable t) {
      error = t;
    }

    assertThat(error).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void settingsAreNotLostTest() {
    InstanceName instanceName = InstanceName.of("my-project", "my-instance");
    String appProfileId = "my-app-profile-id";
    String endpoint = "some.other.host:123";
    CredentialsProvider credentialsProvider = Mockito.mock(CredentialsProvider.class);

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder()
            .setInstanceName(instanceName)
            .setAppProfileId(appProfileId)
            .setEndpoint(endpoint)
            .setCredentialsProvider(credentialsProvider);

    verifyBuilder(builder, instanceName, appProfileId, endpoint, credentialsProvider);
    verifySettings(builder.build(), instanceName, appProfileId, endpoint, credentialsProvider);
    verifyBuilder(
        builder.build().toBuilder(), instanceName, appProfileId, endpoint, credentialsProvider);
  }

  private void verifyBuilder(
      EnhancedBigtableStubSettings.Builder builder,
      InstanceName instanceName,
      String appProfileId,
      String endpoint,
      CredentialsProvider credentialsProvider) {
    assertThat(builder.getInstanceName()).isEqualTo(instanceName);
    assertThat(builder.getAppProfileId()).isEqualTo(appProfileId);
    assertThat(builder.getEndpoint()).isEqualTo(endpoint);
    assertThat(builder.getCredentialsProvider()).isEqualTo(credentialsProvider);
  }

  private void verifySettings(
      EnhancedBigtableStubSettings settings,
      InstanceName instanceName,
      String appProfileId,
      String endpoint,
      CredentialsProvider credentialsProvider) {
    assertThat(settings.getInstanceName()).isEqualTo(instanceName);
    assertThat(settings.getAppProfileId()).isEqualTo(appProfileId);
    assertThat(settings.getEndpoint()).isEqualTo(endpoint);
    assertThat(settings.getCredentialsProvider()).isEqualTo(credentialsProvider);
  }

  @Test
  public void multipleChannelsByDefaultTest() {
    InstanceName dummyInstanceName = InstanceName.of("my-project", "my-instance");

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder().setInstanceName(dummyInstanceName);

    InstantiatingGrpcChannelProvider provider =
        (InstantiatingGrpcChannelProvider) builder.getTransportChannelProvider();

    assertThat(provider.toBuilder().getPoolSize()).isGreaterThan(1);
  }

  @Test
  public void readRowsIsNotLostTest() {
    InstanceName dummyInstanceName = InstanceName.of("my-project", "my-instance");

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder().setInstanceName(dummyInstanceName);

    RetrySettings retrySettings =
        RetrySettings.newBuilder()
            .setMaxAttempts(10)
            .setTotalTimeout(Duration.ofHours(1))
            .setInitialRpcTimeout(Duration.ofSeconds(10))
            .setRpcTimeoutMultiplier(1)
            .setMaxRpcTimeout(Duration.ofSeconds(10))
            .setJittered(true)
            .build();

    builder
        .readRowsSettings()
        .setIdleTimeout(Duration.ofMinutes(5))
        .setRetryableCodes(Code.ABORTED, Code.DEADLINE_EXCEEDED)
        .setRetrySettings(retrySettings)
        .build();

    assertThat(builder.readRowsSettings().getIdleTimeout()).isEqualTo(Duration.ofMinutes(5));
    assertThat(builder.readRowsSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.readRowsSettings().getRetrySettings()).isEqualTo(retrySettings);

    assertThat(builder.build().readRowsSettings().getIdleTimeout())
        .isEqualTo(Duration.ofMinutes(5));
    assertThat(builder.build().readRowsSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().readRowsSettings().getRetrySettings()).isEqualTo(retrySettings);

    assertThat(builder.build().toBuilder().readRowsSettings().getIdleTimeout())
        .isEqualTo(Duration.ofMinutes(5));
    assertThat(builder.build().toBuilder().readRowsSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().toBuilder().readRowsSettings().getRetrySettings())
        .isEqualTo(retrySettings);
  }

  @Test
  public void readRowsHasSaneDefaultsTest() {
    ServerStreamingCallSettings.Builder<Query, Row> builder =
        EnhancedBigtableStubSettings.newBuilder().readRowsSettings();

    verifyRetrySettingAreSane(builder.getRetryableCodes(), builder.getRetrySettings());
  }

  @Test
  public void sampleRowKeysSettingsAreNotLostTest() {
    InstanceName dummyInstanceName = InstanceName.of("my-project", "my-instance");

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder().setInstanceName(dummyInstanceName);

    RetrySettings retrySettings =
        RetrySettings.newBuilder()
            .setMaxAttempts(10)
            .setTotalTimeout(Duration.ofHours(1))
            .setInitialRpcTimeout(Duration.ofSeconds(10))
            .setRpcTimeoutMultiplier(1)
            .setMaxRpcTimeout(Duration.ofSeconds(10))
            .setJittered(true)
            .build();

    builder
        .sampleRowKeysSettings()
        .setRetryableCodes(Code.ABORTED, Code.DEADLINE_EXCEEDED)
        .setRetrySettings(retrySettings)
        .build();

    assertThat(builder.sampleRowKeysSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.sampleRowKeysSettings().getRetrySettings()).isEqualTo(retrySettings);

    assertThat(builder.build().sampleRowKeysSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().sampleRowKeysSettings().getRetrySettings()).isEqualTo(retrySettings);

    assertThat(builder.build().toBuilder().sampleRowKeysSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().toBuilder().sampleRowKeysSettings().getRetrySettings())
        .isEqualTo(retrySettings);
  }

  @Test
  public void sampleRowKeysHasSaneDefaultsTest() {
    UnaryCallSettings.Builder<String, List<KeyOffset>> builder =
        EnhancedBigtableStubSettings.newBuilder().sampleRowKeysSettings();
    verifyRetrySettingAreSane(builder.getRetryableCodes(), builder.getRetrySettings());
  }

  @Test
  public void mutateRowSettingsAreNotLostTest() {
    InstanceName dummyInstanceName = InstanceName.of("my-project", "my-instance");

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder().setInstanceName(dummyInstanceName);

    RetrySettings retrySettings =
        RetrySettings.newBuilder()
            .setMaxAttempts(10)
            .setTotalTimeout(Duration.ofHours(1))
            .setInitialRpcTimeout(Duration.ofSeconds(10))
            .setRpcTimeoutMultiplier(1)
            .setMaxRpcTimeout(Duration.ofSeconds(10))
            .setJittered(true)
            .build();

    builder
        .mutateRowSettings()
        .setRetryableCodes(Code.ABORTED, Code.DEADLINE_EXCEEDED)
        .setRetrySettings(retrySettings)
        .build();

    assertThat(builder.mutateRowSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.mutateRowSettings().getRetrySettings()).isEqualTo(retrySettings);

    assertThat(builder.build().mutateRowSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().mutateRowSettings().getRetrySettings()).isEqualTo(retrySettings);

    assertThat(builder.build().toBuilder().mutateRowSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().toBuilder().mutateRowSettings().getRetrySettings())
        .isEqualTo(retrySettings);
  }

  @Test
  public void mutateRowHasSaneDefaultsTest() {
    UnaryCallSettings.Builder<RowMutation, Void> builder =
        EnhancedBigtableStubSettings.newBuilder().mutateRowSettings();
    verifyRetrySettingAreSane(builder.getRetryableCodes(), builder.getRetrySettings());
  }

  @Test
  public void mutateRowsSettingsAreNotLostTest() {
    InstanceName dummyInstanceName = InstanceName.of("my-project", "my-instance");

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder().setInstanceName(dummyInstanceName);

    RetrySettings retrySettings =
        RetrySettings.newBuilder()
            .setMaxAttempts(10)
            .setTotalTimeout(Duration.ofHours(1))
            .setInitialRpcTimeout(Duration.ofSeconds(10))
            .setRpcTimeoutMultiplier(1)
            .setMaxRpcTimeout(Duration.ofSeconds(10))
            .setJittered(true)
            .build();

    BatchingSettings batchingSettings = BatchingSettings.newBuilder().build();

    builder
        .mutateRowsSettings()
        .setRetryableCodes(Code.ABORTED, Code.DEADLINE_EXCEEDED)
        .setRetrySettings(retrySettings)
        .setBatchingSettings(batchingSettings)
        .build();

    assertThat(builder.mutateRowsSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.mutateRowsSettings().getRetrySettings()).isEqualTo(retrySettings);
    assertThat(builder.mutateRowsSettings().getBatchingSettings()).isSameAs(batchingSettings);

    assertThat(builder.build().mutateRowsSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().mutateRowsSettings().getRetrySettings()).isEqualTo(retrySettings);
    assertThat(builder.build().mutateRowsSettings().getBatchingSettings())
        .isSameAs(batchingSettings);

    assertThat(builder.build().toBuilder().mutateRowsSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().toBuilder().mutateRowsSettings().getRetrySettings())
        .isEqualTo(retrySettings);
    assertThat(builder.build().toBuilder().mutateRowsSettings().getBatchingSettings())
        .isSameAs(batchingSettings);
  }

  @Test
  public void mutateRowsHasSaneDefaultsTest() {
    BatchingCallSettings.Builder<RowMutation, Void> builder =
        EnhancedBigtableStubSettings.newBuilder().mutateRowsSettings();

    verifyRetrySettingAreSane(builder.getRetryableCodes(), builder.getRetrySettings());

    assertThat(builder.getBatchingSettings().getDelayThreshold())
        .isIn(Range.open(Duration.ZERO, Duration.ofMinutes(1)));
    assertThat(builder.getBatchingSettings().getElementCountThreshold())
        .isIn(Range.open(0L, 1_000L));
    assertThat(builder.getBatchingSettings().getIsEnabled()).isTrue();
    assertThat(builder.getBatchingSettings().getRequestByteThreshold())
        .isLessThan(256L * 1024 * 1024);
    assertThat(
            builder.getBatchingSettings().getFlowControlSettings().getMaxOutstandingElementCount())
        .isLessThan(10_000L);
    assertThat(
            builder.getBatchingSettings().getFlowControlSettings().getMaxOutstandingRequestBytes())
        .isLessThan(512L * 1024 * 1024);
  }

  @Test
  public void checkAndMutateRowSettingsAreNotLostTest() {
    InstanceName dummyInstanceName = InstanceName.of("my-project", "my-instance");

    EnhancedBigtableStubSettings.Builder builder =
        EnhancedBigtableStubSettings.newBuilder().setInstanceName(dummyInstanceName);

    RetrySettings retrySettings = RetrySettings.newBuilder().build();
    builder
        .checkAndMutateRowSettings()
        .setRetryableCodes(Code.ABORTED, Code.DEADLINE_EXCEEDED)
        .setRetrySettings(retrySettings)
        .build();

    assertThat(builder.checkAndMutateRowSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.checkAndMutateRowSettings().getRetrySettings()).isSameAs(retrySettings);

    assertThat(builder.build().checkAndMutateRowSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().checkAndMutateRowSettings().getRetrySettings())
        .isSameAs(retrySettings);

    assertThat(builder.build().toBuilder().checkAndMutateRowSettings().getRetryableCodes())
        .containsAllOf(Code.ABORTED, Code.DEADLINE_EXCEEDED);
    assertThat(builder.build().toBuilder().checkAndMutateRowSettings().getRetrySettings())
        .isSameAs(retrySettings);
  }

  @Test
  public void checkAndMutateRowSettingsAreSane() {
    UnaryCallSettings.Builder<ConditionalRowMutation, Boolean> builder =
        EnhancedBigtableStubSettings.newBuilder().checkAndMutateRowSettings();

    // CheckAndMutateRow is not retryable in the case of toggle mutations. So it's disabled by
    // default.
    assertThat(builder.getRetrySettings().getMaxAttempts()).isAtMost(1);
    assertThat(builder.getRetryableCodes()).isEmpty();
  }

  private void verifyRetrySettingAreSane(Set<Code> retryCodes, RetrySettings retrySettings) {
    assertThat(retryCodes).containsAllOf(Code.DEADLINE_EXCEEDED, Code.UNAVAILABLE, Code.ABORTED);

    assertThat(retrySettings.getMaxAttempts()).isGreaterThan(1);
    assertThat(retrySettings.getTotalTimeout()).isGreaterThan(Duration.ZERO);

    assertThat(retrySettings.getInitialRetryDelay()).isGreaterThan(Duration.ZERO);
    assertThat(retrySettings.getRetryDelayMultiplier()).isAtLeast(1.0);
    assertThat(retrySettings.getMaxRetryDelay()).isGreaterThan(Duration.ZERO);

    assertThat(retrySettings.getInitialRpcTimeout()).isGreaterThan(Duration.ZERO);
    assertThat(retrySettings.getRpcTimeoutMultiplier()).isAtLeast(1.0);
    assertThat(retrySettings.getMaxRpcTimeout()).isGreaterThan(Duration.ZERO);
  }
}
