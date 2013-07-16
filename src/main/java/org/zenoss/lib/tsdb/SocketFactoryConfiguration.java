package org.zenoss.lib.tsdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SocketFactoryConfiguration {

    /** tcp keepalive setting */
    @JsonProperty
    private boolean keepAlive;

    /** tcp send buffer size, set <= 0 to disable */
    @JsonProperty
    private int sendBufferSize;

    /** tcp receive buffer size, set <= 0 to disable */
    @JsonProperty
    private int receiveBufferSize;

    /** tcp connect timeout, set <= 0 to disable and use defaults */
    @JsonProperty
    private int connectTimeout;

    /**  socket read timeout, set <= 0 to use defaults */
    @JsonProperty
    private int soTimeout;

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }
}
