/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.stats;

import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.HdrHistogram.Histogram;

/**
 * Encapsulates statistics gathered by the test.
 *
 * @author adam
 */
public class Statistics {

    public static final long MAX_LATENCY = 5000;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Total number of opened sessions
    private final AtomicInteger openSessionCount = new AtomicInteger(0);

    // Records the distribution of latencies between requesting that a session
    // is opened and the callback acknowledging that it has been opened.
    private volatile Histogram openSessionLatencyHistogram = new Histogram(MAX_LATENCY, 3);

    // Total number of sessions closed by the client
    private final AtomicInteger closedByClientCount = new AtomicInteger(0);

    // Total number of sessions closed by the server
    private final AtomicInteger closedByServerCount = new AtomicInteger(0);

    // Records the distribution of latencies between requesting that a session
    // is closed and the callback acknowledging that it has been closed.
    private volatile Histogram closeSessionLatencyHistogram = new Histogram(MAX_LATENCY, 3);

    // Total number of sessions which errored during open
    private final AtomicInteger openSessionErrorCount = new AtomicInteger(0);

    // Number of topic updates received
    private final AtomicInteger topicUpdatesReceived = new AtomicInteger(0);

    // Number of bytes received on topics
    private final AtomicLong topicBytesReceived = new AtomicLong(0);

    // Number of subscriptions received
    private final AtomicInteger topicSubscriptions = new AtomicInteger(0);

    // Number of unsubscriptions received
    private final AtomicInteger topicUnsubscriptions = new AtomicInteger(0);

    // Cumulative delay from ping roundtrips with the server
    private final AtomicLong pingRoundtripTime = new AtomicLong(0);

    // The number of pings that have been sent.
    private final AtomicInteger pingCount = new AtomicInteger(0);

    // Records distribution of ping roundtrip times
    private volatile Histogram pingHistogram = new Histogram(MAX_LATENCY, 3);

    // The time when this statistics instance is considered started.
    private final AtomicLong startTime = new AtomicLong(0);

    // The time when the first session was opened
    private final AtomicLong openTime = new AtomicLong(0);

    // The time when the first message was received
    private final AtomicLong firstMessageTime = new AtomicLong(0);

    // The number of actions that have been processed.
    private final AtomicLong actionCount = new AtomicLong(0);

    public Statistics() {
    }

    public int incOpenSessionCount(int v) {
        return openSessionCount.addAndGet(v);
    }
    public int incOpenSessionCount() {
        return incOpenSessionCount(1);
    }

    public void recordOpenSessionLatency(long v) {
        openSessionLatencyHistogram.recordValue(Math.min(v, MAX_LATENCY));
    }

    public int incClosedByClientCount(int v) {
        return closedByClientCount.addAndGet(v);
    }
    public int incClosedByClientCount() {
        return incClosedByClientCount(1);
    }

    public int incClosedByServerCount(int v) {
        return closedByServerCount.addAndGet(v);
    }
    public int incClosedByServerCount() {
        return incClosedByServerCount(1);
    }

    public void recordCloseSessionLatency(long v) {
        closeSessionLatencyHistogram.recordValue(Math.min(v, MAX_LATENCY));
    }

    public int incOpenSessionErrorCount(int v) {
        return openSessionErrorCount.addAndGet(v);
    }

    public int incOpenSessionErrorCount() {
        return incOpenSessionErrorCount(1);
    }

    public int incTopicUpdatesReceived() {
        return incTopicUpdatesReceived(1);
    }

    public int incTopicUpdatesReceived(int v) {
        return topicUpdatesReceived.addAndGet(v);
    }

    public long incTopicBytesReceived(long v) {
        return topicBytesReceived.addAndGet(v);
    }

    public int incTopicSubscriptions(int v) {
        return topicSubscriptions.addAndGet(v);
    }
    public int incTopicSubscriptions() {
        return incTopicSubscriptions(1);
    }

    public int incTopicUnsubscriptions(int v) {
        return topicUnsubscriptions.addAndGet(v);
    }
    public int incTopicUnsubscriptions() {
        return incTopicUnsubscriptions(1);
    }

    public long incPingRoundtripTime(long time) {
        return pingRoundtripTime.addAndGet(time);
    }

    public int incPingCount(int v) {
        return pingCount.addAndGet(v);
    }
    public int incPingCount() {
        return incPingCount(1);
    }

    public void recordPingRoundtrip(long v) {
        pingHistogram.recordValue(Math.min(v, MAX_LATENCY));
    }

    public int getOpenSessionCount() {
        return openSessionCount.get();
    }

    public Histogram getOpenSessionLatencyHistogram() {
        return openSessionLatencyHistogram;
    }

    public int getClosedByClientCount() {
        return closedByClientCount.get();
    }

    public int getClosedByServerCount() {
        return closedByServerCount.get();
    }

    public Histogram getCloseSessionLatencyHistogram() {
        return closeSessionLatencyHistogram;
    }

    public int getOpenSessionErrorCount() {
        return openSessionErrorCount.get();
    }

    public int getTopicUpdatesReceived() {
        return topicUpdatesReceived.get();
    }

    public long getTopicBytesReceived() {
        return topicBytesReceived.get();
    }

    public long getPingRoundtripTime() {
        return pingRoundtripTime.get();
    }

    public int getPingCount() {
        return pingCount.get();
    }

    public Histogram getPingHistogram() {
        return pingHistogram;
    }

    public void setOpenTime() {
        openTime.compareAndSet(0, System.currentTimeMillis());
    }

    public void setFirstMessageTime(long time) {
        firstMessageTime.compareAndSet(0, time);
    }
    public void setFirstMessageTime() {
        setFirstMessageTime(System.currentTimeMillis());
    }

    public int getTopicSubscriptions() {
        return topicSubscriptions.get();
    }

    public int getTopicUnsubscriptions() {
        return topicUnsubscriptions.get();
    }

    public void setStartTime(long time) {
        startTime.compareAndSet(0, time);
    }
    public void setStartTime() {
        setStartTime(System.currentTimeMillis());
    }

    public long getStartTime() {
        return startTime.get();
    }

    public long getOpenTime() {
        return openTime.get();
    }

    public long getFirstMessageTime() {
        return firstMessageTime.get();
    }

    public long getActionCount() {
        return actionCount.get();
    }
}
