/*
 * ****************************************************************************
 *
 *  Copyright (C) Zenoss, Inc. 2014, all rights reserved.
 *
 *  This content is made available according to terms specified in
 *  License.zenoss distributed with this file.
 *
 * ***************************************************************************
 */
package org.zenoss.lib.tsdb;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pools connections to OpenTSDB
 * @author cschellenger
 */
public class OpenTsdbClientPool extends GenericObjectPool<OpenTsdbClient> {
    
    static final Logger log = LoggerFactory.getLogger(OpenTsdbClientPool.class);

    public OpenTsdbClientPool(OpenTsdbClientPoolConfiguration configuration) {
        this(configuration, new OpenTsdbClientFactory(configuration));
    }

    public OpenTsdbClientPool(OpenTsdbClientPoolConfiguration config, OpenTsdbClientFactory clientFactory) {
        super(clientFactory, config.getClientConfigurations().size(), WHEN_EXHAUSTED_BLOCK, config.getMaxWaitTime());
        setTestOnBorrow(true);
        this.tsdbFactory = clientFactory;
    }
    
    public int clearErrorCount() {
        return tsdbFactory.clearErrorCount();
    }

    public boolean hasCollision() {
        return tsdbFactory.hasCollision();
    }
    
    private final OpenTsdbClientFactory tsdbFactory;
}
