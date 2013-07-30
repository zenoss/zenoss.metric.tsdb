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

    public OpenTsdbClientFactoryConfiguration getClientFactoryConfiguration() {
        return clientFactoryConfiguration;
    }

    public List<OpenTsdbClientConfiguration> getClientConfigurations() {
        return clientConfigurations;
    }

    public long getMaxKeepAliveTime() {
        return maxKeepAliveTime;
    }

    public long getMinTestTime() {
        return minTestTime;
    }
    
    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxKeepAliveTime(long maxKeepAliveTime) {
        this.maxKeepAliveTime = maxKeepAliveTime;
    }
    
    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public void setMinTestTime(long minTestTime) {
        this.minTestTime = minTestTime;
    }

    @SuppressWarnings({"unused"})
    public void setClientFactoryConfigurations(OpenTsdbClientFactoryConfiguration clientFactoryConfiguration) {
        this.clientFactoryConfiguration = clientFactoryConfiguration;
    }

    @SuppressWarnings({"unused"})
    public void setClientConfiguration(List<OpenTsdbClientConfiguration> clients) {
        this.clientConfigurations = clients;
    }
}
