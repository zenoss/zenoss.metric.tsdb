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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class OpenTsdbClientPoolConfiguration {

    /**
     * Client factory configuration
     */
    @Valid
    @JsonProperty("clientFactory")
    private OpenTsdbClientFactoryConfiguration clientFactoryConfiguration = new OpenTsdbClientFactoryConfiguration();

    /**
     * Clients to  create for pool
     */
    @Valid
    @JsonProperty("clients")
    private List<OpenTsdbClientConfiguration> clientConfigurations = new ArrayList<>();

    /**
     * how long should a client be kept alive before closing in ms
     */
    @JsonProperty
    private long maxKeepAliveTime = 5 * 60 * 1000;

    /**
     * how long between testing a client's liveliness in ms
     */
    @JsonProperty
    private long minTestTime = 60 * 1000;
    
    @JsonProperty
    private long maxWaitTime = 10_000L;
    
    @JsonProperty
    private Integer clientBufferSize = 8192;
    
    /**
     * The size of the output stream buffer.
     * @return size
     */
    public Integer getClientBufferSize() {
        return clientBufferSize;
    }

    /**
     * Client factory configuration
     * @return configuration
     */
    public OpenTsdbClientFactoryConfiguration getClientFactoryConfiguration() {
        return clientFactoryConfiguration;
    }

    /**
     * Configuration details for OpenTSDB clients
     * @return list of client configurations
     */
    public List<OpenTsdbClientConfiguration> getClientConfigurations() {
        return clientConfigurations;
    }

    /**
     * Maximum time a client be kept alive before closing
     * @return time in milliseconds
     */
    public long getMaxKeepAliveTime() {
        return maxKeepAliveTime;
    }

    /**
     * Time between testing a client's liveliness
     * @return time in milliseconds
     */
    public long getMinTestTime() {
        return minTestTime;
    }
    
    /**
     * The maximum time to block when waiting for a client to become available
     * from the pool
     * @return time in milliseconds
     */
    public long getMaxWaitTime() {
        return maxWaitTime;
    }
    
    /**
     * The size of the output stream buffer.
     * @param bufferSize size
     */
    public void setClientBufferSize(Integer bufferSize) {
        this.clientBufferSize = bufferSize;
    }

    /**
     * The maximum time to block when waiting for a client to become available
     * from the pool
     * @param maxKeepAliveTime time in milliseconds
     */
    public void setMaxKeepAliveTime(long maxKeepAliveTime) {
        this.maxKeepAliveTime = maxKeepAliveTime;
    }
    
    /**
     * The maximum time to block when waiting for a client to become available
     * from the pool
     * @param maxWaitTime time in milliseconds
     */
    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * Time between testing a client's liveliness
     * @param minTestTime time in milliseconds
     */
    public void setMinTestTime(long minTestTime) {
        this.minTestTime = minTestTime;
    }

    /**
     * Client factory configuration
     * @param clientFactoryConfiguration 
     */
    @SuppressWarnings({"unused"})
    public void setClientFactoryConfigurations(OpenTsdbClientFactoryConfiguration clientFactoryConfiguration) {
        this.clientFactoryConfiguration = clientFactoryConfiguration;
    }

    /**
     * Configuration details for OpenTSDB clients
     * @param clients 
     */
    @SuppressWarnings({"unused"})
    public void setClientConfiguration(List<OpenTsdbClientConfiguration> clients) {
        this.clientConfigurations = clients;
    }
}
