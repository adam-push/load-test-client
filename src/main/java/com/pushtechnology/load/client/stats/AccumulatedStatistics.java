package com.pushtechnology.load.client.stats;

/**
 * @author andy
 */
public class AccumulatedStatistics extends Statistics {

    public AccumulatedStatistics() {
    }

    public int incOpenSessionCount(int v) {
        return super.incOpenSessionCount(v);
    }

    public int incClosedByClientCount(int v) {
        return super.incClosedByClientCount(v);
    }

    public int incClosedByServerCount(int v) {
        return super.incClosedByServerCount(v);
    }

    public int incOpenSessionErrorCount(int v) {
        return super.incOpenSessionErrorCount(v);
    }

    public int incTopicUpdatesReceived(int v) {
        return super.incTopicUpdatesReceived(v);
    }

    public long incTopicBytesReceived(long v) {
        return super.incTopicBytesReceived(v);
    }

    public int incTopicSubscriptions(int v) {
        return super.incTopicSubscriptions(v);
    }

    public int incTopicUnsubscriptions(int v) {
        return super.incTopicUnsubscriptions(v);
    }

    public long incPingRoundtripTime(long time) {
        return super.incPingRoundtripTime(time);
    }

    public int incPingCount(int v) {
        return super.incPingCount(v);
    }
}
