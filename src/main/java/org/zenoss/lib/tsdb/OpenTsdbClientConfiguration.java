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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration information for connections to OpenTSDB.
 * 
 * @see OpenTsdbClientPoolConfiguration
 * @see OpenTsdbClientFactory
 */
public class OpenTsdbClientConfiguration {
    
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    @JsonProperty
    private Integer maxConnections = 1;

    /**
     * The name of the host running OpenTSDB
     * @return hostname
     */
    public String getHost() {
        return host;
    }
    
    /**
     * The port used to connect to OpenTSDB
     * @return port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * The number of connections to OpenTSDB that can be opened simultaneously
     * @return max connections
     */
    public Integer getMaxConnections() {
        return maxConnections;
    }

    /**
     * The name of the host running OpenTSDB
     * @param host hostname
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * The port used to connect to OpenTSDB
     * @param port port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The number of connections to OpenTSDB that can be opened simultaneously
     * @param maxConnections 
     */
    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }
}
