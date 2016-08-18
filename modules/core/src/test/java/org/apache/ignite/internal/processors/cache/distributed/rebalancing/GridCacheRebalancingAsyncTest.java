/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.distributed.rebalancing;

import java.util.Map;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicWriteOrderMode;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.fair.FairAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteKernal;
import org.apache.ignite.internal.processors.cache.IgniteInternalCache;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtPartitionState;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtPartitionTopology;
import org.apache.ignite.internal.processors.cache.distributed.dht.preloader.GridDhtPartitionMap2;
import org.apache.ignite.internal.util.typedef.internal.U;

/**
 *
 */
public class GridCacheRebalancingAsyncTest extends GridCacheRebalancingSyncSelfTest {
    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration iCfg = super.getConfiguration(gridName);

        for (CacheConfiguration cacheCfg : iCfg.getCacheConfiguration()) {
            cacheCfg.setCacheMode(CacheMode.PARTITIONED);
            cacheCfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
            cacheCfg.setAtomicWriteOrderMode(CacheAtomicWriteOrderMode.PRIMARY);
            cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);

            cacheCfg.setRebalanceMode(CacheRebalanceMode.ASYNC);
            cacheCfg.setRebalanceBatchesPrefetchCount(2);
            cacheCfg.setRebalanceDelay(30000);
            cacheCfg.setRebalanceTimeout(10000);

            cacheCfg.setBackups(1);
            FairAffinityFunction aff = new FairAffinityFunction(1024);
            cacheCfg.setAffinity(aff);
        }

        return iCfg;
    }

    /**
     * @throws Exception Exception.
     */
    public void testRebalanceIsSkipped() throws Exception {
        Ignite ignite = startGrid(0);

        for (int i = 1; i < 5; i++)
            startGrid(i);

        generateData(ignite, 0, 0);

        startGrid(5);

        generateData(ignite, 0, 1);

        for (int i = 0; i < 3; i++) {
            boolean hasMovingPart = false;

            for (String cacheName : ignite.cacheNames()) {
                IgniteInternalCache<Object, Object> cache = ((IgniteKernal)ignite).getCache(cacheName);

                System.out.println(cache.name() + " (size = " + cache.size() + ")");

                for (GridDhtPartitionMap2 map : cache.context().topology().partitionMap(false).values()) {
                    System.out.print("Node:" + map.nodeId() + ", hasMoving " + map.hasMovingPartitions() + ", owning = ");

                    int owning = 0;
                    int moving = 0;
                    int evectd = 0;

                    for (Map.Entry<Integer, GridDhtPartitionState> entry : map.entrySet()) {
                        owning += entry.getValue() == GridDhtPartitionState.OWNING ? 1 : 0;
                        moving += entry.getValue() == GridDhtPartitionState.MOVING ? 1 : 0;
                        evectd += entry.getValue() == GridDhtPartitionState.EVICTED ? 1 : 0;
                    }

                    System.out.println(owning + ", evicted = " + evectd + ", moving = " + moving);

                    if (moving > 0)
                        hasMovingPart = true;
                }
            }

            if (!hasMovingPart)
                break;

            U.sleep(30000);
        }

        for (String cacheName : ignite.cacheNames()) {
            GridDhtPartitionTopology top = ((IgniteKernal)ignite).getCache(cacheName).context().topology();

            for (GridDhtPartitionMap2 map : top.partitionMap(false).values())
                assertFalse(map.hasMovingPartitions());
        }
    }
}