package org.zenoss.lib.tsdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class OpenTsdbClientConfiguration {
    @JsonProperty
    private String host;

    @JsonProperty
    private Integer port;

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
