/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tez.engine.common.shuffle.impl;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.metrics.MetricsContext;
import org.apache.hadoop.metrics.MetricsRecord;
import org.apache.hadoop.metrics.MetricsUtil;
import org.apache.hadoop.metrics.Updater;
import org.apache.tez.common.Constants;
import org.apache.tez.common.TezJobConfig;
import org.apache.tez.engine.records.TezTaskAttemptID;

class ShuffleClientMetrics implements Updater {

  private MetricsRecord shuffleMetrics = null;
  private int numFailedFetches = 0;
  private int numSuccessFetches = 0;
  private long numBytes = 0;
  private int numThreadsBusy = 0;
  private final int numCopiers;
  
  ShuffleClientMetrics(TezTaskAttemptID reduceId, Configuration jobConf, 
      String user, String jobName) {
    this.numCopiers = 
        jobConf.getInt(
            TezJobConfig.TEZ_ENGINE_SHUFFLE_PARALLEL_COPIES, 
            TezJobConfig.DEFAULT_TEZ_ENGINE_SHUFFLE_PARALLEL_COPIES);

    MetricsContext metricsContext = MetricsUtil.getContext(Constants.TEZ);
    this.shuffleMetrics = 
      MetricsUtil.createRecord(metricsContext, "shuffleInput");
    this.shuffleMetrics.setTag("user", user);
    this.shuffleMetrics.setTag("jobName", jobName);
    this.shuffleMetrics.setTag("jobId", 
        reduceId.getTaskID().getVertexID().getDAGId().toString());
    this.shuffleMetrics.setTag("taskId", reduceId.toString());
    this.shuffleMetrics.setTag("sessionId", 
        jobConf.get(
            TezJobConfig.TEZ_ENGINE_METRICS_SESSION_ID, 
            TezJobConfig.DEFAULT_TEZ_ENGINE_METRICS_SESSION_ID));
    metricsContext.registerUpdater(this);
  }
  public synchronized void inputBytes(long numBytes) {
    this.numBytes += numBytes;
  }
  public synchronized void failedFetch() {
    ++numFailedFetches;
  }
  public synchronized void successFetch() {
    ++numSuccessFetches;
  }
  public synchronized void threadBusy() {
    ++numThreadsBusy;
  }
  public synchronized void threadFree() {
    --numThreadsBusy;
  }
  public void doUpdates(MetricsContext unused) {
    synchronized (this) {
      shuffleMetrics.incrMetric("shuffle_input_bytes", numBytes);
      shuffleMetrics.incrMetric("shuffle_failed_fetches", 
                                numFailedFetches);
      shuffleMetrics.incrMetric("shuffle_success_fetches", 
                                numSuccessFetches);
      if (numCopiers != 0) {
        shuffleMetrics.setMetric("shuffle_fetchers_busy_percent",
            100*((float)numThreadsBusy/numCopiers));
      } else {
        shuffleMetrics.setMetric("shuffle_fetchers_busy_percent", 0);
      }
      numBytes = 0;
      numSuccessFetches = 0;
      numFailedFetches = 0;
    }
    shuffleMetrics.update();
  }
}
