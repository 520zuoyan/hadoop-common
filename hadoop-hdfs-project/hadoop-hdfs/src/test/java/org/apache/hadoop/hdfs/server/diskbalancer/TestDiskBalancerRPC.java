/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.hadoop.hdfs.server.diskbalancer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.hdfs.server.diskbalancer.connectors.ClusterConnector;
import org.apache.hadoop.hdfs.server.diskbalancer.connectors.ConnectorFactory;
import org.apache.hadoop.hdfs.server.diskbalancer.datamodel.DiskBalancerCluster;
import org.apache.hadoop.hdfs.server.diskbalancer.datamodel.DiskBalancerDataNode;
import org.apache.hadoop.hdfs.server.diskbalancer.planner.GreedyPlanner;
import org.apache.hadoop.hdfs.server.diskbalancer.planner.NodePlan;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;

public class TestDiskBalancerRPC {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private MiniDFSCluster cluster;
  private Configuration conf;

  @Before
  public void setUp() throws Exception {
    conf = new HdfsConfiguration();
    cluster = new MiniDFSCluster.Builder(conf).numDataNodes(2).build();
    cluster.waitActive();
  }

  @After
  public void tearDown() throws Exception {
    if (cluster != null) {
      cluster.shutdown();
    }
  }

  @Test
  public void TestSubmitTestRpc() throws Exception {
    final int dnIndex = 0;
    cluster.restartDataNode(dnIndex);
    cluster.waitActive();
    ClusterConnector nameNodeConnector =
        ConnectorFactory.getCluster(cluster.getFileSystem(0).getUri(), conf);

    DiskBalancerCluster diskBalancerCluster = new DiskBalancerCluster(nameNodeConnector);
    diskBalancerCluster.readClusterInfo();
    Assert.assertEquals(cluster.getDataNodes().size(),
                                    diskBalancerCluster.getNodes().size());
    diskBalancerCluster.setNodesToProcess(diskBalancerCluster.getNodes());
    DiskBalancerDataNode node = diskBalancerCluster.getNodes().get(dnIndex);
    GreedyPlanner planner = new GreedyPlanner(10.0f, node);
    NodePlan plan = new NodePlan(node.getDataNodeName(), node.getDataNodePort
        ());
    planner.balanceVolumeSet(node, node.getVolumeSets().get("DISK"), plan);
    final int planVersion = 0; // So far we support only one version.
    DataNode dataNode = cluster.getDataNodes().get(dnIndex);

    String planHash = DigestUtils.sha512Hex(plan.toJson());

    // Since submitDiskBalancerPlan is not implemented yet, it throws an
    // Exception, this will be modified with the actual implementation.
    thrown.expect(DiskbalancerException.class);
    dataNode.submitDiskBalancerPlan(planHash, planVersion, 10, plan.toJson());


  }

  @Test
  public void TestCancelTestRpc() throws Exception {
    final int dnIndex = 0;
    cluster.restartDataNode(dnIndex);
    cluster.waitActive();
    ClusterConnector nameNodeConnector =
        ConnectorFactory.getCluster(cluster.getFileSystem(0).getUri(), conf);

    DiskBalancerCluster diskBalancerCluster = new DiskBalancerCluster(nameNodeConnector);
    diskBalancerCluster.readClusterInfo();
    Assert.assertEquals(cluster.getDataNodes().size(),
        diskBalancerCluster.getNodes().size());
    diskBalancerCluster.setNodesToProcess(diskBalancerCluster.getNodes());
    DiskBalancerDataNode node = diskBalancerCluster.getNodes().get(0);
    GreedyPlanner planner = new GreedyPlanner(10.0f, node);
    NodePlan plan = new NodePlan(node.getDataNodeName(), node.getDataNodePort
        ());
    planner.balanceVolumeSet(node, node.getVolumeSets().get("DISK"), plan);

    final int planVersion = 0; // So far we support only one version.
    DataNode dataNode = cluster.getDataNodes().get(dnIndex);
    String planHash = DigestUtils.sha512Hex(plan.toJson());

    // Since submitDiskBalancerPlan is not implemented yet, it throws an
    // Exception, this will be modified with the actual implementation.
    try {
      dataNode.submitDiskBalancerPlan(planHash, planVersion, 10, plan.toJson());
    } catch (DiskbalancerException ex) {
      // Let us ignore this for time being.
    }
    thrown.expect(DiskbalancerException.class);
    dataNode.cancelDiskBalancePlan(planHash);

  }

  @Test
  public void TestQueryTestRpc() throws Exception {
    final int dnIndex = 0;
    cluster.restartDataNode(dnIndex);
    cluster.waitActive();
    ClusterConnector nameNodeConnector =
        ConnectorFactory.getCluster(cluster.getFileSystem(0).getUri(), conf);

    DiskBalancerCluster diskBalancerCluster = new DiskBalancerCluster
        (nameNodeConnector);
    diskBalancerCluster.readClusterInfo();
    Assert.assertEquals(cluster.getDataNodes().size(),
        diskBalancerCluster.getNodes().size());
    diskBalancerCluster.setNodesToProcess(diskBalancerCluster.getNodes());
    DiskBalancerDataNode node = diskBalancerCluster.getNodes().get(0);
    GreedyPlanner planner = new GreedyPlanner(10.0f, node);
    NodePlan plan = new NodePlan(node.getDataNodeName(), node.getDataNodePort
        ());
    planner.balanceVolumeSet(node, node.getVolumeSets().get("DISK"), plan);

    final int planVersion = 0; // So far we support only one version.
    DataNode dataNode = cluster.getDataNodes().get(dnIndex);
    String planHash = DigestUtils.sha512Hex(plan.toJson());

    // Since submitDiskBalancerPlan is not implemented yet, it throws an
    // Exception, this will be modified with the actual implementation.
    try {
      dataNode.submitDiskBalancerPlan(planHash, planVersion, 10, plan.toJson());
    } catch (DiskbalancerException ex) {
      // Let us ignore this for time being.
    }

    // TODO : This will be fixed when we have implementation for this
    // function in server side.
    thrown.expect(DiskbalancerException.class);
    dataNode.queryDiskBalancerPlan();
  }
}
