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
package com.google.cloud.bigtable.data.v2.stub.mutaterows;

import com.google.api.core.ApiFunction;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.grpc.GrpcStatusCode;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.rpc.ApiCallContext;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.ApiExceptionFactory;
import com.google.api.gax.rpc.ServerStreamingCallable;
import com.google.api.gax.rpc.StatusCode;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.bigtable.v2.MutateRowsRequest.Builder;
import com.google.bigtable.v2.MutateRowsResponse;
import com.google.bigtable.v2.MutateRowsResponse.Entry;
import com.google.cloud.bigtable.data.v2.models.MutateRowsException;
import com.google.cloud.bigtable.data.v2.models.MutateRowsException.FailedMutation;
import com.google.cloud.bigtable.gaxx.retrying.NonCancellableFuture;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.rpc.Code;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;


class MutateRowsAttemptCallable implements Callable<Void> {
  // Synthetic status for Mutations that didn't get a result. It will be exposed in
  // MutateRowsException's FailedMutations.
  private static final StatusCode LOCAL_UNKNOWN_STATUS = new StatusCode() {
    @Override
    public Code getCode() {
      return Code.UNKNOWN;
    }

    @Override
    public Object getTransportCode() {
      return null;
    }
  };

  // Everything needed to issue an RPC
  private final ServerStreamingCallable<MutateRowsRequest, MutateRowsResponse> inner;
  private final ApiCallContext callContext;
  private MutateRowsRequest currentRequest;

  // Everything needed to build a retry request
  private List<Integer> indexMap;
  private final Set<StatusCode.Code> retryableCodes;
  private List<FailedMutation> permanentFailures;

  // Parent controller
  private RetryingFuture<Void> externalFuture;


  // Simple wrappers for handling result futures
  private final ApiFunction<List<MutateRowsResponse>, Void> attemptSuccessfulCallback = new ApiFunction<List<MutateRowsResponse>, Void>() {
    @Override
    public Void apply(List<MutateRowsResponse> responses) {
      handleAttemptSuccess(responses);
      return null;
    }
  };

  private final ApiFunction<Throwable, List<MutateRowsResponse>> attemptFailedCallback = new ApiFunction<Throwable, List<MutateRowsResponse>>() {
    @Override
    public List<MutateRowsResponse> apply(Throwable throwable) {
      handleAttemptError(throwable);
      return null;
    }
  };

  MutateRowsAttemptCallable(
      ServerStreamingCallable<MutateRowsRequest, MutateRowsResponse> inner,
      MutateRowsRequest originalRequest, ApiCallContext callContext, Set<StatusCode.Code> retryableCodes) {
    this.inner = inner;
    this.currentRequest = originalRequest;
    this.callContext = callContext;
    this.retryableCodes = retryableCodes;

    permanentFailures = Lists.newArrayList();
  }

  public void setExternalFuture(
      RetryingFuture<Void> externalFuture) {
    this.externalFuture = externalFuture;
  }

  @Override
  public Void call() {
    try {
      Preconditions.checkNotNull(externalFuture, "External future must be set before starting an attempt");

      // Configure the deadline
      ApiCallContext currentCallContext = null;
      if (callContext != null) {
        currentCallContext = callContext.withTimeout(externalFuture.getAttemptSettings().getRpcTimeout());
      }

      // Handle concurrent cancellation
      externalFuture.setAttemptFuture(new NonCancellableFuture<Void>());
      if (externalFuture.isDone()) {
        return null;
      }

      // Make the actual call
      ApiFuture<List<MutateRowsResponse>> innerFuture = inner.all().futureCall(currentRequest, currentCallContext);

      // Handle RPC level errors by wrapping them in a MutateRowsException
      ApiFuture<List<MutateRowsResponse>> catching = ApiFutures
          .catching(innerFuture, Throwable.class, attemptFailedCallback);

      // Inspect the results and either propagate the success, or prepare to retry the failed mutations
      ApiFuture<Void> transformed = ApiFutures.transform(catching, attemptSuccessfulCallback);

      // Notify the parent of the attempt
      externalFuture.setAttemptFuture(transformed);
    } catch (Throwable e) {
      externalFuture.setAttemptFuture(ApiFutures.<Void>immediateFailedFuture(e));
    }

    return null;
  }

  // Attempt result handlers
  private void handleAttemptError(Throwable throwable) {
    ApiException entryError = createMissingEntryError(throwable);
    List<FailedMutation> allFailures;

    if (entryError.isRetryable()) {
      allFailures = Lists
          .newArrayListWithCapacity(permanentFailures.size() + currentRequest.getEntriesCount());
      allFailures.addAll(permanentFailures);
    } else {
      allFailures = permanentFailures;
    }

    for (int i = 0; i < currentRequest.getEntriesCount(); i++) {
      int origIndex = remapIndex(i);

      allFailures.add(
          FailedMutation.create(origIndex, entryError)
      );
    }
    // NOTE: indexMap & currentRequest are implicitly reused here

    throw new MutateRowsException(throwable, allFailures, entryError.isRetryable());
  }

  private void handleAttemptSuccess(List<MutateRowsResponse> responses) {
    List<FailedMutation> allFailures = Lists.newArrayList(permanentFailures);
    MutateRowsRequest lastRequest = currentRequest;

    Builder builder = lastRequest.toBuilder().clearEntries();
    List<Integer> newIndexMap = Lists.newArrayList();

    for (MutateRowsResponse response : responses) {
      for (Entry entry : response.getEntriesList()) {
        if (entry.getStatus().getCode() == Code.OK_VALUE) {
          continue;
        }

        int origIndex = remapIndex((int) entry.getIndex());

        FailedMutation failedMutation = FailedMutation
            .create(origIndex, createEntryError(entry.getStatus()));

        allFailures.add(failedMutation);

        if (!failedMutation.getError().isRetryable()) {
          permanentFailures.add(failedMutation);
        } else {
          newIndexMap.add(origIndex);
          builder.addEntries(lastRequest.getEntries((int) entry.getIndex()));
        }
      }
    }

    currentRequest = builder.build();
    indexMap = newIndexMap;

    if (!allFailures.isEmpty()) {
      boolean isRetryable = builder.getEntriesCount() > 0;
      throw new MutateRowsException(null, allFailures, isRetryable);
    }
  }


  // Helpers
  private int remapIndex(int index) {
    return (indexMap != null) ? indexMap.get(index) : index;
  }

  private ApiException createEntryError(com.google.rpc.Status protoStatus) {
    io.grpc.Status grpcStatus = io.grpc.Status.fromCodeValue(protoStatus.getCode())
        .withDescription(protoStatus.getMessage());

    StatusCode gaxStatusCode = GrpcStatusCode.of(grpcStatus.getCode());

    return ApiExceptionFactory
        .createException(grpcStatus.asRuntimeException(), gaxStatusCode, retryableCodes.contains(gaxStatusCode.getCode()));
  }


  private ApiException createMissingEntryError(Throwable overallRequestError) {
    if (overallRequestError instanceof ApiException) {
      ApiException requestApiException = (ApiException) overallRequestError;

      return ApiExceptionFactory.createException(
          "Didn't receive a result for this mutation entry",
          overallRequestError,
          requestApiException.getStatusCode(),
          requestApiException.isRetryable()
      );
    }

    return ApiExceptionFactory.createException(
        "Didn't receive a result for this mutation entry",
        overallRequestError,
        LOCAL_UNKNOWN_STATUS,
        false
    );
  }
}
