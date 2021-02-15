/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.stats;

/**
 * Accumulates statistics; while statistics are normally reset every second (or
 * some configured interval), it is useful to record many statistics over the
 * lifetime of the test.
 *
 * @author adam
 */
public class StatsAccumulator {

    public static void accumulate(Statistics accumulated, Statistics statistics) {
        accumulated.incOpenSessionCount(statistics.getOpenSessionCount());
        accumulated.incClosedByClientCount(statistics.getClosedByClientCount());
        accumulated.incClosedByServerCount(statistics.getClosedByServerCount());
        accumulated.incOpenSessionErrorCount(statistics.getOpenSessionErrorCount());
        accumulated.incTopicUpdatesReceived(statistics.getTopicUpdatesReceived());
        accumulated.incTopicBytesReceived(statistics.getTopicBytesReceived());
        accumulated.incTopicSubscriptions(statistics.getTopicSubscriptions());
        accumulated.incTopicUnsubscriptions(statistics.getTopicUnsubscriptions());
        accumulated.incPingRoundtripTime(statistics.getPingRoundtripTime());
        accumulated.incPingCount(statistics.getPingCount());
    }
}
