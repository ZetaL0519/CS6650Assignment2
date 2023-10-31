package org.client2;

public class Counter {
    private int successfulReq;
    private int failedReq;

    public Counter() {
        this.successfulReq = 0;
        this.failedReq = 0;
    }

    public synchronized void incrementSuccessfulReq(int increment) {
        this.successfulReq += increment;
    }

    public synchronized void incrementFailedReq(int increment) {
        this.failedReq += increment;
    }

    public int getSuccessfulReq() {
        return this.successfulReq;
    }

    public int getFailedReq() {
        return this.failedReq;
    }
}
