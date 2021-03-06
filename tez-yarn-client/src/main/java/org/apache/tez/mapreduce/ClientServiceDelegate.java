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

package org.apache.tez.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.v2.LogParams;
import org.apache.hadoop.mapreduce.TaskCompletionEvent;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;

import java.io.IOException;

public class ClientServiceDelegate {

  private final Configuration conf;
  private final ResourceMgrDelegate rm;

  // FIXME
  // how to handle completed jobs that the RM does not know about?

  public ClientServiceDelegate(Configuration conf, ResourceMgrDelegate rm,
      JobID jobId) {
    this.conf = new Configuration(conf); // Cloning for modifying.
    // For faster redirects from AM to HS.
    this.conf.setInt(
        CommonConfigurationKeysPublic.IPC_CLIENT_CONNECT_MAX_RETRIES_KEY,
        this.conf.getInt(MRJobConfig.MR_CLIENT_TO_AM_IPC_MAX_RETRIES,
            MRJobConfig.DEFAULT_MR_CLIENT_TO_AM_IPC_MAX_RETRIES));
    this.rm = rm;
  }

  public org.apache.hadoop.mapreduce.Counters getJobCounters(JobID jobId)
      throws IOException, InterruptedException {
    // FIXME needs counters support from DAG
    // with a translation layer on client side
    org.apache.hadoop.mapreduce.Counters empty =
        new org.apache.hadoop.mapreduce.Counters();
    return empty;
  }

  public TaskCompletionEvent[] getTaskCompletionEvents(JobID jobId,
      int fromEventId, int maxEvents)
      throws IOException, InterruptedException {
    // FIXME seems like there is support in client to query task failure
    // related information
    // However, api does not make sense for DAG
    return new TaskCompletionEvent[0];
  }

  public String[] getTaskDiagnostics(org.apache.hadoop.mapreduce.TaskAttemptID
      taId)
      throws IOException, InterruptedException {
    // FIXME need support to query task diagnostics?
    return new String[0];
  }
  
  public JobStatus getJobStatus(JobID oldJobID) throws IOException {
    org.apache.hadoop.mapreduce.v2.api.records.JobId jobId =
      TypeConverter.toYarn(oldJobID);
    ApplicationReport appReport =
        rm.getApplicationReport(jobId.getAppId());
    JobStatus jobStatus =
        new DAGJobStatus(appReport);
    return jobStatus;
  }

  public org.apache.hadoop.mapreduce.TaskReport[] getTaskReports(
      JobID oldJobID, TaskType taskType)
       throws IOException{
    // FIXME need support to query task reports?
    throw new UnsupportedOperationException();
  }

  public boolean killTask(TaskAttemptID taskAttemptID, boolean fail)
       throws IOException {
    // FIXME need support to kill a task attempt?
    throw new UnsupportedOperationException();
  }

  public boolean killJob(JobID oldJobID)
       throws IOException {
    // FIXME need support to kill a dag?
    // Should this be just an RM killApplication?
    // For one dag per AM, RM kill should suffice
    throw new UnsupportedOperationException();
  }

  public LogParams getLogFilePath(JobID oldJobID,
      TaskAttemptID oldTaskAttemptID)
      throws YarnRemoteException, IOException {
    // FIXME logs for an attempt?
    throw new UnsupportedOperationException();
  }
}
