package org.zenoss.lib.tsdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OpenTsdbClientPoolConfiguration {
    @JsonProperty
    private int receiveBufferSize;

    @JsonProperty
    private int sendBufferSize;

    @JsonProperty
    private boolean keepAlive;

    @JsonProperty
    private List<OpenTsdbClientConfiguration> clientConfigurations;

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setClientConfigurations( List<OpenTsdbClientConfiguration> clients) {
        this.clientConfigurations = clients;
    }

    public List<OpenTsdbClientConfiguration> getClientConfigurations() {
        return clientConfigurations;
    }
}
