/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.action;

import com.pushtechnology.diffusion.client.features.Pings;
import com.pushtechnology.diffusion.client.features.Pings.PingCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.callbacks.SessionPingCallback;

/**
 * This action sends a ping to the server in order to measure roundtrip times. A
 * session is chosen randomly from the currently open sessions.
 *
 * @author adam
 */
public class PingAction extends Action {

    // Callback records the roundtrip time.
    private final PingCallback PING_CALLBACK = new SessionPingCallback();

    public PingAction(String name) {
        super(name);
    }

    @Override
    public void run() {
        // Choose a random session and issue a ping from it.
        Object[] arr = Subscriber.ACTIVE_SESSIONS.values().toArray();
        if (arr.length == 0) {
            return;
        }
        int rnd = Subscriber.RANDOM.nextInt(arr.length);
        Session session = (Session) arr[rnd];

        Pings pings = session.feature(Pings.class);
        pings.pingServer(PING_CALLBACK);
    }
}
