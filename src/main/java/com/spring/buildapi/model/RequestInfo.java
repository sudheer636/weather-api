package com.spring.buildapi.model;

public class RequestInfo {
    private int requestCount;
    private long timestamp;

    public RequestInfo(int requestCount, long timestamp) {
        this.requestCount = requestCount;
        this.timestamp = timestamp;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public int getRequestCount() {
        return this.requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void incrementRequestCount(){this.requestCount= this.requestCount+1;}
}

