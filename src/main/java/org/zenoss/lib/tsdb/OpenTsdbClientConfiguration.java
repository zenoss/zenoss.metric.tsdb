package org.zenoss.lib.tsdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenTsdbClientConfiguration {
    
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    @JsonProperty
    private Integer maxConnections = 1;

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }
}
