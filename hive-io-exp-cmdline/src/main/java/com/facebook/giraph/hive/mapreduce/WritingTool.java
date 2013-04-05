/*
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
package com.facebook.giraph.hive.mapreduce;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;

import com.facebook.giraph.hive.output.HiveApiOutputFormat;
import com.facebook.giraph.hive.output.HiveOutputDescription;
import com.facebook.giraph.hive.record.HiveWritableRecord;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.facebook.giraph.hive.mapreduce.SampleOutputFormat.SAMPLE_PROFILE_ID;

/*
  CREATE TABLE hive_io_test (
    i1 BIGINT,
    i2 BIGINT,
    i3 BIGINT
  )
  PARTITIONED BY (ds STRING)
  TBLPROPERTIES ('RETENTION_PLATINUM'='90')
 */
public class WritingTool extends Configured implements Tool {
  @Override
  public int run(String[] args) throws Exception {
    HiveOutputDescription outputDesc = new HiveOutputDescription();
    outputDesc.setDbName("default");
    outputDesc.setTableName("hive_io_test");
    Map<String, String> partitionValues = ImmutableMap.of("ds", "2013-04-01");
    outputDesc.setPartitionValues(partitionValues);
    HiveApiOutputFormat.initProfile(getConf(), outputDesc, SAMPLE_PROFILE_ID);

    Job job = new Job(getConf(), "hive-io-writing");
    job.setMapperClass(SampleMapper.class);
    job.setInputFormatClass(SampleInputFormat.class);
    job.setMapOutputKeyClass(NullWritable.class);
    job.setMapOutputValueClass(HiveWritableRecord.class);
    job.setOutputFormatClass(SampleOutputFormat.class);

    job.setNumReduceTasks(0);

    job.submit();
    return job.waitForCompletion(true) ? 0 : 1;
  }
}