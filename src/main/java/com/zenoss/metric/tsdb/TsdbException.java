package com.zenoss.metric.tsdb;

public class TsdbException extends Exception {
    public TsdbException() {
    }

    public TsdbException(String message) {
        super(message);
    }
}
