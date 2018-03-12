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

import com.google.api.gax.retrying.RetryingExecutor;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.rpc.ApiCallContext;
import com.google.api.gax.rpc.ServerStreamingCallable;
import com.google.api.gax.rpc.StatusCode;
import com.google.api.gax.rpc.StatusCode.Code;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.bigtable.v2.MutateRowsRequest;
import com.google.bigtable.v2.MutateRowsResponse;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * A UnaryCallable that will keep issuing calls to an inner callable until it succeeds or times out.
 *
 * <p>Package-private for internal use.
 */
public class MutateRowsRetryingCallable extends UnaryCallable<MutateRowsRequest, Void> {
  private final ApiCallContext callContextPrototype;
  private final ServerStreamingCallable<MutateRowsRequest, MutateRowsResponse> callable;
  private final RetryingExecutor<Void> executor;
  private final ImmutableSet<Code> retryCodes;

  public MutateRowsRetryingCallable(
      ApiCallContext callContextPrototype,
      ServerStreamingCallable<MutateRowsRequest, MutateRowsResponse> callable,
      RetryingExecutor<Void> executor,
      Set<StatusCode.Code> retryCodes
      ) {
    this.callContextPrototype = Preconditions.checkNotNull(callContextPrototype);
    this.callable = Preconditions.checkNotNull(callable);
    this.executor = Preconditions.checkNotNull(executor);
    this.retryCodes = ImmutableSet.copyOf(retryCodes);
  }

  @Override
  public RetryingFuture<Void> futureCall(MutateRowsRequest request, ApiCallContext inputContext) {
    ApiCallContext context = callContextPrototype.nullToSelf(inputContext);
    MutateRowsAttemptCallable retryCallable =
        new MutateRowsAttemptCallable(callable, request, context, retryCodes);

    RetryingFuture<Void> retryingFuture = executor.createFuture(retryCallable);
    retryCallable.setExternalFuture(retryingFuture);
    retryCallable.call();

    return retryingFuture;
  }

  @Override
  public String toString() {
    return String.format("retrying(%s)", callable);
  }
}
