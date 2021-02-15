/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.callbacks;

import com.pushtechnology.diffusion.client.features.Pings;
import com.pushtechnology.diffusion.client.features.Pings.PingCallback;
import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.stats.Statistics;

/**
 * Callback when a session issues a ping to the server. The roundtrip time is
 * recorded for statistical purposes.
 *
 * @author adam
 */
public class SessionPingCallback extends PingCallback.Default {

    @Override
    public void onPingResponse(Pings.PingDetails details) {
        Statistics stats = Subscriber.STATS_COLLECTOR.getStatistics();
        synchronized (stats) {
            stats.incPingCount();
            stats.incPingRoundtripTime(details.getRoundTripTime());
            stats.recordPingRoundtrip(details.getRoundTripTime());
        }
    }

}
