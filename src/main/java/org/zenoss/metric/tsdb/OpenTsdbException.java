package org.zenoss.metric.tsdb;

public class OpenTsdbException extends Exception {
    public OpenTsdbException() {
    }

    public OpenTsdbException(String message) {
        super(message);
    }
}
